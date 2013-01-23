package se.vgregion.alfresco.repo.web.scripts.content;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @overridden projects/remote-api/source/java/org/alfresco/repo/web/scripts/content/ContentGet.java
 */
public class ContentGet extends org.alfresco.repo.web.scripts.content.ContentGet {

  private static final Log LOG = LogFactory.getLog(ContentGet.class);

  /**
   * format definied by RFC 822, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3
   */
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

  private ServiceUtils _serviceUtils;

  public void setServiceUtils(ServiceUtils serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  @Override
  protected void streamContent(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, QName propertyQName, boolean attach, String attachFileName) throws IOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrieving content from node ref " + nodeRef.toString() + " (property: " + propertyQName.toString() + ") (attach: " + attach + ")");
    }

    // for some reason, attachFileName is not set and therefor we need get the filename here
    final String filename = extractFilename(nodeRef);

    // check If-Modified-Since header and set Last-Modified header as appropriate
    Date modified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);

    if (modified != null) {
      long modifiedSince = -1;

      String modifiedSinceStr = req.getHeader("If-Modified-Since");

      if (modifiedSinceStr != null) {
        try {
          modifiedSince = dateFormat.parse(modifiedSinceStr).getTime();
        } catch (Throwable e) {
          if (LOG.isInfoEnabled()) {
            LOG.info("Browser sent badly-formatted If-Modified-Since header: " + modifiedSinceStr);
          }
        }

        if (modifiedSince > 0L) {
          // round the date to the ignore millisecond value which is not supplied by header
          long modDate = (modified.getTime() / 1000L) * 1000L;

          if (modDate <= modifiedSince) {
            // this is an addition to the original file, as this header needs to be present for IE to work better
            setAttachment(res, attach, filename);

            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

            return;
          }
        }
      }
    }

    // get the content reader
    ContentReader reader = contentService.getReader(nodeRef, propertyQName);

    if (reader == null || !reader.exists()) {
      throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to locate content for node ref " + nodeRef + " (property: " + propertyQName.toString() + ")");
    }

    // Stream the content
    streamContentImpl(req, res, reader, attach, modified, modified == null ? null : String.valueOf(modified.getTime()), filename);
  }

  private String extractFilename(NodeRef nodeRef) {
    final String extension = _serviceUtils.getFileExtension(nodeRef);

    String filename = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

    filename = FilenameUtils.getBaseName(filename);

    return "\"" + filename + extension + "\"";
  }

}
