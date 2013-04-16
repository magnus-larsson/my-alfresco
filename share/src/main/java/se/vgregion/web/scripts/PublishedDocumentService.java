package se.vgregion.web.scripts;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.processor.BaseProcessorExtension;

@SuppressWarnings("serial")
public class PublishedDocumentService extends BaseProcessorExtension implements Serializable {

  public PublishedStatus getPublishedStatus(String nodeRef) {
    boolean published = isPublished(nodeRef);
    boolean hasbeen = hasBeenPublished(nodeRef);
    boolean future = willBePublished(nodeRef);
    boolean publishedold = oldIsPublished(nodeRef);

    PublishedStatus status = new PublishedStatus();

    status.future = future;
    status.hasbeen = hasbeen;
    status.published = published;
    status.publishedold = publishedold;

    return status;
  }

  /**
   * Returns true if the current document version is the latest one published.
   * 
   * @param nodeRef
   * @return
   */
  private boolean isPublished(String nodeRef) {
    foo();

    return true;
  }

  /**
   * Returns true if an older version of the document is currently published.
   * 
   * @param nodeRef
   * @return
   */
  private boolean oldIsPublished(String nodeRef) {
    return true;
  }

  /**
   * Returns true if the document will be published in the future.
   * 
   * @param nodeRef
   * @return
   */
  private boolean willBePublished(String nodeRef) {
    return true;
  }

  /**
   * Returns true if the document at some point as been published but any
   * version is no longer published.
   * 
   * @param nodeRef
   * @return
   */
  private boolean hasBeenPublished(String nodeRef) {
    return true;
  }

  private void foo() {
    RequestContext requestContext = ThreadLocalRequestContext.getRequestContext();

    String userId = requestContext.getUserId();

    if (userId == null || AuthenticationUtil.isGuest(userId)) {
      throw new AlfrescoRuntimeException("User ID must exist and cannot be guest.");
    }

    try {
      Connector conn = requestContext.getServiceRegistry().getConnectorService().getConnector("alfresco");

      Response response = conn.call("/enterprise/sync/config");

      if (response.getStatus().getCode() == Status.STATUS_OK) {
      } else {
        throw new AlfrescoRuntimeException("Unable to retrieve Sync Mode configuration from Alfresco: " + response.getStatus().getCode());
      }
    } catch (ConnectorServiceException ex) {
      throw new AlfrescoRuntimeException("Unable to retrieve Sync Mode configuration from Alfresco: " + ex.getMessage(), ex);
    }

  }

}
