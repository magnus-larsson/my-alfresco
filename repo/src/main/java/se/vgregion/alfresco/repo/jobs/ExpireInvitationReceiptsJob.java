package se.vgregion.alfresco.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ExpireInvitationReceiptsJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ClusteredExecuter expireInvitationReceipts = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("expireInvitationReceipts");

    expireInvitationReceipts.execute();
  }

}
