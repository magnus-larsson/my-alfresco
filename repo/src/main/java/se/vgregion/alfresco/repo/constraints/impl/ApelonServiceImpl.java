package se.vgregion.alfresco.repo.constraints.impl;

import keywordservices.wsdl.metaservice_vgr_se.v2.GetKeywordsRequest;
import keywordservices.wsdl.metaservice_vgr_se.v2.KeywordService;
import org.alfresco.repo.cache.EhCacheAdapter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import se.vgr.metaservice.schema.document.v1.TextDocumentType;
import se.vgr.metaservice.schema.node.v2.NodePropertyType;
import se.vgr.metaservice.schema.node.v2.NodeType;
import se.vgr.metaservice.schema.request.v1.FilterByPropertiesListType;
import se.vgr.metaservice.schema.request.v1.IdentificationType;
import se.vgr.metaservice.schema.request.v1.OptionsType;
import se.vgr.metaservice.schema.request.v1.OptionsType.FilterByProperties;
import se.vgr.metaservice.schema.request.v1.OptionsType.FilterByProperties.Entry;
import se.vgr.metaservice.schema.response.v1.NodeListResponseObjectType;
import se.vgregion.alfresco.repo.constraints.ApelonService;
import se.vgregion.alfresco.repo.model.ApelonNode;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;
import vocabularyservices.wsdl.metaservice_vgr_se.v2.FindNodesByNameRequest;
import vocabularyservices.wsdl.metaservice_vgr_se.v2.FindNodesRequest;
import vocabularyservices.wsdl.metaservice_vgr_se.v2.GetVocabularyRequest;
import vocabularyservices.wsdl.metaservice_vgr_se.v2.VocabularyService;

import java.io.Serializable;
import java.util.*;

public class ApelonServiceImpl implements ApelonService {

  private static String DOCUMENT_TYPE_LIST_CACHEKEY = "documentTypeList";

  private static String RECORD_TYPE_LIST_CACHEKEY = "recordTypeList";

  private VocabularyService _vocabularyService;

  private SearchService _searchService;

  private NodeService _nodeService;

  private EhCacheAdapter<String, List<NodeRef>> _cache;

  private KeywordService _keywordService;

  public void setCache(EhCacheAdapter<String, List<NodeRef>> cache) {
    _cache = cache;
  }

  public void setVocabularyService(VocabularyService vocabularyService) {
    _vocabularyService = vocabularyService;
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setKeywordService(KeywordService keywordService) {
    _keywordService = keywordService;
  }

  @Override
  public List<ApelonNode> getVocabulary(String path) {
    return getVocabulary(path, true);
  }

  @Override
  public List<ApelonNode> getVocabulary(String path, boolean sort) {
    return getVocabulary(path, sort, 100000);
  }

  @Override
  public List<ApelonNode> getVocabulary(String path, boolean sort, int returnSize) {
    long start = System.currentTimeMillis();

    GetVocabularyRequest request = new GetVocabularyRequest();
    String requestId = UUID.randomUUID().toString();

    OptionsType options = new OptionsType();
    options.setWordsToReturn(returnSize);
    options.setInputWords(0);
    options.setMatchSynonyms(false);
    options.setSynomize(false);

    request.setRequestId(requestId);
    request.setPath(path);
    request.setOptions(options);

    NodeListResponseObjectType response = _vocabularyService.getVocabulary(request);

    List<NodeType> nodeList = response.getNodeList().getNode();

    List<ApelonNode> result = createApelonNodeList(nodeList, sort);

    long end = System.currentTimeMillis();

    System.out.println("ApelonService.getVocabulary(): " + (end - start) + " ms");

    return result;
  }

  private List<ApelonNode> createApelonNodeList(List<NodeType> nodeList, boolean sort) {
    List<ApelonNode> result = new ArrayList<ApelonNode>();

    for (NodeType node : nodeList) {
      ApelonNode apelonNode = new ApelonNode();

      List<NodePropertyType> nodePropertyList = node.getNodeProperties().getNodeProperty();

      for (NodePropertyType nodeProperty : nodePropertyList) {
        String name = nodeProperty.getName();
        String value = nodeProperty.getValue();

        apelonNode.addProperty(name, value);
      }

      apelonNode.setName(node.getName());
      apelonNode.setInternalId(node.getInternalId());
      apelonNode.setNamespaceId(node.getNamespaceId());
      apelonNode.setSourceId(node.getSourceId());
      apelonNode.setHasChildren(node.isHasChildren());

      result.add(apelonNode);
    }

    if (sort) {
      // sort the list alphabetically
      Comparator<ApelonNode> comparator = new ListComparator();
      Collections.sort(result, comparator);
    }
    return result;
  }

  @Override
  public List<ApelonNode> findNodes(String namespace, String propertyName, String propertyValue) {
    return findNodes(namespace, propertyName, propertyValue, true);
  }

  @Override
  public List<ApelonNode> findNodes(String namespace, String propertyName, String propertyValue, boolean sort) {
    return findNodes(namespace, propertyName, propertyValue, sort, 100000);
  }

  @Override
  public List<ApelonNode> findNodes(String namespace, String propertyName, String propertyValue, boolean sort,
                                    int returnSize) {
    IdentificationType identification = new IdentificationType();
    identification.setProfileId(ApelonService.PROFILE_ID);
    identification.setUserId(AuthenticationUtil.getFullyAuthenticatedUser());

    OptionsType options = new OptionsType();
    options.setWordsToReturn(returnSize);

    NodeListResponseObjectType response;

    if (StringUtils.isBlank(propertyName)) {
      response = internalFindNodes(namespace, options, identification);
    } else {
      response = internalFindNodes(namespace, options, identification, propertyName, propertyValue);
    }

    String errorMessage = response.getErrorMessage();

    if (StringUtils.isNotBlank(errorMessage)) {
      throw new RuntimeException("Error from Apelon: " + errorMessage);
    }

    List<NodeType> nodeList = response.getNodeList().getNode();

    return createApelonNodeList(nodeList, sort);
  }

  private NodeListResponseObjectType internalFindNodes(String namespace, OptionsType options, IdentificationType identification,
                                                       String propertyName, String propertyValue) {
    if (StringUtils.isNotBlank(propertyName)) {
      FilterByProperties filter = new FilterByProperties();
      Entry filterEntry = new Entry();
      FilterByPropertiesListType filterPropertyValue = new FilterByPropertiesListType();

      filterPropertyValue.getFilter().add(propertyValue);

      filterEntry.setKey(propertyName);
      filterEntry.setValue(filterPropertyValue);

      filter.getEntry().add(filterEntry);

      options.setFilterByProperties(filter);
    }

    FindNodesRequest request = new FindNodesRequest();

    request.setIdentification(identification);
    request.setNameSpaceName(namespace);
    request.setOptions(options);
    request.setRequestId(UUID.randomUUID().toString());

    return _vocabularyService.findNodes(request);
  }

  private NodeListResponseObjectType internalFindNodes(String namespace, OptionsType options, IdentificationType identification) {
    FindNodesByNameRequest request = new FindNodesByNameRequest();

    request.setIdentification(identification);
    request.setNameSpaceName(namespace);
    request.setOptions(options);
    request.setRequestId(UUID.randomUUID().toString());
    request.setName("*");

    return _vocabularyService.findNodesByName(request);
  }

  @Override
  public List<NodeRef> getRecordTypeList(String documentTypeId) {
    return getRecordTypeList(documentTypeId, true);
  }

  @Override
  public List<NodeRef> getRecordTypeList(String documentTypeId, boolean sort) {
    if (StringUtils.isBlank(documentTypeId)) {
      return new ArrayList<NodeRef>();
    }

    List<NodeRef> recordTypeList = getRecordTypeList();

    List<NodeRef> result = new ArrayList<NodeRef>();

    for (NodeRef nodeRef : recordTypeList) {
      Serializable cachedDocumentTypeId = _nodeService.getProperty(nodeRef, VgrModel.PROP_APELON_RTDOCUMENTTYPEID);

      if (cachedDocumentTypeId == null) {
        continue;
      }

      @SuppressWarnings("unchecked")
      List<String> cachedDocumentTypeList = (List<String>) cachedDocumentTypeId;

      if (!cachedDocumentTypeList.contains(documentTypeId)) {
        continue;
      }

      result.add(nodeRef);
    }

    if (sort) {
      // sort the list alphabetically
      Comparator<NodeRef> comparator = new NodeListComparator();
      Collections.sort(result, comparator);
    }

    return result;
  }

  public List<NodeRef> getRecordTypeList() {
    List<NodeRef> result = _cache.get(ApelonServiceImpl.RECORD_TYPE_LIST_CACHEKEY);

    if (result != null) {
      return result;
    }

    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    String query = "TYPE:\"apelon:recordType\" AND @apelon\\:rtType:\"Handlingstyp\"";
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(100000);

    ResultSet resultSet = _searchService.query(searchParameters);

    result = new ArrayList<NodeRef>();

    try {
      for (ResultSetRow row : resultSet) {
        NodeRef nodeRef = row.getChildAssocRef().getChildRef();
        result.add(nodeRef);
      }
    } finally {
      ServiceUtils.closeQuietly(resultSet);
    }

    _cache.put(ApelonServiceImpl.RECORD_TYPE_LIST_CACHEKEY, result);

    return result;
  }

  @Override
  public List<NodeRef> getDocumentTypeList() {
    return getDocumentTypeList(true);
  }

  @Override
  public List<NodeRef> getDocumentTypeList(boolean sort) {
    List<NodeRef> result = _cache.get(ApelonServiceImpl.DOCUMENT_TYPE_LIST_CACHEKEY);

    if (result != null) {
      return result;
    }

    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery("TYPE:\"apelon:documentType\"");
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(100);

    ResultSet resultSet = _searchService.query(searchParameters);

    result = new ArrayList<NodeRef>();

    try {
      for (ResultSetRow row : resultSet) {
        NodeRef nodeRef = row.getNodeRef();

        result.add(nodeRef);
      }
    } finally {
      ServiceUtils.closeQuietly(resultSet);
    }

    if (sort) {
      // sort the list alphabetically
      Comparator<NodeRef> comparator = new NodeListComparator();
      Collections.sort(result, comparator);
    }

    _cache.put(ApelonServiceImpl.DOCUMENT_TYPE_LIST_CACHEKEY, result);

    return result;
  }

  @Override
  public List<NodeRef> getDocumentStatusList() {
    return getDocumentStatusList(true);
  }

  @Override
  public List<NodeRef> getDocumentStatusList(boolean sort) {
    return getNodes("apelon:documentStatus", sort);
  }

  @Override
  public List<NodeRef> getLanguageList() {
    return getLanguageList(true);
  }

  @Override
  public List<NodeRef> getLanguageList(boolean sort) {
    return getNodes("apelon:language", sort);
  }

  @Override
  public List<NodeRef> getHsacodeList() {
    return getHsacodeList(true);
  }

  @Override
  public List<NodeRef> getHsacodeList(boolean sort) {
    return getNodes("apelon:hsacode", sort);
  }

  @Override
  public List<NodeRef> getNodes(String apelonType) {
    return getNodes(apelonType, true);
  }

  @Override
  public List<NodeRef> getNodes(String apelonType, boolean sort) {
    String cacheKey = apelonType + "_" + (sort ? "SORT" : "NOSORT");

    List<NodeRef> result = _cache.get(cacheKey);

    if (result != null) {
      return result;
    }

    String query = "TYPE:\"" + apelonType + "\"";

    final SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(1000000);

    ResultSet resultSet = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<ResultSet>() {

      @Override
      public ResultSet doWork() throws Exception {
        return _searchService.query(searchParameters);
      }

    }, AuthenticationUtil.getSystemUserName());

    result = new ArrayList<NodeRef>();

    try {
      for (ResultSetRow row : resultSet) {
        NodeRef nodeRef = row.getNodeRef();

        result.add(nodeRef);
      }
    } finally {
      ServiceUtils.closeQuietly(resultSet);
    }

    if (sort) {
      // sort the list alphabetically
      Comparator<NodeRef> comparator = new NodeListComparator();
      Collections.sort(result, comparator);
    }

    _cache.put(cacheKey, result);

    return result;
  }

  static class ListComparator implements Comparator<ApelonNode>, Serializable {

    private static long serialVersionUID = 6945775292248924071L;

    @Override
    public int compare(ApelonNode node1, ApelonNode node2) {
      return node1.getName().compareToIgnoreCase(node2.getName());
    }

  }

  private class NodeListComparator implements Comparator<NodeRef> {

    @Override
    public int compare(NodeRef node1, NodeRef node2) {
      Serializable temp1 = getName(node1);
      Serializable temp2 = getName(node2);

      String name1 = temp1 != null ? temp1.toString() : "";
      String name2 = temp2 != null ? temp2.toString() : "";

      return name1.compareToIgnoreCase(name2);
    }

    private Serializable getName(final NodeRef nodeRef) {
      return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Serializable>() {

        @Override
        public Serializable doWork() throws Exception {
          return _nodeService.getProperty(nodeRef, VgrModel.PROP_APELON_NAME);
        }

      }, AuthenticationUtil.getSystemUserName());
    }

  }

  @Override
  public ApelonNode getDocumentTypeApelonNode(String documentType) {
    List<NodeRef> documentTypes = getDocumentTypeList();

    if (documentTypes.size() == 0) {
      return null;
    }

    NodeRef documentTypeNode = documentTypes.get(0);

    ApelonNode documentTypeApelonNode = new ApelonNode();

    documentTypeApelonNode.setInternalId((String) _nodeService.getProperty(documentTypeNode, VgrModel.PROP_APELON_INTERNALID));
    documentTypeApelonNode.setName((String) _nodeService.getProperty(documentTypeNode, VgrModel.PROP_APELON_NAME));
    documentTypeApelonNode.setNamespaceId((String) _nodeService.getProperty(documentTypeNode, VgrModel.PROP_APELON_NAMESPACEID));
    documentTypeApelonNode.setSourceId((String) _nodeService.getProperty(documentTypeNode, VgrModel.PROP_APELON_SOURCEID));

    return documentTypeApelonNode;
  }

  @Override
  public ApelonNode getRecordTypeApelonNode(String documentType, String recordType) {
    ApelonNode documentTypeApelonNode = getDocumentTypeApelonNode(documentType);

    if (documentTypeApelonNode == null) {
      return null;
    }

    String query = "TYPE:\"apelon:recordType\" AND @apelon\\:rtDocumentTypeId:\"" + documentTypeApelonNode.getInternalId()
            + "\" AND @apelon\\:name:\"" + recordType + "\"";

    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(100000);

    ResultSet resultSet = _searchService.query(searchParameters);

    try {
      if (resultSet.length() != 1) {
        return null;
      }

      ResultSetRow row = resultSet.getRow(0);

      ApelonNode recordTypeApelonNode = new ApelonNode();

      recordTypeApelonNode.setInternalId((String) row.getValue(VgrModel.PROP_APELON_INTERNALID));
      recordTypeApelonNode.setName((String) row.getValue(VgrModel.PROP_APELON_NAME));
      recordTypeApelonNode.setNamespaceId((String) row.getValue(VgrModel.PROP_APELON_NAMESPACEID));
      recordTypeApelonNode.setSourceId((String) row.getValue(VgrModel.PROP_APELON_SOURCEID));

      return recordTypeApelonNode;
    } finally {
      ServiceUtils.closeQuietly(resultSet);
    }
  }

  @Override
  public NodeRef getRecordTypeFromPath(String recordTypePath) {
    List<NodeRef> recordTypes = getRecordTypeList();

    for (NodeRef recordType : recordTypes) {
      List<ChildAssociationRef> properties = _nodeService.getChildAssocs(recordType);

      for (ChildAssociationRef property : properties) {
        NodeRef propertyNode = property.getChildRef();

        String key = (String) _nodeService.getProperty(propertyNode, VgrModel.PROP_APELON_KEY);

        if (!key.equalsIgnoreCase("Sökväg")) {
          continue;
        }

        @SuppressWarnings("unchecked")
        List<String> values = (List<String>) _nodeService.getProperty(propertyNode, VgrModel.PROP_APELON_VALUE);

        if (!values.get(0).equalsIgnoreCase(recordTypePath)) {
          continue;
        }

        return recordType;
      }
    }

    return null;
  }

  @Override
  public List<ApelonNode> getKeywords(String text) {
    GetKeywordsRequest request = new GetKeywordsRequest();

    TextDocumentType document = new TextDocumentType();
    document.setTextContent(text);

    IdentificationType identification = new IdentificationType();
    identification.setProfileId(ApelonService.PROFILE_ID);
    identification.setUserId(AuthenticationUtil.getFullyAuthenticatedUser());

    OptionsType options = new OptionsType();
    options.setWordsToReturn(50);
    options.setInputWords(100);
    options.setMatchSynonyms(true);
    options.setSynomize(true);

    request.setDocument(document);
    request.setRequestId(UUID.randomUUID().toString());
    request.setIdentification(identification);
    request.setOptions(options);

    NodeListResponseObjectType response = _keywordService.getKeywords(request);

    List<NodeType> nodes = response.getNodeList().getNode();

    return createApelonNodeList(nodes, false);
  }

}
