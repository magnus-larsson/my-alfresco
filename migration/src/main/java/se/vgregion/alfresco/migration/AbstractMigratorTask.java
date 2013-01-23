package se.vgregion.alfresco.migration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractMigratorTask implements MigratorTask {

  protected String _destinationShareHost;

  protected int _destinationSharePort;

  protected String _sourceAlfrescoHost;

  protected int _sourceAlfrescoPort;

  protected String _destinationAlfrescoHost;

  protected int _destinationAlfrescoPort;

  private String _username;

  private String _password;

  public void setSourceAlfrescoHost(final String sourceAlfrescoHost) {
    _sourceAlfrescoHost = sourceAlfrescoHost;
  }

  public void setSourceAlfrescoPort(final int sourceAlfrescoPort) {
    _sourceAlfrescoPort = sourceAlfrescoPort;
  }

  public void setDestinationAlfrescoHost(final String destinationAlfrescoHost) {
    _destinationAlfrescoHost = destinationAlfrescoHost;
  }

  public void setDestinationAlfrescoPort(final int destinationAlfrescoPort) {
    _destinationAlfrescoPort = destinationAlfrescoPort;
  }

  public void setDestinationShareHost(final String destinationShareHost) {
    _destinationShareHost = destinationShareHost;
  }

  public void setDestinationSharePort(final int destinationSharePort) {
    _destinationSharePort = destinationSharePort;
  }

  public void setUsername(final String username) {
    _username = username;
  }

  public void setPassword(final String password) {
    _password = password;
  }

  protected RestTemplate createRestTemplate(final String url, final int port) {
    final HttpClient client = new HttpClient();

    final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(_username, _password);
    client.getState().setCredentials(new AuthScope(url, port, AuthScope.ANY_REALM), credentials);
    final CommonsClientHttpRequestFactory commons = new CommonsClientHttpRequestFactory(client);
    final RestTemplate alfrescoDevRest = new RestTemplate(commons);

    final List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
    messageConverters.add(new StringHttpMessageConverter());
    messageConverters.add(new FormHttpMessageConverter());
    alfrescoDevRest.setMessageConverters(messageConverters);

    return alfrescoDevRest;
  }

  protected List<Map<String, Object>> getSites(final String host, final int port) {
    final RestTemplate restTemplate = createRestTemplate(host, port);

    final List<Map<String, Object>> result = restTemplate.getForObject("http://" + host + ":" + port
        + "/alfresco/service/api/sites", List.class, Collections.EMPTY_MAP);

    return result;
  }

}
