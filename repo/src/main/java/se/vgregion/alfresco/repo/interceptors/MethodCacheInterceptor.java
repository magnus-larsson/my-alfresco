package se.vgregion.alfresco.repo.interceptors;

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class MethodCacheInterceptor implements MethodInterceptor, InitializingBean {

  private static final Logger LOG = Logger.getLogger(MethodCacheInterceptor.class);

  private Cache _cache;

  public void setCache(Cache cache) {
    _cache = cache;
  }

  public Object invoke(MethodInvocation invocation) throws Throwable {
    String targetName = invocation.getThis().getClass().getName();
    String methodName = invocation.getMethod().getName();
    Object[] arguments = invocation.getArguments();

    Object result;

    String cacheKey = getCacheKey(targetName, methodName, arguments);
    Element element = _cache.get(cacheKey);

    LOG.debug("About the check if " + cacheKey + " is in the cache.");

    if (element == null) {
      // call target/sub-interceptor
      result = invocation.proceed();
      element = new Element(cacheKey, (Serializable) result);
      _cache.put(element);

      LOG.debug(cacheKey + " was not in the cache, but should be now");
    } else {
      LOG.debug(cacheKey + " was in the cache");
    }

    return element.getValue();
  }

  private String getCacheKey(String targetName, String methodName, Object[] arguments) {
    StringBuffer sb = new StringBuffer();
    sb.append(targetName).append(".").append(methodName);
    if ((arguments != null) && (arguments.length != 0)) {
      for (int i = 0; i < arguments.length; i++) {
        sb.append(".").append(arguments[i]);
      }
    }

    return sb.toString();
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_cache, "A cache is required. Use setCache(Cache) to provide one.");
  }

}
