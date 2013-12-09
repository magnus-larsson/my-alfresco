package se.vgregion.alfresco.repo.ldap;

import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class Person {

  private String _firstName;

  private String _lastName;

  private String _userName;

  private String _organisation;

  private String _email;

  public Person(final String firstName, final String lastName, final String userName, final String organisation, final String email) {
    _firstName = firstName;
    _lastName = lastName;
    _userName = userName;
    _organisation = organisation;
    _email = email;
  }

  public String getFirstName() {
    return _firstName;
  }

  public void setFirstName(final String firstName) {
    _firstName = firstName;
  }

  public String getLastName() {
    return _lastName;
  }

  public void setLastName(final String lastName) {
    _lastName = lastName;
  }

  public String getUserName() {
    return _userName;
  }

  public void setUserName(final String userName) {
    _userName = userName;
  }

  public String getOrganisation() {
    return _organisation;
  }

  public void setOrganisation(final String organisation) {
    _organisation = organisation;
  }

  public String getRepresentation() {
    return ServiceUtilsImpl.getRepresentation(_firstName, _lastName, _userName, _organisation);
  }

  public String getEmail() {
    return _email;
  }

  public void setEmail(final String email) {
    _email = email;
  }

}
