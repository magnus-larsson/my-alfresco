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
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class PushToPubSubHubBubServer extends ClusteredExecuter {

  private static Logger LOG = Logger.getLogger(PushToPubSubHubBubServer.class);

  private SearchService _searchService;

  private PushService _pushService;

  private NodeService _nodeService;

  private BehaviourFilter _behaviourFilter;

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

  @Override
  protected String getJobName() {
    return "Push to pubsubhubbub server";
  }

  @Override
  protected void executeInternal() {
    final RetryingTransactionCallback<Void> execution = new RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        // first all documents must have a current "cm:modified" flag
        executeUpdate();

        // after that, push the files to the push server
        executePush();

        return null;
      }
    };

    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        _transactionService.getRetryingTransactionHelper().doInTransaction(execution, false, true);

        return null;
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  private void executeUpdate() {
    // disable all behaviours, we can't have the modified date updated for
    // this...
    _behaviourFilter.disableAllBehaviours();

    String now = formatNow();

    // set the "cm:modified" property for all published documents
    setUpdatedProperty(findPublishedDocuments(now));

    // set the "cm:modified" property for all unpublished documents
    setUpdatedProperty(findUnpublishedDocuments(now));

    _behaviourFilter.enableAllBehaviours();
  }

  private void executePush() {
    // disable all behaviours, we can't have the modified date updated for
    // this...
    _behaviourFilter.disableAllBehaviours();

    String now = formatNow();

    // send the published documents to the push server
    sendToPush(findPublishedDocuments(now), VgrModel.PROP_PUSHED_FOR_PUBLISH);

    // send the unpublished documents to the push server
    sendToPush(findUnpublishedDocuments(now), VgrModel.PROP_PUSHED_FOR_UNPUBLISH);

    _behaviourFilter.enableAllBehaviours();
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

  private void sendToPush(List<NodeRef> nodeRefs, QName property) {
    // report the files as pushed either if they're really pushed or if the
    // service is set to test mode
    boolean pushed = _pushService.pushFiles(nodeRefs) || _test;

    if (!pushed) {
      if (LOG.isDebugEnabled()) {

        for (NodeRef nodeRef : nodeRefs) {
          LOG.debug("For some reason the node ref '" + nodeRef + "' could not be pushed.");
        }
      }

      return;
    }

    for (NodeRef nodeRef : nodeRefs) {
      refreshLock();

      _nodeService.setProperty(nodeRef, property, new Date());

      if (LOG.isDebugEnabled()) {
        LOG.debug("NodeRef pushed to '" + property + "': " + nodeRef);
      }
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
      ServiceUtils.closeQuietly(result);
    }

    return nodeRefs;
  }

  protected List<NodeRef> findPublishedDocuments(String now) {
    ResultSet result = findDocuments(findPublishedDocumentsQuery(now));

    try {
      return result.getNodeRefs();
    } finally {
      ServiceUtils.closeQuietly(result);
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
