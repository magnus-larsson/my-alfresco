package se.vgregion.alfresco.repo.jobs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.mail.SendMailService;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class ValidityChecker extends ClusteredExecuter {

  private static final Logger LOG = Logger.getLogger(ValidityChecker.class);

  private SearchService _searchService;

  private NodeService _nodeService;

  private SendMailService _sendMailService;

  private String _mailFrom;

  private List<Map<String, ?>> _emails;
  
  private BehaviourFilter _behaviourFilter;

  
  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    this._behaviourFilter = behaviourFilter;
  }
  
  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setSendMailService(SendMailService sendMailService) {
    _sendMailService = sendMailService;
  }

  public void setMailFrom(String mailFrom) {
    _mailFrom = mailFrom;
  }

  public void setEmails(List<Map<String, ?>> emails) {
    _emails = emails;
  }

  @Override
  protected String getJobName() {
    return "Validity Checker";
  }

  @Override
  protected void executeInternal() {
    AuthenticationUtil.runAs(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        sendFirstEmail();

        sendSecondEmail();

        return null;
      }
    }, AuthenticationUtil.SYSTEM_USER_NAME);
  }

  protected void sendFirstEmail() {
    Map<String, ?> mail = _emails.get(0);

    String subject = I18NUtil.getMessage((String) mail.get("subject"));

    int daysBefore = Integer.parseInt((String) mail.get("daysBefore"));

    ResultSet result = findDocuments(daysBefore, null);

    try {
      for (NodeRef node : result.getNodeRefs()) {
        List<String> to = new ArrayList<String>(new LinkedHashSet<String>(getFirstEmailRecipients(node)));

        String source = (String) _nodeService.getProperty(node, VgrModel.PROP_SOURCE);
        
        String identifier = (String) _nodeService.getProperty(node, VgrModel.PROP_IDENTIFIER);
        
        String title = (String) _nodeService.getProperty(node, VgrModel.PROP_TITLE);

        String body = I18NUtil.getMessage((String) mail.get("body"), daysBefore, source, identifier, title);

        _sendMailService.sendTextMail(subject, _mailFrom, to, body);
        boolean enabled = _behaviourFilter.isEnabled(node);
        if (enabled) {
          _behaviourFilter.disableBehaviour(node);
        }
        _nodeService.setProperty(node, VgrModel.PROP_SENT_EMAILS, 1);
        if (enabled) {
          _behaviourFilter.enableBehaviour(node);
        }
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }
  }

  protected void sendSecondEmail() {
    Map<String, ?> mail = _emails.get(1);

    String subject = I18NUtil.getMessage((String) mail.get("subject"));

    int daysBefore = Integer.parseInt((String) mail.get("daysBefore"));

    ResultSet result = findDocuments(daysBefore, 1);

    try {
      for (NodeRef node : result.getNodeRefs()) {
        List<String> to = new ArrayList<String>(new LinkedHashSet<String>(getSecondEmailRecipients(node)));

        String source = (String) _nodeService.getProperty(node, VgrModel.PROP_SOURCE);

        String identifier = (String) _nodeService.getProperty(node, VgrModel.PROP_IDENTIFIER);
        
        String title = (String) _nodeService.getProperty(node, VgrModel.PROP_TITLE);

        String body = I18NUtil.getMessage((String) mail.get("body"), daysBefore, source, identifier, title);

        _sendMailService.sendTextMail(subject, _mailFrom, to, body);

        _nodeService.setProperty(node, VgrModel.PROP_SENT_EMAILS, 2);
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }
  }

  private List<String> getFirstEmailRecipients(NodeRef node) {
    @SuppressWarnings("unchecked")
    List<String> creatorIds = (List<String>) _nodeService.getProperty(node, VgrModel.PROP_CREATOR_ID);

    String savedById = (String) _nodeService.getProperty(node, VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID);

    List<String> recipients = new ArrayList<String>();

    if (creatorIds != null && creatorIds.size() > 0) {
      recipients.addAll(creatorIds);
    }

    if (StringUtils.isNotBlank(savedById)) {
      recipients.add(savedById);
    }

    if (LOG.isDebugEnabled()) {
      for (String recipient : recipients) {
        LOG.debug("First email recipients: " + recipient);
      }
    }

    return recipients;
  }

  private List<String> getSecondEmailRecipients(NodeRef node) {
    List<String> recipients = getFirstEmailRecipients(node);

    @SuppressWarnings("unchecked")
    List<String> creatorDocumentIds = (List<String>) _nodeService.getProperty(node, VgrModel.PROP_CREATOR_DOCUMENT_ID);

    if (creatorDocumentIds != null && creatorDocumentIds.size() > 0) {
      recipients.addAll(creatorDocumentIds);
    }

    if (LOG.isDebugEnabled()) {
      for (String recipient : recipients) {
        LOG.debug("Second email recipients: " + recipient);
      }
    }

    return recipients;
  }

  private ResultSet findDocuments(int days, Integer sentEmails) {
    Date now = new Date();
    Date old = new DateTime(now.getTime()).plusDays(days).toDate();

    String sNow = formatDate(now);
    String sOld = formatDate(old);

    StringBuffer query = new StringBuffer();

    query.append("TYPE:\"vgr:document\" AND ");
    query.append("ASPECT:\"vgr:published\" AND ");
    query.append("vgr:dc\\.date\\.availablefrom:[MIN TO \"" + sNow + "\"] AND ");
    query.append("(ISNULL:\"vgr:dc.date.availableto\" OR vgr:dc\\.date\\.availableto:[\"" + sNow + "\" TO MAX]) AND ");
    query.append("ISNOTNULL:\"vgr:dc.date.validto\" AND vgr:dc\\.date\\.validto:[MIN TO \"" + sOld + "\"] AND ");
    query.append("ISNOTNULL:\"vgr:pushed-for-publish\" AND ");

    if (sentEmails == null) {
      query.append("(ISNULL:\"vgr:sent-emails\" OR vgr:sent\\-emails:0)");
    } else {
      query.append("ISNOTNULL:\"vgr:sent-emails\" AND vgr:sent\\-emails:" + sentEmails);

    }

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

  private String formatDate(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    return sdf.format(date);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Assert.notNull(_nodeService);
    Assert.notNull(_searchService);
    Assert.notNull(_sendMailService);
    Assert.hasText(_mailFrom);
    Assert.notNull(_behaviourFilter);
  }

}
