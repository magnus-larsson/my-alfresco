package se.vgregion.alfresco.repo.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Class for updating all documents in a folder with the metadata of the folder,
 * when the metadata for the folder has been updated.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class UpdateSiteFolderMetadataPolicy extends AbstractPolicy implements OnUpdatePropertiesPolicy {

  private static final Logger LOG = Logger.getLogger(UpdateSiteFolderMetadataPolicy.class);

  private FileFolderService _fileFolderService;

  private Behaviour _behaviour;

  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  @Override
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    _behaviour.disable();

    try {
      boolean impersonated = false;

      if (AuthenticationUtil.getRunAsAuthentication() == null) {
        AuthenticationUtil.setRunAsUserSystem();

        impersonated = true;
      }

      boolean updated = updateFolderProperties(nodeRef, before, after);

      if (impersonated) {
        AuthenticationUtil.clearCurrentSecurityContext();
      }

      if (LOG.isDebugEnabled() && updated) {
        LOG.debug(this.getClass().getName());
      }
    } finally {
      _behaviour.enable();
    }
  }

  private boolean updateFolderProperties(NodeRef folder, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    boolean updated = false;

    // first check if the folder exists
    if (!_nodeService.exists(folder)) {
      return updated;
    }

    // check if the folder is in a document library, if not, return
    if (!isDocumentLibrary(folder)) {
      return updated;
    }

    // make sure the folder has the aspect METADATA in order to continue
    if (!_nodeService.hasAspect(folder, VgrModel.ASPECT_METADATA)) {
      return updated;
    }

    Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

    // iterate through all the properties of the folder
    for (Entry<QName, Serializable> entry : after.entrySet()) {
      QName key = entry.getKey();

      // only replicate the VGR properties
      if (!key.getNamespaceURI().equals(VgrModel.VGR_URI)) {
        continue;
      }

      Serializable valueBefore = before.get(key);
      Serializable valueAfter = entry.getValue();

      String sValueBefore = valueBefore == null ? "" : valueBefore.toString();
      String sValueAfter = valueAfter == null ? "" : valueAfter.toString();

      // if the value is the same before and after then continue
      if (sValueBefore.equalsIgnoreCase(sValueAfter)) {
        continue;
      }

      properties.put(key, valueAfter);
    }

    List<FileInfo> files = _fileFolderService.listFiles(folder);

    for (FileInfo fileInfo : files) {
      NodeRef file = fileInfo.getNodeRef();

      if (!_nodeService.hasAspect(file, VgrModel.ASPECT_STANDARD)) {
        continue;
      }

      if (_nodeService.hasAspect(file, RenditionModel.ASPECT_HIDDEN_RENDITION)) {
        continue;
      }

      if (_nodeService.hasAspect(file, ContentModel.ASPECT_TEMPORARY)) {
        continue;
      }

      if (_nodeService.hasAspect(file, ContentModel.ASPECT_LOCKABLE)) {
        continue;
      }

      if (_nodeService.hasAspect(file, ContentModel.ASPECT_WORKING_COPY)) {
        continue;
      }

      _nodeService.addProperties(file, properties);

      updated = true;
    }

    return updated;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _behaviour = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.EVERY_EVENT);

    _policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_FOLDER, _behaviour);
  }

}
