package se.vgregion.alfresco.repo.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

public class ServiceUtilsTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Test
  public void testGetBaseLink() {
    final Properties properties = new Properties();

    final ServiceUtils utils = new ServiceUtils();
    utils.setGlobalProperties(properties);

    String link = utils.getBaseLink("holger");
    assertEquals("http://alfresco.vgregion.se/holger", link);

    properties.put("holger.port", "80");
    link = utils.getBaseLink("holger");
    assertEquals("http://alfresco.vgregion.se/holger", link);

    properties.put("holger.port", "8080");
    link = utils.getBaseLink("holger");
    assertEquals("http://alfresco.vgregion.se:8080/holger", link);

    properties.put("holger.port", "443");
    link = utils.getBaseLink("holger");
    assertEquals("https://alfresco.vgregion.se/holger", link);

    properties.put("urban.port", "1234");
    link = utils.getBaseLink("urban");
    assertEquals("http://alfresco.vgregion.se:1234/urban", link);
  }

  @Test
  public void testGetSourceSuccess() {
    final Properties properties = new Properties();
    properties.put("alfresco.host", "www.myserver.com");
    properties.put("alfresco.port", "443");
    properties.put("alfresco.context", "repo");

    final ServiceUtils utils = new ServiceUtils();
    utils.setGlobalProperties(properties);

    final NodeService nodeService = context.mock(NodeService.class);
    utils.setNodeService(nodeService);

    final NodeRef publishedNodeRef = new NodeRef("workspace://SpacesStore/publishedNodeRef");
    final NodeRef sourceNodeRef = new NodeRef("workspace://SpacesStore/sourceNodeRef");

    context.checking(new Expectations() {
      {
        oneOf(nodeService).getProperty(publishedNodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);
        will(returnValue(sourceNodeRef));
      }
    });

    final String source = utils.getDocumentIdentifier(publishedNodeRef);

    assertEquals("https://www.myserver.com/repo/service/vgr/storage/node/content/workspace/SpacesStore/sourceNodeRef?a=false&guest=true", source);

    context.assertIsSatisfied();
  }

  @Test
  public void testGetSourceFail() throws Exception {
    final Properties properties = new Properties();

    final ServiceUtils utils = new ServiceUtils();
    utils.setGlobalProperties(properties);

    final NodeService nodeService = context.mock(NodeService.class);
    utils.setNodeService(nodeService);

    final NodeRef publishedNodeRef = new NodeRef("workspace://SpacesStore/publishedNodeRef");

    context.checking(new Expectations() {
      {
        oneOf(nodeService).getProperty(publishedNodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);
        will(returnValue(null));
      }
    });

    try {
      utils.getDocumentIdentifier(publishedNodeRef);
      throw new RuntimeException("Must not come here...");
    } catch (final RuntimeException ex) {
      assertTrue(ex.getMessage(), ex.getMessage().indexOf("has no vgr:dc.source.documentid") > 0);
    }

    context.assertIsSatisfied();
  }

}
