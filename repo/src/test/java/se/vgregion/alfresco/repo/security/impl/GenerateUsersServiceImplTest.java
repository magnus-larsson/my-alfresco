package se.vgregion.alfresco.repo.security.impl;

import junit.framework.Assert;

import org.alfresco.service.cmr.security.PersonService;
import org.junit.Test;

public class GenerateUsersServiceImplTest {

  @Test
  public void testGenerateUsers() {
    final GenerateUsersServiceImpl service = new GenerateUsersServiceImpl();

    final PersonService personService = null;

    service.setPersonService(personService);

    final int generated = service.generateUsers(10);

    Assert.assertEquals(10, generated);
  }

}
