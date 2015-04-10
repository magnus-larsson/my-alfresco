package se.vgregion.alfresco.repo.resetpassword;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.model.ContentModel;
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
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import se.vgregion.alfresco.repo.mail.SendMailService;
import se.vgregion.alfresco.repo.resetpassword.impl.ResetPasswordServiceImpl;

public class ResetPasswordServiceImplTest {
  public class TestSiteInfo implements SiteInfo {

    @Override
    public NodeRef getNodeRef() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getSitePreset() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getShortName() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getTitle() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void setTitle(String title) {
      // TODO Auto-generated method stub

    }

    @Override
    public String getDescription() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void setDescription(String description) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setIsPublic(boolean isPublic) {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean getIsPublic() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public SiteVisibility getVisibility() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void setVisibility(SiteVisibility visibility) {
      // TODO Auto-generated method stub

    }

    @Override
    public Map<QName, Serializable> getCustomProperties() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Serializable getCustomProperty(QName name) {
      // TODO Auto-generated method stub
      return null;
    }

  }

  Mockery context;

  SiteService siteService;
  PersonService personService;
  AuthorityService authorityService;
  SendMailService sendMailService;
  NodeService nodeService;
  MutableAuthenticationService authenticationService;
  SiteInfo siteInfo = new TestSiteInfo();
  SiteInfo siteInfoOther = new TestSiteInfo();
  Set<String> internalUserZones = new HashSet<String>();
  Set<String> externalUserZones = new HashSet<String>();

  Set<String> authoritiesForUser = new HashSet<String>();
  Set<String> authoritiesForAdmin = new HashSet<String>();

  ResetPasswordServiceImpl rps = new ResetPasswordServiceImpl();

  private static final Logger LOG = Logger.getLogger(ResetPasswordServiceImplTest.class);

  private static final String WORKSPACE_AND_STORE = "workspace://SpacesStore/";
  private static final String DUMMY_NODE_ID_1 = "cafebabe-cafe-babe-cafe-babecafebab1";

  @Before
  public void setUp() throws Exception {
    context = new Mockery();

    siteService = context.mock(SiteService.class);
    siteInfo = context.mock(SiteInfo.class);
    personService = context.mock(PersonService.class);
    authorityService = context.mock(AuthorityService.class);
    sendMailService = context.mock(SendMailService.class);
    nodeService = context.mock(NodeService.class);
    authenticationService = context.mock(MutableAuthenticationService.class);

    rps.setSiteService(siteService);
    rps.setPersonService(personService);
    rps.setAuthorityService(authorityService);
    rps.setSendMailService(sendMailService);
    rps.setNodeService(nodeService);
    rps.setMutableAuthenticationService(authenticationService);

    internalUserZones.add("TEST_ZONE");
    internalUserZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
    externalUserZones.add("TEST_ZONE");

    authoritiesForUser.add("TEST_AUTHORITY");
    authoritiesForAdmin.add("TEST_AUTHORITY");
    authoritiesForAdmin.add(PermissionService.ADMINISTRATOR_AUTHORITY);

  }

  @Test
  public void testIsUserASiteAdminForUserB() {

    context.checking(new Expectations() {
      {
        // siteService.getSite
        allowing(siteService).getSite("site");
        will(returnValue(siteInfo));
        allowing(siteService).getSite("othersite");
        will(returnValue(siteInfoOther));
        allowing(siteService).getSite("nonexistingsite");
        will(returnValue(null));
        // siteService.isMember
        allowing(siteService).isMember("site", "admin");
        will(returnValue(true));
        allowing(siteService).isMember("site", "noadmin");
        will(returnValue(true));
        allowing(siteService).isMember("site", "user");
        will(returnValue(true));
        allowing(siteService).isMember("othersite", "admin");
        will(returnValue(false));
        allowing(siteService).isMember("site", "usernotmember");
        will(returnValue(false));
        // siteService.getMembersRole
        allowing(siteService).getMembersRole("site", "admin");
        will(returnValue(SiteModel.SITE_MANAGER));
        allowing(siteService).getMembersRole("site", "noadmin");
        will(returnValue(SiteModel.SITE_CONSUMER));
        allowing(siteService).getMembersRole("site", "user");
        will(returnValue(SiteModel.SITE_CONSUMER));
        // personService.personExists
        allowing(personService).personExists("admin");
        will(returnValue(true));
        allowing(personService).personExists("user");
        will(returnValue(true));
        allowing(personService).personExists("noadmin");
        will(returnValue(true));
        allowing(personService).personExists("usernotmember");
        will(returnValue(true));
        allowing(personService).personExists("nonexistinguser");
        will(returnValue(false));

      }
    });

    assertTrue(rps.isUserASiteAdminForUserB("admin", "user", "site"));
    assertFalse(rps.isUserASiteAdminForUserB("noadmin", "user", "site"));
    assertFalse(rps.isUserASiteAdminForUserB("admin", "nonexistinguser", "site"));
    assertFalse(rps.isUserASiteAdminForUserB("admin", "usernotmember", "site"));
    assertFalse(rps.isUserASiteAdminForUserB("admin", "user", "othersite"));
    assertFalse(rps.isUserASiteAdminForUserB("admin", "user", "nonexistingsite"));
  }

  @Test
  public void testIsUserInternalUser() {

    context.checking(new Expectations() {
      {
        // personService.personExists
        allowing(personService).personExists("internaluser");
        will(returnValue(true));
        allowing(personService).personExists("externaluser");
        will(returnValue(true));
        allowing(personService).personExists("nonexistinguser");
        will(returnValue(false));
        // authorityService.getAuthorityZones
        allowing(authorityService).getAuthorityZones("internaluser");
        will(returnValue(internalUserZones));
        allowing(authorityService).getAuthorityZones("externaluser");
        will(returnValue(externalUserZones));
      }
    });

    assertTrue(rps.isUserInternalUser("internaluser"));
    assertFalse(rps.isUserInternalUser("externaluser"));
    assertFalse(rps.isUserInternalUser("nonexistinguser"));

  }

  @Test(expected = NoSuchPersonException.class)
  public void testResetPassword() {
    final NodeRef nr = new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_1);
    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(ContentModel.PROP_EMAIL, "to@email.com");
    final Properties globalProperties = new Properties();
    globalProperties.put("mail.from.default", "from@mail.com");
    rps.setGlobalProperties(globalProperties);
    I18NUtil.registerResourceBundle("vgr-mail");
    context.checking(new Expectations() {
      {
        // personService.personExists
        allowing(personService).personExists("user");
        will(returnValue(true));
        allowing(personService).personExists("nonexistinguser");
        will(returnValue(false));
        // personService.getPerson
        allowing(personService).getPerson("user", false);
        will(returnValue(nr));
        // personService.setPersonProperties
        allowing(authenticationService).setAuthentication(with("user"), with(any(char[].class)));
        // authorityService.getAuthoritiesForUser
        allowing(authorityService).getAuthoritiesForUser("user");
        will(returnValue(authoritiesForUser));
        // nodeService.getProperties
        allowing(nodeService).getProperties(nr);
        will(returnValue(properties));
        // sendMailService.sendTextMail
        allowing(sendMailService).sendTextMail(with(any(String.class)), with("from@mail.com"), with(any(String.class)), with(any(String.class)));
      }
    });

    String password;
    password = rps.resetPassword("user", false);
    assertTrue(password.length() == 8);
    password = rps.resetPassword("user", true);
    assertTrue(password.length() == 8);
    password = rps.resetPassword("nonexistinguser", false);
  }

}
