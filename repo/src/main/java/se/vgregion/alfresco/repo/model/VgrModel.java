package se.vgregion.alfresco.repo.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public interface VgrModel {

  public static final String VGR_URI = "http://www.vgregion.se/model/1.0";
  public static final String VGR_SHORT = "vgr";
  public static final String APELON_URI = "http://www.vgregion.se/apelon/1.0";
  public static final String KIV_URI = "http://www.vgregion.se/kiv/1.0";

  public static final QName TYPE_VGR_DOCUMENT = QName.createQName(VGR_URI, "document");
  public static final QName TYPE_APELON_DOCUMENTTYPE = QName.createQName(APELON_URI, "documentType");
  public static final QName TYPE_APELON_RECORDTYPE = QName.createQName(APELON_URI, "recordType");
  public static final QName TYPE_APELON_PROPERTY = QName.createQName(APELON_URI, "property");
  public static final QName TYPE_KIV_UNIT = QName.createQName(KIV_URI, "unit");
  public static final QName TYPE_PDF = QName.createQName(VGR_URI, "pdf");

  public static final QName ASPECT_PUBLISHED = QName.createQName(VGR_URI, "published");
  public static final QName ASPECT_STANDARD = QName.createQName(VGR_URI, "standard");
  public static final QName ASPECT_METADATA = QName.createQName(VGR_URI, "metadata");
  public static final QName ASPECT_PERSON = QName.createQName(VGR_URI, "person");
  public static final QName ASPECT_WATCHED = QName.createQName(VGR_URI, "watched");
  public static final QName ASPECT_VGRDOK = QName.createQName(VGR_URI, "vgrdok");
  public static final QName ASPECT_DONOTTOUCH = QName.createQName(VGR_URI, "donottouch");
  public static final QName ASPECT_AUTO_PUBLISH = QName.createQName(VGR_URI, "auto-publish");
  public static final QName ASPECT_AUTO_DELETABLE = QName.createQName(VGR_URI, "auto_deletable");
  public static final QName ASPECT_NOARCHIVE = QName.createQName(VGR_URI, "noarchive");

  public static final QName PROP_CHECKSUM = QName.createQName(VGR_URI, "dc.identifier.checksum");
  public static final QName PROP_CHECKSUM_NATIVE = QName.createQName(VGR_URI, "dc.identifier.checksum.native");
  public static final QName PROP_STATUS_DOCUMENT = QName.createQName(VGR_URI, "vgr.status.document");
  public static final QName PROP_STATUS_DOCUMENT_ID = QName.createQName(VGR_URI, "vgr.status.document.id");
  public static final QName PROP_STATUS_DOCUMENT_OLD = QName.createQName(VGR_URI, "hc.status.document");
  public static final QName PROP_TITLE = QName.createQName(VGR_URI, "dc.title");
  public static final QName PROP_TITLE_FILENAME = QName.createQName(VGR_URI, "dc.title.filename");
  public static final QName PROP_TITLE_FILENAME_NATIVE = QName.createQName(VGR_URI, "dc.title.filename.native");
  public static final QName PROP_IDENTIFIER = QName.createQName(VGR_URI, "dc.identifier");
  public static final QName PROP_IDENTIFIER_NATIVE = QName.createQName(VGR_URI, "dc.identifier.native");
  public static final QName PROP_IDENTIFIER_TEMP = QName.createQName(VGR_URI, "dc.identifier.temp");
  public static final QName PROP_IDENTIFIER_DOCUMENTID = QName.createQName(VGR_URI, "dc.identifier.documentid");
  public static final QName PROP_IDENTIFIER_VERSION = QName.createQName(VGR_URI, "dc.identifier.version");
  public static final QName PROP_FORMAT_EXTENT_MIMETYPE = QName.createQName(VGR_URI, "dc.format.extent.mimetype");
  public static final QName PROP_FORMAT_EXTENT_MIMETYPE_NATIVE = QName.createQName(VGR_URI, "dc.format.extent.mimetype.native");
  public static final QName PROP_FORMAT_EXTENT_EXTENSION = QName.createQName(VGR_URI, "dc.format.extension");
  public static final QName PROP_FORMAT_EXTENT_EXTENSION_NATIVE = QName.createQName(VGR_URI, "dc.format.extension.native");
  public static final QName PROP_CONTRIBUTOR_SAVEDBY = QName.createQName(VGR_URI, "dc.contributor.savedby");
  public static final QName PROP_CONTRIBUTOR_SAVEDBY_ID = QName.createQName(VGR_URI, "dc.contributor.savedby.id");
  public static final QName PROP_DATE_SAVED = QName.createQName(VGR_URI, "dc.date.saved");
  public static final QName PROP_LANGUAGE = QName.createQName(VGR_URI, "dc.language");
  public static final QName PROP_ACCESS_RIGHT = QName.createQName(VGR_URI, "dc.rights.accessrights");
  public static final QName PROP_REPRESENTATION = QName.createQName(VGR_URI, "representation");
  public static final QName PROP_DATE_ISSUED = QName.createQName(VGR_URI, "dc.date.issued");
  public static final QName PROP_PUBLISHER = QName.createQName(VGR_URI, "dc.publisher");
  public static final QName PROP_PUBLISHER_ID = QName.createQName(VGR_URI, "dc.publisher.id");
  public static final QName PROP_PUSHED_FOR_PUBLISH = QName.createQName(VGR_URI, "pushed-for-publish");
  public static final QName PROP_PUSHED_FOR_UNPUBLISH = QName.createQName(VGR_URI, "pushed-for-unpublish");
  public static final QName PROP_PUSHED_COUNT = QName.createQName(VGR_URI, "pushed-count");
  public static final QName PROP_PUBLISH_STATUS = QName.createQName(VGR_URI, "publish-status");
  public static final QName PROP_UNPUBLISH_STATUS = QName.createQName(VGR_URI, "unpublish-status");
  public static final QName PROP_DATE_AVAILABLE_TO = QName.createQName(VGR_URI, "dc.date.availableto");
  public static final QName PROP_DATE_AVAILABLE_FROM = QName.createQName(VGR_URI, "dc.date.availablefrom");
  public static final QName PROP_SOURCE = QName.createQName(VGR_URI, "dc.source");
  public static final QName PROP_SOURCE_TEMP = QName.createQName(VGR_URI, "dc.source.temp");
  public static final QName PROP_SOURCE_DOCUMENTID = QName.createQName(VGR_URI, "dc.source.documentid");
  public static final QName PROP_SOURCE_ORIGIN = QName.createQName(VGR_URI, "dc.source.origin");
  public static final QName PROP_DESCRIPTION = QName.createQName(VGR_URI, "dc.description");
  public static final QName PROP_CREATOR = QName.createQName(VGR_URI, "dc.creator");
  public static final QName PROP_CREATOR_ID = QName.createQName(VGR_URI, "dc.creator.id");
  public static final QName PROP_TYPE_DOCUMENT = QName.createQName(VGR_URI, "dc.type.document");
  public static final QName PROP_TYPE_RECORD = QName.createQName(VGR_URI, "dc.type.record");
  public static final QName PROP_TYPE_RECORD_ID = QName.createQName(VGR_URI, "dc.type.record.id");
  public static final QName PROP_CREATOR_PROJECT_ASSIGNMENT = QName.createQName(VGR_URI, "dc.creator.project-assignment");
  public static final QName PROP_TYPE_DOCUMENT_SERIE = QName.createQName(VGR_URI, "dc.type.document.serie");
  public static final QName PROP_TYPE_DOCUMENT_ID = QName.createQName(VGR_URI, "dc.type.document.id");
  public static final QName PROP_CREATOR_FUNCTION = QName.createQName(VGR_URI, "dc.creator.function");
  public static final QName PROP_PUBLISHER_PROJECT_ASSIGNMENT = QName.createQName(VGR_URI, "dc.publisher.project-assignment");
  public static final QName PROP_PUBLISHER_FORUNIT = QName.createQName(VGR_URI, "dc.publisher.forunit");
  public static final QName PROP_PUBLISHER_FORUNIT_ID = QName.createQName(VGR_URI, "dc.publisher.forunit.id");
  public static final QName PROP_COVERAGE_HSACODE = QName.createQName(VGR_URI, "dc.coverage.hsacode");
  public static final QName PROP_COVERAGE_HSACODE_ID = QName.createQName(VGR_URI, "dc.coverage.hsacode.id");
  public static final QName PROP_CREATOR_DOCUMENT = QName.createQName(VGR_URI, "dc.creator.document");
  public static final QName PROP_CREATOR_DOCUMENT_ID = QName.createQName(VGR_URI, "dc.creator.document.id");
  public static final QName PROP_CREATOR_FORUNIT = QName.createQName(VGR_URI, "dc.creator.forunit");
  public static final QName PROP_CREATOR_FORUNIT_ID = QName.createQName(VGR_URI, "dc.creator.forunit.id");
  public static final QName PROP_CREATOR_RECORDSCREATOR = QName.createQName(VGR_URI, "dc.creator.recordscreator");
  public static final QName PROP_CREATOR_RECORDSCREATOR_ID = QName.createQName(VGR_URI, "dc.creator.recordscreator.id");
  public static final QName PROP_CONTRIBUTOR_ACCEPTEDBY = QName.createQName(VGR_URI, "dc.contributor.acceptedby");
  public static final QName PROP_CONTRIBUTOR_ACCEPTEDBY_ID = QName.createQName(VGR_URI, "dc.contributor.acceptedby.id");
  public static final QName PROP_CONTRIBUTOR_CONTROLLEDBY = QName.createQName(VGR_URI, "dc.contributor.controlledby");
  public static final QName PROP_CONTRIBUTOR_CONTROLLEDBY_ID = QName.createQName(VGR_URI, "dc.contributor.controlledby.id");
  public static final QName PROP_DOCUMENT_STRUCTURE = QName.createQName(VGR_URI, "dc.dc.type.document.structure");
  public static final QName PROP_DOCUMENT_STRUCTURE_ID = QName.createQName(VGR_URI, "dc.dc.type.document.structure.id");

  // properties specific for the VGR Dok aspect
  public static final QName PROP_VGR_DOK_DOCUMENT_ID = QName.createQName(VGR_URI, "vgr_dok_document_id");
  public static final QName PROP_VGR_DOK_VERSION = QName.createQName(VGR_URI, "vgr_dok_version");
  public static final QName PROP_VGR_DOK_UNIVERSAL_ID = QName.createQName(VGR_URI, "vgr_dok_universal_id");
  public static final QName PROP_VGR_DOK_FILEPATH = QName.createQName(VGR_URI, "vgr_dok_filepath");
  public static final QName PROP_VGR_DOK_MOVED_TO_PATH = QName.createQName(VGR_URI, "vgr_dok_moved_to_path");
  public static final QName PROP_VGR_DOK_MOVED_TO_VERSION = QName.createQName(VGR_URI, "vgr_dok_moved_to_version");
  public static final QName PROP_VGR_DOK_DOCUMENT_STATUS = QName.createQName(VGR_URI, "vgr_dok_document_status");
  public static final QName PROP_VGR_DOK_PUBLISH_TYPE = QName.createQName(VGR_URI, "vgr_dok_publish_type");

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
  public static final QName PROP_KIV_MODIFIED = QName.createQName(KIV_URI, "modified");
  public static final QName PROP_KIV_ACCESSED = QName.createQName(KIV_URI, "accessed");

  public static final String STATUS_MAJOR = "Utgava";

  public static final String STATUS_MINOR = "Utkast";

  public static final QName ASSOC_PUBLISHED_TO_STORAGE = QName.createQName(VGR_URI, "published-to-storage");

  public static final String DEFAULT_LANGUAGE = "Svenska";
  public static final String DEFAULT_ACCESS_RIGHT = "Intranät";
  public static final String ACCESS_RIGHT_INTRANET = "Intranät";
  public static final String ACCESS_RIGHT_INTERNET = "Internet";

  public static final QName RD_PDF = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "pdf");

  public static final QName PROP_AUTO_PUBLISH_MAJOR_VERSION = QName.createQName(VGR_URI, "auto_publish_major_version");
  public static final QName PROP_AUTO_PUBLISH_ALL_VERSIONS = QName.createQName(VGR_URI, "auto_publish_all_versions");

  public static final QName PROP_AUTO_DELETABLE_MAX_AGE = QName.createQName(VGR_URI, "max_age");
  public static final QName PROP_AUTO_DELETABLE_DELETE_NODE = QName.createQName(VGR_URI, "delete_node");
  public static final QName PROP_AUTO_DELETABLE_MIMETYPES = QName.createQName(VGR_URI, "mimetypes");

  public static final QName PROP_SENT_EMAILS = QName.createQName(VGR_URI, "sent-emails");
  
  public static final QName PROP_PERSON_RESPONSIBILITY_CODE = QName.createQName(VGR_URI, "responsibility_code");
  public static final QName PROP_PERSON_ORGANIZATION_DN = QName.createQName(VGR_URI, "organization_dn");
  
  public static final QName ASPECT_THUMBNAIL_PHOTO = QName.createQName(VGR_URI, "thumbnail-photo-aspect");
  public static final QName PROP_THUMBNAIL_PHOTO = QName.createQName(VGR_URI, "thumbnailPhoto");
 
  
}
