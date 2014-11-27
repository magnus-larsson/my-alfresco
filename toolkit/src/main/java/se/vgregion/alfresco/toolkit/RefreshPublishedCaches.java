package se.vgregion.alfresco.toolkit;

import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;

@Component("vgr.refreshPublishedCaches")
public class RefreshPublishedCaches extends ClusteredExecuter {

  @Autowired
  private IndexCacheService _indexCacheService;
  
  @Override
  protected String getJobName() {
    return "Refresh Published Caches";
  }
  
  @Override
  protected void executeInternal() {
    AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        RetryingTransactionHelper transactionHelper = _transactionService.getRetryingTransactionHelper();

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {

          @Override
          public Void execute() throws Throwable {
            _indexCacheService.cachePublishedNodes();

            _indexCacheService.cacheOrphans();

            return null;
          }

        }, false, true);

        return null;
      }
    });
  }

  @Override
  @Autowired
  public void setJobLockService(JobLockService jobLockService) {
    super.setJobLockService(jobLockService);
  }

  @Override
  @Autowired
  public void setTransactionService(TransactionService transactionService) {
    super.setTransactionService(transactionService);
  }
  
  @Override
  @Autowired
  public void setRepositoryState(RepositoryState repositoryState) {
    super.setRepositoryState(repositoryState);
  }

}
