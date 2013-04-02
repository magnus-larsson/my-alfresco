package se.vgregion.alfresco.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Job that synchronises the Apelon data source.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class ApelonSynchronisationJob implements Job {

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    final List<ClusteredExecuter> synchronisations = (List<ClusteredExecuter>) context.getJobDetail().getJobDataMap().get("synchronisations");

    for (final ClusteredExecuter synchronisation : synchronisations) {
      synchronisation.execute();
    }
  }

}
