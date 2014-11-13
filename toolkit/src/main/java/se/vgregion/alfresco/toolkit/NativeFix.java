package se.vgregion.alfresco.toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.publish.PublishingService;
import se.vgregion.alfresco.repo.storage.StorageService;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

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
public class NativeFix {

  @Autowired
  private SearchService _searchService;

  @Autowired
  private StorageService _storageService;

  @Autowired
  private PublishingService _publishingService;

  @Autowired
  private NodeService _nodeService;

  @Autowired
  private ContentService _contentService;

  @Autowired
  private ServiceUtils _serviceUtils;

  @Autowired
  @Resource(name = "policyBehaviourFilter")
  private BehaviourFilter _behaviourFilter;

  @Uri(method = HttpMethod.GET, value = { "/vgr/toolkit/native/find" }, defaultFormat = "json")
  public Resolution findFailedNative(WebScriptResponse response) throws IOException {
    Failed failed = findFailedNative();

    final List<NodeRef> result = new ArrayList<NodeRef>();

    for (Pair pair : failed.list) {
      result.add(pair.node);
    }

    return new JsonWriterResolution() {

      @Override
      protected void writeJson(JSONWriter jsonWriter) throws JSONException {
        jsonWriter.object().key("failed").value(result).key("total").value(result.size()).endObject();
      }

    };
  }

  @Uri(method = HttpMethod.GET, value = { "/vgr/toolkit/native/fix" }, defaultFormat = "json")
  public Resolution fixFailedNative(WebScriptResponse response) throws IOException {
    final Failed failed = findFailedNative();

    for (Pair pair : failed.list) {
      _behaviourFilter.disableBehaviour();

      try {
        populateNativeProperties(pair.node, pair.pdf);

        _nodeService.setProperty(pair.node, VgrModel.PROP_PUSHED_FOR_PUBLISH, null);
        _nodeService.setProperty(pair.node, VgrModel.PROP_PUBLISH_STATUS, null);
        _nodeService.setProperty(pair.node, VgrModel.PROP_PUSHED_COUNT, null);
        _nodeService.setProperty(pair.node, ContentModel.PROP_MODIFIED, new Date());
      } finally {
        _behaviourFilter.enableBehaviour();
      }
    }

    return new JsonWriterResolution() {

      @Override
      protected void writeJson(JSONWriter jsonWriter) throws JSONException {
        jsonWriter.object().key("success").value(failed.list.size()).key("total").value(failed.total).endObject();
      }

    };
  }

  private Failed findFailedNative() {
    Failed failed = new Failed();

    String query = "ISNULL:\"vgr:dc.title.filename.native\" AND TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\"";

    SearchParameters parameters = new SearchParameters();
    parameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    parameters.setQuery(query);

    ResultSet nodes = _searchService.query(parameters);

    failed.total = nodes.length();

    try {
      for (NodeRef nodeRef : nodes.getNodeRefs()) {
        if (!_publishingService.isPublished(nodeRef)) {
          continue;
        }

        if (failed.list.size() >= 500) {
          break;
        }

        NodeRef pdfNodeRef = _storageService.getPdfaRendition(nodeRef);

        if (pdfNodeRef == null) {
          System.out.println("No PDF/A rendition found for '" + nodeRef + "' to populate natives for...");

          continue;
        }

        failed.list.add(new Pair(nodeRef, pdfNodeRef));
      }
    } finally {
      nodes.close();
    }

    return failed;
  }

  private void populateNativeProperties(NodeRef nodeRef, NodeRef pdfNodeRef) {
    Serializable storedFilename = _nodeService.getProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME);

    ContentReader nativeContentReader = _contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

    InputStream nativeInputStream = nativeContentReader.getContentInputStream();

    Serializable nativeChecksum = _serviceUtils.getChecksum(nativeInputStream);
    Serializable nativeExtension = _serviceUtils.getFileExtension(nodeRef, false);
    Serializable nativeFilename = FilenameUtils.removeExtension((String) storedFilename) + "." + nativeExtension;
    Serializable nativeIdentifier = _serviceUtils.getDocumentIdentifier(nodeRef, true);
    Serializable nativeMimetype = _serviceUtils.getMimetype(nodeRef);

    _nodeService.setProperty(nodeRef, VgrModel.PROP_CHECKSUM_NATIVE, nativeChecksum);
    _nodeService.setProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME_NATIVE, nativeFilename);
    _nodeService.setProperty(nodeRef, VgrModel.PROP_IDENTIFIER_NATIVE, nativeIdentifier);
    _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE_NATIVE, nativeMimetype);
    _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_EXTENSION_NATIVE, nativeExtension);

    ContentReader pdfContentReader = _contentService.getReader(pdfNodeRef, ContentModel.PROP_CONTENT);

    InputStream pdfInputStream = pdfContentReader.getContentInputStream();

    Serializable pdfChecksum = _serviceUtils.getChecksum(pdfInputStream);
    Serializable pdfExtension = _serviceUtils.getFileExtension(pdfNodeRef, false);
    Serializable pdfFilename = FilenameUtils.removeExtension((String) storedFilename) + "." + pdfExtension;
    Serializable pdfIdentifier = _serviceUtils.getDocumentIdentifier(nodeRef);
    Serializable pdfMimetype = _serviceUtils.getMimetype(pdfNodeRef);

    _nodeService.setProperty(nodeRef, VgrModel.PROP_CHECKSUM, pdfChecksum);
    _nodeService.setProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME, pdfFilename);
    _nodeService.setProperty(nodeRef, VgrModel.PROP_IDENTIFIER, pdfIdentifier);
    _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE, pdfMimetype);
    _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_EXTENSION, pdfExtension);
  }

  private class Failed {
    int total = 0;
    List<Pair> list = new ArrayList<Pair>();
  }

  private class Pair {

    public Pair(NodeRef nodeRef, NodeRef pdfNodeRef) {
      this.node = nodeRef;
      this.pdf = pdfNodeRef;
    }

    NodeRef node;
    NodeRef pdf;
  }

}
