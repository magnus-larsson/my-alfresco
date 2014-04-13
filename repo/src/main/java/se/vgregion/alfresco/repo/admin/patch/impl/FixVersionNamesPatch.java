package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.I18NUtil;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class FixVersionNamesPatch extends AbstractPatch {

  private static final String MSG_SUCCESS = "vgr.patch.fixVersionNamesPatch.result";

  // set the max size for 10 MB
  private static final long MAX_SIZE = 10485760;

  private static final Logger LOG = Logger.getLogger(FixVersionNamesPatch.class);

  private VersionService _versionService;

  private ContentService _contentService;

  public void setVersionService(final VersionService versionService) {
    _versionService = versionService;
  }

  public void setContentService(final ContentService contentService) {
    _contentService = contentService;
  }

  @Override
  protected String applyInternal() throws Exception {
    final String query = "TYPE:\"vgr:document\"";
    final SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    final ResultSet result = searchService.query(searchParameters);

    LOG.debug("Total amount of vgr:document files: " + result.length());

    int countFixedFilenames = 0;
    int countFixedContent = 0;

    try {
      for (final NodeRef nodeRef : result.getNodeRefs()) {
        // if the node has been deleted, continue
        if (!nodeService.exists(nodeRef)) {
          continue;
        }

        final VersionHistory versionHistory = _versionService.getVersionHistory(nodeRef);

        if (versionHistory == null) {
          continue;
        }

        final Collection<Version> versions = versionHistory.getAllVersions();

        for (final Version version : versions) {
          final NodeRef versionNodeRef = version.getFrozenStateNodeRef();

          final String name = nodeService.getProperty(versionNodeRef, ContentModel.PROP_NAME).toString();
          final Serializable filename = nodeService.getProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME);

          if (fixFilename(versionNodeRef, name, filename)) {
            countFixedFilenames++;
          }

          if (fixContent(versionNodeRef, nodeRef)) {
            countFixedContent++;
          }
        }
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }

    LOG.debug("Fixed " + countFixedFilenames + " document filenames in history.");
    LOG.debug("Fixed " + countFixedContent + " document content in history.");

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private boolean fixContent(final NodeRef versionNodeRef, final NodeRef nodeRef) {
    final ContentReader versionContentReader = _contentService.getReader(versionNodeRef, ContentModel.PROP_CONTENT);
    final ContentReader contentReader = _contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

    // if not content reader, something is very, very wrong. clone the
    // content from the original to the versions
    final long contentSize = contentReader != null ? contentReader.getSize() : 0; 

    if (versionContentReader == null && contentReader != null && contentSize < MAX_SIZE) {
      final ContentWriter versionContentWriter = _contentService.getWriter(VersionUtil.convertNodeRef(versionNodeRef),
          ContentModel.PROP_CONTENT, true);

      versionContentWriter.putContent(contentReader);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Fixed content for version node '" + versionNodeRef + "'");
      }

      return true;
    }

    return false;
  }

  private boolean fixFilename(final NodeRef versionNodeRef, final String name, final Serializable filename) {
    final String id = versionNodeRef.toString();
    
    if (filename == null) {
      return false;
    }

    // if the ID and the name is NOT the same, it's correct
    if (!id.endsWith("/" + name)) {
      return false;
    }

    // if the filename is an ID, then exit because it's not a filename
    if (isId(filename.toString())) {
      return false;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Name: " + name);
      LOG.debug("Frozen noderef: " + versionNodeRef);
      LOG.debug("Filename: " + filename);
      LOG.debug("Noderef: " + versionNodeRef.toString());
      LOG.debug("");
    }

    nodeService.setProperty(VersionUtil.convertNodeRef(versionNodeRef), ContentModel.PROP_NAME, filename);

    return true;
  }

  private boolean isId(final String filename) {
    // 6c387097-30b2-47ed-b30d-305757886740
    final String[] parts = StringUtils.split(filename, "-");

    if (parts.length != 5) {
      return false;
    }

    return parts[0].length() == 8 && parts[1].length() == 4 && parts[2].length() == 4 && parts[3].length() == 4
        && parts[4].length() == 12;
  }

}
