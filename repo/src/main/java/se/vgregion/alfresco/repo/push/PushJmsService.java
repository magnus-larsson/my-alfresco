package se.vgregion.alfresco.repo.push;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Service interface for sending push messages to Mule
 * 
 * @author Marcus Svensson <marcus.svensson (at) redpill-linpro.com>
 * @author Niklas Ekman <niklas.ekman (at) redpill-linpro.com>
 */
public interface PushJmsService {
  
  /**
   * Push documents to Mule JMS queue for status updates
   * @param nodeRefs - List of noderefs to push
   * @param property - The QName used to indicate if this is a Publish or Unpublish event
   * @return
   */
  boolean pushToJms(List<NodeRef> nodeRefs, QName property);
  
  /**
   * Push documents to Mule JMS queue for status updates
   * @param nodeRef - nodeRef to push
   * @param property - The QName used to indicate if this is a Publish or Unpublish event
   * @return
   */
  boolean pushToJms(NodeRef nodeRef, QName property);
}
