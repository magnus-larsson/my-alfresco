package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class FixMissingIdValuesJob extends AbstractPatchJob {

  private static final Logger LOG = Logger.getLogger(FixMissingIdValuesJob.class);

  private static final Pattern parenthesisPattern = Pattern.compile(".+\\(([^\\(]*)\\).*");

  public SearchService _searchService;

  public NodeService _nodeService;

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

    execute();

    setPatchApplied();
  }

  private void execute() {
    final int count = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Integer>() {

      @Override
      public Integer doWork() throws Exception {
        int total = 0;

        total += new PatchContributorSavedby().patch();
        total += new PatchCreator().patch();
        total += new PatchCreatorDocument().patch();
        total += new PatchContributorAcceptedby().patch();
        total += new PatchContributorControlledby().patch();
        total += new PatchPublisher().patch();

        return total;
      }

    }, AuthenticationUtil.getSystemUserName());

    LOG.info("Successfully patched the ID value for " + count + " nodes.");
  }

  abstract class AbstractPatchIdValue implements PatchIdValue {

    ResultSet findDocuments() {
      final String query = getQuery();

      final SearchParameters searchParameters = new SearchParameters();

      searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
      searchParameters.setQuery(query);
      searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
      searchParameters.setMaxItems(-1);

      return _searchService.query(searchParameters);
    }

    @Override
    public int patch() {
      final ResultSet documents = findDocuments();

      LOG.info("Documents to patch " + getPatchedFieldname() + " for: " + documents.length());

      int count = 0;

      try {
        for (final ResultSetRow document : documents) {
          count = changeNodeId(document) ? count + 1 : count;
        }
      } finally {
        ServiceUtils.closeQuietly(documents);
      }

      return count;
    }

    private boolean changeNodeId(final ResultSetRow document) {
      final RetryingTransactionHelper.RetryingTransactionCallback<Boolean> execution = new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {

        @Override
        public Boolean execute() throws Throwable {
          _behaviourFilter.disableAllBehaviours();

          final NodeRef nodeRef = document.getNodeRef();

          final Serializable value = document.getValue(getPatchValueQName());

          if (value == null || StringUtils.isBlank(value.toString())) {
            return false;
          }

          String parsedValue = value.toString();

          final String valueAfterPipe = getValueAfterPipe(parsedValue);

          parsedValue = getValueBeforePipe(parsedValue);

          if (!parsedValue.equalsIgnoreCase(valueAfterPipe)) {
            _nodeService.setProperty(nodeRef, getPatchValueQName(), valueAfterPipe);
          }

          parsedValue = getParenthesisValue(parsedValue);

          _nodeService.setProperty(nodeRef, getPatchedQName(), parsedValue);

          LOG.info("Patched " + getPatchedFieldname() + " with '" + parsedValue + "' parsed from '" + value + "'");

          return true;
        }
      };

      return _retryingTransactionHelper.doInTransaction(execution);
    }

    String getParenthesisValue(final String value) {
      final Matcher matcher = parenthesisPattern.matcher(value);

      final boolean match = matcher.matches();

      if (!match) {
        return value;
      }

      return matcher.group(1);
    }

    String getValueBeforePipe(final String value) {
      final int index = value.indexOf("|");

      if (index >= 0) {
        return value.substring(0, index);
      }

      return value;
    }

    String getValueAfterPipe(final String value) {
      final int index = value.indexOf("|");

      if (index >= 0) {
        return value.substring(index + 1, value.length());
      }

      return value;
    }

  }

  interface PatchIdValue {

    String getQuery();

    QName getPatchedQName();

    QName getPatchValueQName();

    String getPatchedFieldname();

    int patch();

  }

  class PatchCreator extends AbstractPatchIdValue {

    @Override
    public String getPatchedFieldname() {
      return "vgr:dc.creator.id";
    }

    @Override
    public String getQuery() {
      return "TYPE:\"vgr:document\" AND ISNULL:\"vgr:dc.creator.id\" AND ISNOTNULL:\"vgr:dc.creator\"";
    }

    @Override
    public QName getPatchValueQName() {
      return VgrModel.PROP_CREATOR;
    }

    @Override
    public QName getPatchedQName() {
      return VgrModel.PROP_CREATOR_ID;
    }

  }

  class PatchPublisher extends AbstractPatchIdValue {

    @Override
    public String getPatchedFieldname() {
      return "vgr:dc.publisher.id";
    }

    @Override
    public String getQuery() {
      return "TYPE:\"vgr:document\" AND ISNULL:\"vgr:dc.publisher.id\" AND ISNOTNULL:\"vgr:dc.publisher\"";
    }

    @Override
    public QName getPatchValueQName() {
      return VgrModel.PROP_PUBLISHER;
    }

    @Override
    public QName getPatchedQName() {
      return VgrModel.PROP_PUBLISHER_ID;
    }

  }

  class PatchCreatorDocument extends AbstractPatchIdValue {

    @Override
    public String getPatchedFieldname() {
      return "vgr:dc.creator.document.id";
    }

    @Override
    public String getQuery() {
      return "TYPE:\"vgr:document\" AND ISNULL:\"vgr:dc.creator.document.id\" AND ISNOTNULL:\"vgr:dc.creator.document\"";
    }

    @Override
    public QName getPatchValueQName() {
      return VgrModel.PROP_CREATOR_DOCUMENT;
    }

    @Override
    public QName getPatchedQName() {
      return VgrModel.PROP_CREATOR_DOCUMENT_ID;
    }

  }

  class PatchContributorAcceptedby extends AbstractPatchIdValue {

    @Override
    public String getPatchedFieldname() {
      return "vgr:dc.contributor.acceptedby.id";
    }

    @Override
    public String getQuery() {
      return "TYPE:\"vgr:document\" AND ISNULL:\"vgr:dc.contributor.acceptedby.id\" AND ISNOTNULL:\"vgr:dc.contributor.acceptedby\"";
    }

    @Override
    public QName getPatchValueQName() {
      return VgrModel.PROP_CONTRIBUTOR_ACCEPTEDBY;
    }

    @Override
    public QName getPatchedQName() {
      return VgrModel.PROP_CONTRIBUTOR_ACCEPTEDBY_ID;
    }

  }

  class PatchContributorControlledby extends AbstractPatchIdValue {

    @Override
    public String getPatchedFieldname() {
      return "vgr:dc.contributor.controlledby.id";
    }

    @Override
    public String getQuery() {
      return "TYPE:\"vgr:document\" AND ISNULL:\"vgr:dc.contributor.controlledby.id\" AND ISNOTNULL:\"vgr:dc.contributor.controlledby\"";
    }

    @Override
    public QName getPatchValueQName() {
      return VgrModel.PROP_CONTRIBUTOR_CONTROLLEDBY;
    }

    @Override
    public QName getPatchedQName() {
      return VgrModel.PROP_CONTRIBUTOR_CONTROLLEDBY_ID;
    }

  }

  class PatchContributorSavedby extends AbstractPatchIdValue {

    @Override
    public String getPatchedFieldname() {
      return "vgr:dc.contributor.savedby.id";
    }

    @Override
    public String getQuery() {
      return "TYPE:\"vgr:document\" AND ISNULL:\"vgr:dc.contributor.savedby.id\" AND ISNOTNULL:\"vgr:dc.contributor.savedby\"";
    }

    @Override
    public QName getPatchValueQName() {
      return VgrModel.PROP_CONTRIBUTOR_SAVEDBY;
    }

    @Override
    public QName getPatchedQName() {
      return VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID;
    }

  }

}
