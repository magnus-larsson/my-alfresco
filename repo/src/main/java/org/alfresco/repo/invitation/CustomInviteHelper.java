// @override projects/repository/source/java/org/alfresco/repo/invitation/InviteHelper.java
package org.alfresco.repo.invitation;

import java.util.Map;

import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.invitation.InvitationNotificationHelper;

public class CustomInviteHelper extends InviteHelper {
  private InvitationNotificationHelper invitationNotificationHelper;

  public void setInvitationNotificationHelper(InvitationNotificationHelper invitationNotificationHelper) {
    this.invitationNotificationHelper = invitationNotificationHelper;
  }

  @Override
  public void approveModeratedInvitation(Map<String,Object> vars) {
    super.approveModeratedInvitation(vars);
    String resourceName = (String) vars.get(WorkflowModelModeratedInvitation.wfVarResourceName);
    String inviteeUserName = (String) vars.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
    
    invitationNotificationHelper.generateModeratedApproveMail(inviteeUserName, resourceName);
  }
  
  @Override
  public void rejectModeratedInvitation(Map<String, Object> vars) {
    String resourceName = (String) vars.get(WorkflowModelModeratedInvitation.wfVarResourceName);
    String inviteeUserName = (String) vars.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
    
    invitationNotificationHelper.generateModeratedRejectMail(inviteeUserName, resourceName);
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    Assert.notNull(invitationNotificationHelper);
  }
}
