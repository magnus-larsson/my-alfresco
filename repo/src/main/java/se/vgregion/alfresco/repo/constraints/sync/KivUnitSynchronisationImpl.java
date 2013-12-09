package se.vgregion.alfresco.repo.constraints.sync;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import se.vgregion.alfresco.repo.constraints.KivService;
import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;
import se.vgregion.alfresco.repo.model.KivUnit;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class KivUnitSynchronisationImpl extends ClusteredExecuter implements InitializingBean, KivUnitSynchronisation {

  private static final Logger LOG = Logger.getLogger(KivUnitSynchronisationImpl.class);

  private KivService _kivService;

  private SearchService _searchService;

  private NodeService _nodeService;

  private FileFolderService _fileFolderService;

  private ThreadPoolExecutor _threadPoolExecutor;

  private Repository _repositoryHelper;

  private NamespaceService _namespaceService;

  public void setKivService(final KivService kivService) {
    _kivService = kivService;
  }

  public void setRepositoryHelper(Repository repositoryHelper) {
    _repositoryHelper = repositoryHelper;
  }

  public void setSearchService(final SearchService searchService) {
    _searchService = searchService;
  }

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setFileFolderService(final FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public void setNamespaceService(NamespaceService namespaceService) {
    _namespaceService = namespaceService;
  }

  @Override
  public void synchronise() {
    execute();
  }

  private void synchroniseInternal() {
    final RetryingTransactionHelper retryingTransactionHelper = _transactionService.getRetryingTransactionHelper();

    AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {

        RetryingTransactionHelper.RetryingTransactionCallback<Object> callback = new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {

          @Override
          public Object execute() throws Throwable {
            List<NodeRef> actualNodes = Collections.synchronizedList(new ArrayList<NodeRef>());

            doSynchronise(actualNodes);

            return null;
          }
        };

        retryingTransactionHelper.doInTransaction(callback, false, true);

        LOG.info("Finished synchronising KIV units.");

        return null;
      }

    });
  }

  private void doSynchronise(final List<NodeRef> actualNodes) {
    final List<KivUnit> units = _kivService.findOrganisationalUnits();

    if (units.size() > 0) {
      synchroniseUnits(actualNodes, units);
    }

    int jobsInQueue = _threadPoolExecutor.getQueue().size();

    int activeCount = _threadPoolExecutor.getActiveCount();

    while (jobsInQueue > 0 || activeCount > 0) {
      try {
        refreshLock();

        Thread.sleep(5000);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Waiting for jobs to complete. Jobs in queue: " + jobsInQueue + " Active jobs: " + activeCount);
        }
        // refreshLock();
      } catch (InterruptedException e) {
        LOG.error("Error while trying to sleep thread", e);
      }

      jobsInQueue = _threadPoolExecutor.getQueue().size();

      activeCount = _threadPoolExecutor.getActiveCount();
    }

    deleteRemovedUnits(actualNodes);
  }

  private void deleteRemovedUnits(List<NodeRef> actualNodes) {
    LOG.debug("Starting processing of deleted Units");

    List<FileInfo> listDeepFolders = _fileFolderService.listDeepFolders(getKivVastraGotalandsregionenNode(), null);

    List<NodeRef> foldersToDelete = new ArrayList<NodeRef>();

    for (FileInfo folder : listDeepFolders) {
      if (!actualNodes.contains(folder.getNodeRef())) {
        foldersToDelete.add(folder.getNodeRef());
      }
    }

    LOG.info("Preparing to delete " + foldersToDelete.size() + " nodes");

    for (NodeRef folderToDelete : foldersToDelete) {
      if (_nodeService.exists(folderToDelete)) {
        _nodeService.deleteNode(folderToDelete);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Deleting node " + folderToDelete.toString() + " and its children.");
        }
      }
    }

    LOG.info("Processing of deleted KIV units complete");
    // TODO
  }

  private void synchroniseUnits(final List<NodeRef> actualNodes, final List<KivUnit> units) {
    synchroniseUnits(actualNodes, units, null);
  }

  private void synchroniseUnits(final List<NodeRef> actualNodes, final List<KivUnit> units, final KivUnit parentUnit) {
    final RetryingTransactionHelper retryingTransactionHelper = _transactionService.getRetryingTransactionHelper();

    for (final KivUnit unit : units) {
      final NodeRef parentNodeRef = parentUnit != null && parentUnit.getNodeRef() != null ? parentUnit.getNodeRef() : findNodeRef(parentUnit);

      RetryingTransactionHelper.RetryingTransactionCallback<Object> callback = new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {

        @Override
        public Object execute() throws Throwable {
          synchroniseUnit(actualNodes, unit, parentNodeRef);

          return null;
        }

      };

      retryingTransactionHelper.doInTransaction(callback, false, true);

      Runnable runnable = new KivSearcher(unit, actualNodes);

      _threadPoolExecutor.execute(runnable);
    }
  }

  private void synchroniseUnit(final List<NodeRef> actualNodes, final KivUnit unit, final NodeRef parentNodeRef) {
    // refreshLock();

    NodeRef nodeRef = unit != null && unit.getNodeRef() != null ? unit.getNodeRef() : findNodeRef(unit);

    // if the node doesn't exist, just exit
    if (nodeRef != null && !_nodeService.exists(nodeRef)) {
      nodeRef = null;
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("# of nodes processed: " + actualNodes.size());
    }

    if (nodeRef == null) {
      nodeRef = createNode(unit, parentNodeRef);

      LOG.info("Created: node '" + unit.getOrganisationalUnit() + "' created");

      if (actualNodes.contains(nodeRef)) {
        LOG.warn("Node " + nodeRef + " already processed");
      } else {
        actualNodes.add(nodeRef);
      }

      return;
    }

    if (actualNodes.contains(nodeRef)) {
      LOG.warn("Node " + nodeRef + " already processed");
    } else {
      actualNodes.add(nodeRef);
    }

    final boolean updated = updateNode(unit, nodeRef);

    if (updated) {
      LOG.info("Updated: node '" + unit.getOrganisationalUnit() + "'.");

      return;
    }

    final boolean moved = moveNode(unit, nodeRef, parentNodeRef);

    if (moved) {
      LOG.info("Moved: node '" + unit.getOrganisationalUnit() + "'.");

      return;
    }

    // last but not the least, if neither created nor updated at least
    // update
    // the accessed date...
    markUnitAsAccessed(nodeRef);
  }

  private boolean moveNode(final KivUnit unit, final NodeRef nodeRef, final NodeRef parentNodeRef) {
    // if there is no parent node ref, then don't move it
    if (parentNodeRef == null) {
      return false;
    }

    // get a list of all parent associations
    final List<ChildAssociationRef> parentAssociations = _nodeService.getParentAssocs(nodeRef);

    // if there are not parent associations, don't move
    if (parentAssociations.size() == 0) {
      return false;
    }

    // get the stored parent node ref
    final NodeRef storedParentNodeRef = parentAssociations.get(0).getParentRef();

    // if the parent node ref is same as stored parent node ref, don't move
    if (parentNodeRef.equals(storedParentNodeRef)) {
      return false;
    }

    try {
      return _fileFolderService.move(nodeRef, parentNodeRef, null) != null;
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private void markUnitAsAccessed(final NodeRef nodeRef) {
    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

    properties.put(VgrModel.PROP_KIV_ACCESSED, new Date().getTime());

    _nodeService.addProperties(nodeRef, properties);
  }

  private boolean updateNode(final KivUnit unit, final NodeRef nodeRef) {
    boolean updated = false;

    final Map<QName, Serializable> updatedValues = new HashMap<QName, Serializable>();

    final String liveHsaIdentity = unit.getHsaIdentity();

    final Serializable savedHsaIdentity = _nodeService.getProperty(nodeRef, VgrModel.PROP_KIV_HSAIDENTITY);

    if (isChanged(liveHsaIdentity, savedHsaIdentity)) {
      updatedValues.put(VgrModel.PROP_KIV_HSAIDENTITY, liveHsaIdentity);
    }

    final String liveOu = unit.getOrganisationalUnit();

    final Serializable savedOu = _nodeService.getProperty(nodeRef, VgrModel.PROP_KIV_OU);

    if (isChanged(liveOu, savedOu)) {
      updatedValues.put(VgrModel.PROP_KIV_OU, liveOu);
    }

    if (updatedValues.size() > 0) {
      updatedValues.put(VgrModel.PROP_KIV_MODIFIED, new Date().getTime());
      updatedValues.put(VgrModel.PROP_KIV_ACCESSED, new Date().getTime());

      _nodeService.addProperties(nodeRef, updatedValues);

      updated = true;
    }

    return updated;
  }

  private NodeRef createNode(final KivUnit unit, final NodeRef parentUnitNodeRef) {
    final NodeRef parent = parentUnitNodeRef != null ? parentUnitNodeRef : getKivVastraGotalandsregionenNode();

    final String name = getValidName(unit.getOrganisationalUnit()) + " - " + unit.getHsaIdentity();

    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(VgrModel.PROP_KIV_OU, unit.getOrganisationalUnit());
    properties.put(VgrModel.PROP_KIV_HSAIDENTITY, unit.getHsaIdentity());
    properties.put(VgrModel.PROP_KIV_DN, unit.getDistinguishedName());
    properties.put(ContentModel.PROP_NAME, name);
    properties.put(VgrModel.PROP_KIV_MODIFIED, new Date().getTime());
    properties.put(VgrModel.PROP_KIV_ACCESSED, new Date().getTime());

    final String uri = _nodeService.getPrimaryParent(parent).getQName().getNamespaceURI();

    final String validLocalName = QName.createValidLocalName(unit.getOrganisationalUnit());

    final NodeRef unitNodeRef = _nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.createQName(uri, validLocalName), VgrModel.TYPE_KIV_UNIT).getChildRef();

    _nodeService.addProperties(unitNodeRef, properties);

    unit.setNodeRef(unitNodeRef);

    return unitNodeRef;
  }

  private NodeRef getKivVastraGotalandsregionenNode() {
    final NodeRef kivTypeNode = getKivTypeNode();

    List<NodeRef> result = _searchService.selectNodes(kivTypeNode, "app:Västra_x0020_Götalandsregionen", null, _namespaceService, false);

    if (result.size() > 0) {
      return result.get(0);
    }

    final String uri = _nodeService.getPrimaryParent(kivTypeNode).getQName().getNamespaceURI();

    final String validLocalName = QName.createValidLocalName("Västra Götalandsregionen");

    final NodeRef kivVastraGotalandsregionenNode = _nodeService.createNode(kivTypeNode, ContentModel.ASSOC_CONTAINS, QName.createQName(uri, validLocalName), VgrModel.TYPE_KIV_UNIT).getChildRef();

    _nodeService.setProperty(kivVastraGotalandsregionenNode, ContentModel.PROP_NAME, "Västra Götalandsregionen - SE2321000131-E000000000001");
    _nodeService.setProperty(kivVastraGotalandsregionenNode, VgrModel.PROP_KIV_HSAIDENTITY, "SE2321000131-E000000000001");
    _nodeService.setProperty(kivVastraGotalandsregionenNode, VgrModel.PROP_KIV_DN, "ou=Västra Götalandsregionen,ou=OrgExtended");
    _nodeService.setProperty(kivVastraGotalandsregionenNode, VgrModel.PROP_KIV_OU, "Västra Götalandsregionen");
    _nodeService.setProperty(kivVastraGotalandsregionenNode, VgrModel.PROP_KIV_MODIFIED, new Date());

    return kivVastraGotalandsregionenNode;
  }

  private NodeRef getKivTypeNode() {
    final NodeRef kivStorageNode = getKivStorageNode();

    List<NodeRef> result = _searchService.selectNodes(kivStorageNode, "app:units", null, _namespaceService, false);

    if (result.size() > 0) {
      return result.get(0);
    }

    final String uri = _nodeService.getPrimaryParent(kivStorageNode).getQName().getNamespaceURI();

    final String validLocalName = QName.createValidLocalName("units");

    final NodeRef kivTypeNode = _nodeService.createNode(kivStorageNode, ContentModel.ASSOC_CONTAINS, QName.createQName(uri, validLocalName), ContentModel.TYPE_FOLDER).getChildRef();

    _nodeService.setProperty(kivTypeNode, ContentModel.PROP_NAME, "Units");

    return kivTypeNode;

  }

  /**
   * Finds or creates the "Kiv" node under the Data Dictionary
   * 
   * @return
   */
  private NodeRef getKivStorageNode() {
    final NodeRef dataDictionaryNode = getDataDictionaryNode();

    List<NodeRef> result = _searchService.selectNodes(dataDictionaryNode, "app:kiv", null, _namespaceService, false);

    if (result.size() > 0) {
      return result.get(0);
    }

    final String uri = _nodeService.getPrimaryParent(dataDictionaryNode).getQName().getNamespaceURI();

    final String validLocalName = QName.createValidLocalName("kiv");

    final NodeRef kivStorageNode = _nodeService.createNode(dataDictionaryNode, ContentModel.ASSOC_CONTAINS, QName.createQName(uri, validLocalName), ContentModel.TYPE_FOLDER).getChildRef();

    _nodeService.setProperty(kivStorageNode, ContentModel.PROP_NAME, "Kiv");

    return kivStorageNode;
  }

  private NodeRef getDataDictionaryNode() {
    NodeRef companyHome = _repositoryHelper.getCompanyHome();

    List<NodeRef> result = _searchService.selectNodes(companyHome, "app:dictionary", null, _namespaceService, false);

    return result.size() == 1 ? result.get(0) : null;
  }

  private NodeRef findNodeRef(final KivUnit unit) {
    if (unit == null) {
      return null;
    }

    final String query = "TYPE:\"kiv:unit\" AND @kiv\\:hsaidentity:\"" + unit.getHsaIdentity() + "\"";

    final ResultSet nodeRefs = search(query);

    NodeRef result = null;

    try {
      if (nodeRefs.length() == 0) {
        result = null;
      } else if (nodeRefs.length() > 1) {
        for (int x = 0; x < nodeRefs.length(); x++) {
          if (!_nodeService.exists(nodeRefs.getNodeRef(x))) {
            Object indexerComponent = ApplicationContextHolder.getApplicationContext().getBean("indexerComponent");

            Method[] methods = indexerComponent.getClass().getMethods();

            for (Method method : methods) {
              if (method.getName().indexOf("updateNode") < 0) {
                continue;
              }
              
              ReflectionUtils.makeAccessible(method);
              
              ReflectionUtils.invokeMethod(method, indexerComponent, nodeRefs.getNodeRef(x));
            }
          } else {
            result = nodeRefs.getNodeRef(x);
          }
        }
      } else {
        result = nodeRefs.getNodeRef(0);
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(nodeRefs);
    }

    return result;
  }

  private ResultSet search(final String query) {
    final SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

    Locale locale = new Locale("sv");
    I18NUtil.setLocale(locale);

    return _searchService.query(searchParameters);
  }

  private String getValidName(final String name) {
    String validName = StringUtils.replace(name, "/", "-");
    validName = StringUtils.replace(validName, "*", " ");
    validName = StringUtils.replace(validName, ":", " ");
    validName = StringUtils.replace(validName, "\\", " ");

    return validName;
  }

  private boolean isChanged(final String liveValue, final Serializable savedValue) {
    final String safeSavedValue = savedValue == null ? "" : savedValue.toString();

    return !StringUtils.equals(liveValue, safeSavedValue);
  }

  @Override
  protected String getJobName() {
    return "Kiv Unit Synchronisation";
  }

  @Override
  protected void executeInternal() {
    synchroniseInternal();
  }

  public class KivSearcher implements Runnable {

    private final KivUnit unit;
    private final List<NodeRef> actualNodes;

    public KivSearcher(final KivUnit unit, final List<NodeRef> actualNodes) {
      this.unit = unit;
      this.actualNodes = actualNodes;
    }

    @Override
    public void run() {
      AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

        public Void doWork() throws Exception {
          RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable {
              final List<KivUnit> subunits = _kivService.findOrganisationalUnits(unit.getDistinguishedName());

              if (subunits.size() > 0) {
                if (LOG.isDebugEnabled()) {
                  LOG.debug("Processing " + subunits.size() + " subunits for " + unit.getDistinguishedName());
                }

                synchroniseUnits(actualNodes, subunits, unit);
              }

              return null;
            }

          };

          _transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

          return null;
        }

      });
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Assert.notNull(_kivService);
    Assert.notNull(_searchService);
    Assert.notNull(_nodeService);
    Assert.notNull(_threadPoolExecutor);
    Assert.notNull(_repositoryHelper);
  }

  public ThreadPoolExecutor getThreadPoolExecutor() {
    return _threadPoolExecutor;
  }

  public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
    this._threadPoolExecutor = threadPoolExecutor;
  }

}
