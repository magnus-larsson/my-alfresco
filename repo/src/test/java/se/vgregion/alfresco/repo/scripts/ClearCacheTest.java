package se.vgregion.alfresco.repo.scripts;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.repo.cache.SimpleCache;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
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
    WebScriptRequest req = context.mock(WebScriptRequest.class);

    Status status = context.mock(Status.class);

    Cache cache = context.mock(Cache.class);

    final ApplicationContext applicationContext = context.mock(ApplicationContext.class);

    final Map<String, SimpleCache> cacheMap = context.mock(Map.class);

    final Collection<SimpleCache> cacheValues = context.mock(Collection.class);

    final Iterator<SimpleCache> cacheIterator = context.mock(Iterator.class);
    
    final SimpleCache simpleCache = context.mock(SimpleCache.class);

    ClearCache clearCache = new ClearCache();

    clearCache.setApplicationContext(applicationContext);

    context.checking(new Expectations() {
      {
        allowing(applicationContext).getBeansOfType(SimpleCache.class);
        will(returnValue(cacheMap));
        allowing(cacheMap).values();
        will(returnValue(cacheValues));
        allowing(cacheValues).iterator();
        will(returnValue(cacheIterator));
        oneOf(cacheIterator).hasNext();
        will(returnValue(true));
        allowing(cacheIterator).next();
        will(returnValue(simpleCache));
        allowing(simpleCache).clear();
        oneOf(cacheIterator).hasNext();
        will(returnValue(false));
      }
    });

    clearCache.executeImpl(req, status, cache);

    context.assertIsSatisfied();
  }

}
