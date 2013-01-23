package se.vgregion.alfresco.repo.scripts;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;

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

    final List<CacheManager> cacheManagers = new ArrayList<CacheManager>();
    final CacheManager cacheManager = context.mock(CacheManager.class);
    cacheManagers.add(cacheManager);

    final ClearCache clearCache = new ClearCache();
    clearCache.setCacheManagers(cacheManagers);

    context.checking(new Expectations() {
      {
        oneOf(cacheManager).clearAll();
      }
    });

    clearCache.executeImpl(req, status, cache);

    context.assertIsSatisfied();
  }

}
