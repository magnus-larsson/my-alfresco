package se.vgregion.alfresco.toolkit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;

public class RefreshPublishedCachesJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ClusteredExecuter refreshPublishedCaches = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("refreshPublishedCaches");

    refreshPublishedCaches.execute();
  }

}
