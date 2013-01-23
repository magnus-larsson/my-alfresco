package se.vgregion.alfresco.repo.scripts;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class Published extends DeclarativeWebScript {

  private Properties _globalProperties;

  public void setGlobalProperties(final Properties globalProperties) {
    _globalProperties = globalProperties;
  }

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
    final Map<String, Object> model = new HashMap<String, Object>();

    model.put("host", _globalProperties.getProperty("alfresco.host", "localhost"));
    model.put("port", _globalProperties.getProperty("alfresco.port", "8080"));
    model.put("context", _globalProperties.getProperty("alfresco.context", "alfresco"));

    return model;
  }

}
