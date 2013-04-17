package se.vgregion.alfresco.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AutoDeleteJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ClusteredExecuter autoDeleter = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("autoDeleter");

    autoDeleter.execute();
  }

}
