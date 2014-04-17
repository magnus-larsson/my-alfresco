package se.vgregion.alfresco.repo.utils;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;

public interface ServiceUtils {
  public String getRepresentation(final String username);

  public String getRepresentation(final NodeRef nodeRef, final QName userProperty);

  public boolean isDocumentLibrary(final NodeRef nodeRef);

  public boolean isStorage(final NodeRef nodeRef);

  public String getCurrentUserName();

  public String getStringValue(final NodeRef nodeRef, final QName property);

  public String getStringValue(final Serializable value);

  public void replicateVersion(final NodeRef nodeRef, final String version);

  public boolean pingServer(final String url);

  public List<SiteInfo> listPublicSites(final String nameFilter, final String sitePresetFilter, final int size);

  public boolean isSiteAdmin(final NodeRef nodeRef);

  public boolean isSiteCollaborator(final NodeRef nodeRef);

  public boolean isAdmin();

  public String getFileExtension(final Serializable content);

  public String getFileExtension(final Serializable content, final boolean withDot);

  public String getFileExtension(final NodeRef nodeRef);

  public String getFileExtension(final NodeRef nodeRef, final boolean withDot);

  public void setDateSaved(final NodeRef nodeRef);

  public void addChecksum(final NodeRef nodeRef);

  public String getChecksum(final File file);

  public String getChecksum(final InputStream inputStream);

  public byte[] md5(final InputStream data);

  public void deleteUnusedUserFolders();

  public String getMimetype(final Serializable content);

  public String getMimetype(final NodeRef nodeRef);

  public String findLanguageCode(final String language);

  public String getBaseLink(final String base);

  public String getRepositoryBaseLink();

  public String getShareBaseLink();

  public String getDocumentIdentifier(final NodeRef nodeRef);

  public String getDocumentIdentifier(final NodeRef nodeRef, final boolean nativ);

  /**
   * Creates a link to the original document in Share
   * 
   * @param nodeRef
   * @return the original link
   */
  public String getDocumentSource(final NodeRef nodeRef);

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
  public Map<String, String> listSiteMembers(final String shortName, final String nameFilter, final String roleFilter, final int size, final boolean collapseGroups);

  public boolean isPublished(final NodeRef nodeRef);

  /**
   * @deprecated use {@link #disableBehaviour()} instead.
   */
  @Deprecated
  public void disableAllBehaviours();

  public void disableBehaviour();

  ResultSet query(String query);
  
}
