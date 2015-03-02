package se.vgregion.alfresco.repo.invitation.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.MailSendException;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.invitation.InvitationNotificationHelper;

public class InvitationNotificationHelperImpl implements InvitationNotificationHelper, InitializingBean {
  private static final Logger LOG = Logger.getLogger(InvitationNotificationHelperImpl.class);
  private static final String APPROVE_TEMPLATE_PATH = "alfresco/module/vgr-repo/templates/email/invite-moderated-approved.ftl";
  private static final String REJECT_TEMPLATE_PATH = "alfresco/module/vgr-repo/templates/email/invite-moderated-rejected.ftl";
  private static final String SUBJECT_APPROVED = "Ansökan om medlemskap godkänd";
  private static final String SUBJECT_REJECTED = "Ansökan om medlemskap avslagen";
  protected Properties globalProperties;
  private SysAdminParams sysAdminParams;
  private PersonService personService;
  private ActionService actionService;
  private NodeService nodeService;
  private SiteService siteService;
  private Repository repository;
  private ServiceRegistry serviceRegistry;

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  public void setSysAdminParams(SysAdminParams sysAdminParams) {
    this.sysAdminParams = sysAdminParams;
  }

  public void setGlobalProperties(Properties globalProperties) {
    this.globalProperties = globalProperties;
  }

  protected Map<String, Serializable> getBasicMailSettings(String subject, String templatePath, String email) {
    Map<String, Serializable> mailSettings = new HashMap<String, Serializable>();
    mailSettings.put(MailActionExecuter.PARAM_SUBJECT, subject);
    mailSettings.put(MailActionExecuter.PARAM_TO, email);
    mailSettings.put(MailActionExecuter.PARAM_FROM, globalProperties.getProperty("mail.from.default"));

    mailSettings.put(MailActionExecuter.PARAM_TEMPLATE, templatePath);
    return mailSettings;
  }

  protected Map<String, Serializable> getBasicTemplateArgs(Invitation invitation, String message) {
    NodeRef inviteeNodeRef = personService.getPersonOrNull(invitation.getInviteeUserName());
    NodeRef inviterNodeRef = personService.getPersonOrNull(AuthenticationUtil.getFullyAuthenticatedUser());
    Map<QName, Serializable> inviteeProperties = nodeService.getProperties(inviteeNodeRef);
    Map<QName, Serializable> inviterProperties = nodeService.getProperties(inviterNodeRef);
    String shortName = invitation.getResourceName();
    SiteInfo site = siteService.getSite(shortName);
    Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
    templateArgs.put("siteName", site.getTitle());
    templateArgs.put("inviteeFirstName", inviteeProperties.get(ContentModel.PROP_FIRSTNAME));
    templateArgs.put("inviteeLastName", inviteeProperties.get(ContentModel.PROP_LASTNAME));
    templateArgs.put("inviterFirstName", inviterProperties.get(ContentModel.PROP_FIRSTNAME));
    templateArgs.put("inviterLastName", inviterProperties.get(ContentModel.PROP_LASTNAME));
    templateArgs.put("message", message);
    return templateArgs;
  }

  protected void addTemplateArgsToMailSettings(Map<String, Serializable> mailSettings, Map<String, Serializable> templateArgs) {
    Map<String, Serializable> templateModel = new HashMap<String, Serializable>();
    templateModel.put("args", (Serializable) templateArgs);
    templateModel.put(TemplateService.KEY_COMPANY_HOME, new TemplateNode(repository.getCompanyHome(), serviceRegistry, null));
    templateModel.put(TemplateService.KEY_SHARE_URL, UrlUtil.getShareUrl(sysAdminParams));
    mailSettings.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);
  }

  @Override
  public void generateModeratedApproveMail(Invitation invitation, String message) {
    NodeRef inviteeNodeRef = personService.getPersonOrNull(invitation.getInviteeUserName());
    if (inviteeNodeRef != null) {
      Map<QName, Serializable> personProperties = nodeService.getProperties(inviteeNodeRef);
      Map<String, Serializable> mailSettings = getBasicMailSettings(SUBJECT_APPROVED, APPROVE_TEMPLATE_PATH, (String) personProperties.get(ContentModel.PROP_EMAIL));
      // Template args
      Map<String, Serializable> templateArgs = getBasicTemplateArgs(invitation, message);
      
      addTemplateArgsToMailSettings(mailSettings, templateArgs);

      Action mailAction = actionService.createAction(MailActionExecuter.NAME);
      mailAction.setExecuteAsynchronously(true);
      mailAction.setParameterValues(mailSettings);
      try {
        actionService.executeAction(mailAction, null);
      } catch (Exception ex) {
        LOG.error("Error while sending mail", ex);
      }
      LOG.debug("Sent out approval of site invitation email for username " + invitation.getInviteeUserName());
    } else {
      LOG.warn("Could not find user with username " + invitation.getInviteeUserName());
    }

  }

  @Override
  public void generateModeratedRejectMail(Invitation invitation, String message) {
    NodeRef inviteeNodeRef = personService.getPersonOrNull(invitation.getInviteeUserName());
    if (inviteeNodeRef != null) {
      Map<QName, Serializable> personProperties = nodeService.getProperties(inviteeNodeRef);
      Map<String, Serializable> mailSettings = getBasicMailSettings(SUBJECT_REJECTED, REJECT_TEMPLATE_PATH, (String) personProperties.get(ContentModel.PROP_EMAIL));
      // Template args
      Map<String, Serializable> templateArgs = getBasicTemplateArgs(invitation, message);
      
      addTemplateArgsToMailSettings(mailSettings, templateArgs);

      Action mailAction = actionService.createAction(MailActionExecuter.NAME);
      mailAction.setExecuteAsynchronously(true);
      mailAction.setParameterValues(mailSettings);
      try {
        actionService.executeAction(mailAction, null);
      } catch (Exception ex) {
        LOG.error("Error while sending mail", ex);
      }
      LOG.debug("Sent out reject of site invitation email for username " + invitation.getInviteeUserName());
    } else {
      LOG.warn("Could not find user with username " + invitation.getInviteeUserName());
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(globalProperties);
    Assert.notNull(sysAdminParams);
    Assert.notNull(personService);
    Assert.notNull(actionService);
    Assert.notNull(nodeService);
    Assert.notNull(siteService);
    Assert.notNull(repository);
    Assert.notNull(serviceRegistry);
  }

}
