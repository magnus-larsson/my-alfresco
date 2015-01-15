package se.vgregion.alfresco.repo.it.jobs;

import static org.junit.Assert.*;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import se.vgregion.alfresco.repo.it.AbstractVgrRepoIntegrationTest;
import se.vgregion.alfresco.repo.jobs.ExpireInvitationReceipts;

public class ExpireInvitationReceiptsIntegrationTest extends AbstractVgrRepoIntegrationTest {
  private static final Logger LOG = Logger.getLogger(ExpireInvitationReceiptsIntegrationTest.class);

  private static final String DEFAULT_USERNAME = "testuser"+System.currentTimeMillis();

  private static SiteInfo site;

  @Autowired
  @Qualifier("vgr.expireInvitationReceipts")
  private ExpireInvitationReceipts eir;
  
  @Autowired
  @Qualifier("InvitationService")
  private InvitationService invitationService;

  @Autowired
  @Qualifier("WorkflowService")
  private WorkflowService workflowService;
  
  @Override
  public void beforeClassSetup() {
    LOG.info("beforeClassSetup begin");
    super.beforeClassSetup();

    createUser(DEFAULT_USERNAME);

    _authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

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
  
  @Test
  public void testJoinRequest() {
    String membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertNull(membersRole);
    ModeratedInvitation inviteModerated = invitationService.inviteModerated("", DEFAULT_USERNAME, Invitation.ResourceType.WEB_SITE, site.getShortName(), SiteModel.SITE_CONSUMER);
    eir.execute();
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
    eir.execute();
    //No receipt generated which is expected
    membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertEquals(SiteModel.SITE_CONSUMER, membersRole);
    _siteService.removeMembership(site.getShortName(), DEFAULT_USERNAME);
    membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertNull(membersRole);
  }
  
  @Test
  public void testInvite() {
    String membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertNull(membersRole);
    NominatedInvitation inviteNominated = invitationService.inviteNominated(DEFAULT_USERNAME, Invitation.ResourceType.WEB_SITE, site.getShortName(), SiteModel.SITE_CONSUMER, "acceptUrl", "rejectUrl");
    eir.execute();
    WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
    taskQuery.setActive(null);
    taskQuery.setProcessId(inviteNominated.getInviteId());
    List<WorkflowTask> allTasks = _workflowService.queryTasks(taskQuery);
    assertNotNull(allTasks);
    assertEquals(1, allTasks.size());
    Invitation approve = invitationService.accept(inviteNominated.getInviteId(), inviteNominated.getTicket());
    
    allTasks = _workflowService.queryTasks(taskQuery);
    assertNotNull(allTasks);
    assertEquals(1, allTasks.size());
    //No receipt generated which is expected
    membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertEquals(SiteModel.SITE_CONSUMER, membersRole);
    
    eir.execute();
    
    allTasks = _workflowService.queryTasks(taskQuery);
    assertNotNull(allTasks);
    assertEquals(0, allTasks.size());
    
    _siteService.removeMembership(site.getShortName(), DEFAULT_USERNAME);
    membersRole = _siteService.getMembersRole(site.getShortName(), DEFAULT_USERNAME);
    assertNull(membersRole);
    eir.execute();
  }

}
