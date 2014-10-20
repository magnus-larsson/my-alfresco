package se.vgregion.alfresco.repo.push;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.publish.NodeRefCallbackHandler;
import se.vgregion.alfresco.repo.publish.PublishingService;
import se.vgregion.alfresco.repo.push.impl.PushLogger;

public class PushToPubSubHubBubServer extends ClusteredExecuter {

  private static final Logger LOG = Logger.getLogger(PushToPubSubHubBubServer.class);

  private NodeService _nodeService;

  private BehaviourFilter _behaviourFilter;

  private PushJmsService _pushJmsService;

  private PublishingService _publishingService;

  @Override
  protected String getJobName() {
    return "Push to pubsubhubbub server";
  }

  @Override
  protected void executeInternal() {
    final Date now = new Date();

    // Send to JMS
    final RetryingTransactionCallback<Void> executionJms = new RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        handlePublishedDocuments(now);

        handleUnpublishedDocuments(now);

        return null;
      }
    };

    AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        return _transactionService.getRetryingTransactionHelper().doInTransaction(executionJms, false, true);
      }

    });
  }

  private void handleUnpublishedDocuments(final Date now) {
    _publishingService.findUnpublishedDocuments(now, null, null, new NodeRefCallbackHandler() {

      @Override
      public void processNodeRef(NodeRef nodeRef) {
        refreshLock();

        PushLogger.logBeforePush(nodeRef, now, _nodeService);
        
        executeUpdate(nodeRef, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
        
        PushLogger.logAfterPush(nodeRef, _nodeService);

        _pushJmsService.pushToJms(nodeRef, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
      }

    }, true, null, null);
  }

  private void handlePublishedDocuments(final Date now) {
    _publishingService.findPublishedDocuments(now, null, null, new NodeRefCallbackHandler() {

      @Override
      public void processNodeRef(NodeRef nodeRef) {
        refreshLock();

        PushLogger.logBeforePush(nodeRef, now, _nodeService);
        
        executeUpdate(nodeRef, VgrModel.PROP_PUSHED_FOR_PUBLISH);
        
        PushLogger.logAfterPush(nodeRef, _nodeService);

        _pushJmsService.pushToJms(nodeRef, VgrModel.PROP_PUSHED_FOR_PUBLISH);

      }

    }, true, null, null);
  }

  private void executeUpdate(NodeRef nodeRef, QName property) {
    // disable all behaviours, we can't have the modified date updated for
    // this...
    _behaviourFilter.disableBehaviour();

    
    try {
      	
      setUpdatedProperty(nodeRef);

      setPublishStatusProperties(nodeRef, property);

      increasePushedCount(nodeRef);
      
    } finally {
      _behaviourFilter.enableBehaviour();
    }
  }

  protected void increasePushedCount(NodeRef nodeRef) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Increasing pushed count for '" + nodeRef + "'");
    }

    Integer pushedCount = (Integer) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_COUNT);

    if (pushedCount == null) {
      pushedCount = 0;
    }

    pushedCount++;

    _nodeService.setProperty(nodeRef, VgrModel.PROP_PUSHED_COUNT, pushedCount);
  }

  protected void setPublishStatusProperties(NodeRef nodeRef, QName property) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Setting published status properties for nodeRef '" + nodeRef + "' and property '" + property + "'");
    }

    _nodeService.setProperty(nodeRef, property, new Date());

    if (VgrModel.PROP_PUSHED_FOR_PUBLISH.equals(property)) {
      _nodeService.setProperty(nodeRef, VgrModel.PROP_PUBLISH_STATUS, null);
    } else {
      _nodeService.setProperty(nodeRef, VgrModel.PROP_UNPUBLISH_STATUS, null);
    }
  }

  protected void setUpdatedProperty(NodeRef nodeRef) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Setting cm:modified to now for '" + nodeRef + "'");
    }

    _nodeService.setProperty(nodeRef, ContentModel.PROP_MODIFIED, new Date());
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void setPushJmsService(PushJmsService pushJmsService) {
    _pushJmsService = pushJmsService;
  }

  public void setPublishingService(PublishingService publishingService) {
    _publishingService = publishingService;
  }

}
