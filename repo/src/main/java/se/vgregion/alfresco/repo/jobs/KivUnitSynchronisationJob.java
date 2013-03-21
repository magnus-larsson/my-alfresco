package se.vgregion.alfresco.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job that synchronises the KIV units.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class KivUnitSynchronisationJob implements Job {

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    ClusteredExecuter kivUnitSynchronisation = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("kivUnitSynchronisation");

    kivUnitSynchronisation.execute();
  }
}
