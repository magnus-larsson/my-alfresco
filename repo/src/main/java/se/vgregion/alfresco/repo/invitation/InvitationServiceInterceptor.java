package se.vgregion.alfresco.repo.invitation;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.invitation.ExternalUsersInvitationService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class InvitationServiceInterceptor implements MethodInterceptor, InitializingBean {

  private boolean _externalActivation = false;

  private ExternalUsersInvitationService _externalUsersInvitationService;

  private PersonService _personService;

  private NodeService _nodeService;

  public void setExternalActivation(final boolean externalActivation) {
    _externalActivation = externalActivation;
  }

  public void setExternalUsersInvitationService(final ExternalUsersInvitationService externalUsersInvitationService) {
    _externalUsersInvitationService = externalUsersInvitationService;
  }

  @Override
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
    if (methodInvocation.getMethod().getName().equals("inviteNominated") && _externalActivation) {
      final Object result = inviteNominated(methodInvocation.getArguments());

      return result;
    }

    return methodInvocation.proceed();
  }

  private Object inviteNominated(final Object[] arguments) {
    if (arguments.length == 7) {
      final String inviteeUserName = (String) arguments[0];
      final ResourceType resourceType = (ResourceType) arguments[1];
      final String resourceName = (String) arguments[2];
      final String inviteeRole = (String) arguments[3];
      final String serverPath = (String) arguments[4];
      final String acceptUrl = (String) arguments[5];
      final String rejectUrl = (String) arguments[6];

      return inviteNominated(inviteeUserName, resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
    } else if (arguments.length == 9) {
      final String inviteeFirstName = (String) arguments[0];
      final String inviteeLastName = (String) arguments[1];
      final String inviteeEmail = (String) arguments[2];
      final ResourceType resourceType = (ResourceType) arguments[3];
      final String resourceName = (String) arguments[4];
      final String inviteeRole = (String) arguments[5];
      final String serverPath = (String) arguments[6];
      final String acceptUrl = (String) arguments[7];
      final String rejectUrl = (String) arguments[8];

      return inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail, resourceType, resourceName, inviteeRole, serverPath, acceptUrl,
          rejectUrl);
    }

    return null;
  }

  private NominatedInvitation inviteNominated(final String inviteeUserName, final Invitation.ResourceType resourceType, final String resourceName,
      final String inviteeRole, final String serverPath, final String acceptUrl, final String rejectUrl) {
    final NodeRef person = _personService.getPerson(inviteeUserName);

    final Serializable firstNameVal = _nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
    final Serializable lastNameVal = _nodeService.getProperty(person, ContentModel.PROP_LASTNAME);
    final Serializable emailVal = _nodeService.getProperty(person, ContentModel.PROP_EMAIL);
    final String firstName = DefaultTypeConverter.INSTANCE.convert(String.class, firstNameVal);
    final String lastName = DefaultTypeConverter.INSTANCE.convert(String.class, lastNameVal);
    final String email = DefaultTypeConverter.INSTANCE.convert(String.class, emailVal);

    return inviteNominated(firstName, lastName, email, inviteeUserName, resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
  }

  private NominatedInvitation inviteNominated(final String inviteeFirstName, final String inviteeLastName, final String inviteeEmail,
      final Invitation.ResourceType resourceType, final String resourceName, final String inviteeRole, final String serverPath,
      final String acceptUrl, final String rejectUrl) {
    return inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail, null, resourceType, resourceName, inviteeRole, serverPath, acceptUrl,
        rejectUrl);
  }

  private NominatedInvitation inviteNominated(final String inviteeFirstName, final String inviteeLastName, final String inviteeEmail,
      final String inviteeUserName, final Invitation.ResourceType resourceType, final String resourceName, final String inviteeRole,
      final String serverPath, final String acceptUrl, final String rejectUrl) {
    if (resourceType == Invitation.ResourceType.WEB_SITE) {
      return _externalUsersInvitationService.startNominatedInvite(inviteeFirstName, inviteeLastName, inviteeEmail, inviteeUserName, resourceType,
          resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
    }

    throw new InvitationException("unknown resource type");
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_externalUsersInvitationService);
  }

}
