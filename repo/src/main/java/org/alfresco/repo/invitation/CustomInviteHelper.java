// @override projects/repository/source/java/org/alfresco/repo/invitation/InviteHelper.java
package org.alfresco.repo.invitation;

import java.util.Map;

public class CustomInviteHelper extends InviteHelper {

  @Override
  public void rejectModeratedInvitation(Map<String, Object> vars) {
    //Disable method since this is sending out a non localized email. VGR specific emails are implemented in InvitationServiceInterceptor and InvitationNotificationHelperImpl
  }
}
