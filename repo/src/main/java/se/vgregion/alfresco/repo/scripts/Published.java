package se.vgregion.alfresco.repo.scripts;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import se.vgregion.alfresco.repo.publish.PublishingService;
import se.vgregion.alfresco.repo.push.PuSHAtomFeedUtil;

public class Published extends AbstractWebScript implements InitializingBean {

  private PuSHAtomFeedUtil _puSHAtomFeedUtil;

  private PublishingService _publishingService;

  @Override
  public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
    Date from = parseFromDate(request.getParameter("from"));
    Date to = parseToDate(request.getParameter("to"));
    String nodeRef = request.getParameter("nodeRef");

    response.setContentEncoding("UTF-8");
    response.setContentType("text/xml");
    OutputStream outputStream = response.getOutputStream();

    try {
      if (StringUtils.isNotBlank(nodeRef)) {
        streamAtomFeed(outputStream, new NodeRef(nodeRef));
      } else {
        streamAtomFeed(outputStream, from, to);
      }
    } finally {
      IOUtils.closeQuietly(outputStream);
    }
  }

  private void streamAtomFeed(OutputStream outputStream, Date from, Date to) {
    _puSHAtomFeedUtil.createDocumentFeed(from, to, outputStream, false);
  }

  private void streamAtomFeed(OutputStream outputStream, NodeRef nodeRef) {
    String feed;

    if (_publishingService.isPublished(nodeRef)) {
      feed = _puSHAtomFeedUtil.createPublishDocumentFeed(nodeRef);
    } else {
      feed = _puSHAtomFeedUtil.createUnPublishDocumentFeed(nodeRef);
    }

    try {
      IOUtils.write(feed, outputStream);
    } catch (IOException ex) {
      throw new AlfrescoRuntimeException(ex.getMessage(), ex);
    }
  }

  private Date parseFromDate(String from) {
    Date date = parseDate(from);

    if (date == null) {
      date = DateTime.now().minusMinutes(30).toDate();
    }

    return date;
  }

  private Date parseToDate(String to) {
    Date date = parseDate(to);

    if (date == null) {
      date = new Date();
    }

    return date;
  }

  private Date parseDate(String date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    try {
      return sdf.parse(date);
    } catch (Exception ex) {
      return null;
    }
  }

  public void setPushAtomFeedUtil(PuSHAtomFeedUtil puSHAtomFeedUtil) {
    _puSHAtomFeedUtil = puSHAtomFeedUtil;
  }

  public void setPublishingService(PublishingService publishingService) {
    _publishingService = publishingService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("puSHAtomFeedUtil", _puSHAtomFeedUtil);
    ParameterCheck.mandatory("publishingService", _publishingService);
  }

}
