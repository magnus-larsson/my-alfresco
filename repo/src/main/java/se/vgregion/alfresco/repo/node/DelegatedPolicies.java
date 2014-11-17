package se.vgregion.alfresco.repo.node;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.springframework.beans.factory.annotation.Autowired;

public class DelegatedPolicies {

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
    _changeTypePolicy.onCreateNode(childAssocRef);

    _defaultSwedishLanguagePolicy.onCreateNode(childAssocRef);

    _createSiteDocumentPolicy.onCreateNode(childAssocRef);

    _enableMetadataWriterPolicy.onCreateNode(childAssocRef);

    _moveWatchedDocumentsPolicy.onCreateNode(childAssocRef);

    _autoPublishPolicy.onCreateNode(childAssocRef);
  }

  @PostConstruct
  protected void postConstruct() {
    if (!_initialized) {
      _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNodeForCmContent", NotificationFrequency.TRANSACTION_COMMIT));

      _initialized = true;
    }
  }

}
