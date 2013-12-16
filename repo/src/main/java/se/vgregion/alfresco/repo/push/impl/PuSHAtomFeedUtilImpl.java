package se.vgregion.alfresco.repo.push.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.push.PuSHAtomFeedUtil;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class PuSHAtomFeedUtilImpl implements InitializingBean, PuSHAtomFeedUtil {
  public static final String NEWLINE = "\n";
  public static final String TAB = "  ";
  public static final String XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  public static final String FEED_START = "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:DC=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:VGR=\"http://purl.org/vgregion/elements/1.0/\">";
  public static final String FEED_END = "</feed>";
  public static final String ENTRY_START = "<entry>";
  public static final String ENTRY_END = "</entry>";
  public static final String TAG_TEMPLATE = "<#name##attributes#>#value#</#name#>";
  public static final String TAG_NOVALUE_TEMPLATE = "<#name##attributes# />";

  private static final String LINK = "https://alfresco.vgregion.se/alfresco/service/vgr/feed/document/published";
  private static final String TITLE = "VGR Publish/Unpublish document feed for content: ";
  private static final String ICON = "https://alfresco.vgregion.se/alfresco/images/logo/AlfrescoLogo16.ico";
  private static final String HOST = "alfresco.vgregion.se";

  private DescriptorService descriptorService;
  private NodeService nodeService;
  private ServiceUtils serviceUtils;
  private String downloadUrl;

  private static final Map<String, QName> propertyMap = new HashMap<String, QName>();
  static {
    // title custom handled
    propertyMap.put("DC:title.alternative", VgrModel.PROP_TITLE_ALTERNATIVE); // multiple
    propertyMap.put("DC:title.filename", VgrModel.PROP_TITLE_FILENAME);
    propertyMap.put("DC:title.filename.native", VgrModel.PROP_TITLE_FILENAME_NATIVE);

    // saved custom handled
    propertyMap.put("updated", ContentModel.PROP_MODIFIED); // skip=true
    propertyMap.put("dcterms:created", ContentModel.PROP_CREATED); // skip=true
    propertyMap.put("DC:creator", VgrModel.PROP_CREATOR); // multiple
    propertyMap.put("DC:creator.id", VgrModel.PROP_CREATOR_ID); // multiple
    propertyMap.put("summary", VgrModel.PROP_DESCRIPTION); // skip=true
    propertyMap.put("DC:description", VgrModel.PROP_DESCRIPTION);
    // language codes custom handled

    propertyMap.put("DC:format.extent.mimetype", VgrModel.PROP_FORMAT_EXTENT_MIMETYPE);
    propertyMap.put("DC:format.extent.mimetype.native", VgrModel.PROP_FORMAT_EXTENT_MIMETYPE_NATIVE);

    propertyMap.put("DC:subject.authorkeywords", VgrModel.PROP_SUBJECT_AUTHOR_KEYWORDS); // multiple
    propertyMap.put("DC:subject.keywords", VgrModel.PROP_SUBJECT_KEYWORDS); // multiple
    propertyMap.put("DC:subject.keywords.id", VgrModel.PROP_SUBJECT_KEYWORDS_ID); // multiple

    propertyMap.put("DC:creator.freetext", VgrModel.PROP_CREATOR_FREETEXT);
    propertyMap.put("DC:creator.document", VgrModel.PROP_CREATOR_DOCUMENT); // multiple
    propertyMap.put("DC:creator.document.id", VgrModel.PROP_CREATOR_DOCUMENT_ID); // multiple
    propertyMap.put("DC:creator.function", VgrModel.PROP_CREATOR_FUNCTION);
    propertyMap.put("DC:creator.forunit", VgrModel.PROP_CREATOR_FORUNIT); // multiple
    propertyMap.put("DC:creator.forunit.id", VgrModel.PROP_CREATOR_FORUNIT_ID); // multiple
    propertyMap.put("DC:creator.recordscreator", VgrModel.PROP_CREATOR_RECORDSCREATOR); // multiple
    propertyMap.put("DC:creator.recordscreator.id", VgrModel.PROP_CREATOR_RECORDSCREATOR_ID); // multiple
    propertyMap.put("DC:creator.project-assignment", VgrModel.PROP_CREATOR_PROJECT_ASSIGNMENT);

    propertyMap.put("DC:publisher", VgrModel.PROP_PUBLISHER);
    propertyMap.put("DC:publisher.id", VgrModel.PROP_PUBLISHER_ID);
    propertyMap.put("DC:publisher.forunit", VgrModel.PROP_PUBLISHER_FORUNIT); // multiple
    propertyMap.put("DC:publisher.forunit.id", VgrModel.PROP_PUBLISHER_FORUNIT_ID); // multiple
    propertyMap.put("DC:publisher.project-assignment", VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT); // multiple

    propertyMap.put("DC:date.issued", VgrModel.PROP_DATE_ISSUED); // skip = true
    propertyMap.put("DC:date.accepted", VgrModel.PROP_DATE_ACCEPTED); // skip=true
    propertyMap.put("DC:date.controlled", VgrModel.PROP_DATE_CONTROLLED); // skip=true
    propertyMap.put("DC:date.validfrom", VgrModel.PROP_DATE_VALID_FROM); // skip=true
    propertyMap.put("DC:date.validto", VgrModel.PROP_DATE_VALID_TO); // skip=true
    propertyMap.put("DC:date.availablefrom", VgrModel.PROP_DATE_AVAILABLE_FROM); // skip=true
    propertyMap.put("DC:date.availableto", VgrModel.PROP_DATE_AVAILABLE_TO); // skip=true

    // available from special handled
    // available to special handled
    propertyMap.put("DC:date.copyrighted", VgrModel.PROP_DATE_COPYRIGHTED); // skip=true

    propertyMap.put("DC:contributor.savedby", VgrModel.PROP_CONTRIBUTOR_SAVEDBY);
    propertyMap.put("DC:contributor.savedby.id", VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID);
    propertyMap.put("DC:contributor.acceptedby", VgrModel.PROP_CONTRIBUTOR_ACCEPTEDBY); // multiple
    propertyMap.put("DC:contributor.acceptedby.id", VgrModel.PROP_CONTRIBUTOR_ACCEPTEDBY_ID); // multiple
    propertyMap.put("DC:contributor.acceptedby.freetext", VgrModel.PROP_CONTRIBUTOR_ACCEPTEDBY_FREETEXT);
    propertyMap.put("DC:contributor.acceptedby.role", VgrModel.PROP_CONTRIBUTOR_ACCEPTEDBY);
    propertyMap.put("DC:contributor.acceptedby.unit.freetext", VgrModel.PROP_CONTRIBUTOR_ACCEPTEDBY_UNIT_FREETEXT);
    propertyMap.put("DC:contributor.controlledby", VgrModel.PROP_CONTRIBUTOR_CONTROLLEDBY); // multiple
    propertyMap.put("DC:contributor.controlledby.id", VgrModel.PROP_CONTRIBUTOR_CONTROLLEDBY_ID); // multiple
    propertyMap.put("DC:contributor.controlledby.freetext", VgrModel.PROP_CONTRIBUTOR_CONTROLLEDBY_FREETEXT);
    propertyMap.put("DC:contributor.controlledby.role", VgrModel.PROP_CONTRIBUTOR_ACCEPTEDBY);
    propertyMap.put("DC:contributor.controlledby.unit.freetext", VgrModel.PROP_CONTRIBUTOR_ACCEPTEDBY_UNIT_FREETEXT);
    propertyMap.put("DC:contributor.unit", VgrModel.PROP_CONTRIBUTOR_UNIT);

    propertyMap.put("DC:type.document", VgrModel.PROP_TYPE_DOCUMENT);
    propertyMap.put("DC:type.document.structure", VgrModel.PROP_DOCUMENT_STRUCTURE);
    propertyMap.put("DC:type.document.structure.id", VgrModel.PROP_DOCUMENT_STRUCTURE_ID);
    propertyMap.put("DC:type.templatename", VgrModel.PROP_TYPE_TEMPLATENAME);
    propertyMap.put("DC:type.record", VgrModel.PROP_TYPE_RECORD);
    propertyMap.put("DC:type.record.id", VgrModel.PROP_TYPE_RECORD_ID);
    propertyMap.put("DC:type.process.name", VgrModel.PROP_TYPE_PROCESS_NAME); // multiple
    propertyMap.put("DC:type.file.process", VgrModel.PROP_TYPE_FILE_PROCESS); // multiple
    propertyMap.put("DC:type.file", VgrModel.PROP_TYPE_FILE); // multiple
    propertyMap.put("DC:type.document.serie", VgrModel.PROP_TYPE_DOCUMENT_SERIE);
    propertyMap.put("DC:type.document.id", VgrModel.PROP_TYPE_DOCUMENT_ID);

    propertyMap.put("DC:format.extent", VgrModel.PROP_FORMAT_EXTENT); // multiple
    propertyMap.put("DC:format.extension", VgrModel.PROP_FORMAT_EXTENT_EXTENSION);
    propertyMap.put("DC:format.extension.native", VgrModel.PROP_FORMAT_EXTENT_EXTENSION_NATIVE);

    propertyMap.put("DC:identifier", VgrModel.PROP_IDENTIFIER);
    propertyMap.put("DC:identifier.native", VgrModel.PROP_IDENTIFIER_NATIVE);
    propertyMap.put("DC:identifier.checksum", VgrModel.PROP_IDENTIFIER_CHECKSUM);
    propertyMap.put("DC:identifier.checksum.native", VgrModel.PROP_IDENTIFIER_CHECKSUM_NATIVE);
    // document id is special handled
    propertyMap.put("DC:identifier.version", VgrModel.PROP_IDENTIFIER_VERSION);
    propertyMap.put("DC:identifier.diarie.id", VgrModel.PROP_IDENTIFIER_DIARIE_ID);
    propertyMap.put("DC:identifier.location", VgrModel.PROP_IDENTIFIER_LOCATION);

    propertyMap.put("DC:source", VgrModel.PROP_SOURCE);
    propertyMap.put("DC:source.documentid", VgrModel.PROP_SOURCE_DOCUMENTID);
    propertyMap.put("DC:source.origin", VgrModel.PROP_SOURCE_ORIGIN);
    propertyMap.put("DC:relation.isversionof", VgrModel.PROP_RELATION_ISVERSIONOF);
    propertyMap.put("DC:relation.replaces", VgrModel.PROP_RELATION_REPLACES); // multiple
    propertyMap.put("DC:coverage.hsacode", VgrModel.PROP_COVERAGE_HSACODE); // multiple
    propertyMap.put("DC:coverage.hsacode.id", VgrModel.PROP_COVERAGE_HSACODE_ID); // multiple
    propertyMap.put("dcterms:audience", VgrModel.PROP_AUDIENCE);
    propertyMap.put("dcterms:audience.id", VgrModel.PROP_AUDIENCE_ID);
    propertyMap.put("VGR:status.document", VgrModel.PROP_STATUS_DOCUMENT);
    propertyMap.put("VGR:status.document", VgrModel.PROP_STATUS_DOCUMENT_ID);
    // access rights is special handled
    // id is special handled
    // download url is special handled

  }

  private static final Set<String> skipSet = new HashSet<String>();
  static {
    skipSet.add("updated");
    skipSet.add("dcterms:created");
    skipSet.add("summary");
    skipSet.add("DC:date.issued");
    skipSet.add("DC:date.accepted");
    skipSet.add("DC:date.controlled");
    skipSet.add("DC:date.validfrom");
    skipSet.add("DC:date.validto");
    skipSet.add("DC:date.availablefrom");
    skipSet.add("DC:date.availableto");
    skipSet.add("DC:date.copyrighted");
  }

  private static final Set<String> multipleSet = new HashSet<String>();
  static {
    multipleSet.add("DC:title.alternative");
    multipleSet.add("DC:creator");
    multipleSet.add("DC:creator.id");
    multipleSet.add("DC:subject.authorkeywords");
    multipleSet.add("DC:subject.keywords");
    multipleSet.add("DC:subject.keywords.id");
    multipleSet.add("DC:creator.document");
    multipleSet.add("DC:creator.document.id");
    multipleSet.add("DC:creator.forunit");
    multipleSet.add("DC:creator.forunit.id");
    multipleSet.add("DC:creator.recordscreator");
    multipleSet.add("DC:creator.recordscreator.id");
    multipleSet.add("DC:publisher.forunit");
    multipleSet.add("DC:publisher.forunit.id");
    multipleSet.add("DC:publisher.project-assignment");
    multipleSet.add("DC:contributor.acceptedby");
    multipleSet.add("DC:contributor.acceptedby.id");
    multipleSet.add("DC:contributor.controlledby");
    multipleSet.add("DC:contributor.controlledby.id");
    multipleSet.add("DC:type.process.name");
    multipleSet.add("DC:type.file.process");
    multipleSet.add("DC:type.file");
    multipleSet.add("DC:format.extent");
    multipleSet.add("DC:relation.replaces");
    multipleSet.add("DC:coverage.hsacode");
    multipleSet.add("DC:coverage.hsacode.id");
  }

  @Override
  public String createPublishDocumentFeed(NodeRef nodeRef) {
    StringBuffer result = new StringBuffer();
    if (nodeService.exists(nodeRef)) {
      Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

      result.append(createHeader(nodeRef, properties));

      result.append(createPublishSpecificData(nodeRef, properties));

      result.append(createCommonData(nodeRef, properties));

      result.append(createFooter());
    }
    return result.toString();
  }

  @Override
  public String createUnPublishDocumentFeed(NodeRef nodeRef) {
    StringBuffer result = new StringBuffer();
    if (nodeService.exists(nodeRef)) {
      Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

      result.append(createHeader(nodeRef, properties));

      result.append(createUnpublishSpecificData(nodeRef, properties));

      result.append(createCommonData(nodeRef, properties));

      result.append(createFooter());
    }
    return result.toString();
  }

  /**
   * Add common data
   * 
   * @param nodeRef
   * @param properties
   * @return
   */
  public String createCommonData(final NodeRef nodeRef, final Map<QName, Serializable> properties) {
    if (properties == null) {
      throw new AlfrescoRuntimeException("Properties map is null");
    }
    StringBuffer sb = new StringBuffer();
    // Special handling of title if missing
    String title = (String) properties.get(VgrModel.PROP_TITLE);
    if (title == null) {
      title = (String) properties.get(ContentModel.PROP_NAME);
    }
    sb.append(createEntryDataTag("title", title, null));
    sb.append(createEntryDataTag("DC:title", title, null));

    // Special handling saved
    Date saved = (Date) properties.get(VgrModel.PROP_DATE_SAVED);
    if (saved == null) {
      saved = (Date) properties.get(VgrModel.PROP_DATE_CREATED);
      sb.append(createEntryDataTag("DC:date.saved", toUTCDate(saved), null)); // skip=true
    }

    // Special handling language
    String languagesStr = (String) properties.get(VgrModel.PROP_LANGUAGE);
    if (languagesStr != null) {
      String[] languages = languagesStr.split(",");
      for (String language : languages) {
        String languageCode = serviceUtils.findLanguageCode(language);
        sb.append(createEntryDataTag("DC:language", languageCode, null)); // skip=true
      }
    }

    // Special handling of document id
    sb.append(createEntryDataTag("DC:identifier.documentid", nodeRef.toString(), null));

    // Special handling of access rights
    String accessRightsStr = (String) properties.get(VgrModel.PROP_ACCESS_RIGHT);
    String theAccessRight = null;
    if (accessRightsStr == null || accessRightsStr.length() == 0) {
      theAccessRight = "";
    } else {
      String[] accessRights = accessRightsStr.split(",");
      if (accessRights.length == 1) {
        theAccessRight = accessRights[0];
      } else {
        for (String accessRight : accessRights) {
          if ("Internet".equalsIgnoreCase(accessRight)) {
            theAccessRight = "Internet";
            break;
          }
        }
        if (theAccessRight == null) {
          theAccessRight = accessRights[0];
        }
      }
    }
    sb.append(createEntryDataTag("DC:rights.accessrights", theAccessRight, null));

    // special handling of id
    sb.append(createEntryDataTag("id", "tag:" + HOST + ",2011-06-30:" + nodeRef.getId(), null));

    // special handling of download url
    Serializable documentId = properties.get(VgrModel.PROP_SOURCE_DOCUMENTID);
    Map<String, Serializable> linkMap = new HashMap<String, Serializable>();
    if (documentId != null) {
      String theDownloadUrl = downloadUrl.replaceAll("#documentId#", documentId.toString());
      linkMap.put("href", theDownloadUrl);
    }
    sb.append(createEntryDataTag("link", null, linkMap));

    sb.append(mapProperties(nodeRef, properties));

    // skip -> tom = skriv inte ut
    // multiple -> flera taggar
    // Kolla datum typ och skriv utc datum
    return sb.toString();
  }

  private String mapProperties(final NodeRef nodeRef, final Map<QName, Serializable> properties) {
    if (properties == null) {
      throw new AlfrescoRuntimeException("Properties map is null");
    }
    StringBuffer sb = new StringBuffer();
    Set<String> keySet = propertyMap.keySet();
    for (String key : keySet) {
      QName qName = propertyMap.get(key);
      Serializable value = properties.get(qName);
      if (value != null && value instanceof String && multipleSet.contains(key) && ((String) value).indexOf(",") != -1) {
        String valueStr = (String) value;
        String[] splits = valueStr.split(",");
        for (String split : splits) {
          sb.append(writeProperty(key, split));
        }
      } else {
        sb.append(writeProperty(key, value));
      }

    }
    return sb.toString();
  }

  private String writeProperty(final String key, final Serializable value) {
    StringBuffer sb = new StringBuffer();
    if (value instanceof Date) {
      Date aDate = (Date) value;
      sb.append(createEntryDataTag(key, toUTCDate(aDate), null));
    } else if (value != null) {
      sb.append(createEntryDataTag(key, value, null));
    } else {
      if (!skipSet.contains(key)) {
        sb.append(createEntryDataTag(key, null, null));
      }
    }
    return sb.toString();
  }

  /**
   * Add specific publish data
   * 
   * @param nodeRef
   * @param properties
   * @return
   */
  public String createPublishSpecificData(NodeRef nodeRef, Map<QName, Serializable> properties) {
    if (properties == null) {
      throw new AlfrescoRuntimeException("Properties map is null");
    }
    StringBuffer sb = new StringBuffer();
    sb.append(createEntryDataTag("published", "true", null));
    Date publishDate = (Date) properties.get(VgrModel.PROP_PUSHED_FOR_PUBLISH);
    sb.append(createEntryDataTag("requestId", "publish_" + nodeRef.toString() + "_" + publishDate.getTime(), null));
    return sb.toString();
  }

  /**
   * Add specific unpublish data
   * 
   * @param nodeRef
   * @param properties
   * @return
   */
  public String createUnpublishSpecificData(NodeRef nodeRef, Map<QName, Serializable> properties) {
    if (properties == null) {
      throw new AlfrescoRuntimeException("Properties map is null");
    }
    StringBuffer sb = new StringBuffer();
    sb.append(createEntryDataTag("published", "false", null));
    Date unpublishDate = (Date) properties.get(VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
    sb.append(createEntryDataTag("requestId", "unpublish_" + nodeRef.toString() + "_" + unpublishDate.getTime(), null));
    return sb.toString();
  }

  /**
   * Create the feed header
   * 
   * @return
   */
  public String createHeader(NodeRef nodeRef, Map<QName, Serializable> properties) {
    if (properties == null) {
      throw new AlfrescoRuntimeException("Properties map is null");
    }
    StringBuffer sb = new StringBuffer();
    sb.append(XML_START + NEWLINE);
    sb.append(FEED_START + NEWLINE);
    sb.append(createDataTag("id", LINK, null));

    Map<String, Serializable> linkMap = new HashMap<String, Serializable>();
    linkMap.put("href", LINK);
    linkMap.put("rel", "self");
    sb.append(createDataTag("link", null, linkMap));

    Descriptor serverDescriptor = descriptorService.getServerDescriptor();
    Map<String, Serializable> generatorMap = new HashMap<String, Serializable>();
    generatorMap.put("version", serverDescriptor.getVersion());
    sb.append(createDataTag("generator", serverDescriptor.getName() + " (" + serverDescriptor.getEdition() + ")", generatorMap));

    sb.append(createDataTag("title", TITLE + nodeRef, null));

    Date updated = new Date();

    sb.append(createDataTag("updated", toUTCDate(updated), null));
    sb.append(createDataTag("icon", ICON, null));
    sb.append(createDataTag("author", NEWLINE + TAB + createDataTag("name", "system", null) + TAB, null));
    sb.append(TAB + ENTRY_START + NEWLINE);
    return sb.toString();
  }

  /**
   * Create the feed footer
   * 
   * @return
   */
  public String createFooter() {
    StringBuffer sb = new StringBuffer();
    sb.append(TAB + ENTRY_END + NEWLINE);
    sb.append(FEED_END + NEWLINE);
    return sb.toString();
  }

  /**
   * Create a data tag
   * 
   * @param name
   *          Mandatory must not be null
   * @param value
   *          Can be empty or null
   * @param attributes
   *          Can be empty or null
   * @return
   */
  public String createDataTag(final String name, final Serializable value, final Map<String, Serializable> attributes) {
    String concatenatedAttributes = "";
    if (attributes != null && attributes.size() > 0) {
      Set<String> keySet = attributes.keySet();
      for (String key : keySet) {
        concatenatedAttributes = concatenatedAttributes + " " + key + "=\"" + attributes.get(key) + "\"";
      }
    }

    StringBuffer sb = new StringBuffer();
    if (value == null || value.toString().length() == 0) {
      sb.append(TAB + TAG_NOVALUE_TEMPLATE.replaceAll("#name#", name).replaceAll("#attributes#", concatenatedAttributes) + NEWLINE);
    } else {
      sb.append(TAB + TAG_TEMPLATE.replaceAll("#name#", name).replaceAll("#value#", value.toString()).replaceAll("#attributes#", concatenatedAttributes) + NEWLINE);
    }
    return sb.toString();

  }

  /**
   * Create an entry data tag
   * 
   * @param name
   *          Mandatory must not be null
   * @param value
   *          Can be empty or null
   * @param attributes
   *          Can be empty or null
   * @return
   */
  public String createEntryDataTag(final String name, final Serializable value, final Map<String, Serializable> attributes) {
    StringBuffer sb = new StringBuffer();
    sb.append(TAB + createDataTag(name, value, attributes));
    return sb.toString();
  }

  public void setDescriptorService(DescriptorService descriptorService) {
    this.descriptorService = descriptorService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setServiceUtils(ServiceUtils serviceUtils) {
    this.serviceUtils = serviceUtils;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  private String toUTCDate(Date date) {
    if (date != null) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
      return sdf.format(date);
    } else {
      return null;
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(descriptorService);
    Assert.notNull(nodeService);
    Assert.notNull(serviceUtils);
    Assert.notNull(downloadUrl);
  }

}
