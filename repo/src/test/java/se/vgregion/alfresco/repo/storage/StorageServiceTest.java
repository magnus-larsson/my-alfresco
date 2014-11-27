package se.vgregion.alfresco.repo.storage;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.impl.StorageServiceImpl;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class StorageServiceTest {

  Mockery m;
  NodeService nodeService;
  StorageServiceImpl ssi;
  private FileFolderService fileFolderService;
  private PermissionService permissionService;
  private CopyService copyService;
  private ServiceUtils serviceUtils;
  private BehaviourFilter behaviourFilter;
  private RenditionService renditionService;
  private ContentService contentService;
  private SearchService searchService;
  private DictionaryService dictionaryService;
  private Repository repository;
  private ActionService actionService;
  private RetryingTransactionHelper retryingTransactionHelper;

  final NodeRef nodeRef1 = new NodeRef("workspace://SpacesStore/node1");
  final NodeRef companyHomeNodeRef = new NodeRef("workspace://SpacesStore/CompanyHome");
  final NodeRef storageNodeRef = new NodeRef("workspace://SpacesStore/StorageNodeRef");

  @Before
  public void setup() throws Exception {
    m = new JUnit4Mockery();
    m.setImposteriser(ClassImposteriser.INSTANCE);
    fileFolderService = m.mock(FileFolderService.class);
    nodeService = m.mock(NodeService.class);
    permissionService = m.mock(PermissionService.class);
    copyService = m.mock(CopyService.class);
    serviceUtils = m.mock(ServiceUtils.class);
    behaviourFilter = m.mock(BehaviourFilter.class);
    renditionService = m.mock(RenditionService.class);
    contentService = m.mock(ContentService.class);
    searchService = m.mock(SearchService.class);
    dictionaryService = m.mock(DictionaryService.class);
    repository = m.mock(Repository.class);
    actionService = m.mock(ActionService.class);
    retryingTransactionHelper = m.mock(RetryingTransactionHelper.class);

    ssi = new StorageServiceImpl();
    ssi.setFileFolderService(fileFolderService);
    ssi.setNodeService(nodeService);
    ssi.setPermissionService(permissionService);
    ssi.setCopyService(copyService);
    ssi.setServiceUtils(serviceUtils);
    ssi.setBehaviourFilter(behaviourFilter);
    ssi.setRenditionService(renditionService);
    ssi.setContentService(contentService);
    ssi.setSearchService(searchService);
    ssi.setDictionaryService(dictionaryService);
    ssi.setRepository(repository);
    ssi.setActionService(actionService);
    ssi.setRetryingTransactionHelper(retryingTransactionHelper);
    ssi.afterPropertiesSet();

    m.checking(new Expectations() {
      {
        allowing(repository).getCompanyHome();
        will(returnValue(companyHomeNodeRef));
        allowing(fileFolderService).searchSimple(companyHomeNodeRef, StorageService.STORAGE_LAGRET);
        will(returnValue(storageNodeRef));
      }
    });
  }

  @After
  public void teardown() {

  }

  @Test
  public void testPublishAlfrescoDocToStorageFolderExists() {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());

    final String year = String.valueOf(calendar.get(Calendar.YEAR));
    final String month = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.MONTH) + 1), 2, "0");
    final String day = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)), 2, "0");

    final NodeRef yearNodeRef = new NodeRef("workspace://SpacesStore/year");
    final NodeRef monthNodeRef = new NodeRef("workspace://SpacesStore/month");
    final NodeRef dayNodeRef = new NodeRef("workspace://SpacesStore/day");
    m.checking(new Expectations() {
      {
        oneOf(nodeService).getChildByName(storageNodeRef, ContentModel.ASSOC_CONTAINS, year);
        will(returnValue(yearNodeRef));
        oneOf(nodeService).getChildByName(yearNodeRef, ContentModel.ASSOC_CONTAINS, month);
        will(returnValue(monthNodeRef));
        oneOf(nodeService).getChildByName(monthNodeRef, ContentModel.ASSOC_CONTAINS, day);
        will(returnValue(dayNodeRef));
      }
    });

    // Test folder structure: Lagret/2014/11/10
    assertEquals(dayNodeRef, ssi.createAlfrescoFolderStructure());
    m.assertIsSatisfied();
  }

  @Test
  public void testPublishAlfrescoDocToStorageFolderCreate() {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());

    final String year = String.valueOf(calendar.get(Calendar.YEAR));
    final String month = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.MONTH) + 1), 2, "0");
    final String day = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)), 2, "0");

    final NodeRef yearNodeRef = new NodeRef("workspace://SpacesStore/year");
    final NodeRef monthNodeRef = new NodeRef("workspace://SpacesStore/month");
    final NodeRef dayNodeRef = new NodeRef("workspace://SpacesStore/day");
    final FileInfo dayFileInfo = m.mock(FileInfo.class);

    m.checking(new Expectations() {
      {
        oneOf(nodeService).getChildByName(storageNodeRef, ContentModel.ASSOC_CONTAINS, year);
        will(returnValue(yearNodeRef));
        oneOf(nodeService).getChildByName(yearNodeRef, ContentModel.ASSOC_CONTAINS, month);
        will(returnValue(monthNodeRef));
        oneOf(nodeService).getChildByName(monthNodeRef, ContentModel.ASSOC_CONTAINS, day);
        will(returnValue(null));
        oneOf(fileFolderService).create(monthNodeRef, day, ContentModel.TYPE_FOLDER);
        will(returnValue(dayFileInfo));
        oneOf(dayFileInfo).getNodeRef();
        will(returnValue(dayNodeRef));
        oneOf(permissionService).setInheritParentPermissions(dayNodeRef, true);
      }
    });

    // Test folder structure: Lagret/YYYY/MM/DD
    assertEquals(dayNodeRef, ssi.createAlfrescoFolderStructure());
    m.assertIsSatisfied();
  }

  @Test
  public void testPublishBariumDocToStorageFolderExists() {
    /**
     * Barium number 2, version X -> Lagret/Barium/2/versions/x Barium number
     * 22, version X -> Lagret/Barium/2/2/versions/x Barium number 222, version
     * X -> Lagret/Barium/2/2/2/versions/x
     */

    // Barium number 23, version 1
    final NodeRef bariumNo23V1NodeRef = new NodeRef("workspace://SpacesStore/bariumNo23V1");
    final NodeRef bariumNo23V1FolderNodeRef = new NodeRef("workspace://SpacesStore/bariumNo23V1Folder");

    final String barium = "Barium";
    final String two = "2";
    final String three = "3";
    final String version = "1.0";
    final NodeRef bariumNodeRef = new NodeRef("workspace://SpacesStore/Barium");

    final NodeRef twoNodeRef = new NodeRef("workspace://SpacesStore/2");
    final NodeRef threeNodeRef = new NodeRef("workspace://SpacesStore/3");
    final NodeRef threeVersionsNodeRef = new NodeRef("workspace://SpacesStore/3_versions");

    m.checking(new Expectations() {
      {
        oneOf(nodeService).getChildByName(storageNodeRef, ContentModel.ASSOC_CONTAINS, barium);
        will(returnValue(bariumNodeRef));
        oneOf(nodeService).getProperty(bariumNo23V1NodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);
        will(returnValue("23"));
        oneOf(nodeService).getProperty(bariumNo23V1NodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(version));
        oneOf(nodeService).getChildByName(bariumNodeRef, ContentModel.ASSOC_CONTAINS, two);
        will(returnValue(twoNodeRef));
        oneOf(nodeService).getChildByName(twoNodeRef, ContentModel.ASSOC_CONTAINS, three);
        will(returnValue(threeNodeRef));
        oneOf(nodeService).getChildByName(threeNodeRef, ContentModel.ASSOC_CONTAINS, "versions");
        will(returnValue(threeVersionsNodeRef));
        oneOf(nodeService).getChildByName(threeVersionsNodeRef, ContentModel.ASSOC_CONTAINS, version);
        will(returnValue(bariumNo23V1FolderNodeRef));
      }
    });

    // Test folder structure
    assertEquals(bariumNo23V1FolderNodeRef, ssi.createBariumFolderStructure(bariumNo23V1NodeRef));
  }

  @Test
  public void testPublishBariumDocToStorageFolderCreate() {
    /**
     * Barium number 2, version X -> Lagret/Barium/2/versions/x Barium number
     * 22, version X -> Lagret/Barium/2/2/versions/x Barium number 222, version
     * X -> Lagret/Barium/2/2/2/versions/x
     */

    // Barium number 23, version 1
    final NodeRef bariumNo23V1NodeRef = new NodeRef("workspace://SpacesStore/bariumNo23V1");
    final NodeRef bariumNo23V1FolderNodeRef = new NodeRef("workspace://SpacesStore/bariumNo23V1Folder");

    final String barium = "Barium";
    final String two = "2";
    final String three = "3";
    final String version = "1.0";
    final NodeRef bariumNodeRef = new NodeRef("workspace://SpacesStore/Barium");

    final NodeRef twoNodeRef = new NodeRef("workspace://SpacesStore/2");
    final NodeRef threeNodeRef = new NodeRef("workspace://SpacesStore/3");
    final NodeRef threeVersionsNodeRef = new NodeRef("workspace://SpacesStore/3_versions");
    final FileInfo dayFileInfo = m.mock(FileInfo.class);

    m.checking(new Expectations() {
      {
        oneOf(nodeService).getChildByName(storageNodeRef, ContentModel.ASSOC_CONTAINS, barium);
        will(returnValue(bariumNodeRef));
        oneOf(nodeService).getProperty(bariumNo23V1NodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);
        will(returnValue("23"));
        oneOf(nodeService).getProperty(bariumNo23V1NodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(version));
        oneOf(nodeService).getChildByName(bariumNodeRef, ContentModel.ASSOC_CONTAINS, two);
        will(returnValue(twoNodeRef));
        oneOf(nodeService).getChildByName(twoNodeRef, ContentModel.ASSOC_CONTAINS, three);
        will(returnValue(threeNodeRef));

        oneOf(nodeService).getChildByName(threeNodeRef, ContentModel.ASSOC_CONTAINS, "versions");
        will(returnValue(threeVersionsNodeRef));
        oneOf(nodeService).getChildByName(threeVersionsNodeRef, ContentModel.ASSOC_CONTAINS, version);
        will(returnValue(null));
        oneOf(fileFolderService).create(threeVersionsNodeRef, version, ContentModel.TYPE_FOLDER);
        will(returnValue(dayFileInfo));
        oneOf(dayFileInfo).getNodeRef();
        will(returnValue(bariumNo23V1FolderNodeRef));
        oneOf(permissionService).setInheritParentPermissions(bariumNo23V1FolderNodeRef, true);
      }
    });

    // Test folder structure
    assertEquals(bariumNo23V1FolderNodeRef, ssi.createBariumFolderStructure(bariumNo23V1NodeRef));
  }

}
