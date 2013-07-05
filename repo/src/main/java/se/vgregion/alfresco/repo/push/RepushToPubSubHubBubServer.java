package se.vgregion.alfresco.repo.push;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;
import se.vgregion.alfresco.repo.model.VgrModel;

public class RepushToPubSubHubBubServer extends ClusteredExecuter {

  private static final Logger LOG = Logger.getLogger(RepushToPubSubHubBubServer.class);

  private PushService _pushService;

  private NodeService _nodeService;

  private BehaviourFilter _behaviourFilter;

  private int _maxRepushCount = 10;

  /**
   * The minimum time (in minutes) that must have passed for a document before a re-push is tried. Default time is set to 20 minutes.
   */
  private int _minimumPushAge = 20;

  @Override
  protected String getJobName() {
    return "RepushToPubSubHubBubServer";
  }

  @Override
  protected void executeInternal() {
    RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        return AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

          @Override
          public Void doWork() throws Exception {
            doExecute();

            return null;
          }
        });
      }
    };

    _transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
  }

  protected void doExecute() {
    refreshLock();

    List<NodeRef> pushed = new ArrayList<NodeRef>();

    pushed.addAll(_pushService.findErroneousPushedFiles(_maxRepushCount, _minimumPushAge));

    if (LOG.isDebugEnabled()) {
      LOG.debug("Found '" + pushed.size() + "' documents eligable for re-push.");
    }

    for (NodeRef nodeRef : pushed) {
      refreshLock();

      if (LOG.isDebugEnabled()) {
        LOG.debug("Adding '" + nodeRef + "' to be re-pushed...");
      }

      _behaviourFilter.disableBehaviour();
      try {
        // null the pushed for publish/unpublish properties to force a re-push
        _nodeService.setProperty(nodeRef, ContentModel.PROP_MODIFIED, new Date());
        _nodeService.setProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_PUBLISH, null);
        _nodeService.setProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_UNPUBLISH, null);
      } finally {
        _behaviourFilter.enableBehaviour();
      }
    }
  }

  public void setPushService(PushService pushService) {
    _pushService = pushService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void setMaxRepushCount(int maxRepushCount) {
    _maxRepushCount = maxRepushCount;
  }

  public void setMinimumPushAge(int minimumPushAge) {
    _minimumPushAge = minimumPushAge;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    ParameterCheck.mandatory("pushService", _pushService);
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("behaviourFilter", _behaviourFilter);
  }

}
