package se.vgregion.alfresco.repo.scripts;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VersionNumber;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class GetPublishStatus extends DeclarativeWebScript implements InitializingBean {

  private NodeService nodeService;
  private StorageService storageService;
  private ServiceUtils serviceUtils;

  private static final Logger LOG = Logger.getLogger(GetPublishStatus.class);
  public static final String OK = "ok";
  public static final String ERROR = "error";

  public static final String STATUS_ERROR = "ERROR";
  public static final String STATUS_PUBLISH_ERROR = "PUBLISH_ERROR";
  public static final String STATUS_UNPUBLISH_ERROR = "UNPUBLISH_ERROR";
  public static final String STATUS_PUBLISHED = "PUBLISHED";
  public static final String STATUS_UNPUBLISHED = "UNPUBLISHED";
  public static final String STATUS_SENT_FOR_PUBLISH = "SENT_FOR_PUBLISH";
  public static final String STATUS_SENT_FOR_UNPUBLISH = "SENT_FOR_UNPUBLISH";
  public static final String STATUS_NOT_PUBLISHED = "NOT_PUBLISHED";
  public static final String STATUS_PREVIOUSLY_PUBLISHED = "PREVIOUSLY_PUBLISHED";
  public static final String STATUS_PREVIOUS_VERSION_PUBLISHED = "PREVIOUS_VERSION_PUBLISHED";

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
    Map<String, Object> model = new HashMap<String, Object>();
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String storeType = templateVars.get("store_type");
    String storeId = templateVars.get("store_id");
    String nodeId = templateVars.get("node_id");
    String docId = templateVars.get("document_id");
    if (nodeId != null && nodeId.indexOf("&") >= 0) {
      nodeId = nodeId.substring(0, nodeId.indexOf("&"));
    }

    String documentId;
    if (nodeId != null) {
      // Validation of parameters
      documentId = storeType + "://" + storeId + "/" + nodeId;
    } else {
      documentId = docId;
    }
    if (documentId == null || documentId.length() == 0) {
      LOG.error("The required parameter documentId is not set.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("The required parameter documentId is not set.");
      status.setRedirect(true);
      model.put("result", STATUS_ERROR);
      return model;
    }
    NodeRef nodeRef = null;

    List<NodeRef> storageVersions = storageService.getStorageVersions(documentId);
    NodeRef latestPublishedStorageVersion = storageVersions.size() > 0 ? storageVersions.get(0) : null;

    if (documentId.contains("workspace://SpacesStore")) {
      try {
        nodeRef = new NodeRef(documentId);
      } catch (Exception e) {
        LOG.error("Malformed document id: " + documentId, e);
        status.setCode(Status.STATUS_BAD_REQUEST);
        status.setMessage("Malformed document id: " + e.getMessage());
        status.setRedirect(true);
        model.put("result", STATUS_ERROR);
        return model;
      }
    } else {
      nodeRef = latestPublishedStorageVersion;

    }

    if (nodeRef == null || !nodeService.exists(nodeRef)) {
      LOG.error("Document with id " + documentId + " does not exist.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Document with id " + documentId + " does not exist.");
      status.setRedirect(true);
      model.put("result", STATUS_ERROR);
      return model;
    }

    if (latestPublishedStorageVersion == null || !nodeService.exists(latestPublishedStorageVersion)) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Document in storage with id " + latestPublishedStorageVersion + " does not exist. Document id is " + documentId);
      }
      model.put("result", STATUS_NOT_PUBLISHED);
      return model;
    }
    boolean someVersionIsPublished = false;
    for (NodeRef storageVersion : storageVersions) {
      // only add published nodes to this list
      if (serviceUtils.isPublished(storageVersion)) {
        someVersionIsPublished = true;
        break;
      }
    }

    if (!nodeService.hasAspect(latestPublishedStorageVersion, VgrModel.ASPECT_PUBLISHED)) {
      LOG.error("Document " + latestPublishedStorageVersion + " does not have the required aspect " + VgrModel.ASPECT_PUBLISHED.toPrefixString());
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Document " + documentId + " does not have the required aspect " + VgrModel.ASPECT_PUBLISHED.toPrefixString());
      status.setRedirect(true);
      model.put("result", STATUS_ERROR);
      return model;
    } else {
      Map<QName, Serializable> properties = nodeService.getProperties(latestPublishedStorageVersion);
      String publishStatus = (String) properties.get(VgrModel.PROP_PUBLISH_STATUS);
      if (publishStatus == null) {
        publishStatus = "";
      }
      String unpublishStatus = (String) properties.get(VgrModel.PROP_UNPUBLISH_STATUS);
      if (unpublishStatus == null) {
        unpublishStatus = "";
      }

      String latestPublishedVersionNumber = (String) properties.get(VgrModel.PROP_IDENTIFIER_VERSION);
      String latestVersionNumber = (String) nodeService.getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
      int versionsCompare = new VersionNumber(latestVersionNumber).compareTo(new VersionNumber(latestPublishedVersionNumber));
      // Verify that we are looking at the latest version
      if (versionsCompare != 0) {
        // We are looking at a different version than the latest one.
        if (storageVersions.size() > 0 && !someVersionIsPublished) {
          model.put("result", STATUS_PREVIOUSLY_PUBLISHED);
        } else {
          model.put("result", STATUS_PREVIOUS_VERSION_PUBLISHED);
        }
      } else {
        // This version is the latest one and is in the storage
        model.put("publishStatus", publishStatus);
        model.put("unpublishStatus", unpublishStatus);
        Date pushedForPublish = (Date) properties.get(VgrModel.PROP_PUSHED_FOR_PUBLISH);
        Date pushedForUnPublish = (Date) properties.get(VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
        model.put("nodeRef", latestPublishedStorageVersion.toString());
        model.put("pushed_for_publish", pushedForPublish);
        model.put("pushed_for_unpublish", pushedForUnPublish);

        if (pushedForUnPublish == null && pushedForPublish != null) {
          if (publishStatus.equalsIgnoreCase(OK)) {
            model.put("result", STATUS_PUBLISHED);
          } else if (publishStatus.equalsIgnoreCase(ERROR)) {
            model.put("result", STATUS_PUBLISH_ERROR);
          } else {
            model.put("result", STATUS_SENT_FOR_PUBLISH);
          }
        } else if (pushedForUnPublish != null) {
          if (pushedForUnPublish.compareTo(pushedForPublish) >= 0) {
            if (unpublishStatus.equalsIgnoreCase(OK)) {
              model.put("result", STATUS_UNPUBLISHED);
            } else if (unpublishStatus.equalsIgnoreCase(ERROR)) {
              model.put("result", STATUS_UNPUBLISH_ERROR);
            } else {
              model.put("result", STATUS_SENT_FOR_UNPUBLISH);
            }
          } else {
            if (publishStatus.equalsIgnoreCase(OK)) {
              model.put("result", STATUS_PUBLISHED);
            } else if (publishStatus.equalsIgnoreCase(ERROR)) {
              model.put("result", STATUS_PUBLISH_ERROR);
            } else {
              model.put("result", STATUS_SENT_FOR_PUBLISH);
            }
          }
        } else if (storageVersions.size() > 1 && someVersionIsPublished) {
          model.put("result", STATUS_PREVIOUS_VERSION_PUBLISHED);
        } else if (storageVersions.size() > 0 && !someVersionIsPublished) {
          model.put("result", STATUS_PREVIOUSLY_PUBLISHED);
        } else if (storageVersions.size() > 0) {
          model.put("result", STATUS_SENT_FOR_PUBLISH);
        } else {
          model.put("result", STATUS_NOT_PUBLISHED);
        }
      }
    }

    return model;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(nodeService);
    Assert.notNull(storageService);
    Assert.notNull(serviceUtils);
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setStorageService(StorageService storageService) {
    this.storageService = storageService;
  }

  public void setServiceUtils(ServiceUtils serviceUtils) {
    this.serviceUtils = serviceUtils;
  }

}
