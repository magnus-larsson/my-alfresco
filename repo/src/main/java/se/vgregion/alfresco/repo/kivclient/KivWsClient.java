package se.vgregion.alfresco.repo.kivclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.CommonsHttpMessageSender;

import se.vgregion.alfresco.repo.model.KivUnit;
import se.vgregion.ws.objects.ArrayOfPerson;
import se.vgregion.ws.objects.ArrayOfUnit;
import se.vgregion.ws.objects.Employment;
import se.vgregion.ws.objects.Person;
import se.vgregion.ws.objects.Unit;
import se.vgregion.ws.services.SearchPersonEmploymentResponse;
import se.vgregion.ws.services.SearchUnit;
import se.vgregion.ws.services.SearchUnitResponse;
import se.vgregion.ws.services.String2ArrayOfAnyTypeMap.Entry;

public class KivWsClient {

  private static final String ORGANIZATION_DN = "vgrstrukturperson";
  private static final String OU = "ou";
  private static final String RESPONSIBILITY_CODE = "vgransvarsnummer";
  private static final String HSA_IDENTITY = "hsaIdentity";
  private static final String VGR_MODIFY_TIMESTAMP = "vgrModifyTimestamp";

  private WebServiceTemplate webServiceTemplate;
  private static final Logger LOG = Logger.getLogger(KivWsClient.class);

  public KivWsClient(String username, String password) throws Exception {
    webServiceTemplate = new WebServiceTemplate();

    CommonsHttpMessageSender sender = new CommonsHttpMessageSender();
    sender.setCredentials(new UsernamePasswordCredentials(username, password));
    sender.afterPropertiesSet();
    webServiceTemplate.setMessageSender(sender);
    webServiceTemplate.afterPropertiesSet();
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

  /**
   * Fetch a persons employment by vgrId and responsibilityCode
   * 
   * @param vgrId
   *          The vgr id of the person
   * @param responsibilityCode
   *          The responsibility code of the person
   * @return the organizationDn for the user (vgrstrukturperson)
   * @throws IOException
   * @throws JAXBException
   */
  public String searchPersonEmployment(String vgrId, String responsibilityCode) throws IOException, JAXBException {
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
      throw new AlfrescoRuntimeException("Query: vgrId: " + vgrId + " responsibilityCode:" + responsibilityCode + ", SearchPersonEmploymentResponse: Expected: 1, Actual: " + persons.size());
    }
    Person person = persons.get(0);
    List<Employment> employments = person.getEmployments().getValue().getEmployment();
    Iterator<Employment> employmentsIt = employments.iterator();
    String organizationDN = "";
    while (employmentsIt.hasNext()) {
      Employment employment = employmentsIt.next();
      List<Entry> entries = employment.getAttributes().getValue().getEntry();
      Iterator<Entry> entriesIt = entries.iterator();
      String vgrstrukturperson = "";
      String ou = "";
      String vgransvarsnummer = "";
      while (entriesIt.hasNext()) {
        Entry entry = entriesIt.next();
        if (ORGANIZATION_DN.equalsIgnoreCase(entry.getKey())) {
          vgrstrukturperson = (String) entry.getValue().getAnyType().get(0);
        } else if (OU.equalsIgnoreCase(entry.getKey())) {
          ou = (String) entry.getValue().getAnyType().get(0);
        } else if (RESPONSIBILITY_CODE.equalsIgnoreCase(entry.getKey())) {
          vgransvarsnummer = (String) entry.getValue().getAnyType().get(0);
        }
        if (ou.length() > 0 && vgrstrukturperson.length() > 0) {
          break;
        }
      }

      if (responsibilityCode.equalsIgnoreCase(vgransvarsnummer)) {
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

  public List<KivUnit> searchUnit(final String searchBase, final Date modifyTimestamp) throws IOException, JAXBException {
    final List<KivUnit> kivUnits = new ArrayList<KivUnit>();
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
    String message = getTemplate("kiv/searchUnit.gt");
    String filter = "vgrModifyTimestamp=*";
    if (modifyTimestamp != null) {
      filter = "vgrModifyTimestamp&gt;=" + dateFormat.format(modifyTimestamp);
    }
    message = message.replace("{filter}", filter);

    final String base = StringUtils.isBlank(searchBase) ? "ou=Org,o=VGR" : searchBase;
    message = message.replace("{searchBase}", base);
    StringWriter sw = new StringWriter();
    if (LOG.isTraceEnabled()) {
      LOG.trace("Request: " + message);
    }
    StreamSource source = new StreamSource(new StringReader(message));
    StreamResult result = new StreamResult(sw);

    webServiceTemplate.sendSourceAndReceiveToResult(source, result);

    SearchUnitResponse searchUnitResponse = XmlToObject(sw.toString(), SearchUnitResponse.class);
    JAXBElement<ArrayOfUnit> response = searchUnitResponse.getReturn();
    List<Unit> units = response.getValue().getUnit();
    Iterator<Unit> unitsIt = units.iterator();

    while (unitsIt.hasNext()) {
      Unit unit = unitsIt.next();
      List<Entry> entries = unit.getAttributes().getValue().getEntry();
      Iterator<Entry> entriesIt = entries.iterator();
      KivUnit kivUnit = new KivUnit();
      while (entriesIt.hasNext()) {
        Entry entry = entriesIt.next();
        
        if (HSA_IDENTITY.equalsIgnoreCase(entry.getKey())) {
          kivUnit.setHsaIdentity((String) entry.getValue().getAnyType().get(0));
        } else if (OU.equalsIgnoreCase(entry.getKey())) {
          kivUnit.setOrganisationalUnit((String) entry.getValue().getAnyType().get(0));
        }
        
      }
      kivUnit.setDistinguishedName(unit.getDn().getValue());
      kivUnits.add(kivUnit);
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("Response: " + sw.toString());
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrieved " + units.size() + " kiv units.");
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("KivUnits: "+ kivUnits.toString());
    }
    return kivUnits;
  }

  public WebServiceTemplate getWebServiceTemplate() {
    return webServiceTemplate;
  }

  public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
    this.webServiceTemplate = webServiceTemplate;
  }
}
