package se.vgregion.alfresco.repo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteServiceException;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import se.vgregion.alfresco.repo.model.VgrModel;

public class ServiceUtils implements InitializingBean {

  private static final int STREAM_BUFFER_LENGTH = 32 * 1024;

  private static final Pattern docLibPattern = Pattern
      .compile("/\\{http://www.alfresco.org/model/application/1.0\\}company_home/\\{http://www.alfresco.org/model/site/1.0\\}sites/\\{http://www.alfresco.org/model/content/1.0\\}.*/\\{http://www.alfresco.org/model/content/1.0\\}documentLibrary/.*");

  private static final Pattern storagePattern = Pattern.compile("/\\{http://www.alfresco.org/model/application/1.0\\}company_home/\\{http://www.alfresco.org/model/content/1.0\\}Lagret/.*");

  private TransactionService _transactionService;

  private NodeService _nodeService;

  private AuthenticationService _authenticationService;

  private PersonService _personService;

  private LockService _lockService;

  private DictionaryService _dictionaryService;

  private SiteServiceImpl _siteService;

  private SearchService _searchService;

  private MimetypeService _mimetypeService;

  private ContentService _contentService;

  private FileFolderService _fileFolderService;

  private Properties _globalProperties;

  private PermissionService _permissionService;

  private AuthorityService _authorityService;

  private BehaviourFilter _behaviourFilter;

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void setTransactionService(final TransactionService transactionService) {
    _transactionService = transactionService;
  }

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setAuthenticationService(final AuthenticationService authenticationService) {
    _authenticationService = authenticationService;
  }

  public void setPersonService(final PersonService personService) {
    _personService = personService;
  }

  public void setLockService(final LockService lockService) {
    _lockService = lockService;
  }

  public void setDictionaryService(final DictionaryService dictionaryService) {
    _dictionaryService = dictionaryService;
  }

  public void setSiteService(final SiteServiceImpl siteService) {
    _siteService = siteService;
  }

  public void setSearchService(final SearchService searchService) {
    _searchService = searchService;
  }

  public void setMimetypeService(final MimetypeService mimetypeService) {
    _mimetypeService = mimetypeService;
  }

  public void setContentService(final ContentService contentService) {
    _contentService = contentService;
  }

  public void setFileFolderService(final FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public void setGlobalProperties(final Properties globalProperties) {
    _globalProperties = globalProperties;
  }

  public void setPermissionService(final PermissionService permissionService) {
    _permissionService = permissionService;
  }

  public void setAuthorityService(final AuthorityService authorityService) {
    _authorityService = authorityService;
  }

  public String getRepresentation(final String username) {
    if (StringUtils.isBlank(username)) {
      return null;
    }

    if (!_personService.personExists(username)) {
      return username;
    }

    final NodeRef userNodeRef = _personService.getPerson(username, false);

    final String firstname = getStringValue(_nodeService.getProperty(userNodeRef, ContentModel.PROP_FIRSTNAME));
    final String lastname = getStringValue(_nodeService.getProperty(userNodeRef, ContentModel.PROP_LASTNAME));
    final String organisation = getStringValue(_nodeService.getProperty(userNodeRef, ContentModel.PROP_ORGANIZATION));

    return ServiceUtils.getRepresentation(firstname, lastname, username, organisation);
  }

  public String getRepresentation(final NodeRef nodeRef, final QName userProperty) {
    final String username = getStringValue(_nodeService.getProperty(nodeRef, userProperty));

    return getRepresentation(username);
  }

  public boolean isDocumentLibrary(final NodeRef nodeRef) {
    final Path path = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Path>() {

      @Override
      public Path doWork() throws Exception {
        return _nodeService.getPath(nodeRef);
      }

    }, AuthenticationUtil.getSystemUserName());

    // if not enough paths, then it's the wrong folder
    if (path.size() <= 5) {
      return false;
    }

    final String pth = path.toString();

    final boolean matches = docLibPattern.matcher(pth).matches();

    return matches;
  }

  public boolean isStorage(final NodeRef nodeRef) {
    Path path = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Path>() {

      @Override
      public Path doWork() throws Exception {
        return _nodeService.getPath(nodeRef);
      }

    }, AuthenticationUtil.getSystemUserName());

    String pth = path.toString();

    boolean matches = storagePattern.matcher(pth).matches();

    return matches;
  }

  public String getCurrentUserName() {
    return _authenticationService.getCurrentUserName();
  }

  public static String getRepresentation(final String firstname, final String lastname, final String username, final String organisation) {
    String result = "";

    if (StringUtils.isNotEmpty(firstname)) {
      result += firstname;
    }

    if (StringUtils.isNotEmpty(lastname)) {
      if (StringUtils.isNotBlank(result)) {
        result += " ";
      }

      result += lastname;
    }

    if (StringUtils.isNotEmpty(username)) {
      result += " (" + username + ") ";
    }

    if (StringUtils.isNotEmpty(organisation)) {
      result += organisation;
    }

    return StringUtils.trim(result);
  }

  public String getStringValue(final NodeRef nodeRef, final QName property) {
    final Serializable value = _nodeService.getProperty(nodeRef, property);

    return getStringValue(value);
  }

  public String getStringValue(final Serializable value) {
    return value == null ? "" : value.toString();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_authenticationService);
    Assert.notNull(_nodeService);
    Assert.notNull(_transactionService);
    Assert.notNull(_personService);
    Assert.notNull(_lockService);
    Assert.notNull(_contentService);
    Assert.notNull(_globalProperties);
  }

  public void replicateVersion(final NodeRef nodeRef, final String version) {
    if (!_nodeService.exists(nodeRef)) {
      return;
    }

    // if it isn't the document library, just exit
    if (!isDocumentLibrary(nodeRef)) {
      return;
    }

    // don't do this for working copies
    if (_nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
      return;
    }

    if (!StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef())) {
      return;
    }

    _nodeService.setProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION, version);
  }

  public boolean pingServer(final String url) {
    boolean result;

    URL u;
    try {
      u = new URL(url);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }

    try {
      final InetAddress address = InetAddress.getByName(u.getHost());

      // Try to reach the specified address within the timeout
      // period. If during this period the address cannot be
      // reach then the method returns false.
      final boolean reachable = address.isReachable(1000);

      if (!reachable) {
        return false;
      }

      final TelnetClient telnetClient = new TelnetClient();
      telnetClient.setDefaultTimeout(1000);
      telnetClient.connect(u.getHost(), u.getPort());

      result = true;
    } catch (final Exception ex) {
      result = false;
    }

    return result;
  }

  public List<SiteInfo> listPublicSites(final String nameFilter, final String sitePresetFilter, final int size) {
    List<SiteInfo> result;

    final NodeRef siteRoot = getSiteRoot();

    if (siteRoot == null) {
      result = new ArrayList<SiteInfo>(0);
    } else {
      QueryParameterDefinition[] params;
      StringBuilder query;

      if (nameFilter != null && nameFilter.length() != 0) {
        final String escNameFilter = LuceneQueryParser.escape(nameFilter.replace('"', ' '));

        // Perform a Lucene search under the Site parent node using *name*,
        // title and description search query
        params = new QueryParameterDefinition[3];

        params[0] = new QueryParameterDefImpl(ContentModel.PROP_NAME, _dictionaryService.getDataType(DataTypeDefinition.TEXT), true, escNameFilter);

        params[1] = new QueryParameterDefImpl(ContentModel.PROP_TITLE, _dictionaryService.getDataType(DataTypeDefinition.TEXT), true, escNameFilter);

        params[2] = new QueryParameterDefImpl(ContentModel.PROP_DESCRIPTION, _dictionaryService.getDataType(DataTypeDefinition.TEXT), true, escNameFilter);

        // get the sites that match the specified names
        query = new StringBuilder(0);

        query.append("+PARENT:\"").append(siteRoot.toString()).append("\" +(@cm\\:name:\"*${cm:name}*\"").append(" @cm\\:title:\"${cm:title}\"").append(" @cm\\:description:\"${cm:description}\"")
        .append(" @st\\:siteVisibility:\"PUBLIC\")");
      } else {
        params = new QueryParameterDefinition[0];

        // get the sites that match the specified names
        query = new StringBuilder();

        query.append("+PARENT:\"").append(siteRoot.toString()).append("\" +(@st\\:siteVisibility:\"PUBLIC\")");

      }

      final ResultSet results = _searchService.query(siteRoot.getStoreRef(), SearchService.LANGUAGE_LUCENE, query.toString(), params);

      try {
        result = new ArrayList<SiteInfo>(results.length());

        for (final NodeRef site : results.getNodeRefs()) {
          // Ignore any node type that is not a "site"
          final QName siteClassName = _nodeService.getType(site);

          if (_dictionaryService.isSubClass(siteClassName, SiteModel.TYPE_SITE) == true) {
            result.add(createSiteInfo(site));

            // break on max size limit reached
            if (result.size() == size) {
              break;
            }
          }
        }
      } finally {
        results.close();
      }
    }

    return result;
  }

  protected NodeRef getSiteRoot() {
    final Method method = ReflectionUtils.findMethod(SiteServiceImpl.class, "getSiteRoot");

    ReflectionUtils.makeAccessible(method);

    return (NodeRef) ReflectionUtils.invokeMethod(method, _siteService);
  }

  protected SiteInfo createSiteInfo(final NodeRef siteNodeRef) {
    try {
      final Method method = ReflectionUtils.findMethod(SiteServiceImpl.class, "createSiteInfo", NodeRef.class);

      ReflectionUtils.makeAccessible(method);

      return (SiteInfo) ReflectionUtils.invokeMethod(method, _siteService, siteNodeRef);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public boolean isSiteAdmin(final NodeRef nodeRef) {
    final String username = getCurrentUserName();

    final SiteInfo site = _siteService.getSite(nodeRef);

    if (site == null) {
      return false;
    }

    final String role = _siteService.getMembersRole(site.getShortName(), username);

    return StringUtils.isBlank(role) ? false : role.equals(SiteModel.SITE_MANAGER);
  }

  public boolean isSiteCollaborator(final NodeRef nodeRef) {
    final String username = getCurrentUserName();

    final SiteInfo site = _siteService.getSite(nodeRef);

    if (site == null) {
      return false;
    }

    final String role = _siteService.getMembersRole(site.getShortName(), username);

    return StringUtils.isBlank(role) ? false : role.equals(SiteModel.SITE_COLLABORATOR);
  }

  public boolean isAdmin() {
    final String username = getCurrentUserName();

    return _authorityService.isAdminAuthority(username);
  }

  public String getFileExtension(final Serializable content) {
    return getFileExtension(content, true);
  }

  public String getFileExtension(final Serializable content, final boolean withDot) {
    final String mimetype = getMimetype(content);

    if (StringUtils.isBlank(mimetype)) {
      return "";
    }

    return (withDot ? "." : "") + _mimetypeService.getExtension(mimetype).toLowerCase();
  }

  public String getFileExtension(final NodeRef nodeRef) {
    return getFileExtension(nodeRef, true);
  }

  public String getFileExtension(final NodeRef nodeRef, final boolean withDot) {
    final Serializable content = _nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

    return getFileExtension(content, withDot);
  }

  public void setDateSaved(final NodeRef nodeRef) {
    final Serializable dateSaved = _nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);

    _nodeService.setProperty(nodeRef, VgrModel.PROP_DATE_SAVED, dateSaved);
  }

  public void addChecksum(final NodeRef nodeRef) {
    final ContentReader contentReader = _contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

    // if the content reader is null, no file content is attached
    if (contentReader == null) {
      return;
    }

    final InputStream inputStream = contentReader.getContentInputStream();

    final String checksum;

    try {
      checksum = getChecksum(inputStream);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }

    _nodeService.setProperty(nodeRef, VgrModel.PROP_CHECKSUM, checksum);
  }

  public String getChecksum(final File file) {
    InputStream inputStream = null;

    try {
      inputStream = new FileInputStream(file);

      return getChecksum(inputStream);
    } catch (final FileNotFoundException ex) {
      throw new java.lang.RuntimeException(ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  public String getChecksum(final InputStream inputStream) {
    return md5Hex(inputStream);
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

  public void deleteUnusedUserFolders() {
    final NodeRef userHomeNodeRef = resolveUserHomeNodeRef();

    final List<FileInfo> folders = _fileFolderService.listFolders(userHomeNodeRef);

    int count = 0;

    for (final FileInfo folder : folders) {
      final NodeRef folderNodeRef = folder.getNodeRef();

      final String folderName = folder.getName();

      if (folderName.length() == 2) {
        System.out.println("Entering sub folder '" + folderName + "'...");

        final int subCount = cleanSubFolder(folder);

        System.out.println("Deleted '" + subCount + "' subfolders in '" + folder.getName() + "'.");

        count = count + subCount;
      }

      // if the folder has children, then it's not a home folder anyways...
      final int children = _fileFolderService.list(folderNodeRef).size();

      if (children > 0) {
        System.out.println("More than 0 children: '" + folder.getName() + "', skipping...");

        continue;
      }

      final boolean isHomeFolder = findFolderUser(folderName, folderNodeRef) != null;

      if (isHomeFolder) {
        System.out.println("The folder '" + folder.getName() + "' is a home folder, skipping...");

        continue;
      }

      _fileFolderService.delete(folderNodeRef);

      System.out.println("Deleted '" + folder.getName() + "'");

      count++;
    }

    System.out.println("Deleted in total '" + count + "' folders");

    System.out.println("Finished!");
  }

  private NodeRef resolveUserHomeNodeRef() {
    final String query = "PATH:\"/app:company_home/app:user_homes\"";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    searchParameters.setQuery(query);

    final ResultSet result = _searchService.query(searchParameters);

    try {
      if (result.length() == 0) {
        return null;
      } else if (result.length() == 1) {
        return result.getNodeRef(0);
      }

      return null;
    } finally {
      ServiceUtils.closeQuietly(result);
    }
  }

  private int cleanSubFolder(final FileInfo folder) {
    if (StringUtils.isNumeric(folder.getName())) {
      return 0;
    }

    final List<FileInfo> subFolders = _fileFolderService.listFolders(folder.getNodeRef());

    int subCount = 0;

    for (final FileInfo subFolder : subFolders) {
      final NodeRef subFolderNodeRef = subFolder.getNodeRef();

      final String subFolderName = subFolder.getName();

      final boolean isHomeFolder = findFolderUser(subFolderName, subFolderNodeRef) != null;

      if (isHomeFolder) {
        System.out.println("The folder '" + folder.getName() + "/" + subFolderName + "' is a home folder, skipping...");

        continue;
      }

      _fileFolderService.delete(subFolderNodeRef);

      System.out.println("Deleted '" + subFolderName + "' of parent '" + folder.getName() + "'");

      subCount++;
    }

    return subCount;
  }

  private NodeRef findFolderUser(final String username, final NodeRef folderNodeRef) {
    if (!_personService.personExists(username)) {
      return null;
    }

    final NodeRef person = _personService.getPerson(username, false);

    final NodeRef homeFolder = (NodeRef) _nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);

    if (folderNodeRef.equals(homeFolder)) {
      return person;
    }

    return null;
  }

  public String getMimetype(final Serializable content) {
    if (content == null) {
      return "";
    }

    if (!(content instanceof ContentData)) {
      return "";
    }

    final ContentData contentData = (ContentData) content;

    return contentData.getMimetype();
  }

  public String getMimetype(final NodeRef nodeRef) {
    final Serializable content = _nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

    return getMimetype(content);
  }

  public String findLanguageCode(final String language) {
    return AuthenticationUtil.runAs(new RunAsWork<String>() {

      @Override
      public String doWork() throws Exception {
        final String code = language;

        final String query = "TYPE:\"apelon:language\" AND apelon:name:\"" + language + "\"";

        final SearchParameters searchParameters = new SearchParameters();

        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.setQuery(query);

        final ResultSet nodes = _searchService.query(searchParameters);

        try {
          for (final NodeRef nodeRef : nodes.getNodeRefs()) {
            final List<ChildAssociationRef> properties = _nodeService.getChildAssocs(nodeRef);

            for (final ChildAssociationRef property : properties) {
              final Map<QName, Serializable> childProperties = _nodeService.getProperties(property.getChildRef());

              final String key = childProperties.get(VgrModel.PROP_APELON_KEY).toString();

              @SuppressWarnings("unchecked")
              final List<String> values = (List<String>) childProperties.get(VgrModel.PROP_APELON_VALUE);

              if (key.equals("Språkkod")) {
                return values.size() > 0 ? values.get(0) : "";
              }
            }
          }
        } finally {
          ServiceUtils.closeQuietly(nodes);
        }

        return code;
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  public String getBaseLink(final String base) {
    final String host = _globalProperties.getProperty(base + ".host", "alfresco.vgregion.se");
    String port = _globalProperties.getProperty(base + ".port", "80");
    final String context = _globalProperties.getProperty(base + ".context", base);
    String protocol = _globalProperties.getProperty(base + ".protocol", "http");

    if (port.equals("80")) {
      protocol = "http";
      port = "";
    } else if (port.equals("443")) {
      protocol = "https";
      port = "";
    }

    port = StringUtils.isBlank(port) ? "" : ":" + port;

    return protocol + "://" + host + port + "/" + context;
  }

  public String getRepositoryBaseLink() {
    return getBaseLink("alfresco");
  }

  public String getShareBaseLink() {
    return getBaseLink("share");
  }

  public String getDocumentIdentifier(final NodeRef nodeRef) {
    return getDocumentIdentifier(nodeRef, false);
  }

  public String getDocumentIdentifier(final NodeRef nodeRef, final boolean nativ) {
    // to get the link to the identifier, the link is based on "source", as it
    // makes a search for the correct source
    final Serializable source = _nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);

    if (source == null) {
      throw new RuntimeException("The node " + nodeRef + " has no vgr:dc.source.documentid, and therefor cannot be a published node.");
    }

    // this link returns the document as inline instead of attachment
    String identifier = getRepositoryBaseLink() + "/service/vgr/storage/node/content/" + source.toString().replace(":/", "") + "?a=false&guest=true";

    if (nativ) {
      identifier += "&native=true";
    }

    return identifier;
  }

  /**
   * Creates a link to the original document in Share
   * 
   * @param nodeRef
   * @return the original link
   */
  public String getDocumentSource(final NodeRef nodeRef) {
    final SiteInfo site = _siteService.getSite(nodeRef);

    if (site == null) {
      return null;
    }

    return getShareBaseLink() + "/page/site/" + site.getShortName() + "/document-details?nodeRef=" + nodeRef.toString();
  }

  /**
   * A version of listMembers that filters also on username and email instead of
   * just first and last name, also the wildcard * is supported
   * <p/>
   * Original desc: List the members of the site. This includes both users and
   * groups if collapseGroups is set to false, otherwise all groups that are
   * members are collapsed into their component users and listed.
   * 
   * @param shortName
   *          site short name
   * @param nameFilter
   *          name filter
   * @param roleFilter
   *          role filter
   * @param size
   *          max results size crop if >0
   * @param collapseGroups
   *          true if collapse member groups into user list, false otherwise
   * @return Map<String, String> the authority name and their role
   */
  public Map<String, String> listSiteMembers(final String shortName, final String nameFilter, final String roleFilter, final int size, final boolean collapseGroups) {
    final SiteInfo site = _siteService.getSite(shortName);

    if (site == null) {
      throw new SiteServiceException("site_service.site_no_exist", new Object[] { shortName });
    }

    // Build an array of name filter tokens pre lowercased to test against
    // person properties
    // We require that matching people have at least one match against one of
    // these on
    // either their firstname or last name
    // For groups, we require a match against the whole filter on the group name
    // or display name
    String nameFilterLower = null;
    Pattern[] nameFilters = new Pattern[0];
    if (nameFilter != null && nameFilter.length() != 0) {
      final StringTokenizer t = new StringTokenizer(nameFilter, " ");
      nameFilters = new Pattern[t.countTokens()];
      for (int i = 0; t.hasMoreTokens(); i++) {
        String p = t.nextToken().toLowerCase();
        p = p.replaceAll("\\*", ".*");
        nameFilters[i] = Pattern.compile(p);
      }
      nameFilterLower = nameFilter.toLowerCase();
    }

    final Map<String, String> members = new HashMap<String, String>(32);

    final QName siteType = _nodeService.getType(site.getNodeRef());
    final Set<String> permissions = _permissionService.getSettablePermissions(siteType);
    final Map<String, String> groupsToExpand = new HashMap<String, String>(32);
    for (final String permission : permissions) {

      if (roleFilter == null || roleFilter.length() == 0 || roleFilter.equals(permission)) {
        final String groupName = _siteService.getSiteRoleGroup(shortName, permission, true);
        final Set<String> authorities = _authorityService.getContainedAuthorities(null, groupName, true);
        for (final String authority : authorities) {
          switch (AuthorityType.getAuthorityType(authority)) {
          case USER:
            boolean addUser = true;
            if (nameFilter != null && nameFilter.length() != 0 && !nameFilter.equals(authority)) {
              // found a filter - does it match person first/last name?
              addUser = matchPerson(nameFilters, authority);
            }
            if (addUser) {
              // Add the user and their permission to the returned map
              members.put(authority, permission);

              // break on max size limit reached
              if (members.size() == size) {
                break;
              }
            }
            break;
          case GROUP:
            if (collapseGroups) {
              if (!groupsToExpand.containsKey(authority)) {
                groupsToExpand.put(authority, permission);
              }
            } else {
              if (nameFilter != null && nameFilter.length() != 0) {
                // found a filter - does it match Group name part?
                if (authority.substring(PermissionService.GROUP_PREFIX.length()).toLowerCase().contains(nameFilterLower)) {
                  members.put(authority, permission);
                } else {
                  // Does it match on the Group Display Name part instead?
                  final String displayName = _authorityService.getAuthorityDisplayName(authority);
                  if (displayName != null && displayName.toLowerCase().contains(nameFilterLower)) {
                    members.put(authority, permission);
                  }
                }
              } else {
                // No name filter add this group
                members.put(authority, permission);
              }
            }
            break;
          }
        }
      }
    }

    if (collapseGroups) {
      for (final Map.Entry<String, String> entry : groupsToExpand.entrySet()) {
        final Set<String> subUsers = _authorityService.getContainedAuthorities(AuthorityType.USER, entry.getKey(), false);
        for (final String subUser : subUsers) {
          boolean addUser = true;
          if (nameFilter != null && nameFilter.length() != 0 && !nameFilter.equals(subUser)) {
            // found a filter - does it match person first/last name?
            addUser = matchPerson(nameFilters, subUser);
          }
          if (addUser) {
            // Add the collapsed user into the members list if they do not
            // already appear in the list
            if (members.containsKey(subUser) == false) {
              members.put(subUser, entry.getValue());
            }

            // break on max size limit reached
            if (members.size() == size) {
              break;
            }
          }
        }
      }
    }

    return members;
  }

  /**
   * Helper to match name filters to Person properties
   * 
   * @param nameFilters
   * @param username
   * @return
   */
  private boolean matchPerson(final Pattern[] nameFilters, final String username) {
    final Map<QName, Serializable> values = _nodeService.getProperties(_personService.getPerson(username, false));
    final String[] terms = { ((String) values.get(ContentModel.PROP_FIRSTNAME)).toLowerCase(), ((String) values.get(ContentModel.PROP_LASTNAME)).toLowerCase(),
        ((String) values.get(ContentModel.PROP_EMAIL)).toLowerCase(), ((String) values.get(ContentModel.PROP_USERNAME)).toLowerCase() };

    // if user inputed two things, i.e two nameFilters we just suppose this is
    // first and last name and check that both should match
    // otherwise one match is enough
    if (nameFilters.length == 2) {
      return nameFilters[0].matcher(terms[0]).matches() && nameFilters[1].matcher(terms[1]).matches();
    } else {
      for (final Pattern filter : nameFilters) {

        for (final String term : terms) {
          if (filter.matcher(term).matches()) { // greedy OR
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean isPublished(final NodeRef nodeRef) {
    final long now = new Date().getTime();

    final Date availableFrom = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_FROM);
    final Date availableTo = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_TO);

    final long safeAvailableFrom = availableFrom != null ? availableFrom.getTime() : 0;
    final long safeAvailableTo = availableTo != null ? availableTo.getTime() : now;

    final boolean published = now <= safeAvailableTo && now >= safeAvailableFrom;
    
    final boolean publishStatus = "ok".equalsIgnoreCase((String)_nodeService.getProperty(nodeRef, VgrModel.PROP_PUBLISH_STATUS));

    return published && publishStatus;
  }

  public void disableAllBehaviours() {
    // find site name
    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        _behaviourFilter.disableAllBehaviours();
        return null;
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  public static void closeQuietly(ResultSet resultSet) {
    try {
      resultSet.close();
    } catch (Exception ex) {
      // just swallow...
    }
  }

}
