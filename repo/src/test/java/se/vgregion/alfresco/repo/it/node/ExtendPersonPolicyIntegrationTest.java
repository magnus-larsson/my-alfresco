package se.vgregion.alfresco.repo.it.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.junit.Test;

import se.vgregion.alfresco.repo.it.AbstractVgrRepoIntegrationTest;
import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class ExtendPersonPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {
  private static final Logger LOG = Logger.getLogger(ExtendPersonPolicyIntegrationTest.class);
  private static final String USER1_USERNAME = "testuser1";
  private static final String USER2_USERNAME = "testuser2";
  // private static SiteInfo site;
  private static NodeRef user1;
  private static NodeRef user2;
  final static String organisationDn = "ou=Bemanningsservice Poolmedarbetare,ou=Verksamhet Bemanningsservice,ou=Område 2,ou=Sahlgrenska Universitetssjukhuset,ou=Org,o=VGR";
  final static String expectedOrganisation = "VGR/Org/Sahlgrenska Universitetssjukhuset/Område 2/Verksamhet Bemanningsservice/Bemanningsservice Poolmedarbetare";

  @Override
  public void beforeClassSetup() {
    LOG.debug("beforeClassSetup");
    super.beforeClassSetup();

    user1 = createUser(USER1_USERNAME);
    LOG.debug("Created user " + USER1_USERNAME + ": " + user1);

    user2 = createUser(USER2_USERNAME);
    LOG.debug("Created user " + USER2_USERNAME + ": " + user2);

    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());

  }

  @Override
  public void afterClassSetup() {
    LOG.debug("afterClassSetup");
    super.afterClassSetup();

    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());
    deleteUser(USER1_USERNAME);
    deleteUser(USER2_USERNAME);
    _authenticationComponent.clearCurrentSecurityContext();
  }

  @Test
  public void testAvatar() {
    LOG.debug("Running test testAvatar");
    assertNotNull(user1);
    assertNotNull(user2);
    assertHasAspect(user1, VgrModel.ASPECT_PERSON);
    assertHasAspect(user1, VgrModel.ASPECT_THUMBNAIL_PHOTO);
    assertHasAspect(user2, VgrModel.ASPECT_PERSON);
    assertHasAspect(user2, VgrModel.ASPECT_THUMBNAIL_PHOTO);
    List<AssociationRef> avatars = _nodeService.getTargetAssocs(user1, ContentModel.ASSOC_AVATAR);
    assertTrue(avatars.size() == 0);
    avatars = _nodeService.getTargetAssocs(user2, ContentModel.ASSOC_AVATAR);
    assertTrue(avatars.size() == 0);

    // Test that avatar is created successfully
    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        _nodeService.setProperty(user1, VgrModel.PROP_THUMBNAIL_PHOTO, "abcdefghijklmn");
        _nodeService.setProperty(user2, VgrModel.PROP_THUMBNAIL_PHOTO, "abcdefghijklmn");
        return null;
      }
    }, false, true);
    avatars = _nodeService.getTargetAssocs(user1, ContentModel.ASSOC_AVATAR);
    assertTrue(avatars.size() == 1);
    avatars = _nodeService.getTargetAssocs(user2, ContentModel.ASSOC_AVATAR);
    assertTrue(avatars.size() == 1);
  }

  @Test
  public void testOrgDn() throws InterruptedException {
    LOG.debug("Running test testOrgDn");
    assertNotNull(user1);
    assertNotNull(user2);
    String organization = (String) _nodeService.getProperty(user1, ContentModel.PROP_ORGANIZATION);
    assertNull(organization);

    String organizationId = (String) _nodeService.getProperty(user1, ContentModel.PROP_ORGID);
    assertNull(organizationId);

    organization = (String) _nodeService.getProperty(user2, ContentModel.PROP_ORGANIZATION);
    assertNull(organization);

    organizationId = (String) _nodeService.getProperty(user2, ContentModel.PROP_ORGID);
    assertNull(organizationId);

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        LOG.debug("Setting organisationDn to " + organisationDn);
        _nodeService.setProperty(user1, VgrModel.PROP_PERSON_ORGANIZATION_DN, organisationDn);
        _nodeService.setProperty(user2, VgrModel.PROP_PERSON_ORGANIZATION_DN, organisationDn);
        return null;
      }
    }, false, true);
    for (int i = 0; i < 20; i++) {
      Thread.sleep(2000); // We need to sleep here since the properties are
                          // updated in an transaction listener
      organization = (String) _nodeService.getProperty(user1, ContentModel.PROP_ORGANIZATION);
      String organization2 = (String) _nodeService.getProperty(user2, ContentModel.PROP_ORGANIZATION);

      if (expectedOrganisation.equals(organization) && expectedOrganisation.equals(organization2)) {
        break;
      }
    }
    assertEquals(expectedOrganisation, organization);

    organizationId = (String) _nodeService.getProperty(user1, ContentModel.PROP_ORGID);
    assertEquals(expectedOrganisation, organizationId);

    organization = (String) _nodeService.getProperty(user2, ContentModel.PROP_ORGANIZATION);
    assertEquals(expectedOrganisation, organization);

    organizationId = (String) _nodeService.getProperty(user2, ContentModel.PROP_ORGID);
    assertEquals(expectedOrganisation, organizationId);
  }

}
