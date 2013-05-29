package se.vgregion.alfresco.repo.scripts;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.push.PushService;

public class ScriptGetDocumentsPublishReport extends DeclarativeWebScript implements InitializingBean {

  private NodeService _nodeService;
  private PushService _pushService;

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
    Map<String, Object> model = new HashMap<String, Object>();

    List<Map<String, Serializable>> documents = new ArrayList<Map<String, Serializable>>();

    String publishStatusParam = req.getParameter("publishstatus");
    String unpublishStatusParam = req.getParameter("unpublishstatus");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    Date dateFrom = null;
    try {
      String parameter = req.getParameter("from");
      if (parameter != null)
        dateFrom = sdf.parse(parameter);
    } catch (ParseException e1) {
      
    }
    Date dateTo = null;
    try {
      String parameter = req.getParameter("to");
      if (parameter != null)
        dateTo = sdf.parse(parameter);
    } catch (ParseException e1) {
      
    }
    if (publishStatusParam == null) {
      publishStatusParam = "";
    }
    if (unpublishStatusParam == null) {
      publishStatusParam = "";
    }
    List<NodeRef> pushedFiles = _pushService.findPushedFiles(publishStatusParam, unpublishStatusParam, dateFrom, dateTo);
    for (NodeRef pushedFileNodeRef : pushedFiles) {
      Map<QName, Serializable> properties = _nodeService.getProperties(pushedFileNodeRef);
      HashMap<String, Serializable> document = new HashMap<String, Serializable>();
      document.put("title", properties.get(VgrModel.PROP_TITLE));
      document.put("id", pushedFileNodeRef.toString());
      document.put("sourceId", properties.get(VgrModel.PROP_SOURCE_DOCUMENTID));
      document.put("pushedForPublish", properties.get(VgrModel.PROP_PUSHED_FOR_PUBLISH));
      document.put("pushedForUnpublish", properties.get(VgrModel.PROP_PUSHED_FOR_UNPUBLISH));
      document.put("publishStatus", properties.get(VgrModel.PROP_PUBLISH_STATUS));
      document.put("unpublishStatus", properties.get(VgrModel.PROP_UNPUBLISH_STATUS));
      documents.add(document);
    }
    model.put("recordsReturned", documents.size());
    model.put("totalRecords", documents.size());
    model.put("startIndex", 0);
    model.put("pageSize", documents.size());
    model.put("documents", documents);
    return model;
  }

  public void setNodeService(NodeService _nodeService) {
    this._nodeService = _nodeService;
  }

  public void setPushService(PushService _pushService) {
    this._pushService = _pushService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_nodeService);
    Assert.notNull(_pushService);
  }

}
