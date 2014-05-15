package se.vgregion.alfresco.repo.kivclient;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

//This class requires soapio endpoint service to be running
public class KivWsClientIntegrationTest {
  private KivWsClient wsClient;

  @Before
  public void setup() throws Exception {
    wsClient = new KivWsClient("test", "test");
    wsClient.setEndpointUri("http://localhost:8088/mockVGRegionWebServiceImplSoapBinding");
  }

  @Test
  public void testSearchPersonEmployment() throws IOException, JAXBException {
    String searchPersonEmployment = wsClient.searchPersonEmployment("{vgrId}", "Infrastruktur 6023");
    assertEquals("ou=Infrastruktur 6023,ou=Infrastruktur,ou=VGR IT,ou=Regionstyrelsen,ou=Org,o=VGR", searchPersonEmployment);
  }
}
