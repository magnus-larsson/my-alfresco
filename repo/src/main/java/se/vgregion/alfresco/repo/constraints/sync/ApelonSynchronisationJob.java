package se.vgregion.alfresco.repo.constraints.sync;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job that synchronises the Apelon data source.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class ApelonSynchronisationJob implements Job {

  private final static Logger LOG = Logger.getLogger(ApelonSynchronisationJob.class);

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Starting job to synchronise Apelon data source.");
    }

    try {
      @SuppressWarnings("unchecked")
      final List<ApelonSynchronisation> synchronisations = (List<ApelonSynchronisation>) context.getJobDetail()
          .getJobDataMap().get("synchronisations");

      for (final ApelonSynchronisation synchronisation : synchronisations) {
        synchronisation.synchronise();
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("Job to synchronise Apelon data source finished.");
      }
    } catch (final Exception ex) {
      LOG.error(ex.getMessage(), ex);
    }
  }

}
