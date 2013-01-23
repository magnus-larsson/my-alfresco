package se.vgregion.alfresco.repo.constraints.sync;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job that synchronises the KIV units.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class KivUnitSynchronisationJob implements Job {

  private final static Logger LOG = Logger.getLogger(KivUnitSynchronisationJob.class);

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Starting job to synchronise KIV units.");
    }

    try {
      final KivUnitSynchronisation kivUnitSynchronisation = (KivUnitSynchronisation) context.getJobDetail()
          .getJobDataMap().get("kivUnitSynchronisation");

      kivUnitSynchronisation.synchronise();

      if (LOG.isDebugEnabled()) {
        LOG.debug("Job to synchronise KIV units finished.");
      }
    } catch (final Exception ex) {
      LOG.error(ex.getMessage(), ex);
    }
  }

}
