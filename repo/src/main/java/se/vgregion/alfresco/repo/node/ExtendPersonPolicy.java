package se.vgregion.alfresco.repo.node;

import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.bind.JAXBContext;

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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.kivclient.KivWsClient;
import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Attach additional information to the person object
 * 
 */
public class ExtendPersonPolicy extends AbstractPolicy implements OnUpdateNodePolicy, OnCreateNodePolicy {
  private ThreadPoolExecutor threadPoolExecutor;
  private TransactionListener transactionListener;
  private TransactionService transactionService;
  private static final String KEY_PERSON_INFO = ExtendPersonPolicy.class.getName() + ".personInfoUpdate";
  private static final Logger LOG = Logger.getLogger(ExtendPersonPolicy.class);
  private KivWsClient kivWsClient;
  private static Boolean initialized = false;

  @Override
  public void onUpdateNode(final NodeRef nodeRef) {
    addPersonAspect(nodeRef);
    updatePersonInfo(nodeRef);
  }

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef nodeRef = childAssocRef.getChildRef();

    addPersonAspect(nodeRef);
    updatePersonInfo(nodeRef);
  }

  /**
   * Adds the person aspect
   * 
   * @param nodeRef
   */
  private void addPersonAspect(final NodeRef nodeRef) {
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
        if (_nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PERSON)) {
          return null;
        }

        // add the aspect
        _nodeService.addAspect(nodeRef, VgrModel.ASPECT_PERSON, null);

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
    AlfrescoTransactionSupport.bindListener(transactionListener);
    AlfrescoTransactionSupport.bindResource(KEY_PERSON_INFO, personNodeRef);
  }

  public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
    this.threadPoolExecutor = threadPoolExecutor;
  }

  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  public KivWsClient getKivWsClient() {
    return kivWsClient;
  }

  public void setKivWsClient(KivWsClient kivWsClient) {
    this.kivWsClient = kivWsClient;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Assert.notNull(threadPoolExecutor);
    if (!initialized) {
      _policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));

      _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
      initialized = true;

    }
    this.transactionListener = new UpdatePersonInfoTransactionListener();
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
      threadPoolExecutor.execute(runnable);
    }
  }

  /**
   * Updates the person user with additional details from KIV
   */
  public class PersonInfoUpdater implements Runnable {
    private NodeRef personNodeRef;
    private JAXBContext jaxbContext = null;

    public PersonInfoUpdater(NodeRef personNodeRef) {
      this.personNodeRef = personNodeRef;
    }

    /**
     * Runner
     */
    public void run() {
      AuthenticationUtil.runAs(new RunAsWork<Void>() {
        public Void doWork() throws Exception {
          return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {

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
        LOG.debug("Handling person info update for " + personNodeRef);
      }

      String responsibiltyCode = (String) _nodeService.getProperty(personNodeRef, VgrModel.PROP_PERSON_RESPONSIBILITY_CODE);
      String userName = (String) _nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
      if (userName != null && userName.length() > 0) {
        if (responsibiltyCode != null && responsibiltyCode.length() > 0) {
          String organizationDn;
          try {
            organizationDn = kivWsClient.searchPersonEmployment(userName, responsibiltyCode);
            if (organizationDn != null && organizationDn.length() > 0) {
              _behaviourFilter.disableBehaviour(personNodeRef);
              _nodeService.setProperty(personNodeRef, VgrModel.PROP_PERSON_ORGANIZATION_DN, organizationDn);

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
              _nodeService.setProperty(personNodeRef, ContentModel.PROP_ORGANIZATION, org);
              _nodeService.setProperty(personNodeRef, ContentModel.PROP_ORGID, org);
              _behaviourFilter.enableBehaviour(personNodeRef);
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
        LOG.warn("Username could not be found for " + personNodeRef);
      }
    }
  }

}
