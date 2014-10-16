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
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import se.vgregion.alfresco.repo.storage.FailedRenditionInfo;
import se.vgregion.alfresco.repo.storage.StorageService;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.ErrorResolution;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.JsonWriterResolution;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

@Component
@WebScript(families = { "VGR" })
@Authentication(AuthenticationType.ADMIN)
public class Index extends AbstractIndex {

  @Autowired
  private SearchService _searchService;

  @Autowired
  private NodeService _nodeService;

  @Autowired
  private StorageService _storageService;

  @Autowired
  private PermissionService _permissionService;

  @Override
  public String getIndexHtmlPath() {
    return "/se/vgregion/alfresco/toolkit/app/index.html";
  }

  @Override
  public String getIndexAppPath() {
    return "/vgr/toolkit/app/";
  }

  @Override
  @Uri(method = HttpMethod.GET, value = { "/vgr/toolkit" }, defaultFormat = "html")
  public Resolution index(WebScriptResponse response) throws IOException {
    return super.index(response);
  }

  @Uri(method = HttpMethod.GET, value = { "/vgr/toolkit/failed" }, defaultFormat = "json")
  public Resolution failed(WebScriptResponse response) throws IOException {
    SearchParameters parameters = new SearchParameters();
    parameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    parameters.setQuery("TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND ASPECT:\"vgr:failedRenditionSource\"");
    // parameters.setQuery("TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\"");

    ResultSet result = _searchService.query(parameters);

    final List<Map<String, Serializable>> nodes = new ArrayList<Map<String, Serializable>>();

    try {
      for (NodeRef node : result.getNodeRefs()) {
        Map<String, Serializable> nodeMap = new HashMap<String, Serializable>();

        String name = (String) _nodeService.getProperty(node, ContentModel.PROP_NAME);
        String path = _nodeService.getPath(node).toDisplayPath(_nodeService, _permissionService);
        Map<String, FailedRenditionInfo> failedRenditions = _storageService.getFailedRenditions(node);

        nodeMap.put("nodeRef", node.toString());
        nodeMap.put("name", name);
        nodeMap.put("path", path);
        nodeMap.put("last", failedRenditions.get("pdfa").getMostRecentFailure());
        nodeMap.put("count", failedRenditions.get("pdfa").getFailureCount());

        nodes.add(nodeMap);
      }

      return new JsonWriterResolution() {

        @Override
        protected void writeJson(JSONWriter jsonWriter) throws JSONException {
          jsonWriter.object().key("data").value(nodes).key("total").value(nodes.size()).endObject();
        }

      };
    } finally {
      result.close();
    }
  }

  @Uri(method = HttpMethod.PUT, value = { "/vgr/toolkit/failed" }, defaultFormat = "json")
  public Resolution rerender(WebScriptRequest request, WebScriptResponse response) throws IOException {
    JSONObject json;
    boolean result;

    try {
      Long timeout = request.getParameter("timeout") != null ? Long.parseLong(request.getParameter("timeout")) : null;

      json = new JSONObject(request.getContent().getContent());

      NodeRef nodeRef = new NodeRef(json.getString("nodeRef"));

      // the try catch is because of backwards compatibility, can be removed
      // after the next installation from the current date (2014-10-02)
      try {
        result = _storageService.createPdfaRendition(nodeRef, timeout);
      } catch (NoSuchMethodError ex) {
        result = _storageService.createPdfaRendition(nodeRef, false);
      }
    } catch (Exception ex) {
      String error = ExceptionUtils.getStackTrace(ex);

      return new ErrorResolution(HttpStatus.INTERNAL_SERVER_ERROR.value(), error);
    }

    final boolean endResult = result;

    return new JsonWriterResolution() {

      @Override
      protected void writeJson(JSONWriter jsonWriter) throws JSONException {
        jsonWriter.object().key("success").value(endResult).endObject();
      }

    };
  }

}
