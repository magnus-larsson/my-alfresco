package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;

import org.alfresco.repo.domain.patch.AppliedPatchDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class FixVgrStatusJob extends AbstractPatchJob {

  private final static Logger LOG = Logger.getLogger(FixVgrStatusJob.class);

  private SearchService _searchService;

  private NodeService _nodeService;

  private BehaviourFilter _behaviourFilter;

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    _searchService = (SearchService) context.getJobDetail().getJobDataMap().get("searchService");
    _nodeService = (NodeService) context.getJobDetail().getJobDataMap().get("nodeService");
    _retryingTransactionHelper = (RetryingTransactionHelper) context.getJobDetail().getJobDataMap().get("retryingTransactionHelper");
    _behaviourFilter = (BehaviourFilter) context.getJobDetail().getJobDataMap().get("behaviourFilter");
    _appliedPatchDAO = (AppliedPatchDAO) context.getJobDetail().getJobDataMap().get("appliedPatchDAO");
    _patchId = (String) context.getJobDetail().getJobDataMap().get("patchId");
    _description = (String) context.getJobDetail().getJobDataMap().get("description");
    _descriptorService = (DescriptorService) context.getJobDetail().getJobDataMap().get("descriptorService");

    if (isPatchApplied()) {
      return;
    }

    applyPatch();

    setPatchApplied();
  }

  private void applyPatch() {
    final int count = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Integer>() {

      @Override
      public Integer doWork() throws Exception {
        return execute();
      }

    }, AuthenticationUtil.getSystemUserName());

    LOG.info("Status changed for " + count + " nodes.");
  }

  private int execute() {
    final String query = "TYPE:\"vgr:document\" AND ISNULL:\"vgr:vgr.status.document\" AND ISNOTNULL:\"vgr:hc.status.document\"";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    final ResultSet documents = _searchService.query(searchParameters);

    LOG.info("Documents to patch vgr.status.document for: " + documents.length());

    int count = 0;

    try {
      for (final ResultSetRow document : documents) {
        count = changeNodeStatus(document) ? count + 1 : count;
      }
    } finally {
      ServiceUtils.closeQuietly(documents);
    }

    return count;
  }

  private boolean changeNodeStatus(final ResultSetRow node) {
    final RetryingTransactionHelper.RetryingTransactionCallback<Boolean> execution = new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {

      @Override
      public Boolean execute() throws Throwable {
        _behaviourFilter.disableAllBehaviours();

        final NodeRef nodeRef = node.getNodeRef();

        if (!_nodeService.exists(nodeRef)) {
          return false;
        }

        final Serializable status = node.getValue(QName.createQName(VgrModel.VGR_URI, "hc.status.document"));

        if (status == null || StringUtils.isBlank(status.toString())) {
          return false;
        }

        final Serializable newStatus = node.getValue(VgrModel.PROP_STATUS_DOCUMENT);

        if (newStatus != null && StringUtils.isNotBlank(newStatus.toString())) {
          return false;
        }

        _nodeService.setProperty(nodeRef, VgrModel.PROP_STATUS_DOCUMENT, status);

        LOG.info("Patched status for: " + nodeRef);

        return true;
      }

    };

    return _retryingTransactionHelper.doInTransaction(execution);
  }

}
