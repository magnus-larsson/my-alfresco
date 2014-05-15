package se.vgregion.alfresco.repo.push.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.QName;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.publish.PublishingService;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class PuSHAtomFeedUtilTest {
  PuSHAtomFeedUtilImpl _puSHAtomFeedUtil;
  NodeService _nodeService;
  Descriptor _descriptor;
  DescriptorService _descriptorService;
  ServiceUtils _serviceUtils;
  PublishingService _publishingService;

  String downloadUrl = "http://localhost:8080/alfresco/service/vgr/storage/node/content/#documentId#?a=false&amp;guest=true";
  final NodeRef publishNodeRef = new NodeRef("workspace", "SpacesStore", "publish");
  final NodeRef unpublishNodeRef = new NodeRef("workspace", "SpacesStore", "unpublish");

  @Rule
  public JUnitRuleMockery _context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Before
  public void setUp() throws Exception {
    _descriptorService = _context.mock(DescriptorService.class);
    _nodeService = _context.mock(NodeService.class);
    _descriptor = _context.mock(Descriptor.class);
    _serviceUtils = _context.mock(ServiceUtils.class);
    _publishingService = _context.mock(PublishingService.class);
    _context.checking(new Expectations() {
      {
        // siteService.getSite
        allowing(_descriptorService).getServerDescriptor();
        will(returnValue(_descriptor));
        allowing(_descriptor).getVersion();
        will(returnValue("VERSION"));
        allowing(_descriptor).getName();
        will(returnValue("Alfresco"));
        allowing(_descriptor).getEdition();
        will(returnValue("Enterprise"));
      }
    });
    _puSHAtomFeedUtil = new PuSHAtomFeedUtilImpl();
    _puSHAtomFeedUtil.setDescriptorService(_descriptorService);
    _puSHAtomFeedUtil.setNodeService(_nodeService);
    _puSHAtomFeedUtil.setServiceUtils(_serviceUtils);
    _puSHAtomFeedUtil.setDownloadUrl(downloadUrl);
    _puSHAtomFeedUtil.setPublishingService(_publishingService);
    _puSHAtomFeedUtil.afterPropertiesSet();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testHeader() {
    String createHeader = _puSHAtomFeedUtil.createHeader(new NodeRef("workspace", "SpacesStore", "id"));
    
    assertNotNull(createHeader);
    
    System.out.println(createHeader);
  }

  @Test
  public void testFooter() {
    String createFooter = _puSHAtomFeedUtil.createFooter();
    assertNotNull(createFooter);
    System.out.println(createFooter);
  }

  @Test
  public void testCreateDataTag() {
    String createDataTag = _puSHAtomFeedUtil.createDataTag("aName", "aValue", null);
    assertEquals("test", PuSHAtomFeedUtilImpl.TAB + "<aName>aValue</aName>" + PuSHAtomFeedUtilImpl.NEWLINE, createDataTag);
  }

  @Test
  public void testCreateEntryDataTag() {
    String createDataTag = _puSHAtomFeedUtil.createEntryDataTag("aName", "aValue", null);
    assertEquals("test", PuSHAtomFeedUtilImpl.TAB + PuSHAtomFeedUtilImpl.TAB + "<aName>aValue</aName>" + PuSHAtomFeedUtilImpl.NEWLINE, createDataTag);
  }

  @Test
  public void testCreatePublishDocumentFeed() {
    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    properties.put(VgrModel.PROP_DATE_AVAILABLE_FROM, new Date());
    properties.put(ContentModel.PROP_NAME, "test.doc");
    properties.put(VgrModel.PROP_DATE_ISSUED, new Date());
    properties.put(VgrModel.PROP_TITLE_ALTERNATIVE, "Titel 1,Titel 2");
    properties.put(VgrModel.PROP_SOURCE_DOCUMENTID, 1234);
    properties.put(VgrModel.PROP_IDENTIFIER, "http://localhost:8080/alfresco/service/vgr/storage/node/content/workspace/SpacesStore/a38c5f20-dfe6-4e7d-b51a-3f1c400b02f6?a=false&guest=true");
    _context.checking(new Expectations() {
      {
        allowing(_nodeService).exists(publishNodeRef);
        will(returnValue(true));
        allowing(_nodeService).getProperties(publishNodeRef);
        will(returnValue(properties));
      }
    });

    String createPublishDocumentFeed = _puSHAtomFeedUtil.createPublishDocumentFeed(publishNodeRef);
    assertNotNull(createPublishDocumentFeed);
    System.out.println(createPublishDocumentFeed);
  }

  @Test
  public void testCreateUnPublishDocumentFeed() {
    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(VgrModel.PROP_PUSHED_FOR_UNPUBLISH, new Date());
    properties.put(VgrModel.PROP_DATE_AVAILABLE_TO, new Date());
    properties.put(ContentModel.PROP_NAME, "test.doc");
    properties.put(VgrModel.PROP_DATE_ISSUED, new Date());
    properties.put(VgrModel.PROP_TITLE_ALTERNATIVE, "Titel 1,Titel 2");
    properties.put(VgrModel.PROP_SOURCE_DOCUMENTID, 1234);
    _context.checking(new Expectations() {
      {
        allowing(_nodeService).exists(unpublishNodeRef);
        will(returnValue(true));
        allowing(_nodeService).getProperties(unpublishNodeRef);
        will(returnValue(properties));
      }
    });

    String createUnPublishDocumentFeed = _puSHAtomFeedUtil.createUnPublishDocumentFeed(unpublishNodeRef);
    assertNotNull(createUnPublishDocumentFeed);
    System.out.println(createUnPublishDocumentFeed);
  }
  
  /*
  @Test
  public void testCreateDocumentFeed() {
    final Date from = DateTime.now().minusMinutes(30).toDate();
    final Date to = new Date();
    final Date now = _context.mock(Date.class);
    final NodeRefCallbackHandler callback = _context.mock(NodeRefCallbackHandler.class);
    
    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    properties.put(VgrModel.PROP_DATE_AVAILABLE_FROM, new Date());
    properties.put(ContentModel.PROP_NAME, "test.doc");
    properties.put(VgrModel.PROP_DATE_ISSUED, new Date());
    properties.put(VgrModel.PROP_TITLE_ALTERNATIVE, "Titel 1,Titel 2");
    properties.put(VgrModel.PROP_SOURCE_DOCUMENTID, 1234);
    properties.put(VgrModel.PROP_IDENTIFIER, "http://localhost:8080/alfresco/service/vgr/storage/node/content/workspace/SpacesStore/a38c5f20-dfe6-4e7d-b51a-3f1c400b02f6?a=false&guest=true");
    _context.checking(new Expectations() {
      {
        allowing(now).getTime();
        will(returnValue(with(any(Long.class))));
        allowing(_nodeService).exists(publishNodeRef);
        will(returnValue(true));
        allowing(_nodeService).getProperties(publishNodeRef);
        will(returnValue(properties));
        allowing(_publishingService).findPublishedDocuments(now, from, to, callback);
        allowing(_publishingService).findUnpublishedDocuments(with(any(Date.class)), with(any(Date.class)), with(any(Date.class)), with(any(NodeRefCallbackHandler.class)));
      }
    });
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    _puSHAtomFeedUtil.createDocumentFeed(from, to, baos);
    
    String feed = baos.toString();
    
    assertNotNull(feed);
    
    System.out.println(feed);
  }
  */
  
}
