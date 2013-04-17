package se.vgregion.alfresco.repo.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class AutoDeleter extends ClusteredExecuter {

  private static final Logger LOG = Logger.getLogger(AutoDeleter.class);

  private NodeService _nodeService;

  private SearchService _searchService;

  private FileFolderService _fileFolderService;

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  private static final ThreadLocal<Configuration> _configuration = new ThreadLocal<Configuration>();

  @Override
  protected String getJobName() {
    return "Auto deleter";
  }

  @Override
  protected void executeInternal() {
    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
      @Override
      public Void doWork() throws Exception {
        RetryingTransactionHelper transactionHelper = _transactionService.getRetryingTransactionHelper();

        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>() {
          @Override
          public Void execute() throws Throwable {
            doAutoDelete();

            return null;
          }
        };

        transactionHelper.doInTransaction(callback, false, true);

        return null;
      }
    }, AuthenticationUtil.getSystemUserName());
  }

  /**
   * Do the auto delete on the whole shebang
   */
  protected void doAutoDelete() {
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    searchParameters.setQuery("ASPECT:\"" + VgrModel.ASPECT_AUTO_DELETABLE + "\" AND TYPE:\"st:site\"");

    ResultSet sites = _searchService.query(searchParameters);

    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug(sites.length() + " sites found with auto delete aspect.");
      }

      for (NodeRef site : sites.getNodeRefs()) {
        autoDeleteSite(site);
      }
    } finally {
      ServiceUtils.closeQuietly(sites);
    }
  }

  /**
   * Do the auto delete on a specific site
   *
   * @param site to do the auto delete for
   */
  private void autoDeleteSite(NodeRef site) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("About to auto delete documents for site " + getSiteInfo(site));
    }

    // first refresh the lock
    refreshLock();

    createConfiguration(site);

    List<String> names = new ArrayList<String>();
    names.add("documentLibrary");

    List<ChildAssociationRef> documentLibraries = _nodeService.getChildrenByName(site, ContentModel.ASSOC_CONTAINS, names);

    if (documentLibraries.size() < 1) {
      return;
    }

    NodeRef documentLibrary = documentLibraries.iterator().next().getChildRef();

    List<FileInfo> folders = _fileFolderService.listDeepFolders(documentLibrary, null);

    // also include the top level folder :)
    folders.add(_fileFolderService.getFileInfo(documentLibrary));

    if (LOG.isDebugEnabled()) {
      LOG.debug("Found " + folders.size() + " folders to look in");
    }

    autoDeleteFolders(folders);
  }

  /**
   * Do the auto delete on a list of folders.
   *
   * @param folders
   */
  private void autoDeleteFolders(List<FileInfo> folders) {
    for (FileInfo folder : folders) {
      autoDeleteFolder(folder);
    }
  }

  /**
   * Do the auto delete for a folder.
   *
   * @param folder
   */
  private void autoDeleteFolder(FileInfo folder) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("About to auto delete in folder " + folder.getName());
    }

    List<FileInfo> files = _fileFolderService.listFiles(folder.getNodeRef());

    if (LOG.isDebugEnabled()) {
      LOG.debug("Found " + files.size() + " files to maybe auto delete");
    }

    for (FileInfo file : files) {
      autoDeleteFile(file);
    }
  }

  private void autoDeleteFile(FileInfo file) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("About to maybe auto delete file " + file.getName());
    }

    if (file.getContentData() == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("No content for document " + file.getName() + "; " + file.getNodeRef());
      }

      return;
    }

    String mimetype = file.getContentData().getMimetype();

    // first check if the mimetype of the document is in the configured mimetype list
    if (!_configuration.get().getMimetypes().contains(mimetype)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Mimetype is " + mimetype + ", not configured for auto deletion");
      }

      return;
    }

    int difference = daysBetween(file.getCreatedDate(), new Date());

    // if the difference is less than the max allowed age, exit
    if (difference < _configuration.get().getMaxAge()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("The age of the file is only " + difference + " which is less than " + _configuration.get().getMaxAge());
      }

      return;
    }

    // ok if we've come this far, it's up for deletion
    if (_configuration.get().getDeleteNode()) {
      // add the noarchive aspect for faster deletion
      _nodeService.addAspect(file.getNodeRef(), VgrModel.ASPECT_NOARCHIVE, Collections.<QName, Serializable>emptyMap());

      _nodeService.deleteNode(file.getNodeRef());

      if (LOG.isDebugEnabled()) {
        LOG.debug("Deleted document " + file.getName() + "; " + file.getNodeRef());
      }
    } else {
      _nodeService.removeProperty(file.getNodeRef(), ContentModel.PROP_CONTENT);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Delete content for document " + file.getName() + "; " + file.getNodeRef());
      }
    }
  }

  private int daysBetween(Date date1, Date date2) {
    return (int) ((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24));
  }

  /**
   * Creates a Configuration object out of the node properties.
   *
   * @param site to create configuration for
   * @return A Configuration object
   */
  @SuppressWarnings("unchecked")
  private void createConfiguration(NodeRef site) {
    Configuration configuration = new Configuration();

    configuration.setMaxAge((Long) _nodeService.getProperty(site, VgrModel.PROP_AUTO_DELETABLE_MAX_AGE));
    configuration.setDeleteNode((Boolean) _nodeService.getProperty(site, VgrModel.PROP_AUTO_DELETABLE_DELETE_NODE));
    configuration.setMimetypes((List<String>) _nodeService.getProperty(site, VgrModel.PROP_AUTO_DELETABLE_MIMETYPES));

    _configuration.set(configuration);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Created auto delete configuration " + configuration);
    }
  }

  private String getSiteInfo(NodeRef site) {
    String name = (String) _nodeService.getProperty(site, ContentModel.PROP_NAME);
    String title = (String) _nodeService.getProperty(site, ContentModel.PROP_TITLE);

    return title + " (" + name + ")";
  }

  class Configuration {

    Long _maxAge = 90l;

    Boolean _deleteNode = Boolean.TRUE;

    List<String> _mimetypes = new ArrayList<String>();

    public void setMaxAge(Long maxAge) {
      _maxAge = maxAge;
    }

    public void setDeleteNode(Boolean deleteNode) {
      _deleteNode = deleteNode;
    }

    public void setMimetypes(List<String> mimetypes) {
      _mimetypes = mimetypes;
    }

    public Long getMaxAge() {
      return _maxAge;
    }

    public Boolean getDeleteNode() {
      return _deleteNode;
    }

    public List<String> getMimetypes() {
      if (_mimetypes == null) {
        _mimetypes = new ArrayList<String>();
      }

      return _mimetypes;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }
  }

}
