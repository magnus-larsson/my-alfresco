package se.vgregion.alfresco.repo.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

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
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxb.JAXBUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.kivclient.KivWsClient;
import se.vgregion.alfresco.repo.model.VgrModel;
//import se.vgregion.client.VGRegionWebServiceClient;
import se.vgregion.ws.services.VGRException;
import se.vgregion.ws.services.ObjectFactory;
import se.vgregion.ws.services.SearchPersonEmployment;

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

    _policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));

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
   * Updates the person user
   */
  public class PersonInfoUpdater implements Runnable {
    private NodeRef personNodeRef;
    private JAXBContext jaxbContext = null;
    
    public PersonInfoUpdater(NodeRef personNodeRef) {
      this.personNodeRef = personNodeRef;
    }

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
     * @throws VGRException 
     * @throws JAXBException 
     * @throws IOException 
     */
    public void runInternal() throws VGRException, JAXBException, IOException {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Handling person info update for " + personNodeRef);
      }
      String vgrId="ricde";
      String department = "Infrastruktur 6023";
      String searchPersonEmployment = kivWsClient.searchPersonEmployment(vgrId, department);
      
      
   }
  }

}
