package se.vgregion.alfresco.migration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class CreateSiteMigration extends AbstractMigratorTask {

  @Override
  public void executeMigration() {
    final List<Map<String, Object>> oldSites = getSites(_sourceAlfrescoHost, _sourceAlfrescoPort);

    for (final Map<String, Object> site : oldSites) {
      final String shortName = site.get("shortName").toString();

      final Map<String, Object> oldSite = getSite(shortName, _sourceAlfrescoHost, _sourceAlfrescoPort);
      final Map<String, Object> newSite = getSite(shortName, _destinationAlfrescoHost, _destinationAlfrescoPort);

      if (newSite == null) {
        createNewSite(oldSite, _destinationAlfrescoHost, _destinationAlfrescoPort);
      }
    }
  }

  private Map<String, Object> getSite(final String shortName, final String host, final int port) {
    final RestTemplate restTemplate = createRestTemplate(host, port);

    final String uri = "http://" + host + ":" + port + "/alfresco/service/api/sites/" + shortName;

    Map<String, Object> result;

    try {
      result = restTemplate.getForObject(uri, Map.class, Collections.EMPTY_MAP);
    } catch (final HttpClientErrorException ex) {
      result = null;
    }

    return result;
  }

  private void createNewSite(final Map<String, Object> oldSite, final String host, final int port) {
    final RestTemplate restTemplate = createRestTemplate(host, port);

    final String uri = "http://" + host + ":" + port + "/alfresco/service/api/sites";

    final Map<String, Object> request = new LinkedHashMap<String, Object>();
    request.put("shortName", oldSite.get("shortName"));
    request.put("sitePreset", oldSite.get("sitePreset"));
    request.put("title", oldSite.get("title"));
    request.put("description", oldSite.get("description"));
    request.put("visibility", oldSite.get("visibility"));

    System.out.println("About to create site: " + request);

    restTemplate.postForLocation(uri, request, Collections.EMPTY_MAP);
  }


}
