package se.vgregion.alfresco.repo.scripts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import se.vgregion.alfresco.repo.publish.PublishingService;
import se.vgregion.alfresco.repo.push.PuSHAtomFeedUtil;

public class PublishedTest {

  Published _published;

  PublishingService _publishingService;

  PuSHAtomFeedUtil _puSHAtomFeedUtil;

  WebScriptRequest _request;
  WebScriptResponse _response;

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Before
  public void setup() throws Exception {
    _publishingService = context.mock(PublishingService.class);

    _puSHAtomFeedUtil = context.mock(PuSHAtomFeedUtil.class);

    _request = context.mock(WebScriptRequest.class);
    _response = context.mock(WebScriptResponse.class);

    _published = new Published();
    _published.setPublishingService(_publishingService);
    _published.setPushAtomFeedUtil(_puSHAtomFeedUtil);
    _published.afterPropertiesSet();
  }

  @Test
  public void testValidFromTo() throws IOException {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    final String sFrom = "2013-04-12T12:12:12";
    final Date from = parseDate(sFrom);
    final String sTo = "2013-04-12T12:42:12";
    final Date to = parseDate(sTo);

    context.checking(new Expectations() {
      {
        allowing(_request).getParameter("from");
        will(returnValue(sFrom));
        allowing(_request).getParameter("to");
        will(returnValue(sTo));
        allowing(_request).getParameter("nodeRef");
        will(returnValue(null));
        allowing(_request).getParameter("maxItems");
        will(returnValue(null));
        allowing(_request).getParameter("skipCount");
        will(returnValue(null));
        allowing(_response).getOutputStream();
        will(returnValue(outputStream));
        allowing(_response).setContentEncoding("UTF-8");
        allowing(_response).setContentType("text/xml");
        allowing(_puSHAtomFeedUtil).createDocumentFeed(from, to, outputStream, false, null, null);
      }
    });

    _published.execute(_request, _response);
  }

  @Test
  public void testInValidFromTo() throws IOException {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    final String sFrom = "2013-04-12";
    final String sTo = "2013-04-12";

    context.checking(new Expectations() {
      {
        allowing(_request).getParameter("from");
        will(returnValue(sFrom));
        allowing(_request).getParameter("to");
        will(returnValue(sTo));
        allowing(_request).getParameter("nodeRef");
        will(returnValue(null));
        allowing(_request).getParameter("maxItems");
        will(returnValue("1"));
        allowing(_request).getParameter("skipCount");
        will(returnValue("1"));
        allowing(_response).getOutputStream();
        will(returnValue(outputStream));
        allowing(_response).setContentEncoding("UTF-8");
        allowing(_response).setContentType("text/xml");
        allowing(_puSHAtomFeedUtil).createDocumentFeed(with(any(Date.class)), with(any(Date.class)), with(any(OutputStream.class)), with(any(Boolean.class)), with(any(Integer.class)), with(any(Integer.class)));
      }
    });

    _published.execute(_request, _response);
  }

  @Test
  public void testPublishedNodeRef() throws IOException {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    final String sFrom = "";
    final String sTo = "";
    final String sNodeRef = "workspace://SpacesStore/kalle";
    final NodeRef nodeRef = new NodeRef(sNodeRef);

    context.checking(new Expectations() {
      {
        allowing(_request).getParameter("from");
        will(returnValue(sFrom));
        allowing(_request).getParameter("to");
        will(returnValue(sTo));
        allowing(_request).getParameter("nodeRef");
        will(returnValue(sNodeRef));
        allowing(_response).getOutputStream();
        will(returnValue(outputStream));
        allowing(_response).setContentType("text/xml");
        allowing(_response).setContentEncoding("UTF-8");
        allowing(_publishingService).isPublished(nodeRef);
        will(returnValue(true));
        allowing(_puSHAtomFeedUtil).createPublishDocumentFeed(nodeRef);
      }
    });

    _published.execute(_request, _response);
  }

  @Test
  public void testUnpublishedNodeRef() throws IOException {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    final String sFrom = "";
    final String sTo = "";
    final String sNodeRef = "workspace://SpacesStore/kalle";
    final NodeRef nodeRef = new NodeRef(sNodeRef);

    context.checking(new Expectations() {
      {
        allowing(_request).getParameter("from");
        will(returnValue(sFrom));
        allowing(_request).getParameter("to");
        will(returnValue(sTo));
        allowing(_request).getParameter("nodeRef");
        will(returnValue(sNodeRef));
        allowing(_response).getOutputStream();
        will(returnValue(outputStream));
        allowing(_response).setContentType("text/xml");
        allowing(_response).setContentEncoding("UTF-8");
        allowing(_publishingService).isPublished(nodeRef);
        will(returnValue(false));
        allowing(_puSHAtomFeedUtil).createUnPublishDocumentFeed(nodeRef);
      }
    });

    _published.execute(_request, _response);
  }

  private Date parseDate(String date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    try {
      return sdf.parse(date);
    } catch (ParseException ex) {
      return null;
    }
  }

}
