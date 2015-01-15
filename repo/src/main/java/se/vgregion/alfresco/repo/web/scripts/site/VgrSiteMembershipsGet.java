package se.vgregion.alfresco.repo.web.scripts.site;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.web.scripts.site.SiteMembershipsGet;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteMemberInfo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.resetpassword.ResetPasswordService;

public class VgrSiteMembershipsGet extends SiteMembershipsGet implements InitializingBean {

  private ResetPasswordService _resetPasswordService;

  public void setResetPasswordService(ResetPasswordService resetPasswordService) {
    _resetPasswordService = resetPasswordService;
  }

  @Override
  protected Map<String, Object> executeImpl(SiteInfo site, WebScriptRequest req, Status status, Cache cache) {
    Map<String, Object> model = super.executeImpl(site, req, status, cache);
    
    Map<String, String> zones = new HashMap<String, String>();

    String authorityType = req.getParameter("authorityType");

    @SuppressWarnings("unchecked")
    Map<String, SiteMemberInfo> members = (Map<String, SiteMemberInfo>) model.get("memberInfo");

    for (SiteMemberInfo authorityObj : members.values()) {
      String username = authorityObj.getMemberName();

      String ftlSafeName = "_" + username;

      if (authorityObj.getMemberName().startsWith("GROUP_")) {
        if (authorityType == null || authorityType.equals("GROUP")) {
          zones.put(ftlSafeName, "null");// No zones are populated for groups
        }
      } else {
        if (authorityType == null || authorityType.equals("USER")) {
          String zone;

          if (_resetPasswordService.isAdminUser(username)) {
            zone = "admin";
          } else if (_resetPasswordService.isUserInternalUser(username)) {
            zone = "internal";
          } else {
            zone = "external";
          }

          zones.put(ftlSafeName, zone);
        }
      }
    }

    model.put("zones", zones);

    return model;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_resetPasswordService);
  }

}
