package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckIn;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;

import java.util.Set;

/**
 * Triggers the creation of PDF rendition for a document which origin is among
 * the ones configured.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class WatchedDocumentUpdatePolicy extends AbstractPolicy implements InitializingBean, OnCheckIn {

  protected StorageService _storageService;

  protected Set<String> _origins;

  public void setStorageService(final StorageService storageService) {
    _storageService = storageService;
  }

  public void setOrigins(final Set<String> origins) {
    _origins = origins;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    _policyComponent.bindClassBehaviour(OnCheckIn.QNAME, VgrModel.TYPE_VGR_DOCUMENT, new JavaBehaviour(this, "onCheckIn",
            NotificationFrequency.TRANSACTION_COMMIT));

    Assert.notNull(_storageService);
    Assert.notNull(_origins);

    super.afterPropertiesSet();
  }

  @Override
  public void onCheckIn(final NodeRef nodeRef) {
    runSafe(new DefaultRunSafe(nodeRef) {

      @Override
      public void execute() {
        doCheckIn(nodeRef);
      }

    });
  }

  private void doCheckIn(NodeRef nodeRef) {
    if (!_nodeService.exists(nodeRef)) {
      return;
    }

    if (_nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
      return;
    }

    if (!_nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PUBLISHED)) {
      return;
    }

    final String origin = _serviceUtils.getStringValue(_nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_ORIGIN));

    // if no origin found, just exit
    if (StringUtils.isBlank(origin)) {
      return;
    }

    // if not in origin list, then exit
    if (!_origins.contains(origin)) {
      return;
    }

    // if we've come this far, it's a document being updated and it's from a
    // configured origin, so create a PDF rendition
    _storageService.createPdfRendition(nodeRef);
  }

}
