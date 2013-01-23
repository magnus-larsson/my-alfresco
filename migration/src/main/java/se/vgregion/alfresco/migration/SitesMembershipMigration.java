package se.vgregion.alfresco.migration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

public class SitesMembershipMigration extends AbstractMigratorTask {

  @Override
  public void executeMigration() {
    final List<Map<String, Object>> sites = getSites(_sourceAlfrescoHost, _sourceAlfrescoPort);

    for (final Map<String, Object> site : sites) {
      final String siteName = site.get("shortName").toString();

      final List<Map<String, Object>> oldSiteMembership = getSiteMembership(siteName, _sourceAlfrescoHost,
          _sourceAlfrescoPort);

      addSiteMembership(siteName, oldSiteMembership, _destinationAlfrescoHost, _destinationAlfrescoPort);
    }
  }

  private void addSiteMembership(final String siteName, final List<Map<String, Object>> oldSiteMemberships,
      final String host, final int port) {
    final RestTemplate restTemplate = createRestTemplate(host, port);

    final String uri = "http://" + host + ":" + port + "/alfresco/service/api/sites/" + siteName + "/memberships";

    for (final Map<String, Object> oldSiteMembership : oldSiteMemberships) {
      final Map<String, Object> authority = (Map<String, Object>) oldSiteMembership.get("authority");
      final String userName = (String) authority.get("userName");
      final String role = (String) oldSiteMembership.get("role");

      final Map<String, Object> request = new HashMap<String, Object>();

      final Map<String, Object> person = new HashMap<String, Object>();
      person.put("userName", userName);

      request.put("role", role);
      request.put("person", person);

      try {
        restTemplate.postForLocation(uri, request, Collections.EMPTY_MAP);
      } catch (final Exception ex) {
        System.out.println("Couldn't set membership for site " + siteName + " and username " + userName);
      }
    }
  }

  private List<Map<String, Object>> getSiteMembership(final String siteName, final String host, final int port) {
    final RestTemplate restTemplate = createRestTemplate(host, port);

    final String uri = "http://" + host + ":" + port + "/alfresco/service/api/sites/" + siteName + "/memberships";

    return restTemplate.getForObject(uri, List.class, Collections.EMPTY_MAP);
  }

}
