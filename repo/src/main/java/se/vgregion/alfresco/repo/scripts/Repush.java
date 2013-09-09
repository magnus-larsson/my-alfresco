package se.vgregion.alfresco.repo.scripts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.push.PushService;
import se.vgregion.alfresco.repo.storage.StorageService;

public class Repush extends DeclarativeWebScript implements InitializingBean {

  private StorageService storageService;
  private NodeService nodeService;
  private BehaviourFilter _behaviourFilter;
  private PushService pushService;
  private static final Logger LOG = Logger.getLogger(Repush.class);

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
    Map<String, Object> model = new HashMap<String, Object>();
    final String nodeRefString = req.getParameter("documentId");
    
    final NodeRef storageNodeRef = new NodeRef(nodeRefString);

    if (!nodeService.exists(storageNodeRef)) {
      LOG.error("Document with id " + storageNodeRef + " does not exist.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Document with id " + storageNodeRef + " does not exist.");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }
    
    if (!nodeService.hasAspect(storageNodeRef, VgrModel.ASPECT_PUBLISHED)) {
      LOG.error("Document with id " + storageNodeRef + " is not published.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Document with id " + storageNodeRef + " is not published.");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }
    
    String documentId = (String) nodeService.getProperty(storageNodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);

    
    //Process all versions from the node up until latest version
    List<NodeRef> nodesToRepush = new ArrayList<NodeRef>();
    final List<ResultSetRow> storageVersions = storageService.getStorageVersions(documentId); //Sorted
    
    boolean hasMatch = false;
    //Invert the list as it is sorted descending
    for (int i = storageVersions.size()-1;i>=0;i--) {
      ResultSetRow storageVersion = storageVersions.get(i);
      if (storageNodeRef.equals(storageVersion.getNodeRef())) {
        hasMatch = true;
      }
      if (hasMatch) {
        nodesToRepush.add(storageVersion.getNodeRef());
      }
    }
    final List<NodeRef> nodesToRepushS = nodesToRepush;
    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        _behaviourFilter.disableBehaviour();
        for (NodeRef nodeToRepush : nodesToRepushS) {
          
          Date now = new Date();
          Date unpublishDate = (Date) nodeService.getProperty(nodeToRepush, VgrModel.PROP_DATE_AVAILABLE_TO);
          if (unpublishDate != null && now.after(unpublishDate)) {
            nodeService.setProperty(nodeToRepush, VgrModel.PROP_PUSHED_FOR_UNPUBLISH, null);
            nodeService.setProperty(nodeToRepush, VgrModel.PROP_UNPUBLISH_STATUS, null);
            if (LOG.isDebugEnabled()) {
              LOG.debug("Repushing storage node (Unpublish) "+nodeToRepush.toString());
            }
          } else {
            nodeService.setProperty(nodeToRepush, VgrModel.PROP_PUSHED_FOR_PUBLISH, null);
            nodeService.setProperty(nodeToRepush, VgrModel.PROP_PUBLISH_STATUS, null);
            if (LOG.isDebugEnabled()) {
              LOG.debug("Repushing storage node (Publish) "+nodeToRepush.toString());
            }
          }
          nodeService.setProperty(nodeToRepush, ContentModel.PROP_MODIFIED, new Date());
          nodeService.setProperty(nodeToRepush, VgrModel.PROP_PUSHED_COUNT, null);
        }
        _behaviourFilter.enableBehaviour();
        return null;
      }
    }, AuthenticationUtil.getSystemUserName());

    model.put("result", "OK");

    return model;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(nodeService);
    Assert.notNull(storageService);
    Assert.notNull(storageService);
    Assert.notNull(pushService);
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setBehaviourFilter(BehaviourFilter _behaviourFilter) {
    this._behaviourFilter = _behaviourFilter;
  }

  public void setStorageService(StorageService storageService) {
    this.storageService = storageService;
  }

  public void setPushService(PushService pushService) {
    this.pushService = pushService;
  }

}
