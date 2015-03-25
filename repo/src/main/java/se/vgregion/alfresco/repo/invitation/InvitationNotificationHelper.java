package se.vgregion.alfresco.repo.invitation;


public interface InvitationNotificationHelper {

  /**
   * Generate an email when a request to join a site is approved
   * @param invitation
   * @param message
   */
  public void generateModeratedApproveMail(String username, String siteShortName);
  /**
   * Generate an email when a request to join a site is rejected
   * @param invitation
   * @param message
   */
  public void generateModeratedRejectMail(String username, String siteShortName);
}
