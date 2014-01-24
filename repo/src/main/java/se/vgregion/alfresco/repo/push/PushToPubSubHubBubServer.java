package se.vgregion.alfresco.repo.push;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class PushToPubSubHubBubServer extends ClusteredExecuter {

  private static Logger LOG = Logger.getLogger(PushToPubSubHubBubServer.class);

  private SearchService _searchService;

  private PushService _pushService;

  private NodeService _nodeService;

  private BehaviourFilter _behaviourFilter;

  private PushJmsService _pushJmsService;

  private boolean _test = false;

  public void setPushService(PushService pushService) {
    _pushService = pushService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  public void setTest(boolean test) {
    _test = test;
  }

  public void setPushJmsService(PushJmsService _pushJmsService) {
    this._pushJmsService = _pushJmsService;
  }

  @Override
  protected String getJobName() {
    return "Push to pubsubhubbub server";
  }

  @Override
  protected void executeInternal() {
    final String now = formatNow();

    // Get the actual nodes we want to push
    final RetryingTransactionCallback<List<NodeRef>> executionSelectPublished = new RetryingTransactionCallback<List<NodeRef>>() {
      @Override
      public List<NodeRef> execute() throws Throwable {
        return findPublishedDocuments(now);
      }
    };

    final RetryingTransactionCallback<List<NodeRef>> executionSelectUnpublished = new RetryingTransactionCallback<List<NodeRef>>() {
      @Override
      public List<NodeRef> execute() throws Throwable {
        return findUnpublishedDocuments(now);
      }
    };

    final List<NodeRef> publishedDocuments = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<NodeRef>>() {
      @Override
      public List<NodeRef> doWork() throws Exception {
        return _transactionService.getRetryingTransactionHelper().doInTransaction(executionSelectPublished, true, false);
      }
    }, AuthenticationUtil.getSystemUserName());

    final List<NodeRef> unpublishedDocuments = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<NodeRef>>() {
      @Override
      public List<NodeRef> doWork() throws Exception {
        return _transactionService.getRetryingTransactionHelper().doInTransaction(executionSelectUnpublished, true, false);
      }
    }, AuthenticationUtil.getSystemUserName());

    // Send to JMS
    final RetryingTransactionCallback<Void> executionJms = new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        executeUpdate(publishedDocuments, unpublishedDocuments);
        _pushJmsService.pushToJms(publishedDocuments, VgrModel.PROP_PUSHED_FOR_PUBLISH);
        _pushJmsService.pushToJms(unpublishedDocuments, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
        return null;
      }
    };

    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        _transactionService.getRetryingTransactionHelper().doInTransaction(executionJms, false, true);
        return null;
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  private void executeUpdate(List<NodeRef> publishedDocuments, List<NodeRef> unpublishedDocuments) {
    // disable all behaviours, we can't have the modified date updated for
    // this...
    _behaviourFilter.disableBehaviour();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Setting cm:modified property for all published documents");
    }
    setUpdatedProperty(publishedDocuments);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Setting cm:modified property for all unpublished documents");
    }
    setUpdatedProperty(unpublishedDocuments);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Setting pushed properties for all published documents");
    }
    setPublishStatusProperties(publishedDocuments, VgrModel.PROP_PUSHED_FOR_PUBLISH);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Setting pushed properties for all unpublished documents");
    }
    setPublishStatusProperties(unpublishedDocuments, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Increasing vgr:pushed-count with 1 for all published documents");
    }
    increasePushedCount(publishedDocuments);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Increasing vgr:pushed-count with 1 for all unpublished documents");
    }
    increasePushedCount(unpublishedDocuments);

    _behaviourFilter.enableBehaviour();
  }

  protected void increasePushedCount(List<NodeRef> nodeRefs) {
    for (NodeRef nodeRef : nodeRefs) {
      refreshLock();

      Integer pushedCount = (Integer) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_COUNT);

      if (pushedCount == null) {
        pushedCount = 0;
      }

      pushedCount++;

      _nodeService.setProperty(nodeRef, VgrModel.PROP_PUSHED_COUNT, pushedCount);
    }
  }

  protected void setPublishStatusProperties(List<NodeRef> nodeRefs, QName property) {
    for (NodeRef nodeRef : nodeRefs) {
      refreshLock();
      _nodeService.setProperty(nodeRef, property, new Date());
      if (VgrModel.PROP_PUSHED_FOR_PUBLISH.equals(property)) {
        _nodeService.setProperty(nodeRef, VgrModel.PROP_PUBLISH_STATUS, null);
      } else {
        _nodeService.setProperty(nodeRef, VgrModel.PROP_UNPUBLISH_STATUS, null);
      }
    }
  }

  private Boolean executePush(List<NodeRef> publishedDocuments, List<NodeRef> unpublishedDocuments) {
    // disable all behaviours, we can't have the modified date updated for
    // this...
    _behaviourFilter.disableBehaviour();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Sending published and unpublished documents to PuSH server");
    }

    List<NodeRef> concatenatedList = new ArrayList<NodeRef>();
    concatenatedList.addAll(publishedDocuments);
    concatenatedList.addAll(unpublishedDocuments);

    boolean pushed = _pushService.pushFiles(concatenatedList) || _test;

    if (!pushed) {
      if (LOG.isDebugEnabled()) {
        for (NodeRef nodeRef : concatenatedList) {
          LOG.debug("For some reason the node ref '" + nodeRef + "' could not be pushed.");
        }
      }
    } else {
      for (NodeRef nodeRef : publishedDocuments) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Pushed NodeRef " + nodeRef + " to property " + VgrModel.PROP_PUSHED_FOR_PUBLISH);
        }
      }
      for (NodeRef nodeRef : unpublishedDocuments) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Pushed NodeRef " + nodeRef + " to property " + VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
        }
      }
    }

    _behaviourFilter.enableBehaviour();
    return pushed;
  }

  private String formatNow() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    return sdf.format(new Date());
  }

  private Date parseNow(String now) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    try {
      return sdf.parse(now);
    } catch (ParseException ex) {
      throw new RuntimeException(ex);
    }
  }

  protected void setUpdatedProperty(List<NodeRef> nodeRefs) {
    for (NodeRef nodeRef : nodeRefs) {
      refreshLock();
      _nodeService.setProperty(nodeRef, ContentModel.PROP_MODIFIED, new Date());
    }
  }

  protected List<NodeRef> findUnpublishedDocuments(String now) {
    ResultSet result = findDocuments(findUnpublishedDocumentsQuery(now));

    Date nw = parseNow(now);

    List<NodeRef> nodeRefs = new ArrayList<NodeRef>();

    try {
      for (NodeRef nodeRef : result.getNodeRefs()) {
        refreshLock();

        Date availableto = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_TO);

        if (availableto == null) {
          continue;
        }

        if (nw.getTime() >= availableto.getTime()) {
          nodeRefs.add(nodeRef);
        }
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }

    return nodeRefs;
  }

  protected List<NodeRef> findPublishedDocuments(String now) {
    ResultSet result = findDocuments(findPublishedDocumentsQuery(now));

    try {
      return result.getNodeRefs();
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }
  }

  private StringBuffer findPublishedDocumentsQuery(String now) {
    StringBuffer query = new StringBuffer();

    query.append("TYPE:\"vgr:document\" AND ");
    query.append("ASPECT:\"vgr:published\" AND ");
    query.append("vgr:dc\\.date\\.availablefrom:[MIN TO \"" + now + "\"] AND ");
    query.append("(ISNULL:\"vgr:dc.date.availableto\" OR vgr:dc\\.date\\.availableto:[\"" + now + "\" TO MAX]) AND ");
    query.append("ISNULL:\"vgr:pushed-for-publish\"");

    if (LOG.isDebugEnabled()) {
      LOG.debug("Query for finding unpushed published documents: " + query.toString());
    }

    return query;
  }

  private StringBuffer findUnpublishedDocumentsQuery(String now) {
    StringBuffer query = new StringBuffer();

    query.append("TYPE:\"vgr:document\" AND ");
    query.append("ASPECT:\"vgr:published\" AND ");
    query.append("ISNOTNULL:\"vgr:dc.date.availableto\" AND ");
    query.append("vgr:dc\\.date\\.availableto:[MIN TO \"" + now + "\"] AND ");
    query.append("ISNOTNULL:\"vgr:pushed-for-publish\" AND ");
    query.append("ISNULL:\"vgr:pushed-for-unpublish\"");

    if (LOG.isDebugEnabled()) {
      LOG.debug("Query for finding unpushed unpublished documents: " + query.toString());
    }

    return query;
  }

  private ResultSet findDocuments(StringBuffer query) {
    SearchParameters searchParameters = new SearchParameters();

    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    searchParameters.setQuery(query.toString());

    ResultSet result = _searchService.query(searchParameters);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Documents found for query: " + query.toString());
      LOG.debug("Count: " + result.length());
      LOG.debug("");
    }

    return result;
  }

}
