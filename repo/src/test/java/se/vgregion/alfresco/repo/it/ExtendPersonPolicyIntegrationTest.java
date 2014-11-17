package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.apache.log4j.Logger;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class ExtendPersonPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {
  private static final Logger LOG = Logger.getLogger(ExtendPersonPolicyIntegrationTest.class);
  private static final String DEFAULT_USERNAME = "testuser";
  private static SiteInfo site;
  private static NodeRef user;
  final static String organisationDn = "ou=Bemanningsservice Poolmedarbetare,ou=Verksamhet Bemanningsservice,ou=Område 2,ou=Sahlgrenska Universitetssjukhuset,ou=Org,o=VGR";
  final static String expectedOrganisation = "VGR/Org/Sahlgrenska Universitetssjukhuset/Område 2/Verksamhet Bemanningsservice/Bemanningsservice Poolmedarbetare";

  @Override
  public void beforeClassSetup() {
    LOG.debug("beforeClassSetup");
    super.beforeClassSetup();
    user = createUser(DEFAULT_USERNAME);
    LOG.debug("Created user " + DEFAULT_USERNAME + ": " + user);
    _authenticationComponent.setCurrentUser(DEFAULT_USERNAME);
    site = createSite();
    LOG.debug("Created site " + site.getShortName());
  }

  @Override
  public void afterClassSetup() {
    LOG.debug("afterClassSetup");
    super.afterClassSetup();
    deleteSite(site);
    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());
    deleteUser(DEFAULT_USERNAME);
    _authenticationComponent.clearCurrentSecurityContext();
  }

  @Test
  public void testAvatar() {
    LOG.debug("Running test testAvatar");
    assertNotNull(user);
    assertHasAspect("Noderef is " + user, user, VgrModel.ASPECT_PERSON);
    assertHasAspect("Noderef is " + user, user, VgrModel.ASPECT_THUMBNAIL_PHOTO);
    List<AssociationRef> avatars = _nodeService.getTargetAssocs(user, ContentModel.ASSOC_AVATAR);
    assertTrue(avatars.size() == 0);

    // Test that avatar is created successfully
    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        _nodeService.setProperty(user, VgrModel.PROP_THUMBNAIL_PHOTO, "abcdefghijklmn");
        return null;
      }
    }, false, true);
    avatars = _nodeService.getTargetAssocs(user, ContentModel.ASSOC_AVATAR);
    assertTrue(avatars.size() == 1);
  }

  @Test
  public void testOrgDn() throws InterruptedException {
    LOG.debug("Running test testOrgDn");
    assertNotNull(user);
    String organization = (String) _nodeService.getProperty(user, ContentModel.PROP_ORGANIZATION);
    assertNull(organization);

    String organizationId = (String) _nodeService.getProperty(user, ContentModel.PROP_ORGID);
    assertNull(organizationId);

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        LOG.debug("Setting organisationDn to " + organisationDn);
        _nodeService.setProperty(user, VgrModel.PROP_PERSON_ORGANIZATION_DN, organisationDn);
        return null;
      }
    }, false, true);
    Thread.sleep(2000); //We need to sleep here since the properties are updated in an transaction listener
    organization = (String) _nodeService.getProperty(user, ContentModel.PROP_ORGANIZATION);
    assertEquals(expectedOrganisation, organization);

    organizationId = (String) _nodeService.getProperty(user, ContentModel.PROP_ORGID);
    assertEquals(expectedOrganisation, organizationId);
  }

}
