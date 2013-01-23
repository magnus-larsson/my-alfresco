package se.vgregion.alfresco.repo.storage;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import se.vgregion.alfresco.repo.admin.patch.impl.AbstractPatchJob;

public class GenerateArchivePdfsJob implements Job {

  private final static Logger LOG = Logger.getLogger(GenerateArchivePdfsJob.class);

  private StorageService _storageService;

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    _storageService = (StorageService) context.getJobDetail().getJobDataMap().get("storageService");

    int count = _storageService.createMissingPdfRenditions();

    LOG.info("Generated PDF/A for " + count + " nodes.");
  }

}
