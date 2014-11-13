package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import se.vgregion.alfresco.repo.model.VgrModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class for updating all documents in a folder with the metadata of the folder,
 * when the metadata for the folder has been updated.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class UpdateSiteFolderMetadataPolicy extends AbstractPolicy implements OnUpdatePropertiesPolicy {

  private final static Logger LOG = Logger.getLogger(UpdateSiteFolderMetadataPolicy.class);

  private FileFolderService _fileFolderService;

  private Behaviour _behaviour;

  public void setFileFolderService(final FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  @Override
  public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after) {
    _behaviour.disable();

    try {
      boolean impersonated = false;

      if (AuthenticationUtil.getRunAsAuthentication() == null) {
        AuthenticationUtil.setRunAsUserSystem();

        impersonated = true;
      }

      updateFolderProperties(nodeRef, before, after);

      if (impersonated) {
        AuthenticationUtil.clearCurrentSecurityContext();
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug(this.getClass().getName());
      }
    } finally {
      _behaviour.enable();
    }
  }

  private void updateFolderProperties(final NodeRef folderNodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after) {
    // first check if the folder exists
    if (!_nodeService.exists(folderNodeRef)) {
      return;
    }

    // check if the folder is in a document library, if not, return
    if (!isDocumentLibrary(folderNodeRef)) {
      return;
    }

    // make sure the folder has the aspect METADATA in order to continue
    if (!_nodeService.hasAspect(folderNodeRef, VgrModel.ASPECT_METADATA)) {
      return;
    }

    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

    // iterate through all the properties of the folder
    for (final Entry<QName, Serializable> entry : after.entrySet()) {
      final QName key = entry.getKey();

      // only replicate the VGR properties
      if (!key.getNamespaceURI().equals(VgrModel.VGR_URI)) {
        continue;
      }

      final Serializable valueBefore = before.get(key);
      final Serializable valueAfter = entry.getValue();

      final String sValueBefore = valueBefore == null ? "" : valueBefore.toString();
      final String sValueAfter = valueAfter == null ? "" : valueAfter.toString();

      // if the value is the same before and after then continue
      if (sValueBefore.equalsIgnoreCase(sValueAfter)) {
        continue;
      }
      properties.put(key, valueAfter);
    }

    final List<FileInfo> files = _fileFolderService.listFiles(folderNodeRef);

    for (final FileInfo file : files) {
      final NodeRef fileNodeRef = file.getNodeRef();

      if (!_nodeService.hasAspect(fileNodeRef, VgrModel.ASPECT_STANDARD)) {
        continue;
      }

      if (_nodeService.hasAspect(fileNodeRef, RenditionModel.ASPECT_HIDDEN_RENDITION)) {
        continue;
      }

      if (_nodeService.hasAspect(fileNodeRef, ContentModel.ASPECT_TEMPORARY)) {
        continue;
      }

      if (_nodeService.hasAspect(fileNodeRef, ContentModel.ASPECT_LOCKABLE)) {
        continue;
      }

      if (_nodeService.hasAspect(fileNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
        continue;
      }

      _nodeService.addProperties(fileNodeRef, properties);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _behaviour = new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

    _policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_FOLDER, _behaviour);
  }

}
