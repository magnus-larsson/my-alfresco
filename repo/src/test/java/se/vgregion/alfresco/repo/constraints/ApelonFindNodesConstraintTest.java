package se.vgregion.alfresco.repo.constraints;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ApelonFindNodesConstraintTest {

  ApelonFindNodesConstraint _constraint;

  @Before
  public void setup() {
    _constraint = new ApelonFindNodesConstraint();
    _constraint.setNamespace("foobar");
    _constraint.setPropertyName("name");
    _constraint.setPropertyValue("value");
  }

  @Test
  public void testGetType() {
    assertEquals("APELON_FIND_NODES", _constraint.getType());
  }

  @Test
  public void testGetParameters() {
    Map<String, Object> parameters = _constraint.getParameters();

    assertEquals("foobar", parameters.get("namespace"));
    assertEquals("name", parameters.get("propertyName"));
    assertEquals("value", parameters.get("propertyValue"));
  }

}
