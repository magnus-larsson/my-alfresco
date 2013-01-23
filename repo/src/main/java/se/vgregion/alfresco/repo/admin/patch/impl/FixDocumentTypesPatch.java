package se.vgregion.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import se.vgregion.alfresco.repo.model.VgrModel;

public class FixDocumentTypesPatch extends AbstractPatch {

  private static final String MSG_SUCCESS = "vgr.patch.fixDocumentTypesPatch.result";

  private PermissionService _permissionService;

  private SiteService _siteService;

  private FileFolderService _fileFolderService;

  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public void setPermissionService(PermissionService permissionService) {
    _permissionService = permissionService;
  }

  public void setSiteService(SiteService siteService) {
    _siteService = siteService;
  }

  @Override
  protected String applyInternal() throws Exception {
    StoreRef store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

    List<SiteInfo> sites = _siteService.listSites(null, null);

    for (SiteInfo site : sites) {
      changeForSite(store, site);
    }

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private void changeForSite(StoreRef store, SiteInfo site) throws FileNotFoundException {
    List<String> list = new ArrayList<String>();
    list.add("documentLibrary");

    FileInfo documentLibrary = _fileFolderService.resolveNamePath(site.getNodeRef(), list);

    List<FileInfo> files = _fileFolderService.listFiles(documentLibrary.getNodeRef());
    List<FileInfo> folders = _fileFolderService.listDeepFolders(documentLibrary.getNodeRef(), null);

    for (FileInfo file : files) {
      changeForFile(file);
    }

    for (FileInfo folder : folders) {
      changeForFolder(folder);
    }
  }

  private void changeForFolder(FileInfo folder) {
    List<FileInfo> files = _fileFolderService.listFiles(folder.getNodeRef());

    for (FileInfo file : files) {
      changeForFile(file);
    }
  }

  private void changeForFile(FileInfo file) {
    NodeRef nodeRef = file.getNodeRef();
    QName type = nodeService.getType(nodeRef);
    Path path = nodeService.getPath(nodeRef);
    String displayPath = path.toDisplayPath(nodeService, _permissionService) + "/" + file.getName();
    
    if (type.isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
      return;
    }

    changeForDocument(file.getNodeRef(), type, displayPath);
  }

  private boolean changeForDocument(NodeRef nodeRef, QName type, String displayPath) {
    boolean result = false;

    try {
      nodeService.setType(nodeRef, VgrModel.TYPE_VGR_DOCUMENT);

      System.out.println("Type changed from '" + type.getNamespaceURI() + "' to 'vgr:document' on: " + displayPath);

      result = true;
    } catch (Throwable ex) {
      System.out.println("Failed to change type to 'vgr:document' on: " + displayPath);
    }

    return result;
  }

}
