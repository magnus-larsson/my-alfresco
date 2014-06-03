package se.vgregion.alfresco.repo.scripts;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ClearCacheTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Test
  public void testExecuteImplWebScriptRequestStatusCache() {
    final WebScriptRequest req = context.mock(WebScriptRequest.class);

    final Status status = context.mock(Status.class);

    final Cache cache = context.mock(Cache.class);

    final ClearCache clearCache = new ClearCache();

    context.checking(new Expectations() {
      {
        
      }
    });

    clearCache.executeImpl(req, status, cache);

    context.assertIsSatisfied();
  }

}
