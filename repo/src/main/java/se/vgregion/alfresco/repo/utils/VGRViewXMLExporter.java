package se.vgregion.alfresco.repo.utils;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.cmr.view.ReferenceType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.springframework.extensions.surf.util.Base64;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;




/**
 * Exporter that exports Repository information to XML (Alfresco Repository View Schema)
 * 
 * @author David Caruana
 */
/*package*/ class VGRViewXMLExporter
implements Exporter
{

  // Repository View Schema Definitions
  private static final String VALUES_LOCALNAME = "values";
  private static final String VALUE_LOCALNAME = "value";
  private static final String CHILDNAME_LOCALNAME = "childName";
  private static final String ASPECTS_LOCALNAME = "aspects";
  private static final String PROPERTIES_LOCALNAME = "properties";
  private static final String ASSOCIATIONS_LOCALNAME = "associations";
  private static final String DATATYPE_LOCALNAME = "datatype";
  private static final String ISNULL_LOCALNAME = "isNull";
  private static final String MLVALUE_LOCALNAME = "mlvalue";
  private static final String LOCALE_LOCALNAME = "locale";
  private static final String ACL_LOCALNAME  = "acl";
  private static final String ACE_LOCALNAME  = "ace";
  private static final String ACCESS_LOCALNAME  = "access";
  private static final String AUTHORITY_LOCALNAME  = "authority";
  private static final String PERMISSION_LOCALNAME  = "permission";
  private static final String INHERITPERMISSIONS_LOCALNAME  = "inherit";
  private static final String REFERENCE_LOCALNAME = "reference";
  private static final String PATHREF_LOCALNAME = "pathref";
  private static final String NODEREF_LOCALNAME = "noderef";
  private static final String METS_LOCALNAME = "mets";
  private static final String METSHDR_LOCALNAME = "metshdr";
  private static final String AGENT_LOCALNAME = "agent";
  private static final String NAME_LOCALNAME = "name";
  private static final String DMDSEC_LOCALNAME = "dmdsec";
  private static final String MDWRAP_LOCALNAME = "mdWrap";
  private static final String XMLDATA_LOCALNAME = "xmlData";
  private static final String FILESEC_LOCALNAME = "fileSec";
  private static final String FILE_LOCALNAME = "file";
  private static final String FCONTENT_LOCALNAME = "FContent";

  private static final String REPOSITORY_METS_PREFIX = "METS";
  private static final String METS_QNAME = "METS:mets";
  private static final String METSHDR_QNAME = "METS:metshdr";
  private static final String AGENT_QNAME = "METS:agent";
  private static final String NAME_QNAME = "METS:name";
  private static final String DMDSEC_QNAME = "METS:dmdsec";
  private static final String MDWRAP_QNAME = "METS:mdWrap";
  private static final String XMLDATA_QNAME = "METS:xmlData";
  private static final String FILESEC_QNAME = "METS:fileSec";
  private static final String FILE_QNAME = "METS:file";
  private static final String FCONTENT_QNAME = "METS:FContent";

  private static QName VALUES_QNAME;
  private static QName VALUE_QNAME;
  private static QName PROPERTIES_QNAME;
  private static QName ASPECTS_QNAME;
  private static QName ASSOCIATIONS_QNAME;
  private static QName CHILDNAME_QNAME;
  private static QName DATATYPE_QNAME;
  private static QName ISNULL_QNAME;
  private static QName ACL_QNAME;
  private static QName ACE_QNAME;
  private static QName ACCESS_QNAME;
  private static QName AUTHORITY_QNAME;
  private static QName PERMISSION_QNAME;
  private static QName INHERITPERMISSIONS_QNAME;
  private static QName REFERENCE_QNAME;
  private static QName PATHREF_QNAME;
  private static QName NODEREF_QNAME;
  private static QName LOCALE_QNAME;
  private static QName MLVALUE_QNAME;
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  // Service dependencies
  private NamespaceService namespaceService;
  private NodeService nodeService;
  private SearchService searchService;
  private DictionaryService dictionaryService;
  private PermissionService permissionService;

  // View context
  private ContentHandler contentHandler;
  private ExporterContext context;

  // Configuration
  private ReferenceType referenceType;

  private String Base64str = null;
  private String MimeType = null;
  private String Size = null;
  /**
   * Construct
   * 
   * @param namespaceService  namespace service
   * @param nodeService  node service
   * @param contentHandler  content handler
   */
  VGRViewXMLExporter(NamespaceService namespaceService, NodeService nodeService, SearchService searchService,
      DictionaryService dictionaryService, PermissionService permissionService, ContentHandler contentHandler)
      {
    this.namespaceService = namespaceService;
    this.nodeService = nodeService;
    this.searchService = searchService;
    this.dictionaryService = dictionaryService;
    this.permissionService = permissionService;
    this.contentHandler = contentHandler;

    VALUE_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, VALUE_LOCALNAME, namespaceService);
    VALUES_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, VALUES_LOCALNAME, namespaceService);
    CHILDNAME_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, CHILDNAME_LOCALNAME, namespaceService);
    ASPECTS_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, ASPECTS_LOCALNAME, namespaceService);
    PROPERTIES_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, PROPERTIES_LOCALNAME, namespaceService);
    ASSOCIATIONS_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, ASSOCIATIONS_LOCALNAME, namespaceService);
    DATATYPE_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, DATATYPE_LOCALNAME, namespaceService);
    ISNULL_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, ISNULL_LOCALNAME, namespaceService);
    ACL_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, ACL_LOCALNAME, namespaceService);
    ACE_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, ACE_LOCALNAME, namespaceService);
    ACCESS_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, ACCESS_LOCALNAME, namespaceService);
    AUTHORITY_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, AUTHORITY_LOCALNAME, namespaceService);
    PERMISSION_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, PERMISSION_LOCALNAME, namespaceService);
    INHERITPERMISSIONS_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, INHERITPERMISSIONS_LOCALNAME, namespaceService);
    REFERENCE_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, REFERENCE_LOCALNAME, namespaceService);
    PATHREF_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, PATHREF_LOCALNAME, namespaceService);
    NODEREF_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, NODEREF_LOCALNAME, namespaceService);
    LOCALE_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, LOCALE_LOCALNAME, namespaceService);
    MLVALUE_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_PREFIX, MLVALUE_LOCALNAME, namespaceService);
      }


  /**
   * Set Reference Type to export with
   * 
   * @param referenceType  reference type to export
   */
  public void setReferenceType(ReferenceType referenceType)
  {
    this.referenceType = referenceType;
  }


  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#start()
   */
  @Override
  public void start(ExporterContext context)
  {
    try
    {
      this.context = context;
      contentHandler.startDocument();

      contentHandler.startPrefixMapping(REPOSITORY_METS_PREFIX, "http://www.loc.gov/METS/");
      contentHandler.startElement(REPOSITORY_METS_PREFIX, METS_LOCALNAME, METS_QNAME, EMPTY_ATTRIBUTES);

      //
      // output METS Header
      //

      AttributesImpl attrs = new AttributesImpl();
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "CREATEDATE", "CREATEDATE", null, ""+context.getExportedDate());
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "RECORDSTATUS", "RECORDSTATUS", null, "Complete");
      contentHandler.startElement(REPOSITORY_METS_PREFIX, METSHDR_LOCALNAME, METSHDR_QNAME, attrs);

      attrs = new AttributesImpl();
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "ROLE", "ROLE", null, "CREATOR");
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "TYPE", "TYPE", null, "INDIVIDUAL");
      contentHandler.startElement(REPOSITORY_METS_PREFIX, AGENT_LOCALNAME, AGENT_QNAME, attrs);

      contentHandler.startElement(NamespaceService.REPOSITORY_VIEW_PREFIX, "name", NAME_QNAME, EMPTY_ATTRIBUTES);
      contentHandler.characters(context.getExportedBy().toCharArray(), 0, context.getExportedBy().length());
      contentHandler.endElement(NamespaceService.REPOSITORY_VIEW_PREFIX, NAME_LOCALNAME, NAME_QNAME);

      contentHandler.endElement(REPOSITORY_METS_PREFIX, AGENT_LOCALNAME, AGENT_QNAME);
      contentHandler.endElement(REPOSITORY_METS_PREFIX, METSHDR_LOCALNAME, METSHDR_QNAME);

      attrs = new AttributesImpl();
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "ID", "ID", null, "dmd");
      contentHandler.startElement(REPOSITORY_METS_PREFIX, DMDSEC_LOCALNAME, DMDSEC_QNAME, attrs);

      attrs = new AttributesImpl();
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "MIMETYPE", "MIMETYPE", null, "text/xml");
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "MDTYPE", "MDTYPE", null, "OTHER");
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "LABEL", "LABEL", null, "Alfresco generated metadata");
      contentHandler.startElement(REPOSITORY_METS_PREFIX, MDWRAP_LOCALNAME, MDWRAP_QNAME, attrs);

      contentHandler.startElement(REPOSITORY_METS_PREFIX, XMLDATA_LOCALNAME, XMLDATA_QNAME, EMPTY_ATTRIBUTES);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process export start event", e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startNamespace(java.lang.String, java.lang.String)
   */
  @Override
  public void startNamespace(String prefix, String uri)
  {
    try
    {
      contentHandler.startPrefixMapping(prefix, uri);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start namespace event - prefix " + prefix + " uri " + uri, e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endNamespace(java.lang.String)
   */
  @Override
  public void endNamespace(String prefix)
  {
    try
    {
      contentHandler.endPrefixMapping(prefix);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end namespace event - prefix " + prefix, e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startNode(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void startNode(NodeRef nodeRef)
  {
    try
    {
      AttributesImpl attrs = new AttributesImpl();

      Path path = nodeService.getPath(nodeRef);
      if (path.size() > 1)
      {
        // a child name does not exist for root
        Path.ChildAssocElement pathElement = (Path.ChildAssocElement)path.last();
        QName childQName = pathElement.getRef().getQName();
        attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, CHILDNAME_LOCALNAME, CHILDNAME_QNAME.toPrefixString(), null, toPrefixString(childQName));
      }

      QName type = nodeService.getType(nodeRef);
      contentHandler.startElement(type.getNamespaceURI(), type.getLocalName(), toPrefixString(type), attrs);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start node event - node ref " + nodeRef.toString(), e);
    }
  }


  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endNode(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void endNode(NodeRef nodeRef)
  {
    try
    {
      QName type = nodeService.getType(nodeRef);
      contentHandler.endElement(type.getNamespaceURI(), type.getLocalName(), toPrefixString(type));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end node event - node ref " + nodeRef.toString(), e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startAspects(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void startAspects(NodeRef nodeRef)
  {
    try
    {
      contentHandler.startElement(ASPECTS_QNAME.getNamespaceURI(), ASPECTS_LOCALNAME, toPrefixString(ASPECTS_QNAME), EMPTY_ATTRIBUTES);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start aspects", e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endAspects(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void endAspects(NodeRef nodeRef)
  {
    try
    {
      contentHandler.endElement(ASPECTS_QNAME.getNamespaceURI(), ASPECTS_LOCALNAME, toPrefixString(ASPECTS_QNAME));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end aspects", e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void startAspect(NodeRef nodeRef, QName aspect)
  {
    try
    {
      contentHandler.startElement(aspect.getNamespaceURI(), aspect.getLocalName(), toPrefixString(aspect), EMPTY_ATTRIBUTES);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start aspect event - node ref " + nodeRef.toString() + "; aspect " + toPrefixString(aspect), e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void endAspect(NodeRef nodeRef, QName aspect)
  {
    try
    {
      contentHandler.endElement(aspect.getNamespaceURI(), aspect.getLocalName(), toPrefixString(aspect));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end aspect event - node ref " + nodeRef.toString() + "; aspect " + toPrefixString(aspect), e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startACL(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void startACL(NodeRef nodeRef)
  {
    try
    {
      AttributesImpl attrs = new AttributesImpl();
      boolean inherit = permissionService.getInheritParentPermissions(nodeRef);
      if (!inherit)
      {
        attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, INHERITPERMISSIONS_LOCALNAME, INHERITPERMISSIONS_QNAME.toPrefixString(), null, "false");
      }
      contentHandler.startElement(ACL_QNAME.getNamespaceURI(), ACL_QNAME.getLocalName(), toPrefixString(ACL_QNAME), attrs);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start ACL event - node ref " + nodeRef.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#permission(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.security.AccessPermission)
   */
  @Override
  public void permission(NodeRef nodeRef, AccessPermission permission)
  {
    try
    {
      // output access control entry
      AttributesImpl attrs = new AttributesImpl();
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, ACCESS_LOCALNAME, ACCESS_QNAME.toPrefixString(), null, permission.getAccessStatus().toString());
      contentHandler.startElement(ACE_QNAME.getNamespaceURI(), ACE_QNAME.getLocalName(), toPrefixString(ACE_QNAME), attrs);

      // output authority
      contentHandler.startElement(AUTHORITY_QNAME.getNamespaceURI(), AUTHORITY_QNAME.getLocalName(), toPrefixString(AUTHORITY_QNAME), EMPTY_ATTRIBUTES);
      String authority = permission.getAuthority();
      contentHandler.characters(authority.toCharArray(), 0, authority.length());
      contentHandler.endElement(AUTHORITY_QNAME.getNamespaceURI(), AUTHORITY_QNAME.getLocalName(), toPrefixString(AUTHORITY_QNAME));

      // output permission
      contentHandler.startElement(PERMISSION_QNAME.getNamespaceURI(), PERMISSION_QNAME.getLocalName(), toPrefixString(PERMISSION_QNAME), EMPTY_ATTRIBUTES);
      String strPermission = permission.getPermission();
      contentHandler.characters(strPermission.toCharArray(), 0, strPermission.length());
      contentHandler.endElement(PERMISSION_QNAME.getNamespaceURI(), PERMISSION_QNAME.getLocalName(), toPrefixString(PERMISSION_QNAME));

      // end access control entry
      contentHandler.endElement(ACE_QNAME.getNamespaceURI(), ACE_QNAME.getLocalName(), toPrefixString(ACE_QNAME));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process permission event - node ref " + nodeRef.toString() + "; permission " + permission);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endACL(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void endACL(NodeRef nodeRef)
  {
    try
    {
      contentHandler.endElement(ACL_QNAME.getNamespaceURI(), ACL_QNAME.getLocalName(), toPrefixString(ACL_QNAME));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end ACL event - node ref " + nodeRef.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startProperties(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void startProperties(NodeRef nodeRef)
  {
    try
    {
      contentHandler.startElement(PROPERTIES_QNAME.getNamespaceURI(), PROPERTIES_LOCALNAME, toPrefixString(PROPERTIES_QNAME), EMPTY_ATTRIBUTES);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start properties", e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endProperties(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void endProperties(NodeRef nodeRef)
  {
    try
    {
      contentHandler.endElement(PROPERTIES_QNAME.getNamespaceURI(), PROPERTIES_LOCALNAME, toPrefixString(PROPERTIES_QNAME));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start properties", e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void startProperty(NodeRef nodeRef, QName property)
  {
    try
    {
      QName encodedProperty = QName.createQName(property.getNamespaceURI(), ISO9075.encode(property.getLocalName()));
      contentHandler.startElement(encodedProperty.getNamespaceURI(), encodedProperty.getLocalName(), toPrefixString(encodedProperty), EMPTY_ATTRIBUTES);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start property event - nodeRef " + nodeRef + "; property " + toPrefixString(property), e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void endProperty(NodeRef nodeRef, QName property)
  {
    try
    {
      QName encodedProperty = QName.createQName(property.getNamespaceURI(), ISO9075.encode(property.getLocalName()));
      contentHandler.endElement(encodedProperty.getNamespaceURI(), encodedProperty.getLocalName(), toPrefixString(encodedProperty));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end property event - nodeRef " + nodeRef + "; property " + toPrefixString(property), e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void startValueCollection(NodeRef nodeRef, QName property)
  {
    try
    {
      // start collection
      contentHandler.startElement(NamespaceService.REPOSITORY_VIEW_PREFIX, VALUES_LOCALNAME, toPrefixString(VALUES_QNAME), EMPTY_ATTRIBUTES);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start collection event - nodeRef " + nodeRef + "; property " + toPrefixString(property), e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void endValueCollection(NodeRef nodeRef, QName property)
  {
    try
    {
      // end collection
      contentHandler.endElement(NamespaceService.REPOSITORY_VIEW_PREFIX, VALUES_LOCALNAME, toPrefixString(VALUES_QNAME));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end collection event - nodeRef " + nodeRef + "; property " + toPrefixString(property), e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#value(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.Serializable)
   */
  @Override
  public void value(NodeRef nodeRef, QName property, Object value, int index)
  {
    try
    {
      // determine data type of value
      QName valueDataType = null;
      PropertyDefinition propDef = dictionaryService.getProperty(property);
      DataTypeDefinition dataTypeDef = propDef == null ? null : propDef.getDataType();
      if (dataTypeDef == null || dataTypeDef.getName().equals(DataTypeDefinition.ANY))
      {
        dataTypeDef = value == null ? null : dictionaryService.getDataType(value.getClass());
        if (dataTypeDef != null)
        {
          valueDataType = dataTypeDef.getName();
        }
      }

      // convert node references to paths
      if (value instanceof NodeRef && referenceType.equals(ReferenceType.PATHREF))
      {
        NodeRef valueNodeRef = (NodeRef)value;
        if (nodeRef.getStoreRef().equals(valueNodeRef.getStoreRef()))
        {
          Path nodeRefPath = createPath(context.getExportParent(), nodeRef, valueNodeRef);
          value = nodeRefPath == null ? null : nodeRefPath.toPrefixString(namespaceService);
        }
      }

      // output value wrapper if value is null or property data type is ANY or value is part of collection
      if (value == null || valueDataType != null || index != -1)
      {
        AttributesImpl attrs = new AttributesImpl();
        if (value == null)
        {
          attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_PREFIX, ISNULL_LOCALNAME, ISNULL_QNAME.toPrefixString(), null, "true");
        }
        if (valueDataType != null)
        {
          attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_PREFIX, DATATYPE_LOCALNAME, DATATYPE_QNAME.toPrefixString(), null, toPrefixString(valueDataType));
        }
        contentHandler.startElement(NamespaceService.REPOSITORY_VIEW_PREFIX, VALUE_LOCALNAME, toPrefixString(VALUE_QNAME), attrs);
      }

      // output value
      String strValue = DefaultTypeConverter.INSTANCE.convert(String.class, value);
      if (strValue != null)
      {
        for (int i = 0; i < strValue.length(); i++)
        {
          char[] temp = new char[]{strValue.charAt(i)};
          contentHandler.characters(temp, 0, 1);
        }
      }

      // output value wrapper if property data type is any
      if (value == null || valueDataType != null || index != -1)
      {
        contentHandler.endElement(NamespaceService.REPOSITORY_VIEW_PREFIX, VALUE_LOCALNAME, toPrefixString(VALUE_QNAME));
      }
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process value event - nodeRef " + nodeRef + "; property " + toPrefixString(property) + "; value " + value, e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#content(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.InputStream)
   */
  @Override
  public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
  {
    MimeType = contentData.getMimetype();
    Size = ""+contentData.getSize();
    byte[] tmp = new byte[(int) contentData.getSize()];
    try {
      content.read(tmp,0,(int) contentData.getSize());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Base64str = Base64.encodeBytes(tmp);
    // TODO: Base64 encode content and send out via Content Handler
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void startAssoc(NodeRef nodeRef, QName assoc)
  {
    try
    {
      contentHandler.startElement(assoc.getNamespaceURI(), assoc.getLocalName(), toPrefixString(assoc), EMPTY_ATTRIBUTES);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start assoc event - nodeRef " + nodeRef + "; association " + toPrefixString(assoc), e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void endAssoc(NodeRef nodeRef, QName assoc)
  {
    try
    {
      contentHandler.endElement(assoc.getNamespaceURI(), assoc.getLocalName(), toPrefixString(assoc));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end assoc event - nodeRef " + nodeRef + "; association " + toPrefixString(assoc), e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startAssocs(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void startAssocs(NodeRef nodeRef)
  {
    try
    {
      contentHandler.startElement(ASSOCIATIONS_QNAME.getNamespaceURI(), ASSOCIATIONS_LOCALNAME, toPrefixString(ASSOCIATIONS_QNAME), EMPTY_ATTRIBUTES);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start associations", e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endAssocs(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void endAssocs(NodeRef nodeRef)
  {
    try
    {
      contentHandler.endElement(ASSOCIATIONS_QNAME.getNamespaceURI(), ASSOCIATIONS_LOCALNAME, toPrefixString(ASSOCIATIONS_QNAME));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end associations", e);
    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#startReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void startReference(NodeRef nodeRef, QName childName)
  {
    try
    {
      // determine format of reference e.g. node or path based
      ReferenceType referenceFormat = referenceType;
      if (nodeRef.equals(nodeService.getRootNode(nodeRef.getStoreRef())))
      {
        referenceFormat = ReferenceType.PATHREF;
      }

      // output reference
      AttributesImpl attrs = new AttributesImpl();
      if (referenceFormat.equals(ReferenceType.PATHREF))
      {
        Path path = createPath(context.getExportParent(), context.getExportParent(), nodeRef);
        attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, PATHREF_LOCALNAME, PATHREF_QNAME.toPrefixString(), null, path.toPrefixString(namespaceService));
      }
      else
      {
        attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, NODEREF_LOCALNAME, NODEREF_QNAME.toPrefixString(), null, nodeRef.toString());
      }
      if (childName != null)
      {
        attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, CHILDNAME_LOCALNAME, CHILDNAME_QNAME.toPrefixString(), null, childName.toPrefixString(namespaceService));
      }
      contentHandler.startElement(REFERENCE_QNAME.getNamespaceURI(), REFERENCE_LOCALNAME, toPrefixString(REFERENCE_QNAME), attrs);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start reference", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#endReference(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void endReference(NodeRef nodeRef)
  {
    try
    {
      contentHandler.endElement(REFERENCE_QNAME.getNamespaceURI(), REFERENCE_LOCALNAME, toPrefixString(REFERENCE_QNAME));
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end reference", e);
    }
  }

  public void startValueMLText(NodeRef nodeRef, Locale locale)
  {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_PREFIX, LOCALE_LOCALNAME, LOCALE_QNAME.toPrefixString(), null, locale.toString());
    try
    {
      contentHandler.startElement(NamespaceService.REPOSITORY_VIEW_PREFIX, MLVALUE_LOCALNAME, MLVALUE_QNAME.toPrefixString(), attrs);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start mlvalue", e);
    }
  }

  @Override
  public void endValueMLText(NodeRef nodeRef)
  {
    try
    {
      contentHandler.endElement(NamespaceService.REPOSITORY_VIEW_PREFIX, MLVALUE_LOCALNAME, MLVALUE_QNAME.toPrefixString());
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end mltext", e);
    }

  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#warning(java.lang.String)
   */
  @Override
  public void warning(String warning)
  {
  }

  /* (non-Javadoc)
   * @see org.alfresco.service.cmr.view.Exporter#end()
   */
  @Override
  public void end()
  {
    try
    {
      AttributesImpl attrs = new AttributesImpl();
      contentHandler.endElement(REPOSITORY_METS_PREFIX, XMLDATA_LOCALNAME, XMLDATA_QNAME);
      contentHandler.endElement(REPOSITORY_METS_PREFIX, MDWRAP_LOCALNAME, MDWRAP_QNAME);
      contentHandler.endElement(REPOSITORY_METS_PREFIX, DMDSEC_LOCALNAME, DMDSEC_QNAME);
      contentHandler.startElement(REPOSITORY_METS_PREFIX, FILESEC_LOCALNAME, FILESEC_QNAME, attrs);
      attrs = new AttributesImpl();
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "ID", "ID", null, "FILE001");
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "MIMETYPE", "MIMETYPE", null, MimeType);
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_1_0_URI, "SIZE", "SIZE", null, Size);
      contentHandler.startElement(REPOSITORY_METS_PREFIX, FILE_LOCALNAME, FILE_QNAME, attrs);
      contentHandler.startElement(REPOSITORY_METS_PREFIX, FCONTENT_LOCALNAME, FCONTENT_QNAME, EMPTY_ATTRIBUTES);
      contentHandler.characters(Base64str.toCharArray(), 0, Base64str.length());
      contentHandler.endElement(REPOSITORY_METS_PREFIX, FCONTENT_LOCALNAME, FCONTENT_QNAME);
      contentHandler.endElement(REPOSITORY_METS_PREFIX, FILE_LOCALNAME, FILE_QNAME);
      contentHandler.endElement(REPOSITORY_METS_PREFIX, FILESEC_LOCALNAME, FILESEC_QNAME);

      contentHandler.endElement(REPOSITORY_METS_PREFIX, METS_LOCALNAME, METS_QNAME);
      contentHandler.endPrefixMapping(REPOSITORY_METS_PREFIX);

      contentHandler.endDocument();
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process end export event", e);
    }
  }

  /**
   * Get the prefix for the specified URI
   * @param uri  the URI
   * @return  the prefix (or null, if one is not registered)
   */
  private String toPrefixString(QName qname)
  {
    return qname.toPrefixString(namespaceService);
  }

  /**
   * Return relative path between from and to references within export root
   * 
   * @param fromRef  from reference
   * @param toRef  to reference
   * @return  path
   */
  private Path createPath(NodeRef rootRef, NodeRef fromRef, NodeRef toRef)
  {
    // Check that item exists first
    if (!nodeService.exists(toRef))
    {
      // return null path
      return null;
    }

    // Check whether item is the root node of the store
    // If so, always return absolute path
    if (toRef.equals(nodeService.getRootNode(toRef.getStoreRef())))
    {
      return nodeService.getPath(toRef);
    }

    // construct relative path
    Path rootPath = createIndexedPath(rootRef, nodeService.getPath(rootRef));
    Path fromPath = createIndexedPath(fromRef, nodeService.getPath(fromRef));
    Path toPath = createIndexedPath(toRef, nodeService.getPath(toRef));
    Path relativePath = null;

    try
    {
      // Determine if 'to path' is a category
      // TODO: This needs to be resolved in a more appropriate manner - special support is
      //       required for categories.
      for (int i = 0; i < toPath.size(); i++)
      {
        Path.Element pathElement = toPath.get(i);
        if (pathElement.getPrefixedString(namespaceService).equals("cm:categoryRoot"))
        {
          Path.ChildAssocElement childPath = (Path.ChildAssocElement)pathElement;
          relativePath = new Path();
          relativePath.append(new Path.ChildAssocElement(new ChildAssociationRef(null, null, null, childPath.getRef().getParentRef())));
          relativePath.append(toPath.subPath(i + 1, toPath.size() -1));
          break;
        }
      }

      if (relativePath == null)
      {
        // Determine if from node is relative to export tree
        int i = 0;
        while (i < rootPath.size() && i < fromPath.size() && rootPath.get(i).equals(fromPath.get(i)))
        {
          i++;
        }
        if (i == rootPath.size())
        {
          // Determine if to node is relative to export tree
          for (NodeRef nodeRef : context.getExportParentList())
          {
            int j = 0;
            Path tryPath = createIndexedPath(nodeRef, nodeService.getPath(nodeRef));
            while (j < tryPath.size() && j < toPath.size() && tryPath.get(j).equals(toPath.get(j)))
            {
              j++;
            }
            if (j == tryPath.size())
            {
              // build relative path between from and to
              relativePath = new Path();
              for (int p = 0; p < fromPath.size() - i; p++)
              {
                relativePath.append(new Path.ParentElement());
              }
              if (j < toPath.size())
              {
                relativePath.append(toPath.subPath(j, toPath.size() - 1));
              }

              break;
            }
          }
        }
      }


      if (relativePath == null)
      {
        // default to absolute path
        relativePath = toPath;
      }
    }
    catch(Throwable e)
    {
      String msg = "Failed to determine relative path: root path=" + rootPath + "; from path=" + fromPath + "; to path=" + toPath;
      throw new ExporterException(msg, e);
    }

    return relativePath;
  }


  /**
   * Helper to convert a path into an indexed path which uniquely identifies a node
   * 
   * @param nodeRef
   * @param path
   * @return
   */
  private Path createIndexedPath(NodeRef nodeRef, Path path)
  {
    // Add indexes for same name siblings
    // TODO: Look at more efficient approach
    for (int i = path.size() - 1; i >= 0; i--)
    {
      Path.Element pathElement = path.get(i);
      if (i > 0 && pathElement instanceof Path.ChildAssocElement)
      {
        int index = 1;  // for xpath index compatibility
        String searchPath = path.subPath(i).toPrefixString(namespaceService);
        List<NodeRef> siblings = searchService.selectNodes(nodeRef, searchPath, null, namespaceService, false);
        if (siblings.size() > 1)
        {
          ChildAssociationRef childAssoc = ((Path.ChildAssocElement)pathElement).getRef();
          NodeRef childRef = childAssoc.getChildRef();
          for (NodeRef sibling : siblings)
          {
            if (sibling.equals(childRef))
            {
              childAssoc.setNthSibling(index);
              break;
            }
            index++;
          }
        }
      }
    }

    return path;
  }


  @Override
  public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_PREFIX, LOCALE_LOCALNAME, LOCALE_QNAME.toPrefixString(), null, locale.toString());
    if(isNull)
    {
      attrs.addAttribute(NamespaceService.REPOSITORY_VIEW_PREFIX, ISNULL_LOCALNAME, ISNULL_QNAME.toPrefixString(), null, "true");
    }
    try
    {
      contentHandler.startElement(NamespaceService.REPOSITORY_VIEW_PREFIX, MLVALUE_LOCALNAME, MLVALUE_QNAME.toPrefixString(), attrs);
    }
    catch (SAXException e)
    {
      throw new ExporterException("Failed to process start mlvalue", e);
    }
  }
}
