package org.alfresco.repo.invitation;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.acegisecurity.Authentication;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.security.ExternalUsersService;
import se.vgregion.alfresco.repo.security.UserSynchronizer;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

/**
 * This class is an overridden class from the original InvitationServiceImpl from Alfresco. In order to limit the amount
 * of duplication of code this has been placed in the same package as the original Alfresco class as there are some
 * package level classes that need to be addressed from this class.
 * <p/>
 * It's purpose is to create an external user when that kind of invitation is initiated.
 * <p/>
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 * @deprecated This feature is not used any longer
 */
public class ExternalUsersInvitationServiceImpl implements InitializingBean, ExternalUsersInvitationService {

  private static final Logger logger = Logger.getLogger(ExternalUsersInvitationServiceImpl.class);

  private UserSynchronizer _userSynchronizer;

  private UserRegistrySynchronizer _userRegistrySynchronizer;

  private InvitationService _invitationService;

  private MutableAuthenticationService _authenticationService;

  private PersonService _personService;

  private PermissionService _permissionService;

  private NodeService _nodeService;

  private SiteService _siteService;

  private WorkflowService _workflowService;

  private NamespaceService _namespaceService;

  private ExternalUsersService _externalUsersService;

  private ServiceUtilsImpl _serviceUtils;

  public void setUserSynchronizer(final UserSynchronizer userSynchronizer) {
    _userSynchronizer = userSynchronizer;
  }

  public void setInvitationService(final InvitationService invitationService) {
    _invitationService = invitationService;
  }

  public void setAuthenticationService(final MutableAuthenticationService authenticationService) {
    _authenticationService = authenticationService;
  }

  public void setNamespaceService(final NamespaceService namespaceService) {
    _namespaceService = namespaceService;
  }

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setPermissionService(final PermissionService permissionService) {
    _permissionService = permissionService;
  }

  public void setPersonService(final PersonService personService) {
    _personService = personService;
  }

  public void setSiteService(final SiteService siteService) {
    _siteService = siteService;
  }

  public void setWorkflowService(final WorkflowService workflowService) {
    _workflowService = workflowService;
  }

  public void setExternalUsersService(final ExternalUsersService externalUsersService) {
    _externalUsersService = externalUsersService;
  }

  public void setUserRegistrySynchronizer(final UserRegistrySynchronizer userRegistrySynchronizer) {
    _userRegistrySynchronizer = userRegistrySynchronizer;
  }

  public void setServiceUtils(final ServiceUtilsImpl serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.alfresco.repo.invitation.ExternalUsersInvitationService# startNominatedInvite(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.invitation.Invitation.ResourceType,
   * java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public NominatedInvitation startNominatedInvite(final String inviteeFirstName, final String inviteeLastName, final String inviteeEmail, final String inviteeUserName,
      final Invitation.ResourceType resourceType, final String siteShortName, final String inviteeSiteRole, final String serverPath, final String acceptUrl, final String rejectUrl) {
    return AuthenticationUtil.runAs(new RunAsWork<NominatedInvitation>() {
      @Override
      public NominatedInvitation doWork() throws Exception {
        return doStartNominatedInvite(inviteeFirstName, inviteeLastName, inviteeEmail, inviteeUserName, resourceType, siteShortName, inviteeSiteRole, serverPath, acceptUrl, rejectUrl);
      }

    }, AuthenticationUtil.getFullyAuthenticatedUser());
  }

  private NominatedInvitation doStartNominatedInvite(final String inviteeFirstName, final String inviteeLastName, final String inviteeEmail, String inviteeUserName,
      final Invitation.ResourceType resourceType, final String siteShortName, final String inviteeSiteRole, final String serverPath, final String acceptUrl, final String rejectUrl) {

    // get the inviter user name (the name of user web script is executed
    // under)
    final String inviterUserName = _authenticationService.getCurrentUserName();

    boolean created = false;

    checkManagerRole(inviterUserName, resourceType, siteShortName);

    if (logger.isDebugEnabled()) {
      logger.debug("startInvite() inviterUserName=" + inviterUserName + " inviteeUserName=" + inviteeUserName + " inviteeFirstName=" + inviteeFirstName + " inviteeLastName=" + inviteeLastName
          + " inviteeEmail=" + inviteeEmail + " siteShortName=" + siteShortName + " inviteeSiteRole=" + inviteeSiteRole);
    }

    //
    // if we have not explicitly been passed an existing user's user name
    // then ....
    //
    // if a person already exists who has the given invitee email address
    //
    // 1) obtain invitee user name from first person found having the
    // invitee email address, first name and last name
    // 2) handle error conditions -
    // (invitee already has an invitation in progress for the given site,
    // or he/she is already a member of the given site
    //
    if (inviteeUserName == null || inviteeUserName.trim().length() == 0) {

      inviteeUserName = null;

      final Set<NodeRef> peopleWithInviteeEmail = _personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, inviteeEmail, 1000);

      if (peopleWithInviteeEmail.size() > 0) {
        // get person already existing who has the given
        // invitee email address
        for (final NodeRef personRef : peopleWithInviteeEmail) {
          final Serializable firstNameVal = _nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
          final Serializable lastNameVal = _nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);

          final String personFirstName = DefaultTypeConverter.INSTANCE.convert(String.class, firstNameVal);
          final String personLastName = DefaultTypeConverter.INSTANCE.convert(String.class, lastNameVal);

          if (personFirstName != null && personFirstName.equalsIgnoreCase(inviteeFirstName)) {
            if (personLastName != null && personLastName.equalsIgnoreCase(inviteeLastName)) {
              // got a match on email, lastname, firstname
              // get invitee user name of that person
              final Serializable userNamePropertyVal = _nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
              inviteeUserName = DefaultTypeConverter.INSTANCE.convert(String.class, userNamePropertyVal);

              if (logger.isDebugEnabled()) {
                logger.debug("not explictly passed username - found matching email, resolved inviteeUserName=" + inviteeUserName);
              }
            }
          }
        }
      }

      if (inviteeUserName == null) {
        // This shouldn't normally happen. Due to the fix for ETHREEOH-3268, the
        // link to invite external users
        // should be disabled when the authentication chain does not allow it.
        if (!_authenticationService.isAuthenticationCreationAllowed()) {
          throw new InvitationException("invitation.invite.authentication_chain");
        }

        // else there are no existing people who have the given invitee
        // email address so create new person
        inviteeUserName = createInviteePerson(inviteeFirstName, inviteeLastName, inviteeEmail, siteShortName);

        created = true;

        if (logger.isDebugEnabled()) {
          logger.debug("not explictly passed username - created new person, inviteeUserName=" + inviteeUserName);
        }
      }
    } else {
      // TODO MER - Is the code block neccessary - seems to do nothing ?
      // inviteeUserName was specified
      final NodeRef person = _personService.getPerson(inviteeUserName);

      // TODO
      Serializable firstNameVal = _nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
      Serializable lastNameVal = _nodeService.getProperty(person, ContentModel.PROP_LASTNAME);
      Serializable emailVal = _nodeService.getProperty(person, ContentModel.PROP_EMAIL);
      firstNameVal = DefaultTypeConverter.INSTANCE.convert(String.class, firstNameVal);
      lastNameVal = DefaultTypeConverter.INSTANCE.convert(String.class, lastNameVal);
      emailVal = DefaultTypeConverter.INSTANCE.convert(String.class, emailVal);
    }

    if (_siteService.isMember(siteShortName, inviteeUserName)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Failed - invitee user is already a member of the site.");
      }

      final Object objs[] = { inviteeUserName, inviteeEmail, siteShortName };
      throw new InvitationExceptionUserError("invitation.invite.already_member", objs);
    }

    //
    // If a user account does not already exist for invitee user name
    // then create a disabled user account for the invitee.
    // Hold a local reference to generated password if disabled invitee
    // account
    // is created, otherwise if a user account already exists for invitee
    // user name, then local reference to invitee password will be "null"
    //
    final String initeeUserNameFinal = inviteeUserName;

    final String inviteePassword = created ? AuthenticationUtil.runAs(new RunAsWork<String>() {
      @Override
      public String doWork() {
        return createInviteeDisabledAccount(initeeUserNameFinal);
      }
    }, AuthenticationUtil.getSystemUserName()) : null;

    // create a ticket for the invite - this is used
    final String inviteTicket = GUID.generate();

    //
    // Start the invite workflow with inviter, invitee and site properties
    //

    final WorkflowDefinition wfDefinition = _workflowService.getDefinitionByName(WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME);

    if (wfDefinition == null) {
      // handle workflow definition does not exist
      final Object objs[] = { WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME };
      throw new InvitationException("invitation.error.noworkflow", objs);
    }

    // Get invitee person NodeRef to add as assignee
    final NodeRef inviteeNodeRef = _personService.getPerson(inviteeUserName);

    final SiteInfo siteInfo = _siteService.getSite(siteShortName);
    String siteDescription = siteInfo.getDescription();
    if (siteDescription == null) {
      siteDescription = "";
    } else if (siteDescription.length() > 255) {
      siteDescription = siteDescription.substring(0, 255);
    }

    // get the workflow description
    final String workflowDescription = generateWorkflowDescription(siteInfo, "invitation.nominated.workflow.description");

    // create workflow properties
    final Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(32);
    workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME, inviterUserName);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME, inviteeUserName);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_EMAIL, inviteeEmail);
    workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, inviteeNodeRef);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_FIRSTNAME, inviteeFirstName);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_LASTNAME, inviteeLastName);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_GEN_PASSWORD, inviteePassword);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME, siteShortName);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TITLE, siteInfo.getTitle());
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_DESCRIPTION, siteDescription);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TYPE, resourceType.toString());
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE, inviteeSiteRole);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_SERVER_PATH, serverPath);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_ACCEPT_URL, acceptUrl);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_REJECT_URL, rejectUrl);
    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITE_TICKET, inviteTicket);

    // start the workflow
    final WorkflowPath wfPath = _workflowService.startWorkflow(wfDefinition.getId(), workflowProps);

    //
    // complete invite workflow start task to send out the invite email
    //

    // get the workflow tasks
    String workflowId = wfPath.getInstance().getId();
    String wfPathId = wfPath.getId();
    List<WorkflowTask> wfTasks = _workflowService.getTasksForWorkflowPath(wfPathId);

    // throw an exception if no tasks where found on the workflow path
    if (wfTasks.size() == 0) {
      final Object objs[] = { WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME };
      throw new InvitationException("invitation.error.notasks", objs);
    }

    //
    // first task in workflow task list (there should only be one)
    // associated
    // with the workflow path id (above) should be "wf:inviteToSiteTask",
    // otherwise
    // throw web script exception
    //
    String wfTaskName = wfTasks.get(0).getName();
    QName wfTaskNameQName = QName.createQName(wfTaskName, _namespaceService);
    QName inviteToSiteTaskQName = WorkflowModelNominatedInvitation.WF_TASK_INVITE_TO_SITE;
    if (!wfTaskNameQName.equals(inviteToSiteTaskQName)) {
      final Object objs[] = { wfPathId, WorkflowModelNominatedInvitation.WF_TASK_INVITE_TO_SITE };
      throw new InvitationException("invitation.error.wrong_first_task", objs);
    }

    // get "inviteToSite" task
    final WorkflowTask wfStartTask = wfTasks.get(0);

    // attach empty package to start task, end it and follow with transition
    // that sends out the invite
    if (logger.isDebugEnabled()) {
      logger.debug("Starting Invite workflow task by attaching empty package...");
    }
    final NodeRef wfPackage = _workflowService.createPackage(null);
    final Map<QName, Serializable> wfTaskProps = new HashMap<QName, Serializable>(1, 1.0f);
    wfTaskProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
    wfTaskProps.put(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID, workflowId);

    if (logger.isDebugEnabled()) {
      logger.debug("Updating Invite workflow task...");
    }

    _workflowService.updateTask(wfStartTask.getId(), wfTaskProps, null, null);

    if (logger.isDebugEnabled()) {
      logger.debug("Transitioning Invite workflow task...");
    }

    try {
      _workflowService.endTask(wfStartTask.getId(), WorkflowModelNominatedInvitation.WF_TRANSITION_SEND_INVITE);
    } catch (final RuntimeException err) {
      if (logger.isDebugEnabled()) {
        logger.debug("Failed - caught error during Invite workflow transition: " + err.getMessage());
      }
      throw err;
    }

    workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITE_TICKET, inviteTicket);

    final NominatedInvitationImpl result = new NominatedInvitationImpl(workflowId, new Date(), workflowProps);

    return result;
  }

  private String generateWorkflowDescription(final SiteInfo siteInfo, final String messageId) {
    String siteTitle = siteInfo.getTitle();

    if (siteTitle == null || siteTitle.length() == 0) {
      siteTitle = siteInfo.getShortName();
    }

    return I18NUtil.getMessage(messageId, siteTitle);
  }

  /**
   * Check that the specified user has manager role over the resource.
   * 
   * @param userId
   * @throws InvitationException
   */
  private void checkManagerRole(final String userId, final Invitation.ResourceType resourceType, final String siteShortName) {
    // if inviter is not the site manager then throw web script exception
    final String inviterRole = _siteService.getMembersRole(siteShortName, userId);

    if (inviterRole == null || inviterRole.equals(SiteModel.SITE_MANAGER) == false) {
      final Object objs[] = { userId, siteShortName };

      throw new InvitationExceptionForbidden("invitation.invite.not_site_manager", objs);
    }
  }

  /**
   * Creates a person for the invitee with a generated user name.
   * 
   * @param inviteeFirstName
   *          first name of invitee
   * @param inviteeLastName
   *          last name of invitee
   * @param inviteeEmail
   *          email address of invitee
   * @param siteShortName
   * @return invitee user name
   */
  private String createInviteePerson(final String inviteeFirstName, final String inviteeLastName, final String inviteeEmail, final String siteShortName) {
    final String inviteeUserName = createExternalUser(inviteeFirstName, inviteeLastName, inviteeEmail, siteShortName);

    AuthenticationUtil.runAs(new RunAsWork<Object>() {

      @Override
      public Object doWork() throws Exception {
        final NodeRef person = _personService.getPerson(inviteeUserName);

        _nodeService.setProperty(person, ContentModel.PROP_SIZE_CURRENT, null);

        _permissionService.setPermission(person, inviteeUserName, PermissionService.ALL_PERMISSIONS, true);

        return null;
      }

    }, AuthenticationUtil.getSystemUserName());

    return inviteeUserName;
  }

  private String createExternalUser(final String inviteeFirstName, final String inviteeLastName, final String inviteeEmail, final String siteShortName) {
    String username = _externalUsersService.createExternalUser(inviteeFirstName, inviteeLastName, inviteeEmail);

    final SiteInfo siteInfo = _siteService.getSite(siteShortName);

    final String siteTitle = siteInfo != null ? siteInfo.getTitle() : siteShortName;

    final String siteLink = _serviceUtils.getShareBaseLink();

    username = _externalUsersService.inviteExternalUser(username, siteTitle, siteLink);

    // _userSynchronizer.synchronizeUser(new Date(1), username);

    // this little sucker clears the current security context... must save and
    // then reset it
    final Authentication authentication = AuthenticationUtil.getFullAuthentication();

    _userRegistrySynchronizer.synchronize(false, false, false);

    AuthenticationUtil.setFullAuthentication(authentication);

    return username;
  }

  /**
   * Creates a disabled user account for the given invitee user name with a generated password
   * 
   * @param inviteeUserName
   * @return password generated for invitee user account
   */
  private String createInviteeDisabledAccount(final String inviteeUserName) {
    // generate password using password generator
    final String generatedPassword = RandomStringUtils.random(10);

    // create disabled user account for invitee user name with generated
    // password
    _authenticationService.createAuthentication(inviteeUserName, generatedPassword.toCharArray());
    _authenticationService.setAuthenticationEnabled(inviteeUserName, false);

    return generatedPassword;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_authenticationService);
    Assert.notNull(_externalUsersService);
    Assert.notNull(_invitationService);
    Assert.notNull(_namespaceService);
    Assert.notNull(_nodeService);
    Assert.notNull(_permissionService);
    Assert.notNull(_personService);
    Assert.notNull(_siteService);
    Assert.notNull(_userSynchronizer);
    Assert.notNull(_workflowService);
    Assert.notNull(_serviceUtils);
  }

}
