package se.vgregion.alfresco.repo.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ISO8601DateFormat;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

public class ReportSiteUsageTest {
  Mockery context;
  ServiceRegistry serviceRegistry;
  NodeService nodeService;
  FileFolderService fileFolderService;
  SiteService siteService;
  ActivityService activityService;
  FileInfo fileInfo;
  SiteInfo siteInfo;
  private static final String WORKSPACE_AND_STORE = "workspace://SpacesStore/";
  private static final String DUMMY_NODE_ID_1 = "cafebabe-cafe-babe-cafe-babecafebab1";
  private static final String DUMMY_NODE_ID_2 = "cafebabe-cafe-babe-cafe-babecafebab2";

  @Before
  public void setUp() throws Exception {

    context = new Mockery();
    serviceRegistry = context.mock(ServiceRegistry.class);
    nodeService = context.mock(NodeService.class);
    fileFolderService = context.mock(FileFolderService.class);
    siteService = context.mock(SiteService.class);
    activityService = context.mock(ActivityService.class);
    fileInfo = context.mock(FileInfo.class);
    siteInfo = context.mock(SiteInfo.class);

  }

  @Test
  public void testGetSiteSizeSuccess() {
    ReportSiteUsage rsu = new ReportSiteUsage();
    final NodeRef siteNodeRef1 = new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_1);
    final NodeRef siteNodeRef2 = new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_2);
    final List<FileInfo> listFolderInfoEmpty = new ArrayList<FileInfo>();
    final List<FileInfo> listFolderInfoNonEmpty = new ArrayList<FileInfo>();
    listFolderInfoNonEmpty.add(fileInfo);
    listFolderInfoNonEmpty.add(fileInfo);
    listFolderInfoNonEmpty.add(fileInfo);

    rsu.setActivityService(activityService);
    rsu.setFileFolderService(fileFolderService);
    rsu.setNodeService(nodeService);
    rsu.setSiteService(siteService);

    context.checking(new Expectations() {
      {
        allowing(serviceRegistry).getNodeService();
        will(returnValue(nodeService));

        allowing(serviceRegistry).getFileFolderService();
        will(returnValue(fileFolderService));

        allowing(fileFolderService).searchSimple(siteNodeRef1, ReportSiteUsage.DOCUMENT_LIBRARY);
        will(returnValue(siteNodeRef1));

        allowing(fileFolderService).listDeepFolders(siteNodeRef1, null);
        will(returnValue(listFolderInfoEmpty));

        allowing(fileFolderService).searchSimple(siteNodeRef2, ReportSiteUsage.DOCUMENT_LIBRARY);
        will(returnValue(siteNodeRef2));

        allowing(fileFolderService).listDeepFolders(siteNodeRef2, null);
        will(returnValue(listFolderInfoNonEmpty));

        allowing(fileInfo).getNodeRef();
        will(returnValue(siteNodeRef2));

        allowing(fileFolderService).listFiles(siteNodeRef2);
        will(returnValue(listFolderInfoNonEmpty));

        allowing(fileFolderService).listFiles(siteNodeRef1);
        will(returnValue(listFolderInfoEmpty));

        ContentData contentData = new ContentData("", "", 1, "");

        allowing(fileInfo).getContentData();
        will(returnValue(contentData));
        allowing(fileFolderService).listFiles(siteNodeRef2);
        will(returnValue(listFolderInfoNonEmpty));
        allowing(nodeService).exists(with(any(NodeRef.class)));
        will(returnValue(true));

      }
    });

    assertEquals(0, rsu.getSiteSize(siteNodeRef1));
    assertEquals(listFolderInfoNonEmpty.size() * listFolderInfoNonEmpty.size() + listFolderInfoNonEmpty.size(), rsu.getSiteSize(siteNodeRef2));
  }

  @Test
  public void testGetSiteSizeFailIfNodeDoesNotExist() {
    ReportSiteUsage rsu = new ReportSiteUsage();
    NodeRef siteNodeRef1 = new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_1);
    NodeRef siteNodeRef2 = new NodeRef("fail" + WORKSPACE_AND_STORE + DUMMY_NODE_ID_2);
    rsu.setActivityService(activityService);
    rsu.setFileFolderService(fileFolderService);
    rsu.setNodeService(nodeService);
    rsu.setSiteService(siteService);

    context.checking(new Expectations() {
      {
        allowing(nodeService).exists(with(any(NodeRef.class)));
        will(returnValue(false));
      }
    });
    
    try {
      rsu.getSiteSize(siteNodeRef1);
      fail();
    } catch (InvalidNodeRefException e) {

    }
    try {
      rsu.getSiteSize(siteNodeRef2);
      fail();
    } catch (InvalidNodeRefException e) {

    }
  }

  @Test
  public void testGetSiteSizeSuccessIfContentIsNull() {
    ReportSiteUsage rsu = new ReportSiteUsage();

    final NodeRef siteNodeRef = new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_1);

    rsu.setActivityService(activityService);
    rsu.setFileFolderService(fileFolderService);
    rsu.setNodeService(nodeService);
    rsu.setSiteService(siteService);

    final List<FileInfo> listFolderInfoNonEmpty = new ArrayList<FileInfo>();
    listFolderInfoNonEmpty.add(fileInfo);

    context.checking(new Expectations() {
      {
        allowing(serviceRegistry).getNodeService();
        will(returnValue(nodeService));

        allowing(nodeService).exists(with(any(NodeRef.class)));
        will(returnValue(true));

        allowing(serviceRegistry).getFileFolderService();
        will(returnValue(fileFolderService));

        allowing(fileFolderService).searchSimple(siteNodeRef, ReportSiteUsage.DOCUMENT_LIBRARY);
        will(returnValue(siteNodeRef));

        allowing(fileFolderService).listFiles(siteNodeRef);
        will(returnValue(listFolderInfoNonEmpty));

        allowing(fileInfo).getContentData();
        will(returnValue(null));

        allowing(fileFolderService).listDeepFolders(siteNodeRef, null);
        will(returnValue(listFolderInfoNonEmpty));

        allowing(fileInfo).getNodeRef();
        will(returnValue(siteNodeRef));

        allowing(fileFolderService).listFiles(siteNodeRef);
        will(returnValue(listFolderInfoNonEmpty));
      }
    });

    long size = rsu.getSiteSize(siteNodeRef);

    assertEquals(0, size);
  }

  @Test
  public void testGetNumberOfSiteMembersSuccess() throws Exception {
    ReportSiteUsage rsu = new ReportSiteUsage();

    final NodeRef siteNodeRef1 = new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_1);
    final NodeRef siteNodeRef2 = new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_2);
    final String shortname1 = "site1";
    final String shortname2 = "site2";

    rsu.setActivityService(activityService);
    rsu.setFileFolderService(fileFolderService);
    rsu.setNodeService(nodeService);
    rsu.setSiteService(siteService);

    context.checking(new Expectations() {
      {
        allowing(serviceRegistry).getNodeService();
        will(returnValue(nodeService));

        allowing(serviceRegistry).getSiteService();
        will(returnValue(siteService));

        allowing(nodeService).exists(with(any(NodeRef.class)));
        will(returnValue(true));

        allowing(siteInfo).getNodeRef();
        will(returnValue(siteNodeRef1));

        allowing(siteInfo).getShortName();
        will(returnValue("site1"));

        Map<String, String> map = new HashMap<String, String>();

        allowing(siteService).listMembers("site1", null, null, 0, true);
        will(returnValue(map));
      }
    });
    assertEquals(0, rsu.getNumberOfSiteMembers(siteInfo));

    setUp();

    rsu.setActivityService(activityService);
    rsu.setFileFolderService(fileFolderService);
    rsu.setNodeService(nodeService);
    rsu.setSiteService(siteService);

    context.checking(new Expectations() {
      {
        allowing(serviceRegistry).getNodeService();
        will(returnValue(nodeService));

        allowing(serviceRegistry).getSiteService();
        will(returnValue(siteService));

        allowing(nodeService).exists(with(any(NodeRef.class)));
        will(returnValue(true));

        allowing(siteInfo).getNodeRef();
        will(returnValue(siteNodeRef2));

        allowing(siteInfo).getShortName();
        will(returnValue("site2"));

        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "1");
        map.put("2", "2");
        allowing(siteService).listMembers("site2", null, null, 0, true);
        will(returnValue(map));
      }
    });

    assertEquals(2, rsu.getNumberOfSiteMembers(siteInfo));
  }

  @Test
  public void testGetNumberOfSiteMembersFail() {
    ReportSiteUsage rsu = new ReportSiteUsage();

    final NodeRef siteNodeRef1 = new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_1);

    rsu.setActivityService(activityService);
    rsu.setFileFolderService(fileFolderService);
    rsu.setNodeService(nodeService);
    rsu.setSiteService(siteService);

    context.checking(new Expectations() {
      {
        allowing(serviceRegistry).getNodeService();
        will(returnValue(nodeService));

        allowing(serviceRegistry).getSiteService();
        will(returnValue(siteService));

        allowing(nodeService).exists(with(any(NodeRef.class)));
        will(returnValue(false));

        allowing(siteInfo).getNodeRef();
        will(returnValue(siteNodeRef1));
      }
    });
    try {
      rsu.getNumberOfSiteMembers(siteInfo);
      assertTrue(false);
    } catch (InvalidNodeRefException e) {

    }
  }

  @Test
  public void testGetLastActivityOnSiteSuccess() throws Exception {
    ReportSiteUsage rsu = new ReportSiteUsage();

    rsu.setActivityService(activityService);
    rsu.setFileFolderService(fileFolderService);
    rsu.setNodeService(nodeService);
    rsu.setSiteService(siteService);

    context.checking(new Expectations() {
      {
        allowing(nodeService).exists(with(any(NodeRef.class)));
        will(returnValue(false));

        allowing(siteInfo).getShortName();
        will(returnValue("site1"));

        List<String> activityFeed = new ArrayList<String>();
        allowing(activityService).getSiteFeedEntries("site1", "json");
        will(returnValue(activityFeed));
      }
    });
    assertNull(rsu.getLastActivityOnSite(siteInfo));

    setUp();

    rsu.setActivityService(activityService);
    rsu.setFileFolderService(fileFolderService);
    rsu.setNodeService(nodeService);
    rsu.setSiteService(siteService);

    final Date testDate = new Date();
    context.checking(new Expectations() {
      {
        allowing(nodeService).exists(with(any(NodeRef.class)));
        will(returnValue(false));

        allowing(siteInfo).getShortName();
        will(returnValue("site2"));

        List<String> activityFeed = new ArrayList<String>();
        activityFeed.add("{\"postDate\": \"" + ISO8601DateFormat.format(testDate) + "\" }");
        allowing(activityService).getSiteFeedEntries("site2", "json");
        will(returnValue(activityFeed));
      }
    });

    assertTrue(testDate.compareTo(rsu.getLastActivityOnSite(siteInfo)) == 0);
  }
}
