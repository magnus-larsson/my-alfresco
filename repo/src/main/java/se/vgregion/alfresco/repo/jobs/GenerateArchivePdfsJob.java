package se.vgregion.alfresco.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class GenerateArchivePdfsJob implements Job {

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    ClusteredExecuter generateArchivePdfs = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("generateArchivePdfs");

    generateArchivePdfs.execute();
  }

}
