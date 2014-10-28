package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.*;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class ExtendPersonPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

  private static final String DEFAULT_USERNAME = "testuser";

  public static final String ORGANIZATION_DN = "ou=Bemanningsservice Poolmedarbetare,ou=Verksamhet Bemanningsservice,ou=Område 2,ou=Sahlgrenska Universitetssjukhuset,ou=Org,o=VGR";

  @Test
  public void test() {
    try {
      final NodeRef user = createUser(DEFAULT_USERNAME);

      AuthenticationUtil.runAs(new RunAsWork<Void>() {

        @Override
        public Void doWork() throws Exception {
          testAsUser(user);

          return null;
        }

      }, DEFAULT_USERNAME);
    } finally {
      deleteUser(DEFAULT_USERNAME);
    }
  }

  protected void testAsUser(NodeRef user) {
    String expectedOrganisation = "VGR/Org/Sahlgrenska Universitetssjukhuset/Område 2/Verksamhet Bemanningsservice/Bemanningsservice Poolmedarbetare";

    assertHasAspect(user, VgrModel.ASPECT_PERSON);
    assertHasAspect(user, VgrModel.ASPECT_THUMBNAIL_PHOTO);
    
    List<AssociationRef> avatars = _nodeService.getTargetAssocs(user, ContentModel.ASSOC_AVATAR);
    assertTrue(avatars.size() == 0);
    
    _nodeService.setProperty(user, VgrModel.PROP_THUMBNAIL_PHOTO, "abcdefghijklmn");
    
    avatars = _nodeService.getTargetAssocs(user, ContentModel.ASSOC_AVATAR);
    assertTrue(avatars.size() == 1);
    
    String organization = (String) _nodeService.getProperty(user, ContentModel.PROP_ORGANIZATION);
    assertNull(organization);

    String organizationId = (String) _nodeService.getProperty(user, ContentModel.PROP_ORGID);
    assertNull(organizationId);
    
    _nodeService.setProperty(user, VgrModel.PROP_PERSON_ORGANIZATION_DN, ORGANIZATION_DN);
    
    organization = (String) _nodeService.getProperty(user, ContentModel.PROP_ORGANIZATION);
    assertEquals(expectedOrganisation, organization);

    organizationId = (String) _nodeService.getProperty(user, ContentModel.PROP_ORGID);
    assertEquals(expectedOrganisation, organizationId);
  }

}
