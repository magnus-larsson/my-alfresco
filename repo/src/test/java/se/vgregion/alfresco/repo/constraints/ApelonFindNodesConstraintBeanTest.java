package se.vgregion.alfresco.repo.constraints;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.ApelonNode;

public class ApelonFindNodesConstraintBeanTest {

  ApelonFindNodesConstraintBean _constraint;

  @Rule
  public JUnitRuleMockery _context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };
  
  ApelonService _apelonService;

  @Before
  public void setup() {
    _apelonService = _context.mock(ApelonService.class);
    _constraint = new ApelonFindNodesConstraintBean();
    _constraint.setApelonService(_apelonService);
  }

  @Test
  public void testGetAllowedValues() {
    ApelonNode apelonNode = new ApelonNode();
    apelonNode.setName("foobar");
    
    final List<ApelonNode> result = new ArrayList<ApelonNode>();
    result.add(apelonNode);
    
    _context.checking(new Expectations() {
      {
        allowing(_apelonService).findNodes("namespace", "name", "value", true);
        will(returnValue(result));
      }
    });
    
    List<String> allowedValues = _constraint.getAllowedValues("namespace", "name", "value");
    
    assertEquals(result.size(), allowedValues.size());
    assertEquals("foobar", allowedValues.get(0));
    
    _context.assertIsSatisfied();
  }

}
