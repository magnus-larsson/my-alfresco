package se.vgregion.alfresco.repo.resetpassword;

public interface ResetPasswordService {
	/**
	 * Checks if user A is site admin on site where user B is a member. User A must not be equal to User B.
	 * @param userA Username
	 * @param userB Username
	 * @param site Site shortname
	 * @return
	 */
	public boolean isUserASiteAdminForUserB(String userA, String userB, String site);
	/**
	 * Checks if user is an internal user
	 * @param user Username
	 * @return
	 */
	public boolean isUserInternalUser(String user);
	
	/**
	 * Checks if user is an admin user
	 * @param user Username
	 * @return
	 */
	public boolean isAdminUser(String user);
	
	/**
	 * Resets the password for a user, the sendMail flag controls whether the password should be sent as an email.
	 * This method excepts checks have been made by the user to control whether the user is allowed to change its password before this method is called.
	 * The password cannot be reset for admin users
	 * @param user Username
	 * @param sendMail
	 * @return
	 */
	public String resetPassword(String user, boolean sendMail);
}
