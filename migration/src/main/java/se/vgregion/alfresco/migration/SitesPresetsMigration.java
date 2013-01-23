package se.vgregion.alfresco.migration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

public class SitesPresetsMigration extends AbstractMigratorTask {

  @Override
  public void executeMigration() {
    final List<Map<String, Object>> newSites = getSites(_sourceAlfrescoHost, _sourceAlfrescoPort);

    for (final Map<String, Object> site : newSites) {
      addSitePresets(site, _destinationShareHost, _destinationSharePort);
    }
  }

  private void addSitePresets(final Map<String, Object> oldSite, final String host, final int port) {
    final RestTemplate restTemplate = createRestTemplate(host, port);

    final String uri = "http://" + host + ":" + port + "/share/page/modules/custom-site?shortName="
        + oldSite.get("shortName") + "&sitePreset=" + oldSite.get("sitePreset");

    System.out.println(uri);

    final RequestCallback requestCallback = new RequestCallback() {

      @Override
      public void doWithRequest(final ClientHttpRequest request) throws IOException {
        request.getHeaders().add("Accept-Charset", "iso-8859-1,utf-8");
        request.getHeaders().add("Accept-Language", "en-us");
      }
    };

    final ResponseExtractor<Object> responseExtractor = new ResponseExtractor<Object>() {

      @Override
      public Object extractData(final ClientHttpResponse response) throws IOException {
        // TODO Auto-generated method stub
        return null;
      }

    };

    restTemplate.execute(uri, HttpMethod.GET, requestCallback, responseExtractor, Collections.EMPTY_MAP);
  }

}
