package org.alfresco.repo.invitation;

import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 * @deprecated This feature is not used any longer
 */
public interface ExternalUsersInvitationService {

  NominatedInvitation startNominatedInvite(final String inviteeFirstName, final String inviteeLastName, final String inviteeEmail,
      final String inviteeUserName, final Invitation.ResourceType resourceType, final String siteShortName, final String inviteeSiteRole,
      final String serverPath, final String acceptUrl, final String rejectUrl);

}