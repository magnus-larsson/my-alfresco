package se.vgregion.alfresco.repo.node;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class DelegatedPolicies {
  private final static Logger LOG = Logger.getLogger(DelegatedPolicies.class);

  @Autowired
  protected PolicyComponent _policyComponent;

  @Autowired
  protected DefaultSwedishLanguagePolicy _defaultSwedishLanguagePolicy;

  @Autowired
  protected ChangeTypePolicy _changeTypePolicy;

  @Autowired
  protected AutoPublishPolicy _autoPublishPolicy;

  @Autowired
  protected CreateSiteDocumentPolicy _createSiteDocumentPolicy;

  @Autowired
  protected EnableMetadataWriterPolicy _enableMetadataWriterPolicy;

  @Autowired
  protected MoveWatchedDocumentsPolicy _moveWatchedDocumentsPolicy;

  private static boolean _initialized = false;

  public void onCreateNodeForCmContent(ChildAssociationRef childAssocRef) {
    if (LOG.isDebugEnabled())
      LOG.trace(this.getClass().getName() + ": onCreateNodeForCmContent end");
    _changeTypePolicy.onCreateNode(childAssocRef);

    _defaultSwedishLanguagePolicy.onCreateNode(childAssocRef);

    _createSiteDocumentPolicy.onCreateNode(childAssocRef);

    _enableMetadataWriterPolicy.onCreateNode(childAssocRef);

    _moveWatchedDocumentsPolicy.onCreateNode(childAssocRef);

    _autoPublishPolicy.onCreateNode(childAssocRef);
    if (LOG.isDebugEnabled())
      LOG.trace(this.getClass().getName() + ": onCreateNodeForCmContent end");
  }

  @PostConstruct
  protected void postConstruct() {
    if (!_initialized) {
      LOG.info("Initialized " + this.getClass().getName());
      _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNodeForCmContent", NotificationFrequency.TRANSACTION_COMMIT));

      _initialized = true;
    }
  }

}
