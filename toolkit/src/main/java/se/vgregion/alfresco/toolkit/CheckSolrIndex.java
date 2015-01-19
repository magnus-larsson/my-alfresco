package se.vgregion.alfresco.toolkit;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.util.CronTriggerBean;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.publish.PublishingService;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.ErrorResolution;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.JsonWriterResolution;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
@WebScript(families = { "VGR" })
@Authentication(AuthenticationType.ADMIN)
public class CheckSolrIndex {

  private static final Logger LOG = Logger.getLogger(CheckSolrIndex.class);

  public enum RepushStatus {
    REPUSHED_FOR_PUBLISHED, REPUSHED_FOR_UNPUBLISHED, DOCUMENT_MISSING
  }

  @Autowired
  private SearchService _searchService;

  @Autowired
  private PublishingService _publishingService;

  @Autowired
  private NodeService _nodeService;

  @Autowired
  private NamespacePrefixResolver _namespacePrefixResolver;

  @Autowired
  private Repository _repository;

  @Autowired
  private ContentService _contentService;

  @Autowired
  private IndexCacheService _indexCacheService;

  @Autowired
  @Resource(name = "policyBehaviourFilter")
  private BehaviourFilter _behaviourFilter;

  @Resource(name = "vgr.refreshPublishedCachesTrigger")
  private CronTriggerBean _refreshPublishedCachesTriggerBean;

  /**
   * Refreshes the cache nodes.
   * 
   * @param response
   * @return
   * @throws IOException
   */
  @Uri(method = HttpMethod.GET, value = { "/vgr/toolkit/cache/refresh" }, defaultFormat = "json")
  public Resolution refreshCache(WebScriptRequest request, WebScriptResponse response) throws IOException {
    String jobName = _refreshPublishedCachesTriggerBean.getJobDetail().getName();
    String groupName = _refreshPublishedCachesTriggerBean.getJobDetail().getGroup();

    try {
      _refreshPublishedCachesTriggerBean.getScheduler().triggerJob(jobName, groupName);
    } catch (SchedulerException ex) {
      return new ErrorResolution(500, ex.getMessage());
    }

    return new JsonWriterResolution() {

      @Override
      protected void writeJson(JSONWriter jsonWriter) throws JSONException {
        jsonWriter.object().key("result").value(true).endObject();
      }

    };
  }

  @Uri(method = HttpMethod.GET, value = { "/vgr/toolkit/cache/check" }, defaultFormat = "json")
  public Resolution check(WebScriptRequest request) {
    final String nodeRef = request.getParameter("nodeRef");

    if (StringUtils.isBlank(nodeRef)) {
      return new ErrorResolution(500, "No 'nodeRef' parameter found!");
    }

    NodeRef node = new NodeRef(nodeRef);

    if (!_nodeService.exists(node)) {
      return new ErrorResolution(404, "Document with nodeRef '" + nodeRef + "' does not exist.");
    }

    // http://solr-index.vgregion.se:8080/solr/ifeed/select/?fq=*:*&version=2.2&start=0&rows=10&indent=on&fl=*&fq=dc.source.documentid:8800

    String url = "http://solr-index.vgregion.se:8080/solr/core0/select";
    String fq = "dc.identifier.documentid:\"" + nodeRef + "\"";

    url += "?fq=" + URLEncoder.encodeUriComponent(fq);
    url += "&fl=dc.identifier.documentid";
    url += "&wt=json";

    try {
      HttpClient client = new HttpClient();

      GetMethod get = new GetMethod(url);

      client.executeMethod(get);

      JsonParser parser = new JsonParser();

      JsonObject json = parser.parse(get.getResponseBodyAsString()).getAsJsonObject();

      final int count = json.getAsJsonObject("response").get("numFound").getAsInt();

      return new JsonWriterResolution() {

        @Override
        protected void writeJson(JSONWriter jsonWriter) throws JSONException {
          jsonWriter.object().key("result").value(count > 0).key("nodeRef").value(nodeRef).endObject();
        }

      };
    } catch (Exception ex) {
      return new ErrorResolution(500, ex.getMessage());
    }
  }

  @Uri(method = HttpMethod.GET, value = { "/vgr/toolkit/cache/{key}" })
  public Resolution cacheGet(@UriVariable(value = "key") String cacheKey, WebScriptRequest request, WebScriptResponse response) {
    NodeRef cache = _indexCacheService.getCacheNode(cacheKey);

    final ContentReader contentReader = _contentService.getReader(cache, ContentModel.PROP_CONTENT);

    if (contentReader == null || !contentReader.exists()) {
      return new ErrorResolution(404);
    }

    final JSONArray json;

    try {
      json = new JSONArray(contentReader.getContentString());
    } catch (Exception ex) {
      return new ErrorResolution(500, ex.getMessage());
    }

    final Date modified = (Date) _nodeService.getProperty(cache, ContentModel.PROP_MODIFIED);

    final String cacheDate = ISO8601DateFormat.format(modified);

    return new JsonWriterResolution() {

      @Override
      protected void writeJson(JSONWriter jsonWriter) throws JSONException {
        jsonWriter.object().key("cacheDate").value(cacheDate).key("orphans").value(json).endObject();
      }

    };
  }

  @Uri(method = HttpMethod.PUT, value = { "/vgr/toolkit/cache/{key}" })
  public Resolution cachePut(@UriVariable(value = "key") String cacheKey, WebScriptRequest request, WebScriptResponse response) {
    NodeRef cache = _indexCacheService.getCacheNode(cacheKey);

    ContentWriter contentWriter = _contentService.getWriter(cache, ContentModel.PROP_CONTENT, true);

    try {
      contentWriter.putContent(request.getContent().getInputStream());
    } catch (Exception ex) {
      ex.printStackTrace();

      return new ErrorResolution(500, ex.getMessage());
    }

    return new JsonWriterResolution() {

      @Override
      protected void writeJson(JSONWriter jsonWriter) throws JSONException {
        jsonWriter.object().key("success").value(true).endObject();
      }

    };
  }

  @Uri(method = HttpMethod.PUT, value = { "/vgr/toolkit/repush" })
  public Resolution repush(WebScriptRequest request, WebScriptResponse response) throws IOException {
    JsonObject json = new JsonParser().parse(request.getContent().getContent()).getAsJsonObject();

    NodeRef nodeRef = new NodeRef(json.get("nodeRef").getAsString());

    final RepushStatus status = repushDocument(nodeRef);

    if (status == RepushStatus.DOCUMENT_MISSING) {
      return new ErrorResolution(404, "Document '" + nodeRef + "' does not exist and can't be repushed...");
    }

    return new JsonWriterResolution() {

      @Override
      protected void writeJson(JSONWriter jsonWriter) throws JSONException {
        jsonWriter.object().key("success").value(status).endObject();
      }

    };
  }

  private RepushStatus repushDocument(NodeRef document) {
    if (!_nodeService.exists(document)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Document '" + document + "' does not exist and can't be repushed...");
      }

      return RepushStatus.DOCUMENT_MISSING;
    }

    _behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
    _behaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);

    try {
      RepushStatus result;

      Date now = new Date();
      Date publishDate = (Date) _nodeService.getProperty(document, VgrModel.PROP_DATE_AVAILABLE_FROM);
      Date unpublishDate = (Date) _nodeService.getProperty(document, VgrModel.PROP_DATE_AVAILABLE_TO);

      if (publishDate == null) {
        publishDate = (Date) _nodeService.getProperty(document, VgrModel.PROP_DATE_ISSUED);
      }

      if (publishDate == null) {
        publishDate = (Date) _nodeService.getProperty(document, ContentModel.PROP_CREATED);
      }

      if (unpublishDate != null && now.after(unpublishDate)) {
        _nodeService.setProperty(document, VgrModel.PROP_PUSHED_FOR_PUBLISH, publishDate);
        _nodeService.setProperty(document, VgrModel.PROP_PUBLISH_STATUS, "OK");
        _nodeService.setProperty(document, VgrModel.PROP_PUSHED_FOR_UNPUBLISH, null);
        _nodeService.setProperty(document, VgrModel.PROP_UNPUBLISH_STATUS, null);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Repushing storage node (Unpublish) " + document.toString());
        }

        result = RepushStatus.REPUSHED_FOR_UNPUBLISHED;
      } else {
        _nodeService.setProperty(document, VgrModel.PROP_PUSHED_FOR_PUBLISH, null);
        _nodeService.setProperty(document, VgrModel.PROP_PUBLISH_STATUS, null);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Repushing storage node (Publish) " + document.toString());
        }

        result = RepushStatus.REPUSHED_FOR_PUBLISHED;
      }

      _nodeService.setProperty(document, ContentModel.PROP_MODIFIED, new Date());
      _nodeService.setProperty(document, VgrModel.PROP_PUSHED_COUNT, null);

      return result;
    } finally {
      _behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
      _behaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
    }
  }

}
