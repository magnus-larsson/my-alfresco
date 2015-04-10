package se.vgregion.alfresco.repo.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class MissingGet extends DeclarativeWebScript {

  private SearchService _searchService;

  private StorageService _storageService;

  private RenditionService _renditionService;

  private NodeService _nodeService;

  private PermissionService _permissionService;

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    int max = req.getParameter("max") != null ? Integer.parseInt(req.getParameter("max")) : -1;
    int skip = req.getParameter("skip") != null ? Integer.parseInt(req.getParameter("skip")) : 0;

    List<NodeRef> documents = getMissingNodes();

    List<Node> nodes = new ArrayList<Node>();

    for (int x = 0; x < documents.size(); x++) {
      if (skip > x) {
        break;
      }

      if (x >= (max + skip)) {
        break;
      }

      NodeRef document = documents.get(x);

      String sourcePath = getSourcePath(document);

      String name = (String) _nodeService.getProperty(document, VgrModel.PROP_TITLE);

      if (StringUtils.isBlank(name)) {
        name = (String) _nodeService.getProperty(document, ContentModel.PROP_NAME);
      }

      String filename = (String) _nodeService.getProperty(document, VgrModel.PROP_TITLE_FILENAME_NATIVE);

      if (StringUtils.isBlank(filename)) {
        filename = (String) _nodeService.getProperty(document, VgrModel.PROP_TITLE_FILENAME);
      }

      Node node = new Node();
      node.nodeRef = document.toString();
      node.name = name;
      node.sourcePath = sourcePath;
      node.storagePath = _nodeService.getPath(document).toDisplayPath(_nodeService, _permissionService);
      node.filename = filename;

      nodes.add(node);
    }

    Map<String, Object> model = new HashMap<String, Object>();

    model.put("nodes", nodes);
    model.put("recordsReturned", nodes.size());
    model.put("startIndex", skip);
    model.put("pageSize", max);
    model.put("totalRecords", documents.size());

    return model;
  }

  private List<NodeRef> getMissingNodes() {
    String query = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\"";

    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setQuery(query);

    ResultSet resultSet = _searchService.query(searchParameters);

    List<NodeRef> documents;

    try {
      documents = resultSet.getNodeRefs();
    } finally {
      ServiceUtilsImpl.closeQuietly(resultSet);
    }

    List<NodeRef> result = new ArrayList<NodeRef>();

    for (NodeRef document : documents) {
      if (!_storageService.pdfaRendable(document)) {
        continue;
      }

      ChildAssociationRef rendition = _renditionService.getRenditionByName(document, VgrModel.RD_PDFA);

      if (rendition != null) {
        continue;
      }

      result.add(document);
    }

    return result;
  }

  private String getSourcePath(NodeRef nodeRef) {
    String sourceId = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);

    String path = "";

    if (sourceId != null && sourceId.length() > 10) {
      NodeRef sourceNodeRef = new NodeRef(sourceId);

      if (_nodeService.exists(sourceNodeRef)) {
        path = _nodeService.getPath(sourceNodeRef).toDisplayPath(_nodeService, _permissionService);
      }
    }

    return path;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setPermissionService(PermissionService permissionService) {
    _permissionService = permissionService;
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

  public void setRenditionService(RenditionService renditionService) {
    _renditionService = renditionService;
  }

  public class Node {

    public String nodeRef;

    public String getNodeRef() {
      return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
      this.nodeRef = nodeRef;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getSourcePath() {
      return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
      this.sourcePath = sourcePath;
    }

    public String getStoragePath() {
      return storagePath;
    }

    public void setStoragePath(String storagePath) {
      this.storagePath = storagePath;
    }

    public String getFilename() {
      return filename;
    }

    public void setFilename(String filename) {
      this.filename = filename;
    }

    public String name;

    public String sourcePath;

    public String storagePath;

    public String filename;

  }

}
