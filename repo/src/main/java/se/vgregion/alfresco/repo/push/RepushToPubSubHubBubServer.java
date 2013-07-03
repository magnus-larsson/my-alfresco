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

import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;
import se.vgregion.alfresco.repo.model.VgrModel;

public class RepushToPubSubHubBubServer extends ClusteredExecuter {

  private PushService _pushService;

  private NodeService _nodeService;

  private BehaviourFilter _behaviourFilter;

  private int _maxRepushCount = 10;

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

    Date start = new Date(1);
    Date end = new Date();

    List<NodeRef> pushed = new ArrayList<NodeRef>();

    pushed.addAll(_pushService.findPushedFiles("ERROR", null, start, end));
    pushed.addAll(_pushService.findPushedFiles(null, "ERROR", start, end));
    pushed.addAll(_pushService.findPushedFiles("ERROR", "ERROR", start, end));
    pushed.addAll(_pushService.findPushedFiles("ERROR", "", start, end));
    pushed.addAll(_pushService.findPushedFiles("", "ERROR", start, end));

    for (NodeRef nodeRef : pushed) {
      refreshLock();

      Integer count = (Integer) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_COUNT);

      System.out.println(count);

      if (count == null) {
        count = 1;
      }

      // there's a limit of the number of times a document can be re-pushed.
      if (count >= _maxRepushCount) {
        continue;
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

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    ParameterCheck.mandatory("pushService", _pushService);
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("behaviourFilter", _behaviourFilter);
  }

}
