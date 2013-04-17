package se.vgregion.alfresco.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ValidityCheckerJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ClusteredExecuter validityChecker = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("validityChecker");

    validityChecker.execute();
  }

}
