package se.vgregion.alfresco.repo.storage.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailHelper;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.util.VersionNumber;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.CreationCallback;
import se.vgregion.alfresco.repo.storage.StorageService;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class StorageServiceImpl implements StorageService, InitializingBean {

  private static final Logger LOG = Logger.getLogger(StorageServiceImpl.class);

  private NodeService _nodeService;

  private FileFolderService _fileFolderService;

  private ServiceRegistry _serviceRegistry;

  private String _storageNodeRef;

  private PermissionService _permissionService;

  private CopyService _copyService;

  private ServiceUtilsImpl _serviceUtils;

  private BehaviourFilter _behaviourFilter;

  private RenditionService _renditionService;

  private ContentService _contentService;

  private Properties _globalProperties;

  private SearchService _searchService;

  private DictionaryService _dictionaryService;

  private RetryingTransactionHelper _retryingTransactionHelper;

  private ThumbnailService _thumbnailService;

  private ActionService _actionService;

  private OwnableService _ownableService;

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setFileFolderService(final FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
    _serviceRegistry = serviceRegistry;
  }

  public void setStorageNodeRef(final String storageNodeRef) {
    _storageNodeRef = storageNodeRef;
  }

  public void setPermissionService(final PermissionService permissionService) {
    _permissionService = permissionService;
  }

  public void setCopyService(final CopyService copyService) {
    _copyService = copyService;
  }

  public void setServiceUtils(final ServiceUtilsImpl serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void setGlobalProperties(final Properties globalProperties) {
    _globalProperties = globalProperties;
  }

  public void setRenditionService(final RenditionService renditionService) {
    _renditionService = renditionService;
  }

  public void setContentService(final ContentService contentService) {
    _contentService = contentService;
  }

  public void setSearchService(final SearchService searchService) {
    _searchService = searchService;
  }

  public void setDictionaryService(DictionaryService dictionaryService) {
    _dictionaryService = dictionaryService;
  }

  public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper) {
    _retryingTransactionHelper = retryingTransactionHelper;
  }

  public void setThumbnailService(ThumbnailService thumbnailService) {
    _thumbnailService = thumbnailService;
  }

  public void setActionService(ActionService actionService) {
    _actionService = actionService;
  }

  public void setOwnableService(OwnableService ownableService) {
    _ownableService = ownableService;
  }

  @Override
  public void publishToStorage(final NodeRef nodeRef) {
    // first check so that each and every condition is met before publishing
    assertPublishable(nodeRef);

    try {
      // create the folder structure <year>/<month>/<day>
      final NodeRef finalFolder = createFolderStructure();

      publishFileToStorage(nodeRef, finalFolder);
    } catch (final Exception ex) {
      LOG.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void publishToStorage(final String sourceNodeRef) {
    final NodeRef nodeRef = new NodeRef(sourceNodeRef);

    publishToStorage(nodeRef);
  }

  private void publishFileToStorage(final NodeRef nodeRef, final NodeRef finalFolder) {
    final String oldName = (String) _nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    final String newName = getUniqueName(finalFolder, oldName);

    // disable the auditable aspect in order to prevent the last updated user to be "system"
    _behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

    // we don't want to up the version, so we disable the versionalbe aspect
    _behaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);

    // add the published date
    final Date now = new Date();

    _nodeService.setProperty(nodeRef, VgrModel.PROP_DATE_ISSUED, now);

    // check if "available from" is set, otherwise set it to NOW
    final Date afrom = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_FROM);

    if (afrom == null) {
      _nodeService.setProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_FROM, now);
    }

    // get the username of the currently logged in person
    final String username = _serviceUtils.getCurrentUserName();

    // get and set the correct publisher string
    _nodeService.setProperty(nodeRef, VgrModel.PROP_PUBLISHER, _serviceUtils.getRepresentation(username));
    _nodeService.setProperty(nodeRef, VgrModel.PROP_PUBLISHER_ID, username);

    // check if this file has been published before, i.e. we've published
    // it,
    // then revoked it, and are now publishing again
    final NodeRef publishedNodeRef = getPublishedNodeRef(nodeRef);

    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        if (publishedNodeRef != null) {
          // then we need to update the copy's properties as well
          _nodeService.setProperty(publishedNodeRef, VgrModel.PROP_DATE_ISSUED, now);
          _nodeService.setProperty(publishedNodeRef, VgrModel.PROP_DATE_AVAILABLE_FROM, _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_FROM));
          _nodeService.setProperty(publishedNodeRef, VgrModel.PROP_PUBLISHER, _serviceUtils.getRepresentation(username));
          _nodeService.setProperty(publishedNodeRef, VgrModel.PROP_PUBLISHER_ID, username);
        } else {
          // new publication! let's copy the node to storage

          // unpublish all old documents
          unpublishFromStorage(nodeRef.toString());

          // make a copy of the source and rename it...
          final NodeRef newNode = _copyService.copyAndRename(nodeRef, finalFolder, ContentModel.ASSOC_CONTAINS, null, true);

          // remove all old published associations
          removePublishedAssocs(newNode);

          // set the new node name
          _nodeService.setProperty(newNode, ContentModel.PROP_NAME, newName);

          // set the nodeRef to document.id
          _nodeService.setProperty(newNode, VgrModel.PROP_IDENTIFIER_DOCUMENTID, newNode.toString());
          _nodeService.setProperty(nodeRef, VgrModel.PROP_IDENTIFIER_DOCUMENTID, newNode.toString());

          // create an association between the two nodes
          _nodeService.createAssociation(nodeRef, newNode, VgrModel.ASSOC_PUBLISHED_TO_STORAGE);

          if (LOG.isDebugEnabled()) {
            LOG.debug("Version to publish: " + _nodeService.getProperty(newNode, VgrModel.PROP_IDENTIFIER_VERSION));
          }

          // add a published aspect to the storage node
          _nodeService.addAspect(newNode, VgrModel.ASPECT_PUBLISHED, null);

          // inherit permissions
          _permissionService.setInheritParentPermissions(newNode, true);

          // set the modified property and the date saved
          final Date now = new Date();
          _nodeService.setProperty(newNode, ContentModel.PROP_MODIFIED, now);
          _serviceUtils.setDateSaved(newNode);

          // set the field "DC.source.documentid" when publishing
          _nodeService.setProperty(newNode, VgrModel.PROP_SOURCE_DOCUMENTID, nodeRef.toString());
          _nodeService.setProperty(nodeRef, VgrModel.PROP_SOURCE_DOCUMENTID, nodeRef.toString());

          // set the field "DC.identifier" & "DC.identifier.native" when publishing
          String identifier = _serviceUtils.getDocumentIdentifier(newNode);
          String nativeIdentifier = _serviceUtils.getDocumentIdentifier(newNode, true);
          _nodeService.setProperty(newNode, VgrModel.PROP_IDENTIFIER, identifier);
          _nodeService.setProperty(newNode, VgrModel.PROP_IDENTIFIER_NATIVE, nativeIdentifier);
          _nodeService.setProperty(nodeRef, VgrModel.PROP_IDENTIFIER, identifier);

          // set the field "DC.source" when publishing on the source
          // nodeRef,
          // this is a link to the document in "Lagret"
          final String documentSource = _serviceUtils.getDocumentSource(nodeRef);
          _nodeService.setProperty(newNode, VgrModel.PROP_SOURCE, documentSource);
          _nodeService.setProperty(nodeRef, VgrModel.PROP_SOURCE, documentSource);

          deleteRenditions(newNode);

          // create a PDF/A rendition of the nodeRef
          createPdfRendition(newNode);
        }

        return null;
      }
    }, AuthenticationUtil.getSystemUserName());
  }

  protected void deleteRenditions(final NodeRef newNode) {
    final List<ChildAssociationRef> renditions = _renditionService.getRenditions(newNode);

    for (final ChildAssociationRef rendition : renditions) {
      _nodeService.deleteNode(rendition.getChildRef());
    }
  }

  private void removePublishedAssocs(final NodeRef nodeRef) {
    final List<NodeRef> children = findPublishedDocuments(nodeRef.toString());

    for (final NodeRef child : children) {
      _nodeService.removeAssociation(nodeRef, child, VgrModel.ASSOC_PUBLISHED_TO_STORAGE);
    }
  }

  /**
   * Returns the published (in Lagret) node for a source node.
   * 
   * @param nodeRef
   *          The source node to look for the published node for.
   * @return nodeRef The published node or null if not published.
   */
  @Override
  public NodeRef getPublishedNodeRef(final NodeRef nodeRef) {
    NodeRef result = null;

    final List<NodeRef> children = findPublishedDocuments(nodeRef.toString());

    for (final NodeRef child : children) {
      final String sourceVersion = _serviceUtils.getStringValue(_nodeService.getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION));
      final String publishedVersion = _serviceUtils.getStringValue(_nodeService.getProperty(child, VgrModel.PROP_IDENTIFIER_VERSION));

      final StoreRef storeRef = child.getStoreRef();

      // if the published node is in the archive store, it's not really
      // published...
      if (storeRef.equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE)) {
        continue;
      }

      final boolean published = StringUtils.equals(sourceVersion, publishedVersion);

      if (published) {
        result = child;

        break;
      }
    }

    return result;
  }

  /**
   * Finds all the published documents for a specific source document id.
   * 
   * @param sourceDocumentId
   * @return
   */
  private List<NodeRef> findPublishedDocuments(String sourceDocumentId) {
    StringBuffer query = new StringBuffer();
    query.append("TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND ");
    query.append("vgr:dc\\.source\\.documentid:\"" + sourceDocumentId + "\"");

    SearchParameters searchParameters = new SearchParameters();

    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setQuery(query.toString());
    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);

    ResultSet result = _searchService.query(searchParameters);

    try {
      return result.getNodeRefs();
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }
  }

  private NodeRef createFolderStructure() {
    final NodeRef storageNodeRef = getStorageNodeRef();

    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());

    final String year = String.valueOf(calendar.get(Calendar.YEAR));
    final String month = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.MONTH) + 1), 2, "0");
    final String day = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)), 2, "0");

    final NodeRef yearFolder = getOrCreateSubFolder(storageNodeRef, year);
    final NodeRef monthFolder = getOrCreateSubFolder(yearFolder, month);
    final NodeRef dayFolder = getOrCreateSubFolder(monthFolder, day);

    return dayFolder;
  }

  private NodeRef getStorageNodeRef() {
    final String query = "PATH:\"/app:company_home\"";

    final ResultSet nodes = _searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);

    try {
      final NodeRef companyHome = nodes.getNodeRef(0);

      NodeRef storage = _fileFolderService.searchSimple(companyHome, "Lagret");

      if (storage == null) {
        storage = _fileFolderService.create(companyHome, "Lagret", ContentModel.TYPE_FOLDER).getNodeRef();

        _permissionService.setInheritParentPermissions(storage, false);
        _permissionService.setPermission(storage, PermissionService.ALL_AUTHORITIES, PermissionService.CONSUMER, true);
        _permissionService.setPermission(storage, "guest", PermissionService.CONSUMER, true);
      }

      return storage;
    } finally {
      ServiceUtilsImpl.closeQuietly(nodes);
    }
  }

  private NodeRef getOrCreateSubFolder(final NodeRef parentNodeRef, final String subFolder) {
    final NodeRef existingFolder = _nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, subFolder);

    NodeRef subFolderNodeRef;

    if (existingFolder == null) {
      subFolderNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {

        @Override
        public NodeRef doWork() throws Exception {
          final FileInfo fileInfo = _fileFolderService.create(parentNodeRef, subFolder, ContentModel.TYPE_FOLDER);

          final NodeRef nodeRef = fileInfo.getNodeRef();

          // inherit permissions
          _permissionService.setInheritParentPermissions(nodeRef, true);

          return nodeRef;
        }

      }, AuthenticationUtil.getSystemUserName());
    } else {
      subFolderNodeRef = existingFolder;
    }

    return subFolderNodeRef;
  }

  private String getUniqueName(final NodeRef destNodeRef, String name) {
    NodeRef existingFile = _nodeService.getChildByName(destNodeRef, ContentModel.ASSOC_CONTAINS, name);

    if (existingFile != null) {
      // Find a new unique name for clashing names
      int counter = 1;
      String tmpFilename = name;
      int dotIndex;

      while (existingFile != null) {
        dotIndex = name.lastIndexOf(".");

        if (dotIndex == 0) {
          // File didn't have a proper 'name' instead it had just a
          // suffix and
          // started with a ".", create "1.txt"
          tmpFilename = counter + name;
        } else if (dotIndex > 0) {
          // Filename contained ".", create "filename-1.txt"
          tmpFilename = name.substring(0, dotIndex) + "-" + counter + name.substring(dotIndex);
        } else {
          // Filename didn't contain a dot at all, create "filename-1"
          tmpFilename = name + "-" + counter;
        }

        existingFile = _nodeService.getChildByName(destNodeRef, ContentModel.ASSOC_CONTAINS, tmpFilename);

        counter++;
      }

      name = tmpFilename;
    }

    return name;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_fileFolderService);
    Assert.notNull(_nodeService);
    Assert.notNull(_serviceRegistry);
    Assert.notNull(_permissionService);
    Assert.notNull(_copyService);
    Assert.notNull(_serviceUtils);
    Assert.notNull(_behaviourFilter);
    Assert.notNull(_globalProperties);
    Assert.notNull(_renditionService);
    Assert.notNull(_contentService);
    Assert.notNull(_searchService);
    Assert.hasText(_storageNodeRef);
    Assert.notNull(_dictionaryService);
    Assert.notNull(_retryingTransactionHelper);
    Assert.notNull(_thumbnailService);
    Assert.notNull(_actionService);
  }

  @Override
  public void unpublishFromStorage(final String sourceDocumentId) {
    unpublishFromStorage(sourceDocumentId, null);
  }

  private void unpublishFromStorage(final String sourceDocumentId, final String currentVersion) {
    Assert.hasText(sourceDocumentId, "Source document id can't be empty.");

    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        _behaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);

        try {
          Date now = new Date();

          final List<NodeRef> previouslyPublished = findPublishedDocuments(sourceDocumentId);

          if (previouslyPublished.isEmpty()) {
            return null;
          }

          for (NodeRef published : previouslyPublished) {
            String version = (String) _nodeService.getProperty(published, VgrModel.PROP_IDENTIFIER_VERSION);

            // if currentVersion is passed, that one should NOT be unpublished
            if (StringUtils.isNotBlank(currentVersion) && version.equals(currentVersion)) {
              continue;
            }

            // check if it's still published
            final Date availiable = (Date) _nodeService.getProperty(published, VgrModel.PROP_DATE_AVAILABLE_TO);

            if (availiable == null || now.equals(availiable) || now.before(availiable)) {
              // find storage copy and alter it too
              _nodeService.setProperty(published, VgrModel.PROP_DATE_AVAILABLE_TO, now);

              // must set modified time stamp so that the feed gets the correct
              // <update> value
              _nodeService.setProperty(published, ContentModel.PROP_MODIFIED, now);

              _serviceUtils.setDateSaved(published);
            }
          }
        } catch (final Exception ex) {
          LOG.error(ex.getMessage(), ex);
          throw new RuntimeException(ex);
        } finally {
          _behaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        }

        return null;
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  @Override
  public void moveToStorage(final NodeRef nodeRef) {
    // here's a very, very slim risk that the Storage folder is not
    // created...
    assertPublishable(nodeRef);

    // create the folder structure <year>/<month>/<day>
    final NodeRef finalFolder = createFolderStructure();

    try {
      String sourceDocumentId = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);

      String currentVersion = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);

      // unpublish all the old versions except this one...
      unpublishFromStorage(sourceDocumentId, currentVersion);

      // set a new name to avoid clashes
      final String oldName = (String) _nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      final String newName = getUniqueName(finalFolder, oldName);
      _nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, newName);

      _fileFolderService.move(nodeRef, finalFolder, null);

      // add a published aspect to the storage node
      _nodeService.addAspect(nodeRef, VgrModel.ASPECT_PUBLISHED, null);

      // inherit permissions
      _permissionService.setInheritParentPermissions(nodeRef, true);

      // set the dc.identifier.documentid to the created nodes id
      _nodeService.setProperty(nodeRef, VgrModel.PROP_IDENTIFIER_DOCUMENTID, nodeRef.toString());

      // get the document identifier for the document
      final String identifier = _serviceUtils.getDocumentIdentifier(nodeRef);

      //
      _nodeService.setProperty(nodeRef, VgrModel.PROP_IDENTIFIER, identifier);

      // remove the owner of the document to prevent deletion and stuff
      _ownableService.setOwner(nodeRef, AuthenticationUtil.getSystemUserName());

      // create a PDF/A rendition
      createPdfRendition(nodeRef);

      if (LOG.isDebugEnabled()) {
        LOG.debug("File '" + nodeRef + "' moved to Lagret.");
      }
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean createPdfRendition(final NodeRef nodeRef) {
    return createPdfRendition(nodeRef, true);
  }

  @Override
  public boolean createPdfRendition(final NodeRef nodeRef, final boolean async) {
    // must first check whether the nodeRef can be transformed into a PDF/A or not...
    if (!pdfaRendable(nodeRef)) {
      return false;
    }

    // Use the thumbnail registy to get the details of the thumbail
    ThumbnailRegistry registry = _thumbnailService.getThumbnailRegistry();

    ThumbnailDefinition details = registry.getThumbnailDefinition("pdfa");

    if (details == null) {
      // Throw exception
      throw new RuntimeException("The thumbnail name pdfa is not registered");
    }

    // If there's nothing currently registered to generate thumbnails for the specified mimetype, then log a message and
    // bail out
    String mimeType = _serviceUtils.getMimetype(nodeRef);

    Serializable value = _nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

    ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);

    if (contentData == null) {
      LOG.info("Unable to create thumbnail '" + details.getName() + "' as there is no content");

      return false;
    }

    if (!registry.isThumbnailDefinitionAvailable(contentData.getContentUrl(), mimeType, contentData.getSize(), nodeRef, details)) {
      LOG.info("Unable to create thumbnail '" + details.getName() + "' for " + mimeType + " as no transformer is currently available");

      return false;
    }

    // Have the thumbnail created
    if (!async) {
      // Create the thumbnail
      _thumbnailService.createThumbnail(nodeRef, ContentModel.PROP_CONTENT, details.getMimetype(), details.getTransformationOptions(), details.getName());
    } else {
      Action action = ThumbnailHelper.createCreateThumbnailAction(details, _serviceRegistry);

      // Queue async creation of thumbnail
      _actionService.executeAction(action, nodeRef, true, true);
    }

    return true;
  }

  private void assertPublishable(final NodeRef nodeRef) {
    // must get the nodeRef to see if the user has create permission in that folder
    NodeRef parentNodeRef = _nodeService.getPrimaryParent(nodeRef).getParentRef();

    if (_permissionService.hasPermission(parentNodeRef, PermissionService.CREATE_CHILDREN) != AccessStatus.ALLOWED) {
      throw new AlfrescoRuntimeException("Only site administrators, site collaborators and system wide administrators can publish to storage.");
    }

    final FileFolderServiceType nodeType = _fileFolderService.getType(_nodeService.getType(nodeRef));

    if (nodeType != FileFolderServiceType.FILE) {
      throw new AlfrescoRuntimeException("Only files can be published.");
    }

    try {
      // vgr:dc.title must be set
      Assert.hasText((String) _nodeService.getProperty(nodeRef, VgrModel.PROP_TITLE), "The published document must have a 'dc.title' property.");

      // dc.type.record must be set
      Assert.hasText((String) _nodeService.getProperty(nodeRef, VgrModel.PROP_TYPE_RECORD), "The published document must have a 'dc.type.record' property.");

      // dc.publisher.forunit or dc.publisher.project-assignment must be
      // set
      @SuppressWarnings("unchecked")
      List<String> publisherForunit = (List<String>) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUBLISHER_FORUNIT);

      @SuppressWarnings("unchecked")
      List<String> publisherProjectAssignment = (List<String>) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT);

      publisherForunit = publisherForunit != null ? publisherForunit : new ArrayList<String>();
      publisherProjectAssignment = publisherProjectAssignment != null ? publisherProjectAssignment : new ArrayList<String>();

      Assert.isTrue(publisherForunit.size() > 0 || publisherProjectAssignment.size() > 0, "Either 'dc.publisher.forunit' or 'dc.publisher.project-assignment' must be set.");
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new AlfrescoRuntimeException(ex.getMessage(), ex);
    }
  }

  @Override
  public int createMissingPdfRenditions() {
    return createMissingPdfRenditions(null);
  }

  @Override
  public int createMissingPdfRenditions(final CreationCallback creationCallback) {
    final int count = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Integer>() {

      @Override
      public Integer doWork() throws Exception {
        final String query = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\"";

        final SearchParameters searchParameters = new SearchParameters();

        searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        searchParameters.setQuery(query);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setMaxItems(-1);

        final ResultSet documents = _searchService.query(searchParameters);

        int count = 0;

        try {
          LOG.info("Documents to maybe generate PDF/A for: " + documents.length());

          for (final ResultSetRow document : documents) {
            try {
              count = createMissingPdfRendition(document) ? count + 1 : count;

              if (creationCallback != null) {
                creationCallback.execute();
              }
            } catch (final Exception ex) {
              continue;
            }
          }
        } finally {
          ServiceUtilsImpl.closeQuietly(documents);
        }

        return count;
      }

    }, AuthenticationUtil.getSystemUserName());

    return count;
  }

  @Override
  public boolean pdfaRendable(NodeRef nodeRef) {
    final String sourceMimetype = _serviceUtils.getMimetype(nodeRef);

    // must first check whether the nodeRef can be transformed into a PDF or
    // not...
    if (_contentService.getTransformer(sourceMimetype, MimetypeMap.MIMETYPE_PDF) == null) {
      return false;
    }

    return true;
  }

  private boolean createMissingPdfRendition(final ResultSetRow node) {
    final RetryingTransactionHelper.RetryingTransactionCallback<Boolean> execution = new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {

      @Override
      public Boolean execute() throws Throwable {
        _behaviourFilter.disableBehaviour();

        final NodeRef nodeRef = node.getNodeRef();

        if (!_nodeService.exists(nodeRef)) {
          return false;
        }

        NodeRef pdfaRendition = _thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, "pdfa");

        // if the rendition is already there, just exit
        if (pdfaRendition != null) {
          return false;
        }

        // create the PDF rendition
        final boolean result = createPdfRendition(nodeRef, false);

        if (!result) {
          return false;
        }

        _behaviourFilter.enableBehaviour();

        LOG.info("Create PDF/A for: " + nodeRef);

        return result;
      }

    };

    return _retryingTransactionHelper.doInTransaction(execution, false, true);
  }

  @Override
  public NodeRef getLatestPublishedStorageVersion(final String nodeRef) {
    List<NodeRef> originalRows = getStorageVersions(nodeRef);

    List<NodeRef> rows = new ArrayList<NodeRef>();

    for (final NodeRef row : originalRows) {
      // only add published nodes to this list
      if (!_serviceUtils.isPublished(row)) {
        continue;
      }

      rows.add(row);
    }

    return rows.size() > 0 ? rows.get(0) : null;
  }

  @Override
  public NodeRef getLatestStorageVersion(String nodeRef) {
    List<NodeRef> rows = getStorageVersions(nodeRef);

    return rows.size() > 0 ? rows.get(0) : null;
  }

  @Override
  public NodeRef getPublishedStorageVersion(String nodeRef, String version) {
    String query = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND vgr:dc_x002e_source_x002e_documentid:\"" + nodeRef + "\" AND vgr:dc_x002e_identifier_x002e_version:\"" + version + "\"";

    SearchParameters searchParameters = new SearchParameters();
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    searchParameters.setQuery(query);

    ResultSet nodes = _searchService.query(searchParameters);

    try {
      NodeRef document = nodes.length() > 0 ? nodes.getNodeRef(0) : null;

      if (document == null) {
        return null;
      }

      return _serviceUtils.isPublished(document) ? document : null;
    } finally {
      ServiceUtilsImpl.closeQuietly(nodes);
    }
  }

  @Override
  public List<NodeRef> getStorageVersions(String nodeRef) {
    String query = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND (vgr:dc_x002e_source_x002e_documentid:\"" + nodeRef + "\" OR vgr:dc_x002e_identifier_x002e_documentid:\"" + nodeRef + "\")";

    Locale locale = new Locale("sv");
    I18NUtil.setLocale(locale);

    SearchParameters searchParameters = new SearchParameters();
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    searchParameters.setQuery(query);

    ResultSet nodes = _searchService.query(searchParameters);

    List<ResultSetRow> rows = new ArrayList<ResultSetRow>();
    List<NodeRef> result = new ArrayList<NodeRef>();

    // don't really know how to do this, we need to have a sorted list AND we
    // need to return just nodeRefs because we have to close the resultSet.
    try {
      for (ResultSetRow row : nodes) {
        rows.add(row);
      }

      Collections.sort(rows, new Comparator<ResultSetRow>() {

        @Override
        public int compare(ResultSetRow row1, ResultSetRow row2) {
          String label1 = (String) _nodeService.getProperty(row1.getNodeRef(), VgrModel.PROP_IDENTIFIER_VERSION);

          String label2 = (String) _nodeService.getProperty(row2.getNodeRef(), VgrModel.PROP_IDENTIFIER_VERSION);

          // sort the list descending (ie. most recent first)
          return new VersionNumber(label2).compareTo(new VersionNumber(label1));
        }

      });

      for (ResultSetRow row : rows) {
        result.add(row.getNodeRef());
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(nodes);
    }

    return result;
  }
}
