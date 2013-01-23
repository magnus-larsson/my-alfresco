package se.vgregion.alfresco.repo.scripts;

import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ProbeTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Test
  public void test() {
    final Properties properties = context.mock(Properties.class);
    final WebScriptRequest request = context.mock(WebScriptRequest.class);
    final Status status = context.mock(Status.class);
    final Cache cache = context.mock(Cache.class);

    final Probe probe = new Probe();
    probe.setGlobalProperties(properties);

    context.checking(new Expectations() {
      {
        oneOf(properties).getProperty("alfresco.host", "localhost");
        will(returnValue("prod.alfresco.com"));
        oneOf(properties).getProperty("alfresco.probe.host", "prod.alfresco.com");
        will(returnValue("probe.alfresco.com"));
      }
    });

    final Map<String, Object> result = probe.executeImpl(request, status, cache);

    final String host = (String) result.get("host");

    Assert.assertEquals("probe.alfresco.com", host);

    context.assertIsSatisfied();
  }

}
