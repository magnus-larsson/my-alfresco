package se.vgregion.alfresco.repo.push.impl;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;

public class PushLogger {

  private final static Logger LOG = Logger.getLogger(PushLogger.class);

  public static void logBeforePush(NodeRef nodeRef, Date now, NodeService nodeService) {
	
    if(LOG.isDebugEnabled()) {
      LOG.debug("Pushing node " + nodeRef.toString() + " at " + now + 
          "\tType: " + nodeService.getType(nodeRef) + 
          "\tHas published aspect: " + nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PUBLISHED) +
          "\tPush count: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_COUNT) +
          "\tPublish status: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUBLISH_STATUS) +
          "\tUnpublish status: " + nodeService.getProperty(nodeRef, VgrModel.PROP_UNPUBLISH_STATUS) +
          "\tDate available from: " + nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_FROM) +
          "\tDate available to: " + nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_TO) + 
          "\tPushed for publish: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_PUBLISH) + 
          "\tPushed for unpublish: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_UNPUBLISH) + 
          "\tDate Modified: " + nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
    }
  }

  public static void logAfterPush(NodeRef nodeRef, NodeService nodeService) {
    if(LOG.isDebugEnabled()) {
      LOG.debug("Pushed node " + nodeRef.toString() + 
          "\tPush count: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_COUNT) +
          "\tPublish status: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUBLISH_STATUS) +
          "\tUnpublish status: " + nodeService.getProperty(nodeRef, VgrModel.PROP_UNPUBLISH_STATUS) +
          "\tDate Modified: " + nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
    }
  }

  public static void logNodeForRepush(NodeRef nodeRef, NodeService nodeService) {
    if(LOG.isDebugEnabled()) {
      LOG.debug("Repush node " + nodeRef.toString() + 
          "\tType: " + nodeService.getType(nodeRef) + 
          "\tHas published aspect: " + nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PUBLISHED) +
          "\tPush count: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_COUNT) +
          "\tPublish status: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUBLISH_STATUS) +
          "\tUnpublish status: " + nodeService.getProperty(nodeRef, VgrModel.PROP_UNPUBLISH_STATUS) +
          "\tDate available from: " + nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_FROM) +
          "\tDate available to: " + nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_TO) + 
          "\tPushed for publish: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_PUBLISH) + 
          "\tPushed for unpublish: " + nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_UNPUBLISH) + 
          "\tDate Modified: " + nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
    }
  }	
}
