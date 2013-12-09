package se.vgregion.alfresco.repo.admin.patch.impl;

import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.patch.AppliedPatchDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class FixDcSourceAndIdentifierJob extends AbstractPatchJob {

  private final static Logger LOG = Logger.getLogger(FixDcSourceAndIdentifierJob.class);

  private SearchService _searchService;

  private NodeService _nodeService;

  private BehaviourFilter _behaviourFilter;

  private ServiceUtilsImpl _serviceUtils;

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
    _serviceUtils = (ServiceUtilsImpl) context.getJobDetail().getJobDataMap().get("serviceUtils");

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

    LOG.info("vgr:dc.source and vgr:dc.identifier changed for " + count + " nodes.");
  }

  private int execute() {
    final String query = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\"";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    final ResultSet documents = _searchService.query(searchParameters);

    LOG.info("Documents to patch vgr:dc.source and vgr:dc.identifier for: " + documents.length());

    int count = 0;

    try {
      for (final ResultSetRow document : documents) {
        count = changeNodeDcSourceAndIdentifier(document) ? count + 1 : count;
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(documents);
    }

    return count;
  }

  private boolean changeNodeDcSourceAndIdentifier(final ResultSetRow node) {
    final RetryingTransactionHelper.RetryingTransactionCallback<Boolean> execution = new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {

      @Override
      public Boolean execute() throws Throwable {
        _behaviourFilter.disableBehaviour();

        final NodeRef nodeRef = node.getNodeRef();

        if (!_nodeService.exists(nodeRef)) {
          return false;
        }

        final String identifier = _serviceUtils.getDocumentIdentifier(nodeRef);

        final boolean applied1 = applyValueOnDocument(identifier, nodeRef, VgrModel.PROP_IDENTIFIER);
        boolean applied2 = false;
        boolean applied3 = false;

        // set the pushed-for-publish to null in order to trigger a new publish
        // _nodeService.setProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_PUBLISH,
        // null);

        // set the source for the associated record in Share, if any
        final NodeRef originalDocument = findOriginalDocument(nodeRef);

        if (originalDocument != null) {
          applied3 = applyValueOnDocument(identifier, originalDocument, VgrModel.PROP_IDENTIFIER);

          // the source is always from the document in "Share"
          final String source = _serviceUtils.getDocumentSource(originalDocument);

          applied2 = applyValueOnDocument(source, nodeRef, VgrModel.PROP_SOURCE);
        } else {
          LOG.warn("No original document found!");
        }

        applyDateOnDocumentIfChanged(nodeRef, applied1, applied2);

        if (applied1 || applied2 || applied3) {
          LOG.info("Patched vgr:dc.source and vgr:dc.identifer for: " + nodeRef);

          return true;
        }

        return false;
      }

      private void applyDateOnDocumentIfChanged(final NodeRef nodeRef, final boolean applied1, final boolean applied2) {
        if (applied1 || applied2) {
          // set the modified property and the date saved
          final Date now = new Date();
          _nodeService.setProperty(nodeRef, ContentModel.PROP_MODIFIED, now);
          _nodeService.setProperty(nodeRef, VgrModel.PROP_DATE_SAVED, now);
        }
      }

      private boolean applyValueOnDocument(final String value, final NodeRef nodeRef, final QName property) {
        final String oldValue = (String) _nodeService.getProperty(nodeRef, property);

        if (StringUtils.equalsIgnoreCase(value, oldValue)) {
          return false;
        }

        // set the identifier and source for the published document
        _nodeService.setProperty(nodeRef, property, value);

        return true;
      }

      private NodeRef findOriginalDocument(final NodeRef nodeRef) {
        final List<AssociationRef> nodes = _nodeService.getSourceAssocs(nodeRef, new QNamePattern() {

          @Override
          public boolean isMatch(final QName qname) {
            return qname.isMatch(VgrModel.ASSOC_PUBLISHED_TO_STORAGE);
          }

        });

        NodeRef originalDocument = nodes.size() > 0 ? nodes.get(0).getSourceRef() : null;

        if (originalDocument == null) {
          final String sourceId = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);

          originalDocument = new NodeRef(sourceId);
        }

        return _nodeService.exists(originalDocument) ? originalDocument : null;
      }

    };

    return _retryingTransactionHelper.doInTransaction(execution);
  }
}
