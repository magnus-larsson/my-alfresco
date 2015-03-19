package se.vgregion.alfresco.repo.node;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
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
public class ExtendPersonPolicy extends AbstractPolicy implements OnUpdateNodePolicy, OnCreateNodePolicy {

  private ContentService _contentService;

  private static final Logger LOG = Logger.getLogger(ExtendPersonPolicy.class);
  private static Boolean _initialized = false;
  private static String avatarName = "ad_avatar.jpg";

  @Override
  public void onUpdateNode(final NodeRef nodeRef) {
    addPersonAspects(nodeRef);
    updatePersonInfo(nodeRef);
    updatePersonThumbnail(nodeRef);
  }

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef nodeRef = childAssocRef.getChildRef();

    addPersonAspects(nodeRef);
    updatePersonInfo(nodeRef);
    updatePersonThumbnail(nodeRef);
  }

  private void updatePersonThumbnail(final NodeRef nodeRef) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Checking if thumbnail is present for user: " + nodeRef);
    }

    Boolean hasThumbnail = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>() {

      @Override
      public Boolean doWork() throws Exception {
        // if the node is gone, exit
        if (nodeRef == null || !_nodeService.exists(nodeRef)) {
          return null;
        }

        String property = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_THUMBNAIL_PHOTO);

        if (StringUtils.isBlank(property)) {
          return false;
        }

        _behaviourFilter.disableBehaviour();
        
        String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();

        try {
          AuthenticationUtil.setFullyAuthenticatedUser(VgrModel.SYSTEM_USER_NAME);
          
          if (!_nodeService.hasAspect(nodeRef, ContentModel.ASPECT_PREFERENCES)) {
            _nodeService.addAspect(nodeRef, ContentModel.ASPECT_PREFERENCES, null);
          }

          // Remove old image if there is one
          List<ChildAssociationRef> childAssocs = _nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_PREFERENCE_IMAGE, RegexQNamePattern.MATCH_ALL);

          for (ChildAssociationRef childAssoc : childAssocs) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Removing old avatar: " + childAssoc.getChildRef());
            }

            _nodeService.deleteNode(childAssoc.getChildRef());
          }

          // Add new image node and write the thumbnail data to it
          NodeRef imageNodeRef = _nodeService.createNode(nodeRef, ContentModel.ASSOC_PREFERENCE_IMAGE, QName.createQName(avatarName), ContentModel.TYPE_CONTENT).getChildRef();

          ContentWriter writer = _contentService.getWriter(imageNodeRef, ContentModel.PROP_CONTENT, true);

          byte[] decodeBase64 = Base64.decodeBase64(property);

          ByteArrayInputStream BAIS = new ByteArrayInputStream(decodeBase64);

          writer.setEncoding("UTF-8");
          writer.setMimetype("image/jpeg");
          writer.putContent(BAIS);

          // Remove old avatar assoc if there is one
          List<AssociationRef> targetAssocs = _nodeService.getTargetAssocs(nodeRef, ContentModel.ASSOC_AVATAR);

          for (AssociationRef targetAssoc : targetAssocs) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Removing old avatar assoc: " + targetAssoc.getTargetRef());
            }

            _nodeService.removeAssociation(nodeRef, targetAssoc.getTargetRef(), ContentModel.ASSOC_AVATAR);
          }

          // Add an avatar association for backwards compatability
          _nodeService.createAssociation(nodeRef, imageNodeRef, ContentModel.ASSOC_AVATAR);

          return true;
        } finally {
          AuthenticationUtil.setFullyAuthenticatedUser(fullyAuthenticatedUser);
          
          _behaviourFilter.enableBehaviour();
        }
      }
    }, VgrModel.SYSTEM_USER_NAME);

    if (hasThumbnail == null) {
      LOG.debug("User does not exist");
    } else if (Boolean.FALSE.equals(hasThumbnail)) {
      LOG.debug("User does not have a thumbnail photo");
    } else {
      if (LOG.isInfoEnabled()) {
        LOG.info("User have a thumbnail photo ");
      }
    }
  }

  /**
   * Adds the person aspect
   * 
   * @param nodeRef
   */
  private void addPersonAspects(final NodeRef nodeRef) {
    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        // if the node is gone, exit
        if (nodeRef == null || !_nodeService.exists(nodeRef)) {
          return null;
        }

        if (!_nodeService.getType(nodeRef).isMatch(ContentModel.TYPE_PERSON)) {
          return null;
        }

        // if the node already has the aspect, exit
        if (!_nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PERSON)) {
          _nodeService.addAspect(nodeRef, VgrModel.ASPECT_PERSON, null);
        }

        // add the aspect
        if (!_nodeService.hasAspect(nodeRef, VgrModel.ASPECT_THUMBNAIL_PHOTO)) {
          _nodeService.addAspect(nodeRef, VgrModel.ASPECT_THUMBNAIL_PHOTO, null);
        }

        return null;
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  /**
   * Bind the listener and prepare it with data
   * 
   * @param personNodeRef
   */
  private void updatePersonInfo(final NodeRef personNodeRef) {

    if (LOG.isDebugEnabled()) {
      LOG.debug("Handling person info update for " + personNodeRef);
    }
    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        if (personNodeRef == null || !_nodeService.exists(personNodeRef)) {
          return null;
        }

        if (!_nodeService.getType(personNodeRef).isMatch(ContentModel.TYPE_PERSON)) {
          return null;
        }

        try {
          String username = (String) _nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);

          String organizationDn = (String) _nodeService.getProperty(personNodeRef, VgrModel.PROP_PERSON_ORGANIZATION_DN);

          if (organizationDn != null && organizationDn.length() > 0) {
            _behaviourFilter.disableBehaviour(personNodeRef);
            try {

              String[] ous = organizationDn.split(",");

              ArrayUtils.reverse(ous);

              List<String> result = new ArrayList<String>();

              for (String ou : ous) {
                ou = ou.split("=")[1];

                result.add(ou);
              }

              String organization = StringUtils.join(result, "/");

              _nodeService.setProperty(personNodeRef, ContentModel.PROP_ORGANIZATION, organization);

              _nodeService.setProperty(personNodeRef, ContentModel.PROP_ORGID, organization);
            } finally {
              _behaviourFilter.enableBehaviour(personNodeRef);
            }
          } else {
            LOG.warn("User organzationDn is empty for user: " + username);
          }
        } catch (Exception e) {
          LOG.error("Error while searching for person employment", e);
        }
        return null;
      }

    }, AuthenticationUtil.getSystemUserName());

  }

  public void setContentService(ContentService contentService) {
    _contentService = contentService;
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    Assert.notNull(_contentService);

    if (!_initialized) {
      _policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));

      _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));

      _initialized = true;
    }
  }
}
