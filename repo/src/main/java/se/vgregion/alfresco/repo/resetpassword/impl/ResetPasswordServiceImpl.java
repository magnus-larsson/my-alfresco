package se.vgregion.alfresco.repo.resetpassword.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.I18NUtil;

import se.vgregion.alfresco.repo.mail.SendMailService;
import se.vgregion.alfresco.repo.resetpassword.ResetPasswordService;

public class ResetPasswordServiceImpl implements ResetPasswordService {

  private static final Logger LOG = Logger
      .getLogger(ResetPasswordServiceImpl.class);
  private SiteService siteService;
  private PersonService personService;
  private NodeService nodeService;
  private AuthorityService authorityService;
  private SendMailService sendMailService;
  private Properties globalProperties;
  private MutableAuthenticationService mutableAuthenticationService;

  private static final int PASSWORD_LENGTH = 8;

  @Override
  public boolean isUserASiteAdminForUserB(final String userA,
      final String userB, final String site) {
    return AuthenticationUtil.runAs(
        new AuthenticationUtil.RunAsWork<Boolean>() {

          @Override
          public Boolean doWork() throws Exception {
            if (userA == null || userB == null || site == null) {
              LOG.error("One of the mandatory fields was null");
              return false;
            }

            // Get site
            SiteInfo siteInfo = siteService.getSite(site);
            if (siteInfo == null) {
              LOG.error("Site " + site + " could not be found");
              return false;
            }

            // Find the role of user A and make sure it is site
            // admin on site
            if (!personService.personExists(userA)) {
              LOG.error("User " + userA + " does not exist");
              return false;
            }
            if (!siteService.isMember(site, userA)) {
              LOG.error("User " + userA
                  + " is not a member of site " + site);
              return false;
            }
            String membersRole = siteService.getMembersRole(site,
                userA);
            if (!SiteModel.SITE_MANAGER
                .equalsIgnoreCase(membersRole)) {
              LOG.error("User " + userA
                  + " is not site manager for site " + site);
              return false;
            }

            // Make sure user B is a member of site
            if (!personService.personExists(userB)) {
              LOG.error("User " + userB + " does not exist");
              return false;
            }
            if (!siteService.isMember(site, userB)) {
              LOG.error("User " + userB
                  + " is not a member of site " + site);
              return false;
            }

            // Make sure that user A and user B are not the same
            if (userA.equalsIgnoreCase(userB)) {
              LOG.error("UserA and UserB is equal: " + userA);
              return false;
            }
            if (LOG.isDebugEnabled()) {
              LOG.debug("User " + userA
                  + " is site admin on site " + site
                  + " and user " + userB
                  + " is a member of the site");
            }
            return true;
          }

        }, AuthenticationUtil.getSystemUserName());

  }

  @Override
  public boolean isUserInternalUser(final String user) {
    return AuthenticationUtil.runAs(
        new AuthenticationUtil.RunAsWork<Boolean>() {

          @Override
          public Boolean doWork() throws Exception {
            // Check if user exists
            if (!personService.personExists(user)) {
              LOG.error("User " + user + " does not exist");
              return false;
            }
            try {
              Set<String> authorityZones = authorityService
                  .getAuthorityZones(user);
              if (authorityZones
                  .contains(AuthorityService.ZONE_AUTH_ALFRESCO)) {
                if (LOG.isTraceEnabled()) {
                  LOG.trace("User " + user
                      + " is internal alfresco user");
                }
                return true;
              } else {
                if (LOG.isTraceEnabled()) {
                  LOG.trace("User " + user
                      + " is an ldap user");
                }
                return false;
              }
            } catch (NoSuchPersonException e) {
              return false;
            }
          }

        }, AuthenticationUtil.getSystemUserName());
  }

  @Override
  public boolean isAdminUser(final String user) {
    return AuthenticationUtil.runAs(
        new AuthenticationUtil.RunAsWork<Boolean>() {

          @Override
          public Boolean doWork() throws Exception {
            // Check if user exists
            if (!personService.personExists(user)) {
              LOG.error("User " + user + " does not exist");
              return false;
            }

            // Check if user is admin, if so, request should be
            // denied
            if (authorityService
                .getAuthoritiesForUser(user)
                .contains(
                    PermissionService.ADMINISTRATOR_AUTHORITY)) {
              if (LOG.isTraceEnabled()) {
                LOG.trace("User " + user + " is admin");
              }
              return true;
            } else {
              if (LOG.isTraceEnabled()) {
                LOG.trace("User " + user + " is not admin");
              }
              return false;
            }
          }

        }, AuthenticationUtil.getSystemUserName());
  }

  @Override
  public String resetPassword(final String user, final boolean sendMail) {
    return AuthenticationUtil.runAs(
        new AuthenticationUtil.RunAsWork<String>() {

          @Override
          public String doWork() throws Exception {
            // Check if user exists
            if (!personService.personExists(user)) {
              LOG.error("User " + user + " does not exist");
              throw new NoSuchPersonException(user);
            }
            NodeRef personNodeRef = personService.getPerson(user,
                false);

            // Check if user is admin, if so, request should be
            // denied
            if (isAdminUser(user)) {
              throw new IllegalArgumentException(
                  "Cannot reset password for an admin user: "
                      + user);
            }

            Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
            String password = generatePassword();
            properties.put(ContentModel.PROP_PASSWORD, password);

            if (sendMail) {
              final Map<QName, Serializable> originalUserProperties = nodeService
                  .getProperties(personNodeRef);
              sendMail(user, password, originalUserProperties);
            }

            LOG.info("User "
                + AuthenticationUtil
                .getFullyAuthenticatedUser()
                + " generated a new password for user " + user);
            // Reset the password
            mutableAuthenticationService.setAuthentication(user,
                password.toCharArray());
            return password;
          }

        }, AuthenticationUtil.getSystemUserName());
  }

  /**
   * Will generate a random alphanumeric password
   * 
   * @return
   */
  private String generatePassword() {
    return RandomStringUtils.randomAlphanumeric(PASSWORD_LENGTH);
  }

  /**
   * Generate the email that should be sent and call the send mail service
   * 
   * @param user
   * @param password
   * @param originalUserProperties
   */
  private void sendMail(String user, String password,
      Map<QName, Serializable> originalUserProperties) {

    String subject = I18NUtil.getMessage("vgr.mail.resetpassword.subject");
    ;
    String from = globalProperties.getProperty("mail.from.default");
    String to = (String) originalUserProperties
        .get(ContentModel.PROP_EMAIL);
    String body = I18NUtil.getMessage("vgr.mail.resetpassword.body", user,
        password);

    sendMailService.sendTextMail(subject, from, to, body);
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setAuthorityService(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  public void setSendMailService(SendMailService sendMailService) {
    this.sendMailService = sendMailService;
  }

  public void setGlobalProperties(final Properties globalProperties) {
    this.globalProperties = globalProperties;
  }

  public void setMutableAuthenticationService(
      MutableAuthenticationService mutableAuthenticationService) {
    this.mutableAuthenticationService = mutableAuthenticationService;
  }
}
