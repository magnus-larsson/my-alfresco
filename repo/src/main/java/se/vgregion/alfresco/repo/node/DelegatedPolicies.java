package se.vgregion.alfresco.repo.node;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class DelegatedPolicies {
  private final static Logger LOG = Logger.getLogger(DelegatedPolicies.class);

  protected PolicyComponent _policyComponent;

  protected DefaultSwedishLanguagePolicy _defaultSwedishLanguagePolicy;

  protected ChangeTypePolicy _changeTypePolicy;

  protected AutoPublishPolicy _autoPublishPolicy;

  protected CreateSiteDocumentPolicy _createSiteDocumentPolicy;

  protected EnableMetadataWriterPolicy _enableMetadataWriterPolicy;

  protected MoveWatchedDocumentsPolicy _moveWatchedDocumentsPolicy;

  private static boolean _initialized = false;

  public void onCreateNodeForCmContent(ChildAssociationRef childAssocRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + ": onCreateNodeForCmContent start");
    }
    
    _changeTypePolicy.onCreateNode(childAssocRef);

    _defaultSwedishLanguagePolicy.onCreateNode(childAssocRef);

    _createSiteDocumentPolicy.onCreateNode(childAssocRef);

    _enableMetadataWriterPolicy.onCreateNode(childAssocRef);

    _moveWatchedDocumentsPolicy.onCreateNode(childAssocRef);

    _autoPublishPolicy.onCreateNode(childAssocRef);
    
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + ": onCreateNodeForCmContent end");
    }
  }

  @PostConstruct
  protected void postConstruct() {
    if (!_initialized) {
      LOG.info("Initialized " + this.getClass().getName());
      
      _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNodeForCmContent", NotificationFrequency.TRANSACTION_COMMIT));

      _initialized = true;
    }
  }
  
  @Required
  public void setAutoPublishPolicy(AutoPublishPolicy autoPublishPolicy) {
    _autoPublishPolicy = autoPublishPolicy;
  }
  
  @Required
  public void setChangeTypePolicy(ChangeTypePolicy changeTypePolicy) {
    _changeTypePolicy = changeTypePolicy;
  }
  
  @Required
  public void setCreateSiteDocumentPolicy(CreateSiteDocumentPolicy createSiteDocumentPolicy) {
    _createSiteDocumentPolicy = createSiteDocumentPolicy;
  }
  
  @Required
  public void setDefaultSwedishLanguagePolicy(DefaultSwedishLanguagePolicy defaultSwedishLanguagePolicy) {
    _defaultSwedishLanguagePolicy = defaultSwedishLanguagePolicy;
  }
  
  @Required
  public void setEnableMetadataWriterPolicy(EnableMetadataWriterPolicy enableMetadataWriterPolicy) {
    _enableMetadataWriterPolicy = enableMetadataWriterPolicy;
  }
  
  @Required
  public void setMoveWatchedDocumentsPolicy(MoveWatchedDocumentsPolicy moveWatchedDocumentsPolicy) {
    _moveWatchedDocumentsPolicy = moveWatchedDocumentsPolicy;
  }
  
  @Required
  public void setPolicyComponent(PolicyComponent policyComponent) {
    _policyComponent = policyComponent;
  }

}
