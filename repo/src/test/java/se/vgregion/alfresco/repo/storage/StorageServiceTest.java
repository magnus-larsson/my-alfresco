package se.vgregion.alfresco.repo.storage;

import static org.junit.Assert.fail;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;

public class StorageServiceTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Test
  public void testPublishToStorageString() {
    // final ServiceUtils serviceUtils = context.mock(ServiceUtils.class);
    //
    // final SearchService searchService = context.mock(SearchService.class);
    //
    // final FileFolderService fileFolderService =
    // context.mock(FileFolderService.class);
    //
    // final ResultSet resultSet = context.mock(ResultSet.class);
    //
    // final NodeService nodeService = context.mock(NodeService.class);
    //
    // final PermissionService permissionService =
    // context.mock(PermissionService.class);
    //
    // final BehaviourFilter behaviourFilter =
    // context.mock(BehaviourFilter.class);
    //
    // final StorageServiceImpl storageService = new StorageServiceImpl();
    // storageService.setServiceUtils(serviceUtils);
    // storageService.setSearchService(searchService);
    // storageService.setFileFolderService(fileFolderService);
    // storageService.setNodeService(nodeService);
    // storageService.setPermissionService(permissionService);
    // storageService.setBehaviourFilter(behaviourFilter);
    //
    // final String sourceNodeRef =
    // "workspace://SpacesStore/12345-12345-12345-12345";
    // final NodeRef nodeRef = new NodeRef(sourceNodeRef);
    // final String user = "admin";
    // final String query = "PATH:\"/app:company_home\"";
    // final NodeRef rootNode = new NodeRef("workspace://SpacesStore/rootNode");
    // final NodeRef storageNode = new
    // NodeRef("workspace://SpacesStore/storageNode");
    // final FileInfo yearFolder = context.mock(FileInfo.class, "year");
    // final NodeRef yearFolderNodeRef = new
    // NodeRef("workspace://SpacesStore/yearNode" + getYear());
    // final FileInfo monthFolder = context.mock(FileInfo.class, "month");
    // final NodeRef monthFolderNodeRef = new
    // NodeRef("workspace://SpacesStore/monthNode" + getMonth());
    // final FileInfo dayFolder = context.mock(FileInfo.class, "day");
    // final NodeRef dayFolderNodeRef = new
    // NodeRef("workspace://SpacesStore/monthNode" + getMonth());
    //
    // context.checking(new Expectations() {
    // {
    // oneOf(serviceUtils).isSiteAdmin(nodeRef);
    // oneOf(serviceUtils).getCurrentUserName();
    // will(returnValue(user));
    // oneOf(searchService).query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    // SearchService.LANGUAGE_FTS_ALFRESCO, query);
    // will(returnValue(resultSet));
    // oneOf(resultSet).getNodeRef(0);
    // will(returnValue(rootNode));
    // oneOf(fileFolderService).searchSimple(rootNode, "Lagret");
    // will(returnValue(storageNode));
    // oneOf(nodeService).getType(storageNode);
    // will(returnValue(ContentModel.TYPE_FOLDER));
    //
    // oneOf(nodeService).getChildByName(storageNode,
    // ContentModel.ASSOC_CONTAINS, getYear());
    // will(returnValue(null));
    // oneOf(fileFolderService).create(storageNode, getYear(),
    // ContentModel.TYPE_FOLDER);
    // will(returnValue(yearFolder));
    // oneOf(yearFolder).getNodeRef();
    // will(returnValue(yearFolderNodeRef));
    // oneOf(permissionService).setInheritParentPermissions(yearFolderNodeRef,
    // true);
    //
    // oneOf(nodeService).getChildByName(yearFolderNodeRef,
    // ContentModel.ASSOC_CONTAINS, getMonth());
    // will(returnValue(null));
    // oneOf(fileFolderService).create(yearFolderNodeRef, getMonth(),
    // ContentModel.TYPE_FOLDER);
    // will(returnValue(monthFolder));
    // oneOf(monthFolder).getNodeRef();
    // will(returnValue(monthFolderNodeRef));
    // oneOf(permissionService).setInheritParentPermissions(monthFolderNodeRef,
    // true);
    //
    // oneOf(nodeService).getChildByName(monthFolderNodeRef,
    // ContentModel.ASSOC_CONTAINS, getDay());
    // will(returnValue(null));
    // oneOf(fileFolderService).create(monthFolderNodeRef, getDay(),
    // ContentModel.TYPE_FOLDER);
    // will(returnValue(dayFolder));
    // oneOf(dayFolder).getNodeRef();
    // will(returnValue(dayFolderNodeRef));
    // oneOf(permissionService).setInheritParentPermissions(dayFolderNodeRef,
    // true);
    //
    // oneOf(nodeService).getType(nodeRef);
    // will(returnValue(VgrModel.TYPE_VGR_DOCUMENT));
    // oneOf(fileFolderService).getType(VgrModel.TYPE_VGR_DOCUMENT);
    // will(returnValue(FileFolderServiceType.FILE));
    // oneOf(nodeService).getProperty(nodeRef, ContentModel.PROP_NAME);
    // will(returnValue("test name"));
    // oneOf(nodeService).getChildByName(dayFolderNodeRef,
    // ContentModel.ASSOC_CONTAINS, "test name");
    // oneOf(behaviourFilter).disableAllBehaviours();
    //
    // oneOf(nodeService).setProperty(nodeRef, VgrModel.PROP_DATE_ISSUED, new
    // Date());
    // }
    //
    // });

    final StorageService storageService = context.mock(StorageService.class);

    final String sourceNodeRef = "workspace://SpacesStore/12345-12345-12345-12345";

    context.checking(new Expectations() {
      {
        oneOf(storageService).publishToStorage(sourceNodeRef);
      }
    });

    storageService.publishToStorage(sourceNodeRef);
  }

  @Test
  public void testPublishToStorageNodeRef() {
    fail("Not yet implemented");
  }

  @Test
  public void testUnpublishFromStorage() {
    fail("Not yet implemented");
  }

  @Test
  public void testMoveToStorage() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetPublishedNodeRef() {
    fail("Not yet implemented");
  }

  @Test
  public void testCreatePdfRendition() {
    fail("Not yet implemented");
  }

  private String getYear() {
    final Calendar calendar = Calendar.getInstance();

    return String.valueOf(calendar.get(Calendar.YEAR));
  }

  private String getMonth() {
    final Calendar calendar = Calendar.getInstance();

    final String month = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.MONTH) + 1), 2, "0");

    return month;
  }

  private String getDay() {
    final Calendar calendar = Calendar.getInstance();

    final String day = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)), 2, "0");

    return day;
  }

}
