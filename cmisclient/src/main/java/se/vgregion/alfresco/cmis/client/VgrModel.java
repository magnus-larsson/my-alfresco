package se.vgregion.alfresco.cmis.client;

import org.alfresco.service.namespace.QName;

public interface VgrModel {

  public static final String VGR_URI = "http://www.vgregion.se/model/1.0";
  public static final String APELON_URI = "http://www.vgregion.se/apelon/1.0";
  public static final String KIV_URI = "http://www.vgregion.se/kiv/1.0";

  public static final QName TYPE_VGR_DOCUMENT = QName.createQName(VGR_URI, "document");
  public static final QName TYPE_APELON_DOCUMENTTYPE = QName.createQName(APELON_URI, "documentType");
  public static final QName TYPE_APELON_RECORDTYPE = QName.createQName(APELON_URI, "recordType");
  public static final QName TYPE_APELON_PROPERTY = QName.createQName(APELON_URI, "property");
  public static final QName TYPE_KIV_UNIT = QName.createQName(KIV_URI, "unit");

  // public static final QName ASPECT_PUBLISHABLE = QName.createQName(VGR_URI,
  // "publishable");
  public static final QName ASPECT_PUBLISHED = QName.createQName(VGR_URI, "published");
  public static final QName ASPECT_STANDARD = QName.createQName(VGR_URI, "standard");
  public static final QName ASPECT_METADATA = QName.createQName(VGR_URI, "metadata");
  public static final QName ASPECT_PERSON = QName.createQName(VGR_URI, "person");

  public static final QName PROP_DESCRIPTION = QName.createQName(VGR_URI, "dc.description");
  public static final QName PROP_CHECKSUM = QName.createQName(VGR_URI, "dc.identifier.checksum");
  public static final QName PROP_STATUS = QName.createQName(VGR_URI, "status");
  public static final QName PROP_TITLE = QName.createQName(VGR_URI, "dc.title");
  public static final QName PROP_TITLE_FILENAME = QName.createQName(VGR_URI, "dc.title.filename");
  public static final QName PROP_IDENTIFIER_DOCUMENTID = QName.createQName(VGR_URI, "dc.identifier.documentid");
  public static final QName PROP_IDENTIFIER_VERSION = QName.createQName(VGR_URI, "dc.identifier.version");
  public static final QName PROP_FORMAT_EXTENT_MIMETYPE = QName.createQName(VGR_URI, "dc.format.extent.mimetype");
  public static final QName PROP_FORMAT_EXTENT_EXTENSION = QName.createQName(VGR_URI, "dc.format.extension");
  public static final QName PROP_CONTRIBUTOR_UPLOADEDBY = QName.createQName(VGR_URI, "dc.contributor.uploadedby");
  // public static final QName PROP_DATE_CREATED = QName.createQName(VGR_URI,
  // "dc.date.created");
  public static final QName PROP_DATE_MODIFIED = QName.createQName(VGR_URI, "dc.date.modified");
  public static final QName PROP_LANGUAGE = QName.createQName(VGR_URI, "dc.language");
  public static final QName PROP_ACCESS_RIGHT = QName.createQName(VGR_URI, "dc.rights.accessrights");
  public static final QName PROP_REPRESENTATION = QName.createQName(VGR_URI, "representation");

  public static final QName PROP_APELON_NAME = QName.createQName(APELON_URI, "name");
  public static final QName PROP_APELON_INTERNALID = QName.createQName(APELON_URI, "internalid");
  public static final QName PROP_APELON_NAMESPACEID = QName.createQName(APELON_URI, "namespaceid");
  public static final QName PROP_APELON_SOURCEID = QName.createQName(APELON_URI, "sourceid");
  public static final QName PROP_APELON_KEY = QName.createQName(APELON_URI, "key");
  public static final QName PROP_APELON_VALUE = QName.createQName(APELON_URI, "value");
  public static final QName PROP_APELON_RTTYPE = QName.createQName(APELON_URI, "rtType");
  public static final QName PROP_APELON_RTDOCUMENTTYPEID = QName.createQName(APELON_URI, "rtDocumentTypeId");

  public static final QName PROP_KIV_OU = QName.createQName(KIV_URI, "ou");
  public static final QName PROP_KIV_DN = QName.createQName(KIV_URI, "dn");
  public static final QName PROP_KIV_HSAIDENTITY = QName.createQName(KIV_URI, "hsaidentity");

  public static final String STATUS_MAJOR = "Utgava";

  public static final String STATUS_MINOR = "Utkast";

  public static final QName ASSOC_PUBLISHED_TO_STORAGE = QName.createQName(VGR_URI, "published-to-storage");

  public static final String DEFAULT_LANGUAGE = "Svenska";
  public static final String DEFAULT_ACCESS_RIGHT = "Intranät";

}
