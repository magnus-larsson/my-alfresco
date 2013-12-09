package se.vgregion.alfresco.repo.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode.ScriptContentData;
import org.alfresco.repo.site.script.Site;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.Content;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class ScriptServiceUtils extends BaseScopableProcessorExtension implements InitializingBean {

  protected SiteService _siteService;

  protected ServiceUtilsImpl _serviceUtils;

  protected ServiceRegistry _serviceRegistry;

  public void setSiteService(final SiteService siteService) {
    _siteService = siteService;
  }

  public void setServiceUtils(final ServiceUtilsImpl serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
    _serviceRegistry = serviceRegistry;
  }

  public void deleteUnusedUserFolders() {
    _serviceUtils.deleteUnusedUserFolders();
  }

  public Site[] listPublicSites(final String nameFilter, final String sitePresetFilter, final int size) {
    final List<SiteInfo> siteInfos = _serviceUtils.listPublicSites(nameFilter, sitePresetFilter, size);

    final List<Site> sites = new ArrayList<Site>(siteInfos.size());

    for (final SiteInfo siteInfo : siteInfos) {
      final Site site = createSite(siteInfo);

      sites.add(site);
    }

    return sites.toArray(new Site[sites.size()]);
  }

  private Site createSite(final SiteInfo siteInfo) {
    try {
      final Constructor<?> constructor = Site.class.getDeclaredConstructor(SiteInfo.class, ServiceRegistry.class, SiteService.class, Scriptable.class);

      ReflectionUtils.makeAccessible(constructor);

      return (Site) constructor.newInstance(siteInfo, _serviceRegistry, _siteService, getScope());
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public String findLanguageCode(final String language) {
    return _serviceUtils.findLanguageCode(language);
  }

  public String getDocumentIdentifier(final String nodeRef) {
    return _serviceUtils.getDocumentIdentifier(new NodeRef(nodeRef));
  }

  public Map<String, String> listMembers(final String shortName, final String nameFilter, final String roleFilter, final int size, final boolean collapseGroups) {
    return _serviceUtils.listSiteMembers(shortName, nameFilter, roleFilter, size, collapseGroups);
  }

  public String copyToTempFile(final Content content) {
    FileOutputStream outputStream = null;

    try {
      final File tempFile = TempFileProvider.createTempFile("upload_", ".tmp");

      outputStream = new FileOutputStream(tempFile);

      IOUtils.copyLarge(content.getInputStream(), outputStream);

      return tempFile.getAbsolutePath();
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      IOUtils.closeQuietly(outputStream);
    }
  }

  public void writeContent(final ScriptContentData contentData, final String filename) {
    InputStream inputStream = null;

    try {
      inputStream = new FileInputStream(filename);

      contentData.write(inputStream);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Parses the supplied accessRights string. If no value is supplied an empty string is returned. If a list with just one entry is supplied, that value is returned. If more than one value is in the
   * list, if Internet is in the list that value is returned, otherwise the first value in the list is returned.
   * 
   * @param accessRights
   * @return
   */
  public String parseAccessRights(List<String> accessRights) {
    if (accessRights == null || accessRights.size() == 0) {
      return "";
    }

    if (accessRights.size() == 1) {
      return accessRights.get(0);
    }

    if (accessRights.contains("Internet")) {
      return "Internet";
    }

    return accessRights.get(0);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_serviceRegistry);
    Assert.notNull(_siteService);
    Assert.notNull(_serviceUtils);
  }

  /**
   * @deprecated use {@link #disableBehaviour()} instead.
   */
  @Deprecated
  public void disableAllBehaviours() {
    _serviceUtils.disableBehaviour();
  }

  public void disableBehaviour() {
    _serviceUtils.disableBehaviour();
  }

  public String removeExtension(String filename) {
    return FilenameUtils.removeExtension(filename);
  }

}
