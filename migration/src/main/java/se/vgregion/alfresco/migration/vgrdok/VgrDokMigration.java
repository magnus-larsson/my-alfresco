package se.vgregion.alfresco.migration.vgrdok;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import se.vgregion.alfresco.migration.AbstractMigratorTask;

public class VgrDokMigration extends AbstractMigratorTask {

  private static final String DEFAULT_STATUS_DOCUMENT = "Flyttat från annat system";
  private static final String DEFAULT_TYPE_RECORD = "Ospecificerat";
  private static final String DEFAULT_PUBLISHER_PROJECT_ASSIGNMENT = "Flyttat från VGRdok";

  // public static final String IMPORT_TEXT_FILE =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/RK_Arbetsdokument/RK_Arbetsdokument_Metadata.txt";
  // public static final String FILES_FOLDER =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/RK_Arbetsdokument/Files";

  public static final String IMPORT_TEXT_FILE = "/Users/niklas/Documents/Jobb/VGR/vgr_dok/Socialdemokraterna_Arbetsdokument/Socialdemokraterna_Arbetsdokument_Metadata.txt";
  public static final String FILES_FOLDER = "/Users/niklas/Documents/Jobb/VGR/vgr_dok/Socialdemokraterna_Arbetsdokument/Files";

  // public static final String IMPORT_TEXT_FILE =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/Politiska_nämnder_och_styrelser_Arbetsdokument/Politiska_nämnder_och_styrelser_Arbetsdokument_Metadata.txt";
  // public static final String FILES_FOLDER =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/Politiska_nämnder_och_styrelser_Arbetsdokument/Files";

  // public static final String IMPORT_TEXT_FILE =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/HMF_Arbetsdokument/HMF_Arbetsdokument_Metadata.txt";
  // public static final String FILES_FOLDER =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/HMF_Arbetsdokument/Files";

  // public static final String IMPORT_TEXT_FILE =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/PV_FBD_Arbetsdokument/PV_FBD_Arbetsdokument_Metadata.txt";
  // public static final String FILES_FOLDER =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/PV_FBD_Arbetsdokument/Files";

  // public static final String IMPORT_TEXT_FILE =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/Regionservice_Arbetsdokument/Regionservice_Arbetsdokument_Metadata.txt";
  // public static final String FILES_FOLDER =
  // "/Users/niklas/Documents/Jobb/VGR/vgr_dok/Regionservice_Arbetsdokument/Files";

  private static final int STREAM_BUFFER_LENGTH = 1024;

  @Override
  public void executeMigration() {
    final Set<VgrDokDocument> documents = createDocumentList();

    final Set<VgrDokDocument> batchlist = new HashSet<VgrDokDocument>();

    int count = 1;

    // split the list into a batch of 500...
    for (final VgrDokDocument document : documents) {
      if (batchlist.size() >= 500) {
        createAcpPackage(batchlist, count);

        count++;

        batchlist.clear();
      }

      batchlist.add(document);
    }

    createAcpPackage(batchlist, count);
  }

  private void createAcpPackage(final Set<VgrDokDocument> documents, final int count) {
    final ByteArrayOutputStream metadataDocument = createMetadataDocument(documents);

    try {
      final ZipOutputStream zipfile = new ZipOutputStream(new FileOutputStream(getZipFileFolder() + File.separator
          + getZipFilename(count)));

      zipfile.putNextEntry(new ZipEntry(getMetadataDocumentName()));
      zipfile.write(metadataDocument.toByteArray());

      for (final VgrDokDocument document : documents) {
        if (document.file == null) {
          continue;
        }

        final ZipEntry entry = new ZipEntry(getSubFolderName() + "/" + document.file.getName());

        try {
          zipfile.putNextEntry(entry);
        } catch (final Exception ex) {
          System.out.println("Error in writing file " + document.file + ", message: " + ex.getMessage());
          continue;
        }

        final InputStream fileStream = new FileInputStream(document.file);
        try {
          zipfile.write(IOUtils.toByteArray(fileStream));
        } catch (final Exception ex) {
          System.out.println("Error in writing file " + document.file);
          throw new RuntimeException(ex);
        } finally {
          IOUtils.closeQuietly(fileStream);
        }
      }

      zipfile.close();
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private String getMetadataDocumentName() {
    return FilenameUtils.getBaseName(IMPORT_TEXT_FILE) + ".xml";
  }

  private String getZipFilename(final int count) {
    return FilenameUtils.getBaseName(IMPORT_TEXT_FILE) + count + ".acp";
  }

  private String getSubFolderName() {
    return FilenameUtils.getBaseName(IMPORT_TEXT_FILE);
  }

  private String getZipFileFolder() {
    return FilenameUtils.getFullPathNoEndSeparator(IMPORT_TEXT_FILE);
  }

  private ByteArrayOutputStream createMetadataDocument(final Set<VgrDokDocument> documents) {
    final ByteArrayOutputStream metadataDocument = new ByteArrayOutputStream();

    final PrintWriter writer = new PrintWriter(metadataDocument);

    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writer.println("<view:view xmlns:view=\"http://www.alfresco.org/view/repository/1.0\">");

    for (final VgrDokDocument document : documents) {
      addDocument(writer, document);
    }

    writer.println("</view:view>");

    writer.flush();

    try {
      IOUtils.copy(new ByteArrayInputStream(metadataDocument.toByteArray()), new FileOutputStream("/tmp/file.xml"));
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }

    validateXml(metadataDocument);

    return metadataDocument;
  }

  private void validateXml(final ByteArrayOutputStream metadataDocument) {
    try {
      // Create a new factory to create parsers
      final DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();

      // Use the factory to create a parser (builder) and use it to parse the
      // document.
      final DocumentBuilder builder = dBF.newDocumentBuilder();

      final InputSource is = new InputSource(new ByteArrayInputStream(metadataDocument.toByteArray()));

      builder.parse(is);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private void addDocument(final PrintWriter writer, final VgrDokDocument document) {
    final String title = escape(document.title);

    writer
        .println("<vgr:document xmlns=\"\" xmlns:sys=\"http://www.alfresco.org/model/system/1.0\" xmlns:vgr=\"http://www.vgregion.se/model/1.0\" xmlns:d=\"http://www.alfresco.org/model/dictionary/1.0\" xmlns:cm=\"http://www.alfresco.org/model/content/1.0\" xmlns:view=\"http://www.alfresco.org/view/repository/1.0\" view:childName=\"cm:"
            + title + "\">");

    addAspects(writer);
    addAcl(writer);
    addProperties(writer, document);

    writer.println("</vgr:document>");
  }

  private void addProperties(final PrintWriter writer, final VgrDokDocument document) {
    writer.println("<view:properties>");

    addProperty(writer, "cm:autoVersionOnUpdateProps", "false");
    addProperty(writer, "cm:autoVersion", "true");
    addProperty(writer, "cm:title", "<view:mlvalue view:locale=\"sv_SE\">standard</view:mlvalue>", false);
    addProperty(writer, "cm:name", getName(document.name));

    if (document.file != null) {
      addProperty(writer, "cm:content", "contentUrl=" + getSubFolderName() + "/" + document.file.getName()
          + "|mimetype=" + document.mimetype + "|size=" + document.size + "|encoding=utf-8|locale=en_US_");
    }

    addProperty(writer, "cm:creator", getUploadedBy(document.uploadedBy));
    addProperty(writer, "cm:modifier", getUploadedBy(document.uploadedBy));
    addProperty(writer, "cm:modified", formatDate(document.createdDate));
    addProperty(writer, "cm:created", formatDate(document.createdDate));

    addProperty(writer, "sys:store-identifier", "SpacesStore");
    addProperty(writer, "sys:store-protocol", "workspace");

    addProperty(writer, "vgr:dc.contributor.savedby", document.createdBy);
    addProperty(writer, "vgr:dc.title", document.title);
    addProperty(writer, "vgr:dc.title.filename", document.filename);
    addProperty(writer, "vgr:dc.source", document.documentId);
    addProperty(writer, "vgr:dc.contributor.savedby", getUploadedBy(document.uploadedBy));
    addProperty(writer, "vgr:dc.date.saved", formatDate(document.createdDate));
    addProperty(writer, "vgr:dc.date.availablefrom", formatDate(document.availableDateFrom));
    addProperty(writer, "vgr:dc.date.availableto", formatDate(document.availableDateTo));
    addProperty(writer, "vgr:dc.description", document.description);
    addProperty(writer, "vgr:dc.creator", document.author);
    addProperty(writer, "vgr:dc.type.document", getDocumentType(document.documentType));
    addProperty(writer, "vgr:dc.type.record", DEFAULT_TYPE_RECORD);
    addProperty(writer, "vgr:hc.status.document", DEFAULT_STATUS_DOCUMENT);
    addProperty(writer, "vgr:dc.creator.project-assignment", document.responsibleProject);
    addProperty(writer, "vgr:dc.type.document.serie", document.documentSeries);
    addProperty(writer, "vgr:dc.type.document.id", document.externalReference);
    addProperty(writer, "vgr:dc.rights.accessrights", getAccessRights(document.accessZone));
    addProperty(writer, "vgr:dc.publisher", document.publishedBy);
    addProperty(writer, "vgr:dc.date.issued", formatDate(document.publishedDate));
    addProperty(writer, "vgr:dc.creator.function", document.responsiblePerson);
    addProperty(writer, "vgr:dc.identifier.checksum", document.checksum);
    addProperty(writer, "vgr:dc.publisher.project-assignment", DEFAULT_PUBLISHER_PROJECT_ASSIGNMENT);

    if (document.file != null) {
      addProperty(writer, "vgr:dc.format.extension", FilenameUtils.getExtension(document.file.getName()));
    }

    addProperty(writer, "vgr:dc.coverage.hsacode", getHsaCode(document.businessAreaMain, document.businessAreaSub));
    addProperty(writer, "vgr:vgrDokUniversalId", document.universalID);
    addProperty(writer, "vgr:vgrDokFilePath", document.filepath);
    // below is to make the versioning work correctly
    addProperty(writer, "vgr:vgrDokVersion", document.version);
    addProperty(writer, "vgr:vgrDokDocumentId", document.documentId);

    writer.println("</view:properties>");

    if (!StringUtils.hasText(document.filepath)) {
      System.out.println(document.universalID + ": " + document.documentId);
    }
  }

  private String getName(String name) {
    name = name.replace("?", "");
    name = name.replace(":", "-");
    name = name.replace("\"", "&quot;");
    name = name.replace("/", "-");

    return name;
  }

  private String getHsaCode(final String businessAreaMain, final String businessAreaSub) {
    if (!StringUtils.hasText(businessAreaMain) || StringUtils.hasText(businessAreaSub)) {
      return "";
    }

    final String businessArea = businessAreaMain + "/" + businessAreaSub;

    String hsa;

    if ("Administration/Information/kommunikation".equals(businessArea)) {
      hsa = "";
    } else if ("Administration/IT /  Informationsteknik".equals(businessArea)) {
      hsa = "";
    } else if ("Administration/Ledningsfrågor".equals(businessArea)) {
      hsa = "";
    } else {
      throw new RuntimeException("Unknown business area '" + businessArea + "'");
    }

    return hsa;
  }

  private String formatDate(final Date date) {
    if (date == null) {
      return null;
    }

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+02:00'");

    return simpleDateFormat.format(date);
  }

  private String getChecksum(final File file) {
    if (file == null) {
      return null;
    }

    InputStream inputStream = null;

    try {
      inputStream = new FileInputStream(file);

      return md5Hex(inputStream);
    } catch (final FileNotFoundException ex) {
      throw new RuntimeException(ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }

  }

  private String md5Hex(final InputStream data) {
    return new String(Hex.encodeHex(md5(data)));
  }

  public byte[] md5(final InputStream data) {
    try {
      return digest(MessageDigest.getInstance("MD5"), data);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private byte[] digest(final MessageDigest digest, final InputStream data) {
    try {
      final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
      int read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);

      while (read > -1) {
        digest.update(buffer, 0, read);
        read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);
      }

      return digest.digest();
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private String getUploadedBy(final String uploadedBy) {
    String result = uploadedBy;

    if (!StringUtils.hasText(result)) {
      result = "admin";
    }

    return result;
  }

  private String getAccessRights(final String accessZone) {
    if (!StringUtils.hasText(accessZone)) {
      return null;
    }

    String result;

    if (accessZone.equals("Intranät")) {
      result = "<view:values><view:value>Intranät</view:value></view:values>";
    } else if (accessZone.equals("Internet")) {
      result = "<view:values><view:value>Internet</view:value></view:values>";
    } else if (accessZone.equals("Extranät")) {
      result = "<view:values><view:value>Intranät</view:value></view:values>";
    } else {
      throw new RuntimeException("The access zone '" + accessZone + "' was not recognized.");
    }

    return result;
  }

  private void addProperty(final PrintWriter writer, final String key, final String value) {
    addProperty(writer, key, value, true);
  }

  private void addProperty(final PrintWriter writer, final String key, String value, final boolean escape) {
    // if no text, just exit
    if (!StringUtils.hasText(value)) {
      return;
    }

    value = StringUtils.replace(value, "LNID:= ", "");

    if (escape) {
      value = escape(value);
    }

    writer.println("<" + key + ">" + value + "</" + key + ">");
  }

  private String getDocumentType(final String documentType) {
    if (!StringUtils.hasText(documentType)) {
      return null;
    }

    String result;

    if (documentType.equals("- Välj dokumenttyp -")) {
      result = null;
    } else if (documentType.equals("Informationsmaterial")) {
      result = "Information";
    } else if (documentType.equals("Mötesanteckning")) {
      result = "Mötesdokument";
    } else if (documentType.equals("Nyhetsbrev")) {
      result = "Information";
    } else if (documentType.equals("Protokoll")) {
      result = "Mötesdokument";
    } else if (documentType.equals("Anbud")) {
      result = "Anbud/Upphandling";
    } else if (documentType.equals("Dagordning")) {
      result = "Mötesdokument";
    } else if (documentType.equals("Bild")) {
      result = "Bild";
    } else if (documentType.equals("Projektdokument")) {
      result = "Projektdokument";
    } else if (documentType.equals("Tjänsteutlåtande")) {
      result = "Tjänsteutlåtande";
    } else if (documentType.equals("Handlingsprogram")) {
      result = "Politisk beslutsberedning";
    } else if (documentType.equals("Rapport")) {
      result = "Rapport/Redovisning";
    } else if (documentType.equals("Handbok")) {
      result = "Beskrivning";
    } else if (documentType.equals("Policy")) {
      result = "Regler/Riktlinjer/Rekommendationer";
    } else if (documentType.equals("Uppdragsdokument")) {
      result = "Beslutsdokument";
    } else if (documentType.equals("Förteckning")) {
      result = "Förteckning";
    } else if (documentType.equals("Anvisning")) {
      result = "Regler/Riktlinjer/Rekommendationer ";
    } else if (documentType.equals("Ansökan")) {
      result = "Ansökan/Anmälan";
    } else if (documentType.equals("Anmälan")) {
      result = "Ansökan/Anmälan";
    } else if (documentType.equals("Lokal rutin")) {
      result = "\"Lokal rutin\"";
    } else if (documentType.equals("Avtal")) {
      result = "Avtal/Överenskommelse";
    } else if (documentType.equals("Plan")) {
      result = "Handlingsplan";
    } else if (documentType.equals("Formulär")) {
      result = "Förteckning";
    } else if (documentType.equals("Interpellation")) {
      result = "Politisk beslutsberedning";
    } else if (documentType.equals("Meddelande")) {
      result = "Korrespondens";
    } else if (documentType.equals("Upphandlingsdokument")) {
      result = "Anbud/Upphandling";
    } else if (documentType.equals("Intyg/betyg")) {
      result = "Intyg";
    } else if (documentType.equals("Motionssvar")) {
      result = "Politisk beslutsberedning";
    } else if (documentType.equals("Kalendarium")) {
      result = "Mötesdokument";
    } else if (documentType.equals("Enkät")) {
      result = "Enkät";
    } else if (documentType.equals("Kallelse")) {
      result = "Kallelse/Inbjudan";
    } else if (documentType.equals("Inbjudan")) {
      result = "Kallelse/Inbjudan";
    } else if (documentType.equals("Beställning")) {
      result = "Beställning";
    } else if (documentType.equals("Reseräkning")) {
      result = "";
    } else {
      throw new RuntimeException("Document type '" + documentType + "' not recognized!");
    }

    return result;
  }

  private void addAcl(final PrintWriter writer) {
    writer.println("<view:acl></view:acl>");
  }

  private void addAspects(final PrintWriter writer) {
    writer.println("<view:aspects>");
    writer.println("<cm:auditable></cm:auditable>");
    writer.println("<cm:titled></cm:titled>");
    writer.println("<cm:versionable></cm:versionable>");
    writer.println("<cm:author></cm:author>");
    writer.println("<sys:referenceable></sys:referenceable>");
    writer.println("<vgr:standard></vgr:standard>");
    writer.println("<vgr:metadata></vgr:metadata>");
    writer.println("<vgr:vgrdok></vgr:vgrdok>");
    writer.println("</view:aspects>");
  }

  private String getMimetype(final String extension) {
    String mimetype;

    if (!StringUtils.hasText(extension)) {
      mimetype = "application/octet-stream";
    } else if (extension.equalsIgnoreCase("pdf")) {
      mimetype = "application/pdf";
    } else if (extension.equalsIgnoreCase("ppt")) {
      mimetype = "application/vnd.ms-powerpoint";
    } else if (extension.equalsIgnoreCase("doc")) {
      mimetype = "application/msword";
    } else if (extension.equalsIgnoreCase("vsd")) {
      mimetype = "application/visio";
    } else if (extension.equalsIgnoreCase("bmp")) {
      mimetype = "image/bmp";
    } else if (extension.equalsIgnoreCase("htm")) {
      mimetype = "text/html";
    } else if (extension.equalsIgnoreCase("html")) {
      mimetype = "text/html";
    } else if (extension.equalsIgnoreCase("dot")) {
      mimetype = "application/msword";
    } else if (extension.equalsIgnoreCase("xls")) {
      mimetype = "application/vnd.ms-excel";
    } else if (extension.equalsIgnoreCase("jpg")) {
      mimetype = "image/jpeg";
    } else if (extension.equalsIgnoreCase("pot")) {
      mimetype = "application/vnd.ms-powerpoint";
    } else if (extension.equalsIgnoreCase("rtf")) {
      mimetype = "application/rtf";
    } else if (extension.equalsIgnoreCase("tiff")) {
      mimetype = "image/tiff";
    } else if (extension.equalsIgnoreCase("eps")) {
      mimetype = "application/eps";
    } else if (extension.equalsIgnoreCase("zip")) {
      mimetype = "application/zip";
    } else {
      throw new RuntimeException("The extension '" + extension + "' has no configured mimetype.");
    }

    return mimetype;
  }

  private Set<VgrDokDocument> createDocumentList() {
    final Set<VgrDokDocument> documents = new HashSet<VgrDokDocument>();

    InputStream inputStream = null;
    Reader bis = null;
    BufferedReader reader = null;
    int lineNumber = 1;
    int totalDuplicates = 0;
    int totalExactDuplicates = 0;

    try {
      inputStream = new FileInputStream(IMPORT_TEXT_FILE);

      bis = new InputStreamReader(inputStream);

      reader = new BufferedReader(bis);

      String line = null;

      while ((line = reader.readLine()) != null) {
        if (!StringUtils.hasText(line)) {
          continue;
        }

        final String[] parts = StringUtils.delimitedListToStringArray(line, "¤");

        if (parts.length < 1) {
          continue;
        }

        final VgrDokDocument document = createDocument(parts);

        populateFile(document);

        int count = 2;

        while (documents.contains(document)) {
          final VgrDokDocument duplicate = findDuplicate(document, documents);

          if (document.checksum.equals(duplicate.checksum) && document.documentId.equals(duplicate.documentId)) {
            System.out.println("The checksums for '" + document.universalID + "' and '" + duplicate.universalID
                + "' matches, it's exactly the same file therefor it's skipped.\n");

            totalExactDuplicates++;

            document.duplicate = true;
            document.name = UUID.randomUUID().toString();
          } else {
            document.name = extractName(document, count);

            System.out
                .println("Duplicate name found on line '" + lineNumber + "', changing to '" + document.name + "'");
            System.out.println("ID 1: " + document.universalID);
            System.out.println("ID 2: " + duplicate.universalID);
            System.out.println();

            count++;

            totalDuplicates++;
          }
        }

        if (document.duplicate) {
          continue;
        }

        documents.add(document);

        lineNumber++;
      }
    } catch (final Exception ex) {
      throw new RuntimeException("Error on line '" + lineNumber + "'", ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(bis);
      IOUtils.closeQuietly(reader);
    }

    if (totalDuplicates > 0) {
      System.out.println();
      System.out.println("Total number of duplicates: " + totalDuplicates);
      System.out.println();
    }

    if (totalExactDuplicates > 0) {
      System.out.println();
      System.out.println("Total number of exact duplicates: " + totalExactDuplicates);
      System.out.println();
    }

    return documents;
  }

  private void populateFile(final VgrDokDocument document) {
    String ext = FilenameUtils.getExtension(document.filename);

    if (StringUtils.hasText(ext)) {
      ext = "." + ext;
    }

    final File file = new File(FILES_FOLDER + File.separator + document.universalID + ext);

    if (!file.exists()) {
      System.out.println("Could not find " + file.getAbsolutePath() + " in folder " + FILES_FOLDER + " ("
          + document.filename + ")");

      return;
    }

    document.file = file;
    document.mimetype = getMimetype(FilenameUtils.getExtension(file.getName()));
    document.size = file.length();
    document.checksum = getChecksum(file);
  }

  private VgrDokDocument findDuplicate(final VgrDokDocument document, final Set<VgrDokDocument> documents) {
    for (final VgrDokDocument duplicate : documents) {
      if (duplicate.equals(document)) {
        return duplicate;
      }
    }

    return null;
  }

  private VgrDokDocument createDocument(final String[] parts) {
    final VgrDokDocument document = new VgrDokDocument();

    int position = 0;

    document.title = parts[position];
    document.documentId = parts[++position];
    document.uploadedBy = parts[++position];
    document.createdBy = parts[++position];
    document.createdDate = parseDate(parts[++position]);
    document.filename = parts[++position];
    document.version = parseVersion(parts[++position]);
    document.documentType = parts[++position];
    document.documentStatus = parts[++position];
    document.filepath = parseFilePath(parts[++position]);
    document.publishedBy = parts[++position];
    document.publishedDate = parseDate(parts[++position]);
    document.description = parts[++position];
    document.publishType = parts[++position];
    document.author = parts[++position];
    document.businessAreaMain = parts[++position];
    document.businessAreaSub = parts[++position];
    document.responsibleProject = parts[++position];
    document.responsiblePerson = parts[++position];
    document.documentSeries = parts[++position];
    document.externalReference = parts[++position];
    document.availableDateFrom = parseDate(parts[++position]);
    document.availableDateTo = parseDate(parts[++position]);
    document.accessZone = parts[++position];
    document.universalID = parts[++position];

    // special title...
    document.title = extractTitle(document);
    // and special name
    document.name = extractName(document, 1);

    return document;
  }

  private String parseVersion(final String value) {
    if (value.equals("1.")) {
      return "0.1";
    }

    try {
      Float.parseFloat(value);

      return value;
    } catch (final Exception ex) {
      System.out.println("Wrong version '" + value + "', replaced with version '0.1'");

      return "0.1";
    }
  }

  private String parseFilePath(String filepath) {
    if (!StringUtils.hasText(filepath)) {
      return null;
    }

    filepath = filepath.startsWith("\\") ? filepath.substring(1) : filepath;
    filepath = filepath.endsWith("\\") ? filepath.substring(0, filepath.length() - 1) : filepath;

    filepath = filepath.replace(":", "-");
    filepath = filepath.replace("/", "-");

    return filepath;
  }

  private String extractName(final VgrDokDocument document, final int count) {
    String name = "";

    if (StringUtils.hasText(document.filepath)) {
      name = document.filepath + "\\";
    }

    name += document.title.replace("\\", "-");

    if (count > 1) {
      name += " (" + count + ")";
    }

    final String extension = FilenameUtils.getExtension(document.filename);

    if (StringUtils.hasText(extension)) {
      name += "." + extension;
    }

    return name.replace("\\", "^");
  }

  private String extractTitle(final VgrDokDocument document) {
    String title = document.title;

    // get the extension from the filename
    final String extension = FilenameUtils.getExtension(document.filename);

    // if the file has an extension, remove that form the title if the title
    // ends with it
    if (StringUtils.hasText(extension) && title.endsWith(extension)) {
      title = StringUtils.replace(title, extension, "");
    }

    // if the title ends with a . remove it
    if (title.endsWith(".")) {
      title = title.substring(0, title.length() - 1);
    }

    return title + " (version " + document.version + ")";
  }

  private Date parseDate(final String date) {
    if (!StringUtils.hasText(date)) {
      return null;
    }

    try {
      final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      return simpleDateFormat.parse(date);
    } catch (final ParseException ex) {
      final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

      try {
        return simpleDateFormat.parse(date);
      } catch (final ParseException ex1) {
        throw new RuntimeException(ex1);
      }
    }
  }

  private String escape(String value) {
    value = value.replace("&", "&amp;");

    value = value.replace("\"", "&quot;");

    value = value.replace("\u001f", "");

    return value;
  }

}
