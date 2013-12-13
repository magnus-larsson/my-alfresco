package se.vgregion.alfresco.repo.node;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.kivclient.KivWsClient;
import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Attach additional information to the person object
 * 
 */
public class ExtendPersonPolicy extends AbstractPolicy implements OnUpdateNodePolicy, OnCreateNodePolicy {

  private ThreadPoolExecutor _threadPoolExecutor;
  private TransactionListener _transactionListener;
  private TransactionService _transactionService;
  private ContentService _contentService;

  private static final String KEY_PERSON_INFO = ExtendPersonPolicy.class.getName() + ".personInfoUpdate";
  private static final Logger LOG = Logger.getLogger(ExtendPersonPolicy.class);
  private KivWsClient _kivWsClient;
  private static Boolean initialized = false;
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
        if (!_nodeService.exists(nodeRef)) {
          return null;
        }

        String property = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_THUMBNAIL_PHOTO);

        if (property != null && property.length() > 0) {
          _behaviourFilter.disableBehaviour();

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

          LOG.debug("Wr: " + writer.getMimetype());

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

          if (LOG.isDebugEnabled()) {
            LOG.debug("Avatar node: " + imageNodeRef);
          }

          _behaviourFilter.enableBehaviour();

          return true;
        }

        return false;
      }
    }, AuthenticationUtil.getSystemUserName());

    if (hasThumbnail == null) {
      LOG.debug("User does not exist");
    } else if (Boolean.FALSE.equals(hasThumbnail)) {
      LOG.debug("User does not have a thumbnail photo");
    } else {
      if (LOG.isInfoEnabled()) {
        LOG.info("User have a thumbnail photo " + nodeRef);
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
        if (!_nodeService.exists(nodeRef)) {
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
  private void updatePersonInfo(NodeRef personNodeRef) {
    AlfrescoTransactionSupport.bindListener(_transactionListener);
    AlfrescoTransactionSupport.bindResource(KEY_PERSON_INFO, personNodeRef);
  }

  public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
    _threadPoolExecutor = threadPoolExecutor;
  }

  public void setTransactionService(TransactionService transactionService) {
    _transactionService = transactionService;
  }

  public void setKivWsClient(KivWsClient kivWsClient) {
    _kivWsClient = kivWsClient;
  }

  public void setContentService(ContentService contentService) {
    _contentService = contentService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Assert.notNull(_threadPoolExecutor);
    Assert.notNull(_kivWsClient);
    Assert.notNull(_contentService);

    if (!initialized) {
      _policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));

      _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));

      initialized = true;

    }
    _transactionListener = new UpdatePersonInfoTransactionListener();
  }

  /**
   * Transaction listener, fires off the new thread after transaction commit.
   */
  private class UpdatePersonInfoTransactionListener extends TransactionListenerAdapter {
    
    @Override
    public void afterCommit() {
      NodeRef personNodeRef = (NodeRef) AlfrescoTransactionSupport.getResource(KEY_PERSON_INFO);
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Requesting person info update for " + personNodeRef);
      }
      
      Runnable runnable = new PersonInfoUpdater(personNodeRef);
      
      _threadPoolExecutor.execute(runnable);
    }
    
  }

  /**
   * Updates the person user with additional details from KIV
   */
  public class PersonInfoUpdater implements Runnable {
    private NodeRef _personNodeRef;

    public PersonInfoUpdater(NodeRef personNodeRef) {
      _personNodeRef = personNodeRef;
    }

    /**
     * Runner
     */
    public void run() {
      AuthenticationUtil.runAs(new RunAsWork<Void>() {
        public Void doWork() throws Exception {
          return _transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable {
              runInternal();
              
              return null;
            }

          }, false, true);

        }
      }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Internal function to allow for unit testing
     * 
     */
    public void runInternal() {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Handling person info update for " + _personNodeRef);
      }

      String responsibiltyCode = (String) _nodeService.getProperty(_personNodeRef, VgrModel.PROP_PERSON_RESPONSIBILITY_CODE);
      
      String userName = (String) _nodeService.getProperty(_personNodeRef, ContentModel.PROP_USERNAME);
      
      if (StringUtils.isNotBlank(userName)) {
        if (responsibiltyCode != null && responsibiltyCode.length() > 0) {
          String organizationDn;
          
          try {
            organizationDn = _kivWsClient.searchPersonEmployment(userName, responsibiltyCode);
            
            if (organizationDn != null && organizationDn.length() > 0) {
              _behaviourFilter.disableBehaviour(_personNodeRef);
              
              _nodeService.setProperty(_personNodeRef, VgrModel.PROP_PERSON_ORGANIZATION_DN, organizationDn);

              String[] ous = organizationDn.split(",");
              
              ArrayUtils.reverse(ous);
              
              String org = "";
              
              for (int i = 0; i < ous.length; i++) {
                String ou = ous[i].split("=")[1];
                
                org = org + ou;
                
                if (i < ous.length - 2) {
                  org = org + "/";
                }
              }
              
              _nodeService.setProperty(_personNodeRef, ContentModel.PROP_ORGANIZATION, org);
              
              _nodeService.setProperty(_personNodeRef, ContentModel.PROP_ORGID, org);
              
              _behaviourFilter.enableBehaviour(_personNodeRef);
            } else {
              LOG.warn("User organzationDn is empty for user: " + userName);
            }
          } catch (Exception e) {
            LOG.error("Error while searching for person employment", e);
          }

        } else {
          LOG.warn("User responsibility code is not available for user: " + userName);
        }
      } else {
        LOG.warn("Username could not be found for " + _personNodeRef);
      }
    }
  }

}
