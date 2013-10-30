package se.vgregion.alfresco.repo.kivclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.CommonsHttpMessageSender;

import se.vgregion.ws.objects.ArrayOfPerson;
import se.vgregion.ws.objects.Employment;
import se.vgregion.ws.objects.Person;
import se.vgregion.ws.services.SearchPersonEmploymentResponse;
import se.vgregion.ws.services.String2ArrayOfAnyTypeMap.Entry;

public class KivWsClient {

  private static final String ORGANIZATION_DN = "vgrstrukturperson";
  private static final String OU = "ou";

  private WebServiceTemplate webServiceTemplate;
  private static final Logger LOG = Logger.getLogger(KivWsClient.class);

  public KivWsClient(String username, String password) {
    webServiceTemplate = new WebServiceTemplate();
    CommonsHttpMessageSender sender = new CommonsHttpMessageSender();
    sender.setCredentials(new UsernamePasswordCredentials(username, password));
    webServiceTemplate.setMessageSender(sender);
  }
  
  public void setEndpointUri(String uri) {
    webServiceTemplate.setDefaultUri(uri);
  }

  private <T> T XmlToObject(String xml, Class<T> type) throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(type);

    Unmarshaller m = context.createUnmarshaller();

    StringReader is = new StringReader(xml);
    JAXBElement object = (JAXBElement) m.unmarshal(is);

    return type.cast(object.getValue());
  }

  public String getTemplate(String uri) throws IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(uri);
    StringWriter writer = new StringWriter();
    IOUtils.copy(inputStream, writer, "UTF-8");
    return writer.toString();
  }

  public String searchPersonEmployment(String vgrId, String department) throws IOException, JAXBException {
    String message = getTemplate("kiv/searchPersonEmployment.gt");
    message = message.replace("{vgrId}", vgrId);
    StringWriter sw = new StringWriter();
    if (LOG.isTraceEnabled()) {
      LOG.trace("Request: " + message);
    }
    StreamSource source = new StreamSource(new StringReader(message));
    StreamResult result = new StreamResult(sw);

    webServiceTemplate.sendSourceAndReceiveToResult(source, result);

    SearchPersonEmploymentResponse response = XmlToObject(sw.toString(), SearchPersonEmploymentResponse.class);
    JAXBElement<ArrayOfPerson> return1 = response.getReturn();
    List<Person> persons = return1.getValue().getPerson();
    if (persons.size() != 1) {
      throw new AlfrescoRuntimeException("SearchPersonEmploymentResponse: Expected: 1, Actual: " + persons.size());
    }
    Person person = persons.get(0);
    List<Employment> employments = person.getEmployments().getValue().getEmployment();
    Iterator<Employment> it = employments.iterator();
    String organizationDN = "";
    while (it.hasNext()) {
      Employment next = it.next();
      List<Entry> entries = next.getAttributes().getValue().getEntry();
      Iterator<Entry> it2 = entries.iterator();
      String vgrstrukturperson = "";
      String ou = "";
      while (it2.hasNext()) {
        Entry next2 = it2.next();
        if (ORGANIZATION_DN.equalsIgnoreCase(next2.getKey())) {
          vgrstrukturperson = (String) next2.getValue().getAnyType().get(0);
        } else if (OU.equalsIgnoreCase(next2.getKey())) {
          ou = (String) next2.getValue().getAnyType().get(0);
        }
        if (ou.length() > 0 && vgrstrukturperson.length() > 0) {
          break;
        }
      }

      if (department.equalsIgnoreCase(ou)) {
        organizationDN = vgrstrukturperson;
      }
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("Response: " + sw.toString());
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrieved organizationDN=" + organizationDN + " for user " + vgrId);
    }

    return organizationDN;
  }

  public WebServiceTemplate getWebServiceTemplate() {
    return webServiceTemplate;
  }
  
  public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
    this.webServiceTemplate = webServiceTemplate;
  }
}
