package se.vgregion.alfresco.repo.scripts;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.namespace.NamespaceService;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import se.vgregion.alfresco.repo.constraints.ApelonService;
import se.vgregion.alfresco.repo.model.ApelonNode;

public class ScriptApelonServiceTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Test
  public void testGetVocabulary() {
    final ApelonService apelonService = context.mock(ApelonService.class);

    final ScriptApelonService script = new ScriptApelonService();
    script.setApelonService(apelonService);

    final ApelonNode apelonNode = context.mock(ApelonNode.class);

    final List<ApelonNode> nodes = new ArrayList<ApelonNode>();
    nodes.add(apelonNode);

    final String path = "/this is a path";
    final boolean sort = true;

    context.checking(new Expectations() {
      {
        oneOf(apelonService).getVocabulary(path, sort);
        will(returnValue(nodes));
        oneOf(apelonNode).getName();
        will(returnValue("This is a name"));
      }
    });

    final List<ApelonNode> result = script.getVocabulary(path, sort);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("This is a name", result.get(0).getName());

    context.assertIsSatisfied();
  }

  @Test
  public void testFindNodes() {
    final ApelonService apelonService = context.mock(ApelonService.class);

    final ScriptApelonService script = new ScriptApelonService();
    script.setApelonService(apelonService);

    final String namespace = NamespaceService.CONTENT_MODEL_1_0_URI;
    final String propertyName = "cm:name";
    final String propertyValue = "This is a value for name";
    final boolean sort = true;

    final ApelonNode apelonNode = context.mock(ApelonNode.class);

    final List<ApelonNode> nodes = new ArrayList<ApelonNode>();
    nodes.add(apelonNode);

    context.checking(new Expectations() {
      {
        oneOf(apelonService).findNodes(namespace, propertyName, propertyValue, sort, 100000);
        will(returnValue(nodes));
        oneOf(apelonNode).getName();
        will(returnValue(propertyName));
      }
    });

    final List<ApelonNode> result = script.findNodes(namespace, propertyName, propertyValue, sort);

    Assert.assertEquals(1, result.size());

    Assert.assertEquals(propertyName, result.get(0).getName());

    context.assertIsSatisfied();
  }

  @Test
  public void testGetNodes() {
  }

  @Test
  public void testGetDocumentTypeList() {
  }

  @Test
  public void testGetRecordTypeList() {
  }

  @Test
  public void testSynchronise() {
  }

}
