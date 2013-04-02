package se.vgregion.alfresco.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class PushServerJob implements Job {

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    ClusteredExecuter pushToPubSubHubBubServer = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("pushToPubSubHubBubServer");

    pushToPubSubHubBubServer.execute();
  }

}
