package se.vgregion.alfresco.repo.storage;

import org.apache.log4j.Logger;
import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;

public class GenerateArchivePdfs extends ClusteredExecuter {

  private final static Logger LOG = Logger.getLogger(GenerateArchivePdfs.class);

  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

  private StorageService _storageService;

  @Override
  protected String getJobName() {
    return "Generate archive pdfs";
  }

  @Override
  protected void executeInternal() {
    int count = _storageService.createMissingPdfRenditions(new CreationCallback() {

      @Override
      public void execute() {
        refreshLock();
      }

    });

    LOG.info("Generated PDF/A for " + count + " nodes.");
  }

}
