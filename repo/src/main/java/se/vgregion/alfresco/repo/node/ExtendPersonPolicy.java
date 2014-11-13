package se.vgregion.alfresco.repo.node;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Attach additional information to the person object
 * 
 */
public class ExtendPersonPolicy extends AbstractPolicy implements OnUpdateNodePolicy, OnCreateNodePolicy, OnUpdatePropertiesPolicy {

  private ContentService _contentService;

  private static final Logger LOG = Logger.getLogger(ExtendPersonPolicy.class);
  private static Boolean _initialized = false;
  private static String avatarName = "ad_avatar.jpg";

  @Override
  public void onUpdateNode(final NodeRef user) {
    addPersonAspects(user);
  }

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef user = childAssocRef.getChildRef();

    addPersonAspects(user);
  }

  @Override
  public void onUpdateProperties(NodeRef user, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    // if the VgrModel.PROP_THUMBNAIL_PHOTO has changed, then update the persons
    // thumbnail
    String beforePhoto = (String) before.get(VgrModel.PROP_THUMBNAIL_PHOTO);
    String afterPhoto = (String) after.get(VgrModel.PROP_THUMBNAIL_PHOTO);

    if (!StringUtils.equals(beforePhoto, afterPhoto) && StringUtils.isNotBlank(afterPhoto)) {
      updatePersonThumbnail(user, afterPhoto);
    }

    // if the VgrModel.PROP_PERSON_ORGANIZATION_DN has changed, then update the
    // stuff from KIV
    String beforeOrganization = (String) before.get(VgrModel.PROP_PERSON_ORGANIZATION_DN);
    String afterOrganization = (String) after.get(VgrModel.PROP_PERSON_ORGANIZATION_DN);

    if (!StringUtils.equals(beforeOrganization, afterOrganization) && StringUtils.isNotBlank(afterOrganization)) {
      updatePersonInfo(user);
    }
  }

  private void updatePersonThumbnail(final NodeRef user, final String thumbnail) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Checking if thumbnail is present for user: " + user);
    }

    Boolean hasThumbnail = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {

      @Override
      public Boolean doWork() throws Exception {
        // if the user is gone, exit
        if (!_nodeService.exists(user)) {
          return null;
        }

        if (StringUtils.isBlank(thumbnail)) {
          return false;
        }

        _behaviourFilter.disableBehaviour();

        if (!_nodeService.hasAspect(user, ContentModel.ASPECT_PREFERENCES)) {
          _nodeService.addAspect(user, ContentModel.ASPECT_PREFERENCES, null);
        }

        // Remove old image if there is one
        List<ChildAssociationRef> childAssocs = _nodeService.getChildAssocs(user, ContentModel.ASSOC_PREFERENCE_IMAGE, RegexQNamePattern.MATCH_ALL);

        for (ChildAssociationRef childAssoc : childAssocs) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Removing old avatar: " + childAssoc.getChildRef());
          }

          _nodeService.deleteNode(childAssoc.getChildRef());
        }

        // Add new image node and write the thumbnail data to it
        NodeRef image = _nodeService.createNode(user, ContentModel.ASSOC_PREFERENCE_IMAGE, QName.createQName(avatarName), ContentModel.TYPE_CONTENT).getChildRef();

        ContentWriter writer = _contentService.getWriter(image, ContentModel.PROP_CONTENT, true);

        byte[] decodeBase64 = Base64.decodeBase64(thumbnail);

        ByteArrayInputStream BAIS = new ByteArrayInputStream(decodeBase64);

        writer.setEncoding("UTF-8");
        writer.setMimetype("image/jpeg");
        writer.putContent(BAIS);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Wr: " + writer.getMimetype());
        }

        // Remove old avatar assoc if there is one
        List<AssociationRef> targetAssocs = _nodeService.getTargetAssocs(user, ContentModel.ASSOC_AVATAR);

        for (AssociationRef targetAssoc : targetAssocs) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Removing old avatar assoc: " + targetAssoc.getTargetRef());
          }

          _nodeService.removeAssociation(user, targetAssoc.getTargetRef(), ContentModel.ASSOC_AVATAR);
        }

        // Add an avatar association for backwards compatibility
        _nodeService.createAssociation(user, image, ContentModel.ASSOC_AVATAR);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Avatar node: " + image);
        }

        _behaviourFilter.enableBehaviour();

        return true;
      }
    });

    if (hasThumbnail == null) {
      LOG.debug("User does not exist");
    } else if (Boolean.FALSE.equals(hasThumbnail)) {
      LOG.debug("User does not have a thumbnail photo");
    } else {
      if (LOG.isInfoEnabled()) {
        LOG.info("User have a thumbnail photo " + user);
      }
    }

  }

  /**
   * Adds the person aspect
   * 
   * @param user
   */
  private void addPersonAspects(final NodeRef user) {
    AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        // if the user is gone, exit
        if (!_nodeService.exists(user)) {
          return null;
        }

        if (!_nodeService.getType(user).isMatch(ContentModel.TYPE_PERSON)) {
          return null;
        }

        // if the node already has the aspect, exit
        if (!_nodeService.hasAspect(user, VgrModel.ASPECT_PERSON)) {
          _nodeService.addAspect(user, VgrModel.ASPECT_PERSON, null);
        }

        // add the aspect
        if (!_nodeService.hasAspect(user, VgrModel.ASPECT_THUMBNAIL_PHOTO)) {
          _nodeService.addAspect(user, VgrModel.ASPECT_THUMBNAIL_PHOTO, null);
        }

        return null;
      }

    });
  }

  /**
   * Bind the listener and prepare it with data
   * 
   * @param user
   */
  private void updatePersonInfo(NodeRef user) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Handling person info update for " + user);
    }

    try {
      String username = (String) _nodeService.getProperty(user, ContentModel.PROP_USERNAME);

      String organizationDn = (String) _nodeService.getProperty(user, VgrModel.PROP_PERSON_ORGANIZATION_DN);

      if (StringUtils.isEmpty(organizationDn)) {
        LOG.warn("User organzationDn is empty for user: " + username);

        return;
      }

      _behaviourFilter.disableBehaviour(user);
      try {

        String[] ous = organizationDn.split(",");

        ArrayUtils.reverse(ous);

        List<String> result = new ArrayList<String>();

        for (String ou : ous) {
          ou = ou.split("=")[1];

          result.add(ou);
        }

        String organization = StringUtils.join(result, "/");

        _nodeService.setProperty(user, ContentModel.PROP_ORGANIZATION, organization);

        _nodeService.setProperty(user, ContentModel.PROP_ORGID, organization);
      } finally {
        _behaviourFilter.enableBehaviour(user);
      }
    } catch (Exception e) {
      LOG.error("Error while searching for person employment", e);
    }
  }

  public void setContentService(ContentService contentService) {
    _contentService = contentService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Assert.notNull(_contentService);

    if (!_initialized) {
      _policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onUpdateNode", NotificationFrequency.EVERY_EVENT));
      _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.EVERY_EVENT));
      _policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.EVERY_EVENT));

      _initialized = true;
    }
  }

}
