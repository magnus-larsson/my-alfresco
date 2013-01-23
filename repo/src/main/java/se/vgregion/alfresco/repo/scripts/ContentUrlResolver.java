package se.vgregion.alfresco.repo.scripts;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class ContentUrlResolver extends BaseScopableProcessorExtension implements InitializingBean {

  private final static Logger LOG = Logger.getLogger(ContentUrlResolver.class);

  private FileFolderService _fileFolderService;

  public void setFileFolderService(final FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public String getContentUrl(final NodeRef nodeRef) {
    if (nodeRef == null) {
      LOG.warn("Could not resolve content url for node == null!");

      return "";
    }

    final FileInfo fileInfo = _fileFolderService.getFileInfo(nodeRef);

    if (fileInfo == null || fileInfo.getContentData() == null) {
      LOG.warn("Could not get content data url for NodeRef " + nodeRef);

      return "";
    }

    final String contentUrl = _fileFolderService.getFileInfo(nodeRef).getContentData().getContentUrl();

    return contentUrl;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_fileFolderService);
  }

}
