package se.vgregion.alfresco.repo.jobs;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

public class ExpireInvitationReceipts extends ClusteredExecuter {

  private static final Logger LOG = Logger.getLogger(ExpireInvitationReceipts.class);

  protected WorkflowService workflowService;

  public void setWorkflowService(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  protected String getJobName() {
    return "Expire Invitation Receipts";
  }

  @Override
  protected void executeInternal() {
    AuthenticationUtil.runAs(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
        taskQuery.setActive(null);
        taskQuery.setWorkflowDefinitionName(WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI);
        taskQuery.setTaskName(WorkflowModelNominatedInvitation.WF_TASK_ACCEPT_INVITE);
        taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);

        List<WorkflowTask> queryTasks = new ArrayList<WorkflowTask>();

        queryTasks.addAll(workflowService.queryTasks(taskQuery, true));
        taskQuery.setTaskName(WorkflowModelNominatedInvitation.WF_TASK_REJECT_INVITE);
        queryTasks.addAll(workflowService.queryTasks(taskQuery, false));
        for (WorkflowTask task : queryTasks) {
          if (LOG.isDebugEnabled()) {
            LOG.info("Ending stale invitation receipt " + task.getPath().getInstance().getId() + ": " + task.getId());
          }
          workflowService.endTask(task.getId(), null);
        }
        return null;
      }
    }, AuthenticationUtil.SYSTEM_USER_NAME);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    Assert.notNull(workflowService);
    LOG.info("Initialized " + ExpireInvitationReceipts.class.getName());
  }

}
