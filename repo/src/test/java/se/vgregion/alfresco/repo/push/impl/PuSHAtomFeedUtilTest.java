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
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class PuSHAtomFeedUtilTest {
  PuSHAtomFeedUtilImpl puSHAtomFeedUtil;
  Mockery context;
  NodeService nodeService;
  Descriptor descriptor;
  DescriptorService descriptorService;
  ServiceUtils serviceUtils;

  String downloadUrl = "http://localhost:8080/alfresco/service/vgr/storage/node/content/#documentId#?a=false&amp;guest=true";
  final NodeRef publishNodeRef = new NodeRef("workspace", "SpacesStore", "publish");
  final NodeRef unpublishNodeRef = new NodeRef("workspace", "SpacesStore", "unpublish");

  @Before
  public void setUp() throws Exception {
    context = new JUnit4Mockery();
    descriptorService = context.mock(DescriptorService.class);
    nodeService = context.mock(NodeService.class);
    descriptor = context.mock(Descriptor.class);
    serviceUtils = context.mock(ServiceUtils.class);
    context.checking(new Expectations() {
      {
        // siteService.getSite
        allowing(descriptorService).getServerDescriptor();
        will(returnValue(descriptor));
        allowing(descriptor).getVersion();
        will(returnValue("VERSION"));
        allowing(descriptor).getName();
        will(returnValue("Alfresco"));
        allowing(descriptor).getEdition();
        will(returnValue("Enterprise"));
      }
    });
    puSHAtomFeedUtil = new PuSHAtomFeedUtilImpl();
    puSHAtomFeedUtil.setDescriptorService(descriptorService);
    puSHAtomFeedUtil.setNodeService(nodeService);
    puSHAtomFeedUtil.setServiceUtils(serviceUtils);
    puSHAtomFeedUtil.setDownloadUrl(downloadUrl);
    puSHAtomFeedUtil.afterPropertiesSet();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testHeader() {
    Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

    String createHeader = puSHAtomFeedUtil.createHeader(new NodeRef("workspace", "SpacesStore", "id"), properties);
    assertNotNull(createHeader);
    System.out.println(createHeader);
  }

  @Test
  public void testFooter() {
    String createFooter = puSHAtomFeedUtil.createFooter();
    assertNotNull(createFooter);
    System.out.println(createFooter);
  }

  @Test
  public void testCreateDataTag() {
    String createDataTag = puSHAtomFeedUtil.createDataTag("aName", "aValue", null);
    assertEquals("test", PuSHAtomFeedUtilImpl.TAB + "<aName>aValue</aName>" + PuSHAtomFeedUtilImpl.NEWLINE, createDataTag);
  }

  @Test
  public void testCreateEntryDataTag() {
    String createDataTag = puSHAtomFeedUtil.createEntryDataTag("aName", "aValue", null);
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
    context.checking(new Expectations() {
      {
        allowing(nodeService).exists(publishNodeRef);
        will(returnValue(true));
        allowing(nodeService).getProperties(publishNodeRef);
        will(returnValue(properties));
      }
    });
    
    String createPublishDocumentFeed = puSHAtomFeedUtil.createPublishDocumentFeed(publishNodeRef);
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
    context.checking(new Expectations() {
      {
        allowing(nodeService).exists(unpublishNodeRef);
        will(returnValue(true));
        allowing(nodeService).getProperties(unpublishNodeRef);
        will(returnValue(properties));
      }
    });
    
    String createUnPublishDocumentFeed = puSHAtomFeedUtil.createUnPublishDocumentFeed(unpublishNodeRef);
    assertNotNull(createUnPublishDocumentFeed);
    System.out.println(createUnPublishDocumentFeed);
  }
}
