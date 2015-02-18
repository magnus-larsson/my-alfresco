package se.vgregion.alfresco.repo.node;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;

public class PropertyReplicationPolicy extends AbstractPolicy implements OnUpdatePropertiesPolicy {

  private static final Logger LOG = Logger.getLogger(PropertyReplicationPolicy.class);

  private Behaviour _behaviour;

  private static boolean _initialized = false;

  @Override
  public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after) {
    // Run as system user to prevent certain access restriction errors which may
    // appear when property updates are made by alfresco when new renditions are
    // created
    AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        _behaviourFilter.disableBehaviour();
        try {
          updateProperties(nodeRef);
        } finally {
          _behaviourFilter.enableBehaviour();
        }
        return null;
      }

    });
  }

  private void updateProperties(final NodeRef nodeRef) {
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

    if (_lockService.getLockStatus(nodeRef) != LockStatus.NO_LOCK) {
      return;
    }

    _nodeService.setProperty(nodeRef, ContentModel.PROP_AUTO_VERSION_PROPS, true);
    _nodeService.setProperty(nodeRef, ContentModel.PROP_AUTO_VERSION, true);

    setTitle(nodeRef);
    setDescription(nodeRef);
    setDateSaved(nodeRef);
    setFilename(nodeRef);
    setExtension(nodeRef);
    setMimetype(nodeRef);
    setContributorSavedBy(nodeRef);
    setVersion(nodeRef);
    setCmName(nodeRef);

    if (LOG.isDebugEnabled()) {
      LOG.debug(this.getClass().getName());
    }
  }

  private void setDescription(NodeRef nodeRef) {
    Serializable description = _nodeService.getProperty(nodeRef, VgrModel.PROP_DESCRIPTION);

    _nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, description);
  }

  private void setCmName(final NodeRef nodeRef) {
    final String title = _serviceUtils.getStringValue(nodeRef, VgrModel.PROP_TITLE);

    String extension = _serviceUtils.getStringValue(nodeRef, VgrModel.PROP_FORMAT_EXTENT_EXTENSION_NATIVE);

    if (StringUtils.isBlank(extension)) {
      extension = _serviceUtils.getStringValue(nodeRef, VgrModel.PROP_FORMAT_EXTENT_EXTENSION);
    }

    if (StringUtils.isBlank(title)) {
      return;
    }

    String name = title;

    if (StringUtils.isNotBlank(extension)) {
      name += "." + extension;
    }

    _nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
  }

  private void setVersion(final NodeRef nodeRef) {
    String version = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_VERSION_LABEL);

    // default to 0.1
    if (StringUtils.isBlank(version)) {
      version = "0.1";
    }

    _serviceUtils.replicateVersion(nodeRef, version);
  }

  /**
   * Replicate the title only if it's not set.
   * 
   * @param nodeRef
   */
  private void setTitle(final NodeRef nodeRef) {
    final String title = _serviceUtils.getStringValue(nodeRef, VgrModel.PROP_TITLE);

    if (StringUtils.isNotBlank(title)) {
      // if the title is set, replicate it to cm:title
      _nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, title);

      return;
    }

    // if the title has not been set, use the cm:name and remove the extension
    String name = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_NAME);

    name = FilenameUtils.removeExtension(name);

    _nodeService.setProperty(nodeRef, VgrModel.PROP_TITLE, name);
    _nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, name);
  }

  private void setDateSaved(final NodeRef nodeRef) {
    _serviceUtils.setDateSaved(nodeRef);
  }

  private void setContributorSavedBy(final NodeRef nodeRef) {
    final String username = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_MODIFIER);

    final String representation = _serviceUtils.getRepresentation(nodeRef, ContentModel.PROP_MODIFIER);

    _nodeService.setProperty(nodeRef, VgrModel.PROP_CONTRIBUTOR_SAVEDBY, representation);

    if (StringUtils.isNotBlank(username)) {
      _nodeService.setProperty(nodeRef, VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID, username);
    }
  }

  private void setMimetype(final NodeRef nodeRef) {
    final ContentData content = (ContentData) _nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

    final String mimetype = content != null ? content.getMimetype() : "";

    _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE, mimetype);
  }

  private void setFilename(final NodeRef nodeRef) {
    final String dcFilename = _serviceUtils.getStringValue(nodeRef, VgrModel.PROP_TITLE_FILENAME);

    // don't set the filename if it exists...
    if (StringUtils.isNotBlank(dcFilename)) {
      return;
    }

    final String name = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_NAME);

    // there must be a name in order to set the filename
    if (StringUtils.isBlank(name)) {
      return;
    }

    _nodeService.setProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME, name);
  }

  private void setExtension(final NodeRef nodeRef) {
    final String dcExtension = _serviceUtils.getStringValue(nodeRef, VgrModel.PROP_FORMAT_EXTENT_EXTENSION);

    // if the extension exists, don't set it again
    if (StringUtils.isNotBlank(dcExtension)) {
      return;
    }

    final Serializable content = _nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
    final String name = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_NAME);

    String extension = StringUtils.isNotBlank(name) && content != null ? FilenameUtils.getExtension(name) : "";

    if (StringUtils.isBlank(extension)) {
      extension = _serviceUtils.getFileExtension(content, false);
    }

    _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_EXTENSION, extension);
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    if (!_initialized) {
      LOG.info("Initialized " + this.getClass().getName());
      _behaviour = new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

      _policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, VgrModel.TYPE_VGR_DOCUMENT, _behaviour);
      _initialized = true;
    }
  }

}
