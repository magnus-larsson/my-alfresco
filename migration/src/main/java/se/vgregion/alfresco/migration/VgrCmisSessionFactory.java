package se.vgregion.alfresco.migration;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class VgrCmisSessionFactory {

  private String _username;

  private String _password;

  private String _url;

  private String _objectFactoryClass;

  public void setUsername(final String username) {
    _username = username;
  }

  public void setPassword(final String password) {
    _password = password;
  }

  public void setUrl(final String url) {
    _url = url;
  }

  public void setObjectFactoryClass(final String objectFactoryClass) {
    _objectFactoryClass = objectFactoryClass;
  }

  public Session createSession() {
    final Map<String, String> parameter = new HashMap<String, String>();

    // user credentials
    parameter.put(SessionParameter.USER, _username);
    parameter.put(SessionParameter.PASSWORD, _password);

    // connection settings
    parameter.put(SessionParameter.ATOMPUB_URL, _url);
    parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

    // set the object factory
    parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, _objectFactoryClass);

    // create session
    final SessionFactory factory = SessionFactoryImpl.newInstance();
    return factory.getRepositories(parameter).get(0).createSession();
  }

}
