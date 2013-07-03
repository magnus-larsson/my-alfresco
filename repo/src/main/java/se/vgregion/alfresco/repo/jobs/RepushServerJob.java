package se.vgregion.alfresco.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RepushServerJob implements Job {

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    ClusteredExecuter repushToPubSubHubBubServer = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("repushToPubSubHubBubServer");

    repushToPubSubHubBubServer.execute();
  }

}
