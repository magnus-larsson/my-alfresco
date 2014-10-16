package se.vgregion.alfresco.toolkit;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.json.JSONException;
import org.json.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import se.vgregion.alfresco.repo.publish.PublishingService;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.JsonWriterResolution;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

@Component
@WebScript(families = { "VGR" })
@Authentication(AuthenticationType.ADMIN)
public class CheckSolrIndex {
  
  @Autowired
  private SearchService _searchService;
  
  @Autowired
  private PublishingService _publishingService;
  
  @Autowired
  private NodeService _nodeService;
  
  @Autowired
  private NamespacePrefixResolver _namespacePrefixResolver;

  /**
   * Gets a list and count of all the published nodeRefs.
   * 
   * @param response
   * @return
   * @throws IOException
   */
  @Uri(method = HttpMethod.GET, value = { "/vgr/toolkit/published" }, defaultFormat = "json")
  public Resolution published(WebScriptResponse response) throws IOException {
    String query = "SELECT v.* FROM cmis:document AS D JOIN vgr:document AS v ON D.cmis:objectId = v.cmis:objectId JOIN vgr:published AS pub ON D.cmis:objectId = pub.cmis:objectId";

    SearchParameters parameters = new SearchParameters();
    parameters.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
    parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    parameters.setQuery(query);

    ResultSet result = _searchService.query(parameters);
    
    final List<Map<String, Serializable>> nodes = new ArrayList<Map<String, Serializable>>();
    
    try {
      for (NodeRef nodeRef : result.getNodeRefs()) {
        if (!_publishingService.isPublished(nodeRef)) {
          continue;
        }
        
        String name = (String) _nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        
        properties.put(ContentModel.PROP_NAME.toPrefixString(_namespacePrefixResolver), name);
        properties.put("nodeRef", nodeRef);
        
        nodes.add(properties);
      }
    } finally {
      result.close();
    }
    
    return new JsonWriterResolution() {

      @Override
      protected void writeJson(JSONWriter jsonWriter) throws JSONException {
        jsonWriter.object().key("total").value(nodes.size()).key("data").value(nodes).endObject();
      }

    };
  }

}
