package se.vgregion.alfresco.cmis.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.alfresco.cmis.client.AlfrescoFolder;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;

public class CreateDocument {

  public static void main(final String[] args) throws FileNotFoundException {
    final File file = new File("/Users/niklas/Downloads/testar.docx");
    final String mimetype = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    final String username = "admin";
    final String password = "admin";
    final String url = "http://localhost:8080/alfresco/service/cmis";
    // final String url =
    // "http://alfresco-lager1.vgregion.se:8080/alfresco/service/cmis";

    final Map<String, String> parameter = new HashMap<String, String>();

    // user credentials
    parameter.put(SessionParameter.USER, username);
    parameter.put(SessionParameter.PASSWORD, password);

    // connection settings
    parameter.put(SessionParameter.ATOMPUB_URL, url);
    parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
    // parameter.put(SessionParameter.COMPRESSION, "true");
    // parameter.put(SessionParameter.CLIENT_COMPRESSION, "true");

    // set the object factory
    parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

    CmisBinding binding = CmisBindingFactory.newInstance().createCmisAtomPubBinding(parameter);

    RepositoryInfo info = binding.getRepositoryService().getRepositoryInfos(null).get(0);

    // create session
    final SessionFactory factory = SessionFactoryImpl.newInstance();
    final Session session = factory.getRepositories(parameter).get(0).createSession();

    String newId = "workspace://SpacesStore/f8a8eb04-76a7-4351-b49d-d7a09656321f";

    final ItemIterable<CmisObject> children = session.getRootFolder().getChildren();

    for (final CmisObject child : children) {
      if (child.getName().equals("Barium")) {
        final Folder folder = (Folder) child;

        // final Document doc = folder.createDocument(properties,
        // getContentStream(file, mimetype), null);
        // newId = doc.getId();

        // newId = createDocument(binding, folder, info, file, mimetype);
        newId = createDocument2(folder, file, mimetype);

        System.out.println(newId);

        break;
      }
    }

    /*
     * if (StringUtils.isNotBlank(newId)) { final Document document = (Document)
     * session.getObject(newId);
     * 
     * ObjectId workingCopyId;
     * 
     * try { workingCopyId = document.checkOut(); } catch (final Exception ex) {
     * document.cancelCheckOut();
     * 
     * workingCopyId = document.checkOut(); }
     * 
     * final Document workingCopy = (Document) session.getObject(workingCopyId);
     * 
     * properties.remove(PropertyIds.NAME);
     * properties.remove(PropertyIds.OBJECT_TYPE_ID);
     * 
     * workingCopy.setContentStream(getContentStream(file, mimetype), true);
     * 
     * workingCopy.checkIn(true, properties, getContentStream(file, mimetype),
     * "This is a new nice document"); }
     */
  }

  private static String createDocument2(Folder folder, File file, String mimetype) throws FileNotFoundException {
    AlfrescoFolder alfrescoFolder = (AlfrescoFolder) folder;

    Map<String, Object> properties = new HashMap<String, Object>();

    properties.put(PropertyIds.OBJECT_TYPE_ID, "D:vgr:document,P:vgr:standard,P:vgr:metadata,P:cm:titled");
    properties.put(PropertyIds.NAME, file.getName());

    ContentStream contentStream = getContentStream(file, mimetype);

    properties.put("vgr:dc.title", file.getName());
    properties.put("cm:title", file.getName());
    properties.put("vgr:dc.description", "My document");
    properties.put("vgr:dc.description", "My document");
    properties.put("vgr:hc.status.document", "Beslutad");
    properties.put("vgr:dc.type.document", "Avtal");
    properties.put("vgr:dc.type.record", "Ospecificerat");
    properties.put("vgr:dc.source", "http://bariumtest.vgregion.se/barium/link.asp?content=2528");
    properties.put("vgr:dc.source.documentid", BigInteger.valueOf(2528));
    properties.put("vgr:dc.identifier.version", "9");
    properties.put("vgr:dc.source.origin", "Barium");
    properties.put("vgr:dc.publisher.project-assignment", "Foobar");

    final GregorianCalendar date = new GregorianCalendar();
    date.setTimeInMillis(System.currentTimeMillis());

    properties.put("vgr:dc.date.accepted", date);

    final AlfrescoDocument document = (AlfrescoDocument) alfrescoFolder.createDocument(properties, contentStream, null);

    return document.getId();
  }

  private static String createDocument(CmisBinding binding, Folder folder, RepositoryInfo info, File file, String mimetype) throws FileNotFoundException {
    List<String> types = new ArrayList<String>();

    types.add("D:vgr:document");
    types.add("P:vgr:standard");
    types.add("P:vgr:metadata");
    types.add("P:cm:titled");

    ContentStream contentStream = getContentStream(file, mimetype);

    List<PropertyData<?>> list = new ArrayList<PropertyData<?>>();

    put(binding, list, PropertyIds.NAME, file.getName());
    put(binding, list, PropertyIds.OBJECT_TYPE_ID, types);
    put(binding, list, "vgr:dc.description", "My document");
    put(binding, list, "vgr:hc.status.document", "Beslutad");
    put(binding, list, "vgr:dc.type.document", "Avtal");
    put(binding, list, "vgr:dc.type.record", "Ospecificerat");
    put(binding, list, "vgr:dc.title", file.getName());
    put(binding, list, "cm:title", file.getName());
    put(binding, list, "vgr:dc.source", "http://bariumtest.vgregion.se/barium/link.asp?content=2528");
    put(binding, list, "vgr:dc.source.documentid", BigInteger.valueOf(2528));
    put(binding, list, "vgr:dc.identifier.version", "1");
    put(binding, list, "vgr:dc.source.origin", "Barium");

    final GregorianCalendar date = new GregorianCalendar();
    date.setTimeInMillis(System.currentTimeMillis());

    put(binding, list, "vgr:dc.date.accepted", date);

    System.out.println(list);

    Properties properties = binding.getObjectFactory().createPropertiesData(list);

    return binding.getObjectService().createDocument(info.getId(), properties, folder.getId(), contentStream, VersioningState.MINOR, null, null, null, null);
  }

  private static void put(CmisBinding binding, List<PropertyData<?>> list, String key, List<String> values) {
    PropertyData<?> property = binding.getObjectFactory().createPropertyStringData(key, values);

    list.add(property);
  }

  private static void put(CmisBinding binding, List<PropertyData<?>> list, String key, String value) {
    PropertyData<?> property = binding.getObjectFactory().createPropertyStringData(key, value);

    list.add(property);
  }

  private static void put(CmisBinding binding, List<PropertyData<?>> list, String key, BigInteger value) {
    PropertyData<?> property = binding.getObjectFactory().createPropertyIntegerData(key, value);

    list.add(property);
  }

  private static void put(CmisBinding binding, List<PropertyData<?>> list, String key, GregorianCalendar value) {
    PropertyData<?> property = binding.getObjectFactory().createPropertyDateTimeData(key, value);

    list.add(property);
  }

  private static ContentStream getContentStream(final File file, final String mimetype) throws FileNotFoundException {
    final InputStream inputStream = new FileInputStream(file);

    ContentStream contentStream = new ContentStreamImpl(file.getName(), BigInteger.valueOf(file.length()), mimetype, inputStream);

    return contentStream;
  }

}
