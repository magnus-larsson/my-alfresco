package se.vgregion.web.scripts;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.json.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.processor.BaseProcessorExtension;

@SuppressWarnings("serial")
public class PublishedDocumentService extends BaseProcessorExtension implements Serializable {

  public PublishedStatus getPublishedStatus(String nodeRef) {
    PublishedStatus status = queryPublishedStatus(nodeRef);

    return status;
  }

  private PublishedStatus queryPublishedStatus(String nodeRef) {
    RequestContext requestContext = ThreadLocalRequestContext.getRequestContext();

    String userId = requestContext.getUserId();

    if (userId == null || AuthenticationUtil.isGuest(userId)) {
      throw new AlfrescoRuntimeException("User ID must exist and cannot be guest.");
    }

    try {
      Connector connector = requestContext.getServiceRegistry().getConnectorService().getConnector("alfresco", userId, ServletUtil.getSession());

      Response response = connector.call("/vgr/publishstatus?nodeRef=" + nodeRef);

      if (response.getStatus().getCode() == Status.STATUS_OK) {
        final JSONObject json = new JSONObject(response.getResponse());

        PublishedStatus status = new PublishedStatus();

        status.published = json.getBoolean("published");
        status.future = json.getBoolean("future");
        status.hasbeen = json.getBoolean("hasbeen");
        status.publishedold = json.getBoolean("publishedold");

        return status;
      } else {
        throw new AlfrescoRuntimeException("Unable to retrieve publish status for node " + nodeRef + " from Alfresco: " + response.getStatus().getCode() + "; " + response.getStatus().getCodeDescription());
      }
    } catch (Exception ex) {
      throw new AlfrescoRuntimeException("Unable to retrieve publish status for node " + nodeRef + " from Alfresco: " + ex.getMessage(), ex);
    }
  }

}
