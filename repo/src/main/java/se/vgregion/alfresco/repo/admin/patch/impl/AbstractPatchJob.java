package se.vgregion.alfresco.repo.admin.patch.impl;

import java.util.Date;

import org.alfresco.repo.admin.patch.AppliedPatch;
import org.alfresco.repo.domain.patch.AppliedPatchDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.quartz.Job;
import org.springframework.extensions.surf.util.I18NUtil;

public abstract class AbstractPatchJob implements Job {

  protected RetryingTransactionHelper _retryingTransactionHelper;

  protected AppliedPatchDAO _appliedPatchDAO;

  protected String _patchId;

  protected String _description;

  protected DescriptorService _descriptorService;

  protected static final String MSG_NOT_RELEVANT = "patch.service.not_relevant";

  protected boolean isPatchApplied() {
    return _retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Boolean>() {

      @Override
      public Boolean execute() throws Throwable {
        final AppliedPatch patch = _appliedPatchDAO.getAppliedPatch(_patchId);

        if (patch == null) {
          return false;
        }

        return patch.getSucceeded();
      }

    });
  }

  protected void setPatchApplied() {
    _retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        AppliedPatch patch = _appliedPatchDAO.getAppliedPatch(_patchId);

        boolean create = false;

        if (patch == null) {
          patch = new AppliedPatch();

          final Descriptor repoDescriptor = _descriptorService.getInstalledRepositoryDescriptor();

          final Descriptor serverDescriptor = _descriptorService.getServerDescriptor();

          final String server = serverDescriptor.getVersion() + " - " + serverDescriptor.getEdition();

          patch.setId(_patchId);
          patch.setTargetSchema(-1);
          patch.setAppliedToSchema(repoDescriptor.getSchema());
          patch.setAppliedToServer(server);
          patch.setWasExecuted(true);
          patch.setFixesToSchema(-1);
          patch.setDescription(I18NUtil.getMessage(_description));
          patch.setReport(I18NUtil.getMessage(MSG_NOT_RELEVANT));

          create = true;
        }

        patch.setAppliedOnDate(new Date());
        patch.setSucceeded(true);

        if (create) {
          _appliedPatchDAO.createAppliedPatch(patch);
        } else {
          _appliedPatchDAO.updateAppliedPatch(patch);
        }

        return null;
      }

    });
  }

}
