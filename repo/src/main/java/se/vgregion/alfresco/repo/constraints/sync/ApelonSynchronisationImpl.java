package se.vgregion.alfresco.repo.constraints.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.constraints.ApelonService;
import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;
import se.vgregion.alfresco.repo.model.ApelonNode;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class ApelonSynchronisationImpl extends ClusteredExecuter implements InitializingBean, ApelonSynchronisation {

  protected ApelonService _apelonService;

  protected String _path;

  protected String _namespace;

  protected String _propertyName;

  protected String _propertyValue;

  protected SearchService _searchService;

  protected NodeService _nodeService;

  protected String _apelonNodeType;

  protected String _apelonNodeTitle;

  /**
   * This parameter controls whether the category should be synced as a
   * hierarchy or not. This is controlled by which parameter is supplied. If path
   * is supplied, then it's supposed to be a hierarchy. If no path and a
   * namespace parameter, then it's supposed to be a flat (non-hierarchial)
   * list.
   */
  protected boolean _hierarchy;

  public void setApelonService(ApelonService apelonService) {
    _apelonService = apelonService;
  }

  public void setPath(String path) {
    _path = path;
  }

  public void setNamespace(String namespace) {
    _namespace = namespace;
  }

  public void setPropertyName(String propertyName) {
    _propertyName = propertyName;
  }

  public void setPropertyValue(String propertyValue) {
    _propertyValue = propertyValue;
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setApelonNodeType(String apelonNodeType) {
    _apelonNodeType = apelonNodeType;
  }

  public void setApelonNodeTitle(String apelonNodeTitle) {
    _apelonNodeTitle = apelonNodeTitle;
  }

  @Override
  protected String getJobName() {
    return "Apelon Synchronisation";
  }

  @Override
  protected void executeInternal() {
    synchronise();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * se.vgregion.alfresco.repo.constraints.sync.ApelonSynchronisation#synchronise
   * ()
   */
  @Override
  public void synchronise() {
    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        doSynchronise();

        return null;
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  protected void doSynchronise() {
    List<ApelonNode> nodeList;

    if (StringUtils.isNotBlank(_path)) {
      nodeList = _apelonService.getVocabulary(_path);
    } else {
      nodeList = _apelonService.findNodes(_namespace, _propertyName, _propertyValue);
    }

    synchroniseNodeList(nodeList);
  }

  protected void synchroniseNodeList(List<ApelonNode> nodeList) {
    synchroniseNodeList(nodeList, null);
  }

  protected void synchroniseNodeList(List<ApelonNode> nodeList, Parent parent) {
    for (ApelonNode node : nodeList) {
      node.setParent(parent != null ? parent.node : null);

      synchroniseNode(node, parent);

      if (node.isHasChildren() && _hierarchy) {
        List<ApelonNode> children = _apelonService.getVocabulary(_path + "/" + node.getPath());

        NodeRef nodeRef = findNodeRef(node);

        synchroniseNodeList(children, new Parent(nodeRef, node));
      }
    }
  }

  private ResultSet search(String query) {
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

    return _searchService.query(searchParameters);
  }

  protected NodeRef findNodeRef(ApelonNode node) {
    if (node == null) {
      return null;
    }

    String name = node.getName();
    String internalId = node.getInternalId();

    String validName = getValidName(name) + " - " + internalId;

    String query = "TYPE:\"apelon:" + _apelonNodeType + "\" AND @cm\\:name:\"" + validName + "\"";

    ResultSet nodeRefs = search(query);

    NodeRef result;

    try {
      if (nodeRefs.length() == 0) {
        result = null;
      } else if (nodeRefs.length() > 1) {
        throw new RuntimeException("Found more than 1 node in Alfresco for apelon:name '" + name
            + "' and apelon:internalid '" + internalId + "'");
      } else {
        result = nodeRefs.getNodeRef(0);
      }
    } finally {
      ServiceUtils.closeQuietly(nodeRefs);
    }

    return result;
  }

  protected void synchroniseNode(final ApelonNode node, final Parent parent) {
    RetryingTransactionHelper helper = _transactionService.getRetryingTransactionHelper();

    RetryingTransactionHelper.RetryingTransactionCallback<Void> callback = new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        doSynchroniseNode(node, parent);

        return null;
      }

    };

    helper.doInTransaction(callback, false, true);
  }

  protected void doSynchroniseNode(final ApelonNode node, final Parent parent) {
    refreshLock();

    NodeRef nodeRef = findNodeRef(node);

    if (nodeRef == null) {
      createNode(node, parent);

      return;
    }

    boolean updated = updateNode(node, nodeRef);

    if (updated) {
      return;
    }
  }

  /**
   * Updates (if changed) the node.
   *
   * @param apelonNode
   * @param apelonNodeRef
   */
  private boolean updateNode(ApelonNode apelonNode, NodeRef apelonNodeRef) {
    boolean updated = false;

    Map<QName, Serializable> updatedValues = new HashMap<QName, Serializable>();

    String liveInternalId = apelonNode.getInternalId();
    Serializable savedInternalId = _nodeService.getProperty(apelonNodeRef, VgrModel.PROP_APELON_INTERNALID);

    if (isChanged(liveInternalId, savedInternalId)) {
      updatedValues.put(VgrModel.PROP_APELON_INTERNALID, liveInternalId);
    }

    String liveNamespaceId = apelonNode.getNamespaceId();
    Serializable savedNamespaceId = _nodeService.getProperty(apelonNodeRef, VgrModel.PROP_APELON_NAMESPACEID);

    if (isChanged(liveNamespaceId, savedNamespaceId)) {
      updatedValues.put(VgrModel.PROP_APELON_NAMESPACEID, liveNamespaceId);
    }

    String liveSourceId = apelonNode.getSourceId();
    Serializable savedSourceId = _nodeService.getProperty(apelonNodeRef, VgrModel.PROP_APELON_SOURCEID);

    if (isChanged(liveSourceId, savedSourceId)) {
      updatedValues.put(VgrModel.PROP_APELON_SOURCEID, liveSourceId);
    }

    if (updatedValues.size() > 0) {
      _nodeService.addProperties(apelonNodeRef, updatedValues);

      updated = true;
    }

    for (Entry<String, List<String>> entry : apelonNode.getProperties().entrySet()) {
      String liveKey = entry.getKey();
      List<String> liveValues = entry.getValue();

      NodeRef savedPropertyNode = getSavedPropertyNode(apelonNodeRef, liveKey);
      List<String> savedValues = getSavedPropertyValues(savedPropertyNode);

      if (isChanged(liveValues, savedValues)) {
        setPropertyValues(apelonNodeRef, savedPropertyNode, liveKey, liveValues);
        updated = true;
      }
    }

    return updated;
  }

  private void setPropertyValues(NodeRef apelonNodeRef, NodeRef propertyNode, String key,
      List<String> values) {
    if (propertyNode == null) {
      addProperty(apelonNodeRef, key, values);
      return;
    }

    _nodeService.setProperty(propertyNode, VgrModel.PROP_APELON_KEY, key);
    _nodeService.setProperty(propertyNode, VgrModel.PROP_APELON_VALUE, (Serializable) values);

    QName type = _nodeService.getType(apelonNodeRef);

    if (type.isMatch(VgrModel.TYPE_APELON_RECORDTYPE) && key.equals("Typ")) {
      _nodeService.setProperty(apelonNodeRef, VgrModel.PROP_APELON_RTTYPE, (Serializable) values);
    }

    if (type.isMatch(VgrModel.TYPE_APELON_RECORDTYPE) && key.equals("Dokumenttyp")) {
      _nodeService.setProperty(apelonNodeRef, VgrModel.PROP_APELON_RTDOCUMENTTYPEID, (Serializable) values);
    }
  }

  private NodeRef getSavedPropertyNode(NodeRef nodeRef, String liveKey) {
    Set<QName> childNodeTypeQNames = new HashSet<QName>();
    childNodeTypeQNames.add(VgrModel.TYPE_APELON_PROPERTY);

    List<ChildAssociationRef> result = _nodeService.getChildAssocs(nodeRef, childNodeTypeQNames);

    for (ChildAssociationRef propertyNode : result) {
      Serializable key = _nodeService.getProperty(propertyNode.getChildRef(), VgrModel.PROP_APELON_KEY);

      if (StringUtils.equals((String) key, liveKey)) {
        return propertyNode.getChildRef();
      }
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  private List<String> getSavedPropertyValues(NodeRef nodeRef) {
    List<String> result = new ArrayList<String>();

    if (nodeRef == null) {
      return result;
    }

    Serializable value = _nodeService.getProperty(nodeRef, VgrModel.PROP_APELON_VALUE);

    if (value instanceof List) {
      result = (List<String>) value;
    } else {
      String stringValue = value != null ? value.toString() : "";

      result.add(stringValue);
    }

    return result;
  }

  private boolean isChanged(String liveValue, Serializable savedValue) {
    String safeSavedValue = savedValue == null ? "" : savedValue.toString();

    return !StringUtils.equals(liveValue, safeSavedValue);
  }

  private boolean isChanged(List<String> liveValues, List<String> savedValues) {
    Collections.sort(liveValues);
    Collections.sort(savedValues);

    String liveValue = StringUtils.join(liveValues.iterator(), "");
    String savedValue = StringUtils.join(savedValues.iterator(), "");

    return !liveValue.equals(savedValue);
  }

  private void createNode(ApelonNode apelonNode, Parent parent) {
    NodeRef parentNodeRef = parent != null ? parent.nodeRef : getApelonTypeNode();

    QName assocQName = QName.createQName(VgrModel.APELON_URI, _apelonNodeType);

    Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(VgrModel.PROP_APELON_NAME, apelonNode.getName());

    if (StringUtils.isNotBlank(apelonNode.getInternalId())) {
      properties.put(VgrModel.PROP_APELON_INTERNALID, apelonNode.getInternalId());
    }

    if (StringUtils.isNotBlank(apelonNode.getNamespaceId())) {
      properties.put(VgrModel.PROP_APELON_NAMESPACEID, apelonNode.getNamespaceId());
    }

    if (StringUtils.isNotBlank(apelonNode.getSourceId())) {
      properties.put(VgrModel.PROP_APELON_SOURCEID, apelonNode.getSourceId());
    }

    String uri = _nodeService.getPrimaryParent(parentNodeRef).getQName().getNamespaceURI();
    String validLocalName = QName.createValidLocalName(apelonNode.getName());

    String name = getValidName(apelonNode.getName()) + " - " + apelonNode.getInternalId();
    properties.put(ContentModel.PROP_NAME, name);

    NodeRef apelonNodeRef = _nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
        QName.createQName(uri, validLocalName), assocQName).getChildRef();
    _nodeService.setProperties(apelonNodeRef, properties);

    addProperties(apelonNode, apelonNodeRef);
  }

  private String getValidName(String name) {
    String validName = StringUtils.replace(name, "/", "-");
    validName = StringUtils.replace(validName, "*", "");
    validName = StringUtils.replace(validName, ":", "");

    return validName;
  }

  private void addProperties(ApelonNode node, NodeRef nodeRef) {
    for (Entry<String, List<String>> entry : node.getProperties().entrySet()) {
      String key = entry.getKey();
      List<String> values = entry.getValue();

      addProperty(nodeRef, key, values);
    }
  }

  private void addProperty(NodeRef apelonNodeRef, String key, List<String> values) {
    if (ArrayUtils.isEmpty(values.toArray())) {
      return;
    }

    Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(VgrModel.PROP_APELON_KEY, key);
    properties.put(VgrModel.PROP_APELON_VALUE, (Serializable) values);
    properties.put(ContentModel.PROP_NAME, getValidName(key));

    String uri = _nodeService.getPrimaryParent(apelonNodeRef).getQName().getNamespaceURI();
    String validLocalName = QName.createValidLocalName(apelonNodeRef.getId() + "-" + key);

    _nodeService.createNode(apelonNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(uri, validLocalName),
        VgrModel.TYPE_APELON_PROPERTY, properties).getChildRef();

    QName type = _nodeService.getType(apelonNodeRef);

    if (type.isMatch(VgrModel.TYPE_APELON_RECORDTYPE) && key.equals("Typ")) {
      _nodeService.setProperty(apelonNodeRef, VgrModel.PROP_APELON_RTTYPE, values.get(0));
    }

    if (type.isMatch(VgrModel.TYPE_APELON_RECORDTYPE) && key.equals("Dokumenttyp")) {
      _nodeService.setProperty(apelonNodeRef, VgrModel.PROP_APELON_RTDOCUMENTTYPEID, (Serializable) values);
    }
  }

  private NodeRef getApelonTypeNode() {
    String query = "PATH:\"/app:company_home/app:dictionary/app:apelon/app:" + _apelonNodeType + "\"";

    ResultSet result = search(query);

    try {
      if (result.length() > 0) {
        return result.getNodeRef(0);
      }
    } finally {
      ServiceUtils.closeQuietly(result);
    }

    NodeRef apelonStorageNode = getApelonStorageNode();

    String uri = _nodeService.getPrimaryParent(apelonStorageNode).getQName().getNamespaceURI();
    String validLocalName = QName.createValidLocalName(_apelonNodeType);

    NodeRef apelonTypeNode = _nodeService.createNode(apelonStorageNode, ContentModel.ASSOC_CONTAINS,
        QName.createQName(uri, validLocalName), ContentModel.TYPE_FOLDER).getChildRef();

    _nodeService.setProperty(apelonTypeNode, ContentModel.PROP_NAME, _apelonNodeTitle);

    return apelonTypeNode;
  }

  /**
   * Finds or creates the "Apelon" node under the Data Dictonary
   *
   * @return
   */
  private NodeRef getApelonStorageNode() {
    String query = "PATH:\"/app:company_home/app:dictionary/app:apelon\"";

    ResultSet result = search(query);

    try {
      if (result.length() > 0) {
        return result.getNodeRef(0);
      }
    } finally {
      ServiceUtils.closeQuietly(result);
    }

    NodeRef dataDictionaryNode = getDataDictionaryNode();

    String uri = _nodeService.getPrimaryParent(dataDictionaryNode).getQName().getNamespaceURI();
    String validLocalName = QName.createValidLocalName("apelon");

    NodeRef apelonStorageNode = _nodeService.createNode(dataDictionaryNode, ContentModel.ASSOC_CONTAINS,
        QName.createQName(uri, validLocalName), ContentModel.TYPE_FOLDER).getChildRef();

    _nodeService.setProperty(apelonStorageNode, ContentModel.PROP_NAME, "Apelon");

    return apelonStorageNode;
  }

  private NodeRef getDataDictionaryNode() {
    String query = "PATH:\"/app:company_home/app:dictionary\"";

    ResultSet result = search(query);

    try {
      return result.getNodeRef(0);
    } finally {
      ServiceUtils.closeQuietly(result);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (StringUtils.isBlank(_path) && StringUtils.isBlank(_namespace)) {
      throw new RuntimeException("Either PATH or NAMESPACE must be set in order to synchronise.");
    }

    Assert.notNull(_apelonService);
    Assert.notNull(_searchService);
    Assert.notNull(_nodeService);
    Assert.notNull(_transactionService, "The transaction service must be set");
    Assert.hasText(_apelonNodeTitle);
    Assert.hasText(_apelonNodeType);

    // if the path parameter is supplied, then the category should be synched as
    // a hierarchy.
    _hierarchy = StringUtils.isNotBlank(_path);
  }

  public class Parent {

    public NodeRef nodeRef;

    public ApelonNode node;

    public String mn;

    public Parent(NodeRef nodeRef, ApelonNode node) {
      this.nodeRef = nodeRef;
      this.node = node;
      this.mn = node.getProperties().containsKey("MN") ? node.getProperties().get("MN").iterator().next() : null;
    }
  }

}
