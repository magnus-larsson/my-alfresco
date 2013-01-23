package se.vgregion.alfresco.repo.scripts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ClearCache extends DeclarativeWebScript {

  private List<CacheManager> _cacheManagers;

  public void setCacheManagers(final List<CacheManager> cacheManagers) {
    _cacheManagers = cacheManagers;
  }

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
    for (final CacheManager cacheManager : _cacheManagers) {
      cacheManager.clearAll();
    }

    final Map<String, Object> model = new HashMap<String, Object>();
    model.put("result", "OK");

    return super.executeImpl(req, status, cache);
  }

}
