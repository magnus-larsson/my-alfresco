package se.vgregion.alfresco.repo.security.impl;

import java.util.Map;

import org.alfresco.service.cmr.security.PersonService;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class GenerateUsersServiceImplTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };
  
  @SuppressWarnings("unchecked")
  @Test
  public void testGenerateUsers() {
    
    final PersonService personService = context.mock(PersonService.class);
    
    context.checking(new Expectations() {
      {
        exactly(10).of(personService).createPerson(with(any(Map.class)));
        will(returnValue(null));        
      }
    });
    
    final GenerateUsersServiceImpl service = new GenerateUsersServiceImpl();

    service.setPersonService(personService);

    final int generated = service.generateUsers(10);

    Assert.assertEquals(10, generated);
    
    context.assertIsSatisfied();
  }

}
