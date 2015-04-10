package se.vgregion.alfresco.repo.scripts;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;
import se.vgregion.alfresco.repo.utils.ServiceUtils;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class GetPublishStatusTest {
  
  Mockery m;
  GetPublishStatus gps;
  
  NodeService nodeService;
  StorageService storageService;
  ServiceUtils serviceUtils;
  
  @Before
  public void setup() throws Exception {
    m = new JUnit4Mockery();
    m.setImposteriser(ClassImposteriser.INSTANCE);
    gps = new GetPublishStatus();
    nodeService = m.mock(NodeService.class);
    storageService = m.mock(StorageService.class);
    serviceUtils = m.mock(ServiceUtils.class);
    
    gps.setNodeService(nodeService);
    gps.setStorageService(storageService);
    gps.setServiceUtils(serviceUtils);
    
    
    gps.afterPropertiesSet();
    m.checking(new Expectations() {
      {
        
      }
    });
    
  }
  
  @Test
  public void testMissingDocumentId() {    
    Cache cache = null;
    final Status status = m.mock(Status.class);
    Map<String, String> templateVars = new HashMap<String, String>();
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        oneOf(status).setCode(400);
        oneOf(status).setMessage(with(any(String.class)));
        oneOf(status).setRedirect(true);
      }
    });
    gps.executeImpl(req, status, cache);
  }
  
  @Test
  public void testStatusNotPublished() {
    final String nodeString = "workspace://SpacesStore/node1";
    final NodeRef nodeRef = new NodeRef(nodeString);
    final NodeRef storageNodeRef = new NodeRef("workspace://SpaceStore/storagenode1");
    final List<NodeRef> storageNodeList = new ArrayList<NodeRef>();
    //storageNodeList.add(storageNodeRef);
    final Map<QName, Serializable> storageNodeProperties = new HashMap<QName, Serializable>();
    final String latestVersion = "1.0";
    final String latestPublishedVersion = "1.0";
    
    storageNodeProperties.put(VgrModel.PROP_IDENTIFIER_VERSION, latestPublishedVersion);
    //storageNodeProperties.put(VgrModel.PROP_PUBLISH_STATUS, GetPublishStatus.OK);
    //storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    
    final Cache cache = null;
    final Status status = m.mock(Status.class);
    
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("document_id", nodeString);
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        
        oneOf(storageService).getStorageVersions(nodeString);
        will(returnValue(storageNodeList));
        
        oneOf(nodeService).exists(nodeRef);
        will(returnValue(true));       
        
      }
    });
    
    
    Map<String, Object> executeImpl = gps.executeImpl(req, status, cache);
    String result = (String) executeImpl.get("result");
    assertEquals(GetPublishStatus.STATUS_NOT_PUBLISHED, result);
    
    m.assertIsSatisfied();
  }
  
  @Test
  public void testStatusPublished() {
    final String nodeString = "workspace://SpacesStore/node1";
    final NodeRef nodeRef = new NodeRef(nodeString);
    final NodeRef storageNodeRef = new NodeRef("workspace://SpaceStore/storagenode1");
    final List<NodeRef> storageNodeList = new ArrayList<NodeRef>();
    storageNodeList.add(storageNodeRef);
    final Map<QName, Serializable> storageNodeProperties = new HashMap<QName, Serializable>();
    final String latestVersion = "1.0";
    final String latestPublishedVersion = "1.0";
    
    storageNodeProperties.put(VgrModel.PROP_IDENTIFIER_VERSION, latestPublishedVersion);
    storageNodeProperties.put(VgrModel.PROP_PUBLISH_STATUS, GetPublishStatus.OK);
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    
    final Cache cache = null;
    final Status status = m.mock(Status.class);
    
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("document_id", nodeString);
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        
        oneOf(storageService).getStorageVersions(nodeString);
        will(returnValue(storageNodeList));
        
        oneOf(nodeService).exists(nodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).exists(storageNodeRef);
        will(returnValue(true));
        
        oneOf(serviceUtils).isPublished(storageNodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).hasAspect(storageNodeRef, VgrModel.ASPECT_PUBLISHED);
        will(returnValue(true));
        
        //Fetch properties of the storage node
        oneOf(nodeService).getProperties(storageNodeRef);
        will(returnValue(storageNodeProperties));
        
        //Fetch the latest version of the node
        oneOf(nodeService).getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(latestVersion));
      }
    });
    
    
    Map<String, Object> executeImpl = gps.executeImpl(req, status, cache);
    String result = (String) executeImpl.get("result");
    assertEquals(GetPublishStatus.STATUS_PUBLISHED, result);
    
    m.assertIsSatisfied();
  }
  
  @Test
  public void testStatusSentForPublish() {
    final String nodeString = "workspace://SpacesStore/node1";
    final NodeRef nodeRef = new NodeRef(nodeString);
    final NodeRef storageNodeRef = new NodeRef("workspace://SpaceStore/storagenode1");
    final List<NodeRef> storageNodeList = new ArrayList<NodeRef>();
    storageNodeList.add(storageNodeRef);
    final Map<QName, Serializable> storageNodeProperties = new HashMap<QName, Serializable>();
    final String latestVersion = "1.0";
    final String latestPublishedVersion = "1.0";
    
    storageNodeProperties.put(VgrModel.PROP_IDENTIFIER_VERSION, latestPublishedVersion);
    //storageNodeProperties.put(VgrModel.PROP_PUBLISH_STATUS, GetPublishStatus.OK);
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    
    final Cache cache = null;
    final Status status = m.mock(Status.class);
    
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("document_id", nodeString);
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        
        oneOf(storageService).getStorageVersions(nodeString);
        will(returnValue(storageNodeList));
        
        oneOf(nodeService).exists(nodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).exists(storageNodeRef);
        will(returnValue(true));
        
        oneOf(serviceUtils).isPublished(storageNodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).hasAspect(storageNodeRef, VgrModel.ASPECT_PUBLISHED);
        will(returnValue(true));
        
        //Fetch properties of the storage node
        oneOf(nodeService).getProperties(storageNodeRef);
        will(returnValue(storageNodeProperties));
        
        //Fetch the latest version of the node
        oneOf(nodeService).getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(latestVersion));
      }
    });
    
    
    Map<String, Object> executeImpl = gps.executeImpl(req, status, cache);
    String result = (String) executeImpl.get("result");
    assertEquals(GetPublishStatus.STATUS_SENT_FOR_PUBLISH, result);
    
    m.assertIsSatisfied();
  }
  
  @Test
  public void testStatusPublishedError() {
    final String nodeString = "workspace://SpacesStore/node1";
    final NodeRef nodeRef = new NodeRef(nodeString);
    final NodeRef storageNodeRef = new NodeRef("workspace://SpaceStore/storagenode1");
    final List<NodeRef> storageNodeList = new ArrayList<NodeRef>();
    storageNodeList.add(storageNodeRef);
    final Map<QName, Serializable> storageNodeProperties = new HashMap<QName, Serializable>();
    final String latestVersion = "1.0";
    final String latestPublishedVersion = "1.0";
    
    storageNodeProperties.put(VgrModel.PROP_IDENTIFIER_VERSION, latestPublishedVersion);
    storageNodeProperties.put(VgrModel.PROP_PUBLISH_STATUS, GetPublishStatus.ERROR);
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    
    final Cache cache = null;
    final Status status = m.mock(Status.class);
    
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("document_id", nodeString);
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        
        oneOf(storageService).getStorageVersions(nodeString);
        will(returnValue(storageNodeList));
        
        oneOf(nodeService).exists(nodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).exists(storageNodeRef);
        will(returnValue(true));
        
        oneOf(serviceUtils).isPublished(storageNodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).hasAspect(storageNodeRef, VgrModel.ASPECT_PUBLISHED);
        will(returnValue(true));
        
        //Fetch properties of the storage node
        oneOf(nodeService).getProperties(storageNodeRef);
        will(returnValue(storageNodeProperties));
        
        //Fetch the latest version of the node
        oneOf(nodeService).getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(latestVersion));
      }
    });
    
    
    Map<String, Object> executeImpl = gps.executeImpl(req, status, cache);
    String result = (String) executeImpl.get("result");
    assertEquals(GetPublishStatus.STATUS_PUBLISH_ERROR, result);
    
    m.assertIsSatisfied();
  }
  
  @Test
  public void testStatusSentForUnpublish() {
    final String nodeString = "workspace://SpacesStore/node1";
    final NodeRef nodeRef = new NodeRef(nodeString);
    final NodeRef storageNodeRef = new NodeRef("workspace://SpaceStore/storagenode1");
    final List<NodeRef> storageNodeList = new ArrayList<NodeRef>();
    storageNodeList.add(storageNodeRef);
    final Map<QName, Serializable> storageNodeProperties = new HashMap<QName, Serializable>();
    final String latestVersion = "1.0";
    final String latestPublishedVersion = "1.0";
    
    storageNodeProperties.put(VgrModel.PROP_IDENTIFIER_VERSION, latestPublishedVersion);
    storageNodeProperties.put(VgrModel.PROP_PUBLISH_STATUS, GetPublishStatus.OK);
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_UNPUBLISH, new Date());
    
    final Cache cache = null;
    final Status status = m.mock(Status.class);
    
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("document_id", nodeString);
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        
        oneOf(storageService).getStorageVersions(nodeString);
        will(returnValue(storageNodeList));
        
        oneOf(nodeService).exists(nodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).exists(storageNodeRef);
        will(returnValue(true));
        
        oneOf(serviceUtils).isPublished(storageNodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).hasAspect(storageNodeRef, VgrModel.ASPECT_PUBLISHED);
        will(returnValue(true));
        
        //Fetch properties of the storage node
        oneOf(nodeService).getProperties(storageNodeRef);
        will(returnValue(storageNodeProperties));
        
        //Fetch the latest version of the node
        oneOf(nodeService).getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(latestVersion));
      }
    });
    
    
    Map<String, Object> executeImpl = gps.executeImpl(req, status, cache);
    String result = (String) executeImpl.get("result");
    assertEquals(GetPublishStatus.STATUS_SENT_FOR_UNPUBLISH, result);
    
    m.assertIsSatisfied();
  }
  
  @Test
  public void testStatusUnpublished() {
    final String nodeString = "workspace://SpacesStore/node1";
    final NodeRef nodeRef = new NodeRef(nodeString);
    final NodeRef storageNodeRef = new NodeRef("workspace://SpaceStore/storagenode1");
    final List<NodeRef> storageNodeList = new ArrayList<NodeRef>();
    storageNodeList.add(storageNodeRef);
    final Map<QName, Serializable> storageNodeProperties = new HashMap<QName, Serializable>();
    final String latestVersion = "1.0";
    final String latestPublishedVersion = "1.0";
    
    storageNodeProperties.put(VgrModel.PROP_IDENTIFIER_VERSION, latestPublishedVersion);
    storageNodeProperties.put(VgrModel.PROP_PUBLISH_STATUS, GetPublishStatus.OK);
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_UNPUBLISH, new Date());
    storageNodeProperties.put(VgrModel.PROP_UNPUBLISH_STATUS, GetPublishStatus.OK);
    
    final Cache cache = null;
    final Status status = m.mock(Status.class);
    
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("document_id", nodeString);
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        
        oneOf(storageService).getStorageVersions(nodeString);
        will(returnValue(storageNodeList));
        
        oneOf(nodeService).exists(nodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).exists(storageNodeRef);
        will(returnValue(true));
        
        oneOf(serviceUtils).isPublished(storageNodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).hasAspect(storageNodeRef, VgrModel.ASPECT_PUBLISHED);
        will(returnValue(true));
        
        //Fetch properties of the storage node
        oneOf(nodeService).getProperties(storageNodeRef);
        will(returnValue(storageNodeProperties));
        
        //Fetch the latest version of the node
        oneOf(nodeService).getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(latestVersion));
      }
    });
    
    
    Map<String, Object> executeImpl = gps.executeImpl(req, status, cache);
    String result = (String) executeImpl.get("result");
    assertEquals(GetPublishStatus.STATUS_UNPUBLISHED, result);
    
    m.assertIsSatisfied();
  }
  
  @Test
  public void testStatusUnpublishError() {
    final String nodeString = "workspace://SpacesStore/node1";
    final NodeRef nodeRef = new NodeRef(nodeString);
    final NodeRef storageNodeRef = new NodeRef("workspace://SpaceStore/storagenode1");
    final List<NodeRef> storageNodeList = new ArrayList<NodeRef>();
    storageNodeList.add(storageNodeRef);
    final Map<QName, Serializable> storageNodeProperties = new HashMap<QName, Serializable>();
    final String latestVersion = "1.0";
    final String latestPublishedVersion = "1.0";
    
    storageNodeProperties.put(VgrModel.PROP_IDENTIFIER_VERSION, latestPublishedVersion);
    storageNodeProperties.put(VgrModel.PROP_PUBLISH_STATUS, GetPublishStatus.OK);
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_UNPUBLISH, new Date());
    storageNodeProperties.put(VgrModel.PROP_UNPUBLISH_STATUS, GetPublishStatus.ERROR);
    
    final Cache cache = null;
    final Status status = m.mock(Status.class);
    
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("document_id", nodeString);
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        
        oneOf(storageService).getStorageVersions(nodeString);
        will(returnValue(storageNodeList));
        
        oneOf(nodeService).exists(nodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).exists(storageNodeRef);
        will(returnValue(true));
        
        oneOf(serviceUtils).isPublished(storageNodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).hasAspect(storageNodeRef, VgrModel.ASPECT_PUBLISHED);
        will(returnValue(true));
        
        //Fetch properties of the storage node
        oneOf(nodeService).getProperties(storageNodeRef);
        will(returnValue(storageNodeProperties));
        
        //Fetch the latest version of the node
        oneOf(nodeService).getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(latestVersion));
      }
    });
    
    
    Map<String, Object> executeImpl = gps.executeImpl(req, status, cache);
    String result = (String) executeImpl.get("result");
    assertEquals(GetPublishStatus.STATUS_UNPUBLISH_ERROR, result);
    
    m.assertIsSatisfied();
  }
  
  @Test
  public void testStatusPreviouslyPublished() {
    final String nodeString = "workspace://SpacesStore/node1";
    final NodeRef nodeRef = new NodeRef(nodeString);
    final NodeRef storageNodeRef = new NodeRef("workspace://SpaceStore/storagenode1");
    final List<NodeRef> storageNodeList = new ArrayList<NodeRef>();
    storageNodeList.add(storageNodeRef);
    final Map<QName, Serializable> storageNodeProperties = new HashMap<QName, Serializable>();
    final String latestVersion = "1.1";
    final String latestPublishedVersion = "1.0";
    
    storageNodeProperties.put(VgrModel.PROP_IDENTIFIER_VERSION, latestPublishedVersion);
    storageNodeProperties.put(VgrModel.PROP_PUBLISH_STATUS, GetPublishStatus.OK);
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_UNPUBLISH, new Date());
    storageNodeProperties.put(VgrModel.PROP_UNPUBLISH_STATUS, GetPublishStatus.ERROR);
    
    final Cache cache = null;
    final Status status = m.mock(Status.class);
    
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("document_id", nodeString);
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        
        oneOf(storageService).getStorageVersions(nodeString);
        will(returnValue(storageNodeList));
        
        oneOf(nodeService).exists(nodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).exists(storageNodeRef);
        will(returnValue(true));
        
        oneOf(serviceUtils).isPublished(storageNodeRef);
        will(returnValue(false));
        
        oneOf(nodeService).hasAspect(storageNodeRef, VgrModel.ASPECT_PUBLISHED);
        will(returnValue(true));
        
        //Fetch properties of the storage node
        oneOf(nodeService).getProperties(storageNodeRef);
        will(returnValue(storageNodeProperties));
        
        //Fetch the latest version of the node
        oneOf(nodeService).getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(latestVersion));
      }
    });
    
    
    Map<String, Object> executeImpl = gps.executeImpl(req, status, cache);
    String result = (String) executeImpl.get("result");
    assertEquals(GetPublishStatus.STATUS_PREVIOUSLY_PUBLISHED, result);
    
    m.assertIsSatisfied();
  }
  
  @Test
  public void testStatusPreviousVersionIsPublished() {
    final String nodeString = "workspace://SpacesStore/node1";
    final NodeRef nodeRef = new NodeRef(nodeString);
    final NodeRef storageNodeRef = new NodeRef("workspace://SpaceStore/storagenode1");
    final List<NodeRef> storageNodeList = new ArrayList<NodeRef>();
    storageNodeList.add(storageNodeRef);
    final Map<QName, Serializable> storageNodeProperties = new HashMap<QName, Serializable>();
    final String latestVersion = "1.1";
    final String latestPublishedVersion = "1.0";
    
    storageNodeProperties.put(VgrModel.PROP_IDENTIFIER_VERSION, latestPublishedVersion);
    storageNodeProperties.put(VgrModel.PROP_PUBLISH_STATUS, GetPublishStatus.OK);
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_PUBLISH, new Date());
    storageNodeProperties.put(VgrModel.PROP_PUSHED_FOR_UNPUBLISH, new Date());
    storageNodeProperties.put(VgrModel.PROP_UNPUBLISH_STATUS, GetPublishStatus.OK);
    
    final Cache cache = null;
    final Status status = m.mock(Status.class);
    
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("document_id", nodeString);
    final WebScriptRequest req = m.mock(WebScriptRequest.class);
    final Match match = new Match(null, templateVars, null);
    m.checking(new Expectations() {
      {
        oneOf(req).getServiceMatch();
        will(returnValue(match));
        
        oneOf(storageService).getStorageVersions(nodeString);
        will(returnValue(storageNodeList));
        
        oneOf(nodeService).exists(nodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).exists(storageNodeRef);
        will(returnValue(true));
        
        oneOf(serviceUtils).isPublished(storageNodeRef);
        will(returnValue(true));
        
        oneOf(nodeService).hasAspect(storageNodeRef, VgrModel.ASPECT_PUBLISHED);
        will(returnValue(true));
        
        //Fetch properties of the storage node
        oneOf(nodeService).getProperties(storageNodeRef);
        will(returnValue(storageNodeProperties));
        
        //Fetch the latest version of the node
        oneOf(nodeService).getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);
        will(returnValue(latestVersion));
      }
    });
    
    
    Map<String, Object> executeImpl = gps.executeImpl(req, status, cache);
    String result = (String) executeImpl.get("result");
    assertEquals(GetPublishStatus.STATUS_PREVIOUS_VERSION_PUBLISHED, result);
    
    m.assertIsSatisfied();
  }

}
