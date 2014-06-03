package se.vgregion.alfresco.repo.scripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.cache.SimpleCache;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ClearCache extends DeclarativeWebScript implements ApplicationContextAware {

  private ApplicationContext _applicationContext;

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
    Map<String, SimpleCache> cacheMap = _applicationContext.getBeansOfType(SimpleCache.class);
    
    for (SimpleCache simpleCache : cacheMap.values()) {
      simpleCache.clear();
    }

    Map<String, Object> model = new HashMap<String, Object>();
    model.put("result", "OK");

    return super.executeImpl(req, status, cache);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    _applicationContext = applicationContext;
  }


}
