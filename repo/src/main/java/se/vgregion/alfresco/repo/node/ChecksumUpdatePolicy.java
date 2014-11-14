package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies.OnContentPropertyUpdatePolicy;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import se.vgregion.alfresco.repo.model.VgrModel;

public class ChecksumUpdatePolicy extends AbstractPolicy implements OnContentPropertyUpdatePolicy {

  private final static Logger LOG = Logger.getLogger(ChecksumUpdatePolicy.class);
  private static boolean _initialized = false;

  @Override
  public void onContentPropertyUpdate(final NodeRef nodeRef, final QName propertyQName, final ContentData beforeValue, final ContentData afterValue) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " - onContentPropertyUpdate begin");
    }
    runSafe(new DefaultRunSafe(nodeRef) {

      @Override
      public void execute() {
        doContentPropertyUpdate(nodeRef, propertyQName);
      }

    });

    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " - onContentPropertyUpdate end");
    }
  }

  private void doContentPropertyUpdate(final NodeRef nodeRef, final QName propertyQName) {

    // if it's not the content property, exit
    if (!propertyQName.isMatch(ContentModel.PROP_CONTENT)) {
      return;
    }

    // if by some chance the version does not exist, do nothing
    if (!_nodeService.exists(nodeRef)) {
      return;
    }

    // if it's the working copy being checked in, do nothing
    if (_nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
      return;
    }

    // if it's anything but locked, don't do anything
    if (_lockService.getLockStatus(nodeRef) != LockStatus.NO_LOCK) {
      return;
    }

    // if it's not the workspace store, do nothing
    if (!StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef())) {
      return;
    }

    _serviceUtils.addChecksum(nodeRef);

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    if (!_initialized) {
      LOG.info("Initialized " + this.getClass().getName());
      _policyComponent
          .bindClassBehaviour(OnContentPropertyUpdatePolicy.QNAME, VgrModel.TYPE_VGR_DOCUMENT, new JavaBehaviour(this, "onContentPropertyUpdate", NotificationFrequency.TRANSACTION_COMMIT));
    }
  }

}
