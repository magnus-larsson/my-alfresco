package se.vgregion.alfresco.repo.node;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
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

  @Override
  public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after) {
    // Run as system user to prevent certain access restriction errors which may
    // appear when property updates are made by alfresco when new renditions are
    // created
    AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        runSafe(new DefaultRunSafe(nodeRef) {

          @Override
          public void execute() {
            _behaviour.disable();
            try {
              updateProperties(nodeRef, before, after);
            } finally {
              _behaviour.enable();
            }
          }
        });

        return null;
      }

    });
  }

  private void updateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
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

    if (!same(true, after.get(ContentModel.PROP_AUTO_VERSION_PROPS))) {
      _nodeService.setProperty(nodeRef, ContentModel.PROP_AUTO_VERSION_PROPS, true);
    }

    if (!same(true, after.get(ContentModel.PROP_AUTO_VERSION))) {
      _nodeService.setProperty(nodeRef, ContentModel.PROP_AUTO_VERSION, true);
    }

    setTitle(nodeRef, after);
    setDescription(nodeRef, before, after);
    setDateSaved(nodeRef, before, after);
    setFilename(nodeRef, before, after);
    setExtension(nodeRef, after);
    setMimetype(nodeRef, after);
    setContributorSavedBy(nodeRef, after);
    setVersion(nodeRef, after);
    setCmName(nodeRef, after);

    if (LOG.isDebugEnabled()) {
      LOG.debug(this.getClass().getName());
    }
  }

  private void setDescription(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    // if the source (VgrModel.PROP_DESCRIPTION) is unchanged, don't replicate
    // this
    if (same(before, after, VgrModel.PROP_DESCRIPTION)) {
      return;
    }

    Serializable description = _nodeService.getProperty(nodeRef, VgrModel.PROP_DESCRIPTION);

    // if the VgrModel.PROP_DESCRIPTION is the same as the
    // ContentModel.PROP_DESCRIPTION, don't replicate
    if (same(description, after.get(ContentModel.PROP_DESCRIPTION))) {
      return;
    }

    _nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, description);
  }

  private void setCmName(NodeRef nodeRef, Map<QName, Serializable> after) {
    String title = (String) after.get(VgrModel.PROP_TITLE);

    String extension = (String) after.get(VgrModel.PROP_FORMAT_EXTENT_EXTENSION_NATIVE);

    if (StringUtils.isBlank(extension)) {
      extension = (String) after.get(VgrModel.PROP_FORMAT_EXTENT_EXTENSION);
    }

    if (StringUtils.isBlank(title)) {
      return;
    }

    String name = title;

    if (StringUtils.isNotBlank(extension)) {
      name += "." + extension;
    }

    if (same(name, after.get(ContentModel.PROP_NAME))) {
      return;
    }

    _nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
  }

  private void setVersion(NodeRef nodeRef, Map<QName, Serializable> after) {
    if (same(after, VgrModel.PROP_IDENTIFIER_VERSION, ContentModel.PROP_VERSION_LABEL)) {
      return;
    }

    String version = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_VERSION_LABEL);

    // default to 0.1
    if (StringUtils.isBlank(version)) {
      version = "0.1";
    }

    if (same(version, after.get(VgrModel.PROP_IDENTIFIER_VERSION))) {
      return;
    }

    _serviceUtils.replicateVersion(nodeRef, version);
  }

  /**
   * Replicate the title only if it's not set.
   * 
   * @param nodeRef
   * @param after
   * @param before
   */
  private void setTitle(NodeRef nodeRef, Map<QName, Serializable> after) {
    String title = _serviceUtils.getStringValue(nodeRef, VgrModel.PROP_TITLE);

    if (StringUtils.isNotBlank(title) && !same(title, after.get(ContentModel.PROP_TITLE))) {
      // if the title is set, replicate it to cm:title
      _nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, title);

      return;
    }

    // if the title has not been set, use the cm:name and remove the extension
    String name = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_NAME);

    name = FilenameUtils.removeExtension(name);

    if (!same(name, after.get(VgrModel.PROP_TITLE))) {
      _nodeService.setProperty(nodeRef, VgrModel.PROP_TITLE, name);
    }

    if (!same(name, after.get(ContentModel.PROP_TITLE))) {
      _nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, name);
    }
  }

  private void setDateSaved(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    // if both VgrModel.PROP_DATE_SAVED and ContentModel.PROP_MODIFIED are the
    // same, don't replicate
    if (same(after, VgrModel.PROP_DATE_SAVED, ContentModel.PROP_MODIFIED)) {
      return;
    }

    // if ContentModel.PROP_MODIFIED is same, don't replicate it's value
    if (same(before, after, ContentModel.PROP_MODIFIED)) {
      return;
    }

    _serviceUtils.setDateSaved(nodeRef);
  }

  private void setContributorSavedBy(NodeRef nodeRef, Map<QName, Serializable> after) {
    String username = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_MODIFIER);

    String representation = _serviceUtils.getRepresentation(nodeRef, ContentModel.PROP_MODIFIER);

    if (!same(representation, after.get(VgrModel.PROP_CONTRIBUTOR_SAVEDBY))) {
      _nodeService.setProperty(nodeRef, VgrModel.PROP_CONTRIBUTOR_SAVEDBY, representation);
    }

    if (StringUtils.isNotBlank(username) && !same(username, after.get(VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID))) {
      _nodeService.setProperty(nodeRef, VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID, username);
    }
  }

  private void setMimetype(NodeRef nodeRef, Map<QName, Serializable> after) {
    ContentData content = (ContentData) _nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

    String mimetype = content != null ? content.getMimetype() : "";

    // if no mimetype found then do nothing
    if (StringUtils.isBlank(mimetype)) {
      return;
    }

    if (same(mimetype, after.get(VgrModel.PROP_FORMAT_EXTENT_MIMETYPE))) {
      return;
    }

    _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE, mimetype);
  }

  private void setFilename(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    if (same(after, VgrModel.PROP_TITLE_FILENAME, ContentModel.PROP_NAME)) {
      return;
    }

    if (same(before, after, ContentModel.PROP_NAME)) {
      return;
    }

    String name = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_NAME);

    // there must be a name in order to set the filename
    if (StringUtils.isBlank(name)) {
      return;
    }

    if (same(name, after.get(VgrModel.PROP_TITLE_FILENAME))) {
      return;
    }

    _nodeService.setProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME, name);
  }

  private void setExtension(NodeRef nodeRef, Map<QName, Serializable> after) {
    Serializable content = _nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
    String name = _serviceUtils.getStringValue(nodeRef, ContentModel.PROP_NAME);

    String extension = StringUtils.isNotBlank(name) && content != null ? FilenameUtils.getExtension(name) : "";

    if (StringUtils.isBlank(extension)) {
      extension = _serviceUtils.getFileExtension(content, false);
    }

    if (same(extension, after.get(VgrModel.PROP_FORMAT_EXTENT_EXTENSION))) {
      return;
    }

    _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_EXTENSION, extension);
  }

  /**
   * Checks whether two different properties are the same
   * 
   * @param before
   * @param after
   * @param property
   * @return
   */
  private boolean same(Map<QName, Serializable> properties, QName property1, QName property2) {
    Serializable value1 = properties.get(property1);
    Serializable value2 = properties.get(property2);

    return same(value1, value2);
  }

  /**
   * Checks whether a property is the same before as after
   * 
   * @param before
   * @param after
   * @param property
   * @return
   */
  private boolean same(Map<QName, Serializable> before, Map<QName, Serializable> after, QName property) {
    Serializable value1 = before.get(property);
    Serializable value2 = after.get(property);

    return same(value1, value2);
  }

  private boolean same(Serializable value1, Serializable value2) {
    if (value1 == null && value2 == null) {
      return true;
    }

    if (value1 == null || value2 == null) {
      return false;
    }

    return value1.equals(value2);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _behaviour = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.EVERY_EVENT);

    _policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, VgrModel.TYPE_VGR_DOCUMENT, _behaviour);
  }

}
