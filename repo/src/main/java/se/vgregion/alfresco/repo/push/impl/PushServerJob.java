package se.vgregion.alfresco.repo.push.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.push.PushService;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class PushServerJob implements Job {

  private final static Logger LOG = Logger.getLogger(PushServerJob.class);

  private SearchService _searchService;

  private PushService _pushService;

  private NodeService _nodeService;

  private BehaviourFilter _behaviourFilter;

  private RetryingTransactionHelper _retryingTransactionHelper;

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    _pushService = (PushService) context.getJobDetail().getJobDataMap().get("pushService");
    _searchService = (SearchService) context.getJobDetail().getJobDataMap().get("searchService");
    _nodeService = (NodeService) context.getJobDetail().getJobDataMap().get("nodeService");
    _behaviourFilter = (BehaviourFilter) context.getJobDetail().getJobDataMap().get("behaviourFilter");
    _retryingTransactionHelper = (RetryingTransactionHelper) context.getJobDetail().getJobDataMap().get("retryingTransactionHelper");

    execute();
  }

  private void execute() {
    // first all documents must have a current "cm:modified" flag
    executeUpdate();

    // after that, push the files to the push server
    executePush();
  }

  private void executeUpdate() {
    final RetryingTransactionHelper.RetryingTransactionCallback<Void> execution = new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        // disable all behaviours, we can't have the modified date updated for
        // this...
        _behaviourFilter.disableAllBehaviours();

        final String now = formatNow();

        // set the "cm:modified" property for all published documents
        setUpdatedProperty(findPublishedDocuments(now));

        // set the "cm:modified" property for all unpublished documents
        setUpdatedProperty(findUnpublishedDocuments(now));

        return null;
      }
    };

    _retryingTransactionHelper.doInTransaction(execution);
  }

  private void executePush() {
    final RetryingTransactionHelper.RetryingTransactionCallback<Void> execution = new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        // disable all behaviours, we can't have the modified date updated for
        // this...
        _behaviourFilter.disableAllBehaviours();

        final String now = formatNow();

        // send the published documents to the push server
        sendToPush(findPublishedDocuments(now), VgrModel.PROP_PUSHED_FOR_PUBLISH);

        // send the unpublished documents to the push server
        sendToPush(findUnpublishedDocuments(now), VgrModel.PROP_PUSHED_FOR_UNPUBLISH);

        return null;
      }

    };

    _retryingTransactionHelper.doInTransaction(execution);
  }

  private String formatNow() {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    return sdf.format(new Date());
  }

  private Date parseNow(final String now) {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    try {
      return sdf.parse(now);
    } catch (final ParseException ex) {
      throw new RuntimeException(ex);
    }
  }

  protected void setUpdatedProperty(final List<NodeRef> nodeRefs) {
    for (final NodeRef nodeRef : nodeRefs) {
      AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

        @Override
        public Void doWork() throws Exception {
          _nodeService.setProperty(nodeRef, ContentModel.PROP_MODIFIED, new Date());

          return null;
        }

      }, AuthenticationUtil.getSystemUserName());
    }
  }

  private void sendToPush(final List<NodeRef> nodeRefs, final QName property) {
    final boolean pushed = _pushService.pushFiles(nodeRefs);

    if (!pushed) {
      if (LOG.isDebugEnabled()) {

        for (final NodeRef nodeRef : nodeRefs) {
          LOG.debug("For some reason the node ref '" + nodeRef + "' could not be pushed.");
        }
      }

      return;
    }

    for (final NodeRef nodeRef : nodeRefs) {
      AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

        @Override
        public Void doWork() throws Exception {
          _nodeService.setProperty(nodeRef, property, new Date());

          return null;
        }

      }, AuthenticationUtil.getSystemUserName());

      if (LOG.isDebugEnabled()) {
        LOG.debug("NodeRef pushed to '" + property + "': " + nodeRef);
      }
    }
  }

  protected List<NodeRef> findUnpublishedDocuments(final String now) {
    final ResultSet result = findDocuments(findUnpublishedDocumentsQuery(now));

    final Date nw = parseNow(now);

    final List<NodeRef> nodeRefs = new ArrayList<NodeRef>();

    try {
      for (final NodeRef nodeRef : result.getNodeRefs()) {

        final Date availableto = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Date>() {

          @Override
          public Date doWork() throws Exception {
            return (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_TO);
          }

        }, AuthenticationUtil.getSystemUserName());

        if (availableto == null) {
          continue;
        }

        if (nw.getTime() >= availableto.getTime()) {
          nodeRefs.add(nodeRef);
        }
      }
    } finally {
      ServiceUtils.closeQuietly(result);
    }

    return nodeRefs;
  }

  protected List<NodeRef> findPublishedDocuments(final String now) {
    final ResultSet result = findDocuments(findPublishedDocumentsQuery(now));

    try {
      return result.getNodeRefs();
    } finally {
      ServiceUtils.closeQuietly(result);
    }
  }

  private StringBuffer findPublishedDocumentsQuery(final String now) {
    final StringBuffer query = new StringBuffer();

    query.append("TYPE:\"vgr:document\" AND ");
    query.append("ASPECT:\"vgr:published\" AND ");
    query.append("@vgr\\:dc.date.availablefrom:[MIN TO " + now + "] AND ");
    query.append("(ISNULL:vgr\\:dc.date.availableto OR @vgr\\:dc.date.availableto:[" + now + " TO MAX]) AND ");
    query.append("ISNULL:vgr\\:pushed-for-publish");

    if (LOG.isDebugEnabled()) {
      LOG.debug("Query for finding unpushed published documents: " + query.toString());
    }

    return query;
  }

  private StringBuffer findUnpublishedDocumentsQuery(final String now) {
    final StringBuffer query = new StringBuffer();

    query.append("TYPE:\"vgr:document\" AND ");
    query.append("ASPECT:\"vgr:published\" AND ");
    query.append("ISNOTNULL:vgr\\:dc.date.availableto AND ");
    query.append("@vgr\\:dc.date.availableto:[MIN TO " + now + "] AND ");
    query.append("ISNOTNULL:vgr\\:pushed-for-publish AND ");
    query.append("ISNULL:vgr\\:pushed-for-unpublish");

    if (LOG.isDebugEnabled()) {
      LOG.debug("Query for finding unpushed unpublished documents: " + query.toString());
    }

    return query;
  }

  private ResultSet findDocuments(final StringBuffer query) {
    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query.toString());

    return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<ResultSet>() {

      @Override
      public ResultSet doWork() throws Exception {
        final ResultSet result = _searchService.query(searchParameters);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Documents found for query: " + query.toString());
          LOG.debug("Count: " + result.length());
          LOG.debug("");
        }

        return result;
      }

    }, AuthenticationUtil.getSystemUserName());
  }

}
