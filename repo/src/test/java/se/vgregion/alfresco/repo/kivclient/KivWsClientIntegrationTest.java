package se.vgregion.alfresco.repo.kivclient;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import se.vgregion.alfresco.kivclient.KivWsClient;

public class KivWsClientIntegrationTest {
  private KivWsClient wsClient;
  @Before
  public void setup() {
    wsClient = new KivWsClient();
    wsClient.setEndpointUri("http://localhost:8088/mockVGRegionWebServiceImplSoapBinding");
  }
  
  @Test
  public void testSearchPersonEmployment() throws IOException, JAXBException {
    String searchPersonEmployment = wsClient.searchPersonEmployment("test");
    assertEquals("ou=Infrastruktur 6023,ou=Infrastruktur,ou=VGR IT,ou=Regionstyrelsen,ou=Org,o=VGR", searchPersonEmployment);
  }
}
