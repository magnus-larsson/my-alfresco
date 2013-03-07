package se.vgregion.alfresco.repo.resetpassword;

import org.alfresco.repo.processor.BaseProcessorExtension;

public class JavascriptCompatibleResetPasswordService
    extends BaseProcessorExtension
    implements ResetPasswordService
{
    private ResetPasswordService impl;

    public void setResetPasswordService(final ResetPasswordService impl)
    {
        this.impl = impl;
    }

	@Override
	public boolean isUserASiteAdminForUserB(String userA, String userB,
			String site) {
		return(impl.isUserASiteAdminForUserB(userA, userB, site));
	}

	@Override
	public boolean isUserInternalUser(String user) {
		return(impl.isUserInternalUser(user));
	}

	@Override
	public String resetPassword(String user, boolean sendMail) {
		throw new UnsupportedOperationException("This method is not supported in the JavaScript API due to security reasons.");
	}

	@Override
	public boolean isAdminUser(String user) {
		return(impl.isAdminUser(user));
	}
}