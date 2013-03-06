package se.vgregion.alfresco.repo.scripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.vgregion.alfresco.repo.resetpassword.ResetPasswordService;

public class SiteAdminResetUserPassword extends DeclarativeWebScript {

	private ResetPasswordService resetPasswordService;

	@Override
	protected Map<String, Object> executeImpl(final WebScriptRequest req,
			final Status status, final Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>();

		Match serviceMatch = req.getServiceMatch();		
		Map<String, String> templateVars = serviceMatch.getTemplateVars();
		String site = templateVars.get("site");
		
		String user = templateVars.get("user");
		
		if (!resetPasswordService.isUserASiteAdminForUserB(AuthenticationUtil.getFullyAuthenticatedUser(), user, site)) {
			status.setCode(status.STATUS_FORBIDDEN,"Access denied: Either you are not site admin on site "+site+" or the user "+user+" is not a member of the site.");
		}		
		else if (!resetPasswordService.isUserInternalUser(user)) {
			status.setCode(status.STATUS_FORBIDDEN,"Access denied: Password cannot be set for an ldap user, only users managed by Alfresco can have their password reset.");
		} else {
			try {
				resetPasswordService.resetPassword(user, true);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return model;
	}

	public void setResetPasswordService(ResetPasswordService resetPasswordService) {
		this.resetPasswordService = resetPasswordService;
	}

	
}
