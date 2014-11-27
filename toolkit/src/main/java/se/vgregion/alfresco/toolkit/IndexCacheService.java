package se.vgregion.alfresco.toolkit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.publish.PublishingService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

@Component
public class IndexCacheService {

  private static final String CACHE_KEY_SOLR_IFEED = "solr_ifeed";

  private static final String CACHE_KEY_SOLR_CORE0 = "solr_core0";

  private static final String CACHE_KEY_ALFRESCO = "alfresco";

  private static final String CACHE_KEY_ALFRESCO_ORPHANS = "alfresco_orphans";
  private static final String CACHE_KEY_SOLR_ORPHANS = "solr_orphans";

  @Autowired
  private NodeService _nodeService;

  @Autowired
  private Repository _repository;

  @Autowired
  private ContentService _contentService;

  @Autowired
  private SearchService _searchService;

  @Autowired
  private PublishingService _publishingService;

  @Autowired
  private NamespacePrefixResolver _namespacePrefixResolver;

  /**
   * Loads the cached nodes and calculates the orphan nodes and caches them.
   */
  public void cacheOrphans() {
    try {
      JsonArray alfrescoNodes = loadCachedNodes(CACHE_KEY_ALFRESCO);
      JsonArray solrNodes = loadCachedNodes(CACHE_KEY_SOLR_CORE0);
      JsonArray alfrescoOrphans = loadCachedNodes(CACHE_KEY_ALFRESCO);
      JsonArray solrOrphans = loadCachedNodes(CACHE_KEY_SOLR_CORE0);

      for (int x = 0; x < alfrescoNodes.size(); x++) {
        JsonElement alfrescoJsonElement = alfrescoNodes.get(x);
        JsonObject alfrescoNode = alfrescoJsonElement.getAsJsonObject();
        String alfrescoNodeRef = alfrescoNode.get("nodeRef").getAsString();

        if (!alfrescoNode.get("published").getAsBoolean()) {
          alfrescoOrphans.remove(alfrescoJsonElement);

          continue;
        }

        for (int y = 0; y < solrNodes.size(); y++) {
          JsonElement solrJsonElement = solrNodes.get(y);

          String solrNodeRef = solrJsonElement.getAsString();

          if (solrNodeRef.equalsIgnoreCase(alfrescoNodeRef)) {
            solrOrphans.remove(solrJsonElement);

            alfrescoOrphans.remove(alfrescoJsonElement);

            break;
          }
        }
      }

      cacheAlfrescoOrphans(alfrescoOrphans);

      cacheSolrOrphans(solrOrphans);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private void cacheSolrOrphans(JsonArray solrTempOrphans) {
    JsonArray solrOrphans = new JsonArray();

    for (int x = 0; x < solrTempOrphans.size(); x++) {
      JsonElement solrJsonElement = solrTempOrphans.get(x);

      String solrNodeRef = solrJsonElement.getAsString();

      solrOrphans.add(new JsonPrimitive(solrNodeRef));
    }

    NodeRef solrOrphanCache = getCacheNode(CACHE_KEY_SOLR_ORPHANS);

    ContentWriter contentWriter = _contentService.getWriter(solrOrphanCache, ContentModel.PROP_CONTENT, true);

    contentWriter.putContent(solrOrphans.toString());
  }

  private void cacheAlfrescoOrphans(JsonArray alfrescoTempOrphans) {
    JsonArray alfrescoOrphans = new JsonArray();

    for (int x = 0; x < alfrescoTempOrphans.size(); x++) {
      JsonElement alfrescoJsonElement = alfrescoTempOrphans.get(x);

      JsonObject alfrescoNode = alfrescoJsonElement.getAsJsonObject();

      String alfrescoNodeRef = alfrescoNode.get("nodeRef").getAsString();

      alfrescoOrphans.add(new JsonPrimitive(alfrescoNodeRef));
    }

    NodeRef alfrescoOrphanCache = getCacheNode(CACHE_KEY_ALFRESCO_ORPHANS);

    ContentWriter contentWriter = _contentService.getWriter(alfrescoOrphanCache, ContentModel.PROP_CONTENT, true);

    contentWriter.putContent(alfrescoOrphans.toString());
  }

  private JsonArray loadCachedNodes(String cacheKey) {
    NodeRef cache = getCacheNode(cacheKey);

    ContentReader contentReader = _contentService.getReader(cache, ContentModel.PROP_CONTENT);

    if (contentReader.getSize() == 0) {
      return null;
    }

    try {
      JsonParser parser = new JsonParser();

      return parser.parse(contentReader.getContentString()).getAsJsonArray();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Fetches and caches all published nodes from Solr (core0 & ifeed).
   * Fetches and caches all published and unpublished nodes from Alfresco.
   * 
   * @return
   */
  public int[] cachePublishedNodes() {
    int count1 = cacheSolrCore0Nodes();
    int count2 = cacheSolrIfeedNodes();
    int count3 = cacheAlfrescoNodes();

    return new int[] { count1, count2, count3 };
  }

  private int cacheAlfrescoNodes() {
    try {
      NodeRef cache = getCacheNode(CACHE_KEY_ALFRESCO);

      ContentWriter contentWriter = _contentService.getWriter(cache, ContentModel.PROP_CONTENT, true);

      JsonArray nodes = getAllAlfrescoNodes();

      contentWriter.putContent(nodes.toString());

      System.out.println("Cached " + nodes.size() + " alfresco nodes.");

      return nodes.size();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private int cacheSolrCore0Nodes() {
    try {
      NodeRef cache = getCacheNode(CACHE_KEY_SOLR_CORE0);

      ContentWriter contentWriter = _contentService.getWriter(cache, ContentModel.PROP_CONTENT, true);

      JsonArray nodes = getAllSolrCore0Nodes();

      contentWriter.putContent(nodes.toString());

      System.out.println("Cached " + nodes.size() + " solr nodes for core 'core0'.");

      return nodes.size();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private int cacheSolrIfeedNodes() {
    try {
      NodeRef cache = getCacheNode(CACHE_KEY_SOLR_IFEED);

      ContentWriter contentWriter = _contentService.getWriter(cache, ContentModel.PROP_CONTENT, true);

      JsonArray nodes = getAllSolrIfeedNodes();

      contentWriter.putContent(nodes.toString());

      System.out.println("Cached " + nodes.size() + " solr nodes for core 'ifeed'.");

      return nodes.size();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private JsonArray getAllSolrCore0Nodes() {
    try {
      JsonArray nodes = new JsonArray();

      JsonObject initial = getSolrCore0Nodes(1, 0);

      double total = initial.get("response").getAsJsonObject().get("numFound").getAsDouble();

      double pages = Math.ceil(total / 1000d);

      for (int page = 0; page < pages; page++) {
        JsonObject result = getSolrCore0Nodes(1000, page * 1000);

        JsonArray array = result.get("response").getAsJsonObject().get("docs").getAsJsonArray();

        for (int x = 0; x < array.size(); x++) {
          JsonElement nodeRef = array.get(x).getAsJsonObject().get("dc.identifier.documentid").getAsJsonArray().get(0);

          nodes.add(nodeRef);
        }
      }

      return nodes;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private JsonArray getAllSolrIfeedNodes() {
    try {
      JsonArray nodes = new JsonArray();

      JsonObject initial = getSolrCore0Nodes(1, 0);

      double total = initial.get("response").getAsJsonObject().get("numFound").getAsDouble();

      double pages = Math.ceil(total / 1000d);

      for (int page = 0; page < pages; page++) {
        JsonObject result = getSolrIfeedNodes(1000, page * 1000);

        JsonArray array = result.get("response").getAsJsonObject().get("docs").getAsJsonArray();

        for (int x = 0; x < array.size(); x++) {
          JsonElement nodeRef = array.get(x).getAsJsonObject().get("dc.identifier.documentid");

          nodes.add(nodeRef);
        }
      }

      return nodes;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private JsonObject getSolrCore0Nodes(int rows, int start) {
    try {
      String url = "http://solr-index.vgregion.se:8080/solr/core0/select";

      url += "?fq=source:p-facet.source.pubsub";
      url += "&fl=dc.identifier.documentid";
      url += "&rows=" + rows;
      url += "&start=" + start;
      url += "&wt=json";

      HttpClient client = new HttpClient();

      GetMethod get = new GetMethod(url);

      client.executeMethod(get);

      JsonParser parser = new JsonParser();

      return parser.parse(get.getResponseBodyAsString()).getAsJsonObject();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private JsonObject getSolrIfeedNodes(int rows, int start) {
    try {
      String url = "http://solr-index.vgregion.se:8080/solr/ifeed/select";

      url += "?fq=published:true";
      url += "&fl=dc.identifier.documentid";
      url += "&rows=" + rows;
      url += "&start=" + start;
      url += "&wt=json";

      HttpClient client = new HttpClient();

      GetMethod get = new GetMethod(url);

      client.executeMethod(get);

      JsonParser parser = new JsonParser();

      return parser.parse(get.getResponseBodyAsString()).getAsJsonObject();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public NodeRef getCacheNode(String key) {
    List<ChildAssociationRef> children = _nodeService.getChildAssocs(_repository.getCompanyHome());

    NodeRef dictionary = null;

    for (ChildAssociationRef child : children) {
      if (child.getQName().isMatch(QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary"))) {
        dictionary = child.getChildRef();
      }
    }

    if (dictionary == null) {
      return null;
    }

    NodeRef cacheParent = _nodeService.getChildByName(dictionary, ContentModel.ASSOC_CONTAINS, "Cache");

    if (cacheParent == null) {
      Map<QName, Serializable> cacheParentProperties = new HashMap<QName, Serializable>();

      cacheParentProperties.put(ContentModel.PROP_NAME, "Cache");

      cacheParent = _nodeService.createNode(dictionary, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Cache"), ContentModel.TYPE_FOLDER,
          cacheParentProperties).getChildRef();
    }

    NodeRef cache = _nodeService.getChildByName(cacheParent, ContentModel.ASSOC_CONTAINS, key);

    if (cache == null) {
      Map<QName, Serializable> cacheProperties = new HashMap<QName, Serializable>();

      cacheProperties.put(ContentModel.PROP_NAME, key);

      cache = _nodeService.createNode(cacheParent, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, key), ContentModel.TYPE_CONTENT, cacheProperties)
          .getChildRef();
    }

    return cache;
  }

  private JsonArray getAllAlfrescoNodes() {
    boolean hasMore = true;

    int start = 0;

    JsonArray result = new JsonArray();

    while (hasMore) {
      JsonArray nodes = getAlfrescoNodes(1000, start * 1000);

      if (nodes.size() == 0) {
        hasMore = false;

        continue;
      }

      result.addAll(nodes);

      start++;
    }

    return result;
  }

  private JsonArray getAlfrescoNodes(int rows, int start) {
    String query = "SELECT v.* FROM cmis:document AS D JOIN vgr:document AS v ON D.cmis:objectId = v.cmis:objectId JOIN vgr:published AS pub ON D.cmis:objectId = pub.cmis:objectId";

    SearchParameters parameters = new SearchParameters();
    parameters.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
    parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    parameters.setQuery(query);
    parameters.setMaxItems(rows);
    parameters.setSkipCount(start);

    ResultSet result = _searchService.query(parameters);

    System.out.println("Fetched " + result.length() + " Alfresco nodes.");

    JsonArray nodes = new JsonArray();

    try {
      for (NodeRef nodeRef : result.getNodeRefs()) {
        JsonObject node = new JsonObject();

        String name = (String) _nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

        node.addProperty(ContentModel.PROP_NAME.toPrefixString(_namespacePrefixResolver), name);
        node.addProperty("nodeRef", nodeRef.toString());
        node.addProperty("published", _publishingService.isPublished(nodeRef));
        node.addProperty("publish-status", (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUBLISH_STATUS));

        nodes.add(node);
      }

      return nodes;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      result.close();
    }
  }

}
