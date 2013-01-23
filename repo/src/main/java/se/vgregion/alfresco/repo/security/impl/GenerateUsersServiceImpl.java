package se.vgregion.alfresco.repo.security.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;

import se.vgregion.alfresco.repo.security.GenerateUsersService;

public class GenerateUsersServiceImpl implements GenerateUsersService {

  private PersonService _personService;

  public void setPersonService(final PersonService personService) {
    _personService = personService;
  }

  @Override
  public int generateUsers(final int amount) {
    int created = 0;

    for (int x = 0; x < amount; x++) {
      generateUser();

      created++;
    }

    return created;
  }

  private void generateUser() {
    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

    _personService.createPerson(properties);
  }

}
