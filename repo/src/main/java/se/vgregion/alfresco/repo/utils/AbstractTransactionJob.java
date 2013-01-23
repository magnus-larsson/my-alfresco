package se.vgregion.alfresco.repo.utils;

import java.util.concurrent.ExecutionException;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.transaction.TransactionService;

public abstract class AbstractTransactionJob {

  private String _runAsUser;
  private TransactionService _transactionService;

  public void execute() {

    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
      public Object doWork() {
        try {
          doExecute();
        } catch (ExecutionException ex) {
          throw new RuntimeException(ex);
        }
        
        return null;
      }
    }, getRunAsUser());
  }

  public String getRunAsUser() {
    return _runAsUser;
  }

  public void setRunAsUser(String runAsUser) {
    _runAsUser = runAsUser;
  }

  protected abstract void doExecute() throws ExecutionException;

  public TransactionService getTransactionService() {
    return _transactionService;
  }

  public void setTransactionService(TransactionService transactionService) {
    _transactionService = transactionService;
  }

}