package se.vgregion.alfresco.repo.scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class TriggerUserUpdate extends DeclarativeWebScript implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(TriggerUserUpdate.class);

  private NodeService _nodeService;

  private SearchService _searchService;

  private TransactionService _transactionService;

  private BehaviourFilter _behaviourFilter;

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    String[] startingLetters = getStartingLetters();

    for (String letters : startingLetters) {
      SearchParameters parameters = new SearchParameters();

      parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
      parameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
      parameters.setQuery("cm\\:userName:\"" + letters + "*\"");

      ResultSet resultSet = null;

      try {
        resultSet = _searchService.query(parameters);

        for (NodeRef nodeRef : resultSet.getNodeRefs()) {
          triggerUserUpdate(nodeRef);
        }

        if (LOG.isInfoEnabled() && resultSet.length() > 0) {
          LOG.info("Triggered '" + resultSet.length() + "' user updates for '" + letters + "'");
        }
      } finally {
        ServiceUtils.closeQuietly(resultSet);
      }
    }

    return new HashMap<String, Object>();
  }

  private void triggerUserUpdate(final NodeRef nodeRef) {
    AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>() {

          @Override
          public Void execute() throws Throwable {
            _behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
            _behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            try {
              _nodeService.setProperty(nodeRef, ContentModel.PROP_MODIFIED, new Date());
            } finally {
              _behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
              _behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            }

            return null;
          }
        };

        _transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        return null;
      }
    });
  }

  protected String[] getStartingLetters() {
    List<String> startingLetters = new ArrayList<String>();

    for (int x = 97; x <= 122; x++) {
      for (int y = 97; y <= 122; y++) {
        String letters = Character.toString((char) x) + Character.toString((char) y);

        startingLetters.add(letters);
      }
    }

    return startingLetters.toArray(new String[] {});
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  public void setTransactionService(TransactionService transactionService) {
    _transactionService = transactionService;
  }

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("searchService", _searchService);
    ParameterCheck.mandatory("transactionService", _transactionService);
    ParameterCheck.mandatory("behaviourFilter", _behaviourFilter);
  }

}
