package se.vgregion.alfresco.repo.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.InvitationType;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;



public class Invitations extends DeclarativeWebScript {

   public  class Invite {
      private final String id;
      private final String site;
      public Invite(final String id,final String site) {
         this.id = id;
         this.site = site;
      }
      public String getId() { return id; }
      public String getSite() { return site; }
   }


  private AuthenticationService _authenticationService;
  private InvitationService _invitationService;
  private SiteService _siteService;

  public void setSiteService(final SiteService siteService) {
   _siteService = siteService;
  }

  public void setInvitationService(final InvitationService invitationService) {
    _invitationService = invitationService;
  }

  public void setAuthenticationService(final AuthenticationService authenticationService) {
    _authenticationService = authenticationService;
  }

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {


    //invitee is current user only
    final String invitee = _authenticationService.getCurrentUserName();

    //find site name
    final List<Invite> data = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<Invite>>() {

        @Override
        public List<Invite> doWork() throws Exception {
             final List<Invitation> invitations = _invitationService.listPendingInvitationsForInvitee(invitee);
             final List<Invite> data = new ArrayList<Invite>(invitations.size());

             for (final Invitation inv: invitations) {
               // if it's not a nominated invitation type, skip the invitation
               if (inv.getInvitationType() != InvitationType.NOMINATED) {
                 continue;
               }

               final SiteInfo s = _siteService.getSite(inv.getResourceName());

               if (s != null) {
                  data.add(new Invite(inv.getInviteId(),s.getTitle()));
               }
             }
             return data;
        }

    }, AuthenticationUtil.getSystemUserName());

    final Map<String,Object> model = new HashMap<String,Object>();
    model.put("invitations",data);
    return model;
  }

}


