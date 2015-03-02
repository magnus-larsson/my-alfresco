package se.vgregion.alfresco.repo.invitation;

import org.alfresco.service.cmr.invitation.Invitation;

public interface InvitationNotificationHelper {

  /**
   * Generate an email when a request to join a site is approved
   * @param invitation
   * @param message
   */
  public void generateModeratedApproveMail(Invitation invitation, String message);
  /**
   * Generate an email when a request to join a site is rejected
   * @param invitation
   * @param message
   */
  public void generateModeratedRejectMail(Invitation invitation, String message);
}
