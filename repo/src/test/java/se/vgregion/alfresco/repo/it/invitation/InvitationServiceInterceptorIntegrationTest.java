package se.vgregion.alfresco.repo.it.invitation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import se.vgregion.alfresco.repo.invitation.InvitationNotificationHelper;
import se.vgregion.alfresco.repo.invitation.InvitationServiceInterceptor;
import se.vgregion.alfresco.repo.it.AbstractVgrRepoIntegrationTest;

public class InvitationServiceInterceptorIntegrationTest extends AbstractVgrRepoIntegrationTest {
  private static final Logger LOG = Logger.getLogger(InvitationServiceInterceptorIntegrationTest.class);

  private static final String DEFAULT_USERNAME = "testuser" + System.currentTimeMillis();

  private static SiteInfo site;

  @Autowired
  @Qualifier("InvitationService")
  private InvitationService invitationService;

  @Autowired
  @Qualifier("WorkflowService")
  private WorkflowService workflowService;

  @Autowired
  @Qualifier("vgr.invitationServiceInterceptor")
  private InvitationServiceInterceptor invitationServiceInterceptor;

  @Autowired
  @Qualifier("vgr.inviteNotificationHelper")
  private InvitationNotificationHelper inviteNotificationHelper;

  Mockery m;
  InvitationNotificationHelper inviteNotificationHelperMock;

  @Override
  public void beforeClassSetup() {
    LOG.info("beforeClassSetup begin");
    super.beforeClassSetup();

    createUser(DEFAULT_USERNAME);

    _authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    // Set up a mock for mail testing in invitations

    site = createSite();
    LOG.info("beforeClassSetup end");
  }

  @Override
  public void afterClassSetup() {
    LOG.info("afterClassSetup begin");
    deleteSite(site);

    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());

    deleteUser(DEFAULT_USERNAME);

    _authenticationComponent.clearCurrentSecurityContext();

    super.afterClassSetup();
    LOG.info("afterClassSetup end");
  }

  public void testJoinRequestApprove() {
    String membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertNull(membersRole);
    ModeratedInvitation inviteModerated = invitationService.inviteModerated("", DEFAULT_USERNAME, Invitation.ResourceType.WEB_SITE, site.getShortName(), SiteModel.SITE_CONSUMER);
    WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
    taskQuery.setActive(null);
    taskQuery.setProcessId(inviteModerated.getInviteId());
    List<WorkflowTask> allTasks = _workflowService.queryTasks(taskQuery);
    assertNotNull(allTasks);
    assertEquals(1, allTasks.size());
    Invitation approve = invitationService.approve(inviteModerated.getInviteId(), "ok");

    allTasks = _workflowService.queryTasks(taskQuery);
    assertNotNull(allTasks);
    assertEquals(0, allTasks.size());
    // No receipt generated which is expected
    membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertEquals(SiteModel.SITE_CONSUMER, membersRole);
    _siteService.removeMembership(site.getShortName(), DEFAULT_USERNAME);
    membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertNull(membersRole);
  }

  public void testJoinRequestReject() {

    String membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertNull(membersRole);
    ModeratedInvitation inviteModerated = invitationService.inviteModerated("", DEFAULT_USERNAME, Invitation.ResourceType.WEB_SITE, site.getShortName(), SiteModel.SITE_CONSUMER);
    WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
    taskQuery.setActive(null);
    taskQuery.setProcessId(inviteModerated.getInviteId());
    List<WorkflowTask> allTasks = _workflowService.queryTasks(taskQuery);
    assertNotNull(allTasks);
    assertEquals(1, allTasks.size());
    Invitation approve = invitationService.reject(inviteModerated.getInviteId(), "not_ok");

    allTasks = _workflowService.queryTasks(taskQuery);
    assertNotNull(allTasks);
    assertEquals(0, allTasks.size());
    // No receipt generated which is expected
    membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertNull(membersRole);
  }

  @Test
  public void testJoinRequestApprove_real() {
    assertNotNull(inviteNotificationHelper);
    invitationServiceInterceptor.setInviteNotificationHelper(inviteNotificationHelper);
    testJoinRequestApprove();
  }

  @Test
  public void testJoinRequestApprove_mock() {
    m = new Mockery();
    inviteNotificationHelperMock = m.mock(InvitationNotificationHelper.class);
    m.checking(new Expectations() {
      {
        oneOf(inviteNotificationHelperMock).generateModeratedApproveMail(with(any(String.class)), with(any(String.class)));
      }
    });

    assertNotNull(inviteNotificationHelperMock);
    invitationServiceInterceptor.setInviteNotificationHelper(inviteNotificationHelperMock);
    testJoinRequestApprove();
  }

  @Test
  public void testJoinRequestReject_real() {
    assertNotNull(inviteNotificationHelper);
    invitationServiceInterceptor.setInviteNotificationHelper(inviteNotificationHelper);
    testJoinRequestReject();
  }

  @Test
  public void testJoinRequestReject_mock() {
    m = new Mockery();
    inviteNotificationHelperMock = m.mock(InvitationNotificationHelper.class);
    m.checking(new Expectations() {
      {
        oneOf(inviteNotificationHelperMock).generateModeratedRejectMail(with(any(String.class)), with(any(String.class)));
      }
    });

    assertNotNull(inviteNotificationHelperMock);
    invitationServiceInterceptor.setInviteNotificationHelper(inviteNotificationHelperMock);
    testJoinRequestReject();
  }
}
