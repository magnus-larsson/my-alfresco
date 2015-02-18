package se.vgregion.alfresco.repo.scripts;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;

public class SetPublishStatus extends DeclarativeWebScript implements InitializingBean {

  private static final String TYPE_PUBLISH_STATUS = "publish";
  private static final String TYPE_UNPUBLISH_STATUS = "unpublish";
  private NodeService nodeService;
  private Properties globalProperties;
  private BehaviourFilter _behaviourFilter;
  private static final Logger LOG = Logger.getLogger(SetPublishStatus.class);

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
    Map<String, Object> model = new HashMap<String, Object>();
    String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();

    String allowedUsersProperty = globalProperties.getProperty("vgr.publishstatus.allowedUsers", "admin");
    String[] allowedUsers;
    if (allowedUsersProperty.contains(",")) {
      allowedUsers = allowedUsersProperty.split(",");
    } else {
      allowedUsers = new String[1];
      allowedUsers[0] = allowedUsersProperty;
    }
    boolean userIsAllowed = false;
    for (String allowedUser : allowedUsers) {
      if (fullyAuthenticatedUser.equalsIgnoreCase(allowedUser)) {
        userIsAllowed = true;
        break;
      }
    }

    // Validation of user
    if (!userIsAllowed) {
      LOG.warn("The user " + fullyAuthenticatedUser + " tried to access the set publish status web script." + " However the user is not on the allowed users list: " + allowedUsersProperty);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("You do not have permission to access the requested resource");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String storeType = templateVars.get("store_type");
    String storeId = templateVars.get("store_id");
    String nodeId = templateVars.get("node_id");
    if (nodeId.indexOf("&") >= 0) {
      nodeId = nodeId.substring(0, nodeId.indexOf("&"));
    }
    // Validation of parameters
    String documentId = storeType + "://" + storeId + "/" + nodeId;

    final String publishingStatus = req.getParameter("status");
    String type = req.getParameter("type");
    if (documentId == null || documentId.length() == 0) {
      LOG.error("The required parameter documentId is not set.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("The required parameter documentId is not set.");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }
    NodeRef nodeRef = null;

    try {
      nodeRef = new NodeRef(documentId);
    } catch (Exception e) {
      LOG.error("Malformed document id: " + documentId, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Malformed document id: " + e.getMessage());
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }
    if (!nodeService.exists(nodeRef)) {
      LOG.error("Document with id " + documentId + " does not exist.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Document with id " + documentId + " does not exist.");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    if (publishingStatus == null || publishingStatus.length() == 0) {
      LOG.error("The required parameter status is not set.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("The required parameter status is not set.");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    if (TYPE_PUBLISH_STATUS.equalsIgnoreCase(type)) {
      type = TYPE_PUBLISH_STATUS;
    } else if (TYPE_UNPUBLISH_STATUS.equalsIgnoreCase(type)) {
      type = TYPE_UNPUBLISH_STATUS;
    } else {
      LOG.error("The required parameter type is invalid: " + type + ". Expected " + TYPE_PUBLISH_STATUS + " or " + TYPE_UNPUBLISH_STATUS);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("The required parameter type is invalid: " + type + ". Expected " + TYPE_PUBLISH_STATUS + " or " + TYPE_UNPUBLISH_STATUS);
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    if (!nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PUBLISHED)) {
      LOG.error("Document " + documentId + " does not have the required aspect " + VgrModel.ASPECT_PUBLISHED.toPrefixString());
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Document " + documentId + " does not have the required aspect " + VgrModel.ASPECT_PUBLISHED.toPrefixString());
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    } else {
      final String aType = type;
      final NodeRef aNodeRef = nodeRef;
      AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

        @Override
        public Void doWork() throws Exception {
          _behaviourFilter.disableBehaviour();
          if (TYPE_PUBLISH_STATUS.equals(aType)) {
            nodeService.setProperty(aNodeRef, VgrModel.PROP_PUBLISH_STATUS, publishingStatus);
          } else {
            nodeService.setProperty(aNodeRef, VgrModel.PROP_UNPUBLISH_STATUS, publishingStatus);
          }
          _behaviourFilter.enableBehaviour();
          return null;
        }
      }, AuthenticationUtil.getSystemUserName());
    }
    model.put("result", "OK");
    if (LOG.isDebugEnabled())
      LOG.debug("Setting " + type + " status " + publishingStatus + " for node " + nodeRef);
    return model;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(nodeService);
    Assert.notNull(globalProperties);
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setGlobalProperties(Properties globalProperties) {
    this.globalProperties = globalProperties;
  }

  public void setBehaviourFilter(BehaviourFilter _behaviourFilter) {
    this._behaviourFilter = _behaviourFilter;
  }

}
