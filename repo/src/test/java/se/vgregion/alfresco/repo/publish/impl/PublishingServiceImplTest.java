package se.vgregion.alfresco.repo.publish.impl;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeService;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class PublishingServiceImplTest {

  private PublishingServiceImpl _publishingService;

  private NodeService _nodeService;

  private ServiceUtils _serviceUtils;

  @Rule
  public JUnitRuleMockery _context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Before
  public void setup() throws Exception {
    _nodeService = _context.mock(NodeService.class);

    _serviceUtils = _context.mock(ServiceUtils.class);

    _publishingService = new PublishingServiceImpl();

    _publishingService.setNodeService(_nodeService);

    _publishingService.setServiceUtils(_serviceUtils);

    _publishingService.afterPropertiesSet();
  }

  @Test
  public void findPublishedDocuemntsQueryWithFromAndTo() throws ParseException {
    String expectedQuery = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND vgr:dc\\.date\\.availablefrom:[MIN TO \"2013-04-10T12:00:00\"] AND (ISNULL:\"vgr:dc.date.availableto\" OR vgr:dc\\.date\\.availableto:[\"2013-04-10T12:00:00\" TO MAX]) AND ISNULL:\"vgr:pushed-for-publish\" AND cm:modified:[\"2013-04-10T11:30:00\" TO \"2013-04-10T12:00:00\"]";

    Date availableDate = parseDate("2013-04-10T12:00:00");
    Date modifiedFrom = parseDate("2013-04-10T11:30:00");
    Date modifiedTo = parseDate("2013-04-10T12:00:00");

    String actualQuery = _publishingService.findPublishedDocumentsQuery(availableDate, modifiedFrom, modifiedTo);

    assertEquals(expectedQuery, actualQuery);
  }

  @Test
  public void findPublishedDocuemntsQueryWithFrom() throws ParseException {
    String expectedQuery = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND vgr:dc\\.date\\.availablefrom:[MIN TO \"2013-04-10T12:00:00\"] AND (ISNULL:\"vgr:dc.date.availableto\" OR vgr:dc\\.date\\.availableto:[\"2013-04-10T12:00:00\" TO MAX]) AND ISNULL:\"vgr:pushed-for-publish\"";

    Date availableDate = parseDate("2013-04-10T12:00:00");
    Date modifiedFrom = parseDate("2013-04-10T11:30:00");
    Date modifiedTo = null;

    String actualQuery = _publishingService.findPublishedDocumentsQuery(availableDate, modifiedFrom, modifiedTo);

    assertEquals(expectedQuery, actualQuery);
  }

  @Test
  public void findPublishedDocuemntsQueryWithTo() throws ParseException {
    String expectedQuery = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND vgr:dc\\.date\\.availablefrom:[MIN TO \"2013-04-10T12:00:00\"] AND (ISNULL:\"vgr:dc.date.availableto\" OR vgr:dc\\.date\\.availableto:[\"2013-04-10T12:00:00\" TO MAX]) AND ISNULL:\"vgr:pushed-for-publish\"";

    Date availableDate = parseDate("2013-04-10T12:00:00");
    Date modifiedTo = parseDate("2013-04-10T11:30:00");
    Date modifiedFrom = null;

    String actualQuery = _publishingService.findPublishedDocumentsQuery(availableDate, modifiedFrom, modifiedTo);

    assertEquals(expectedQuery, actualQuery);
  }

  @Test
  public void findPublishedDocuemntsQuery() throws ParseException {
    String expectedQuery = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND vgr:dc\\.date\\.availablefrom:[MIN TO \"2013-04-10T12:00:00\"] AND (ISNULL:\"vgr:dc.date.availableto\" OR vgr:dc\\.date\\.availableto:[\"2013-04-10T12:00:00\" TO MAX]) AND ISNULL:\"vgr:pushed-for-publish\"";

    Date availableDate = parseDate("2013-04-10T12:00:00");
    Date modifiedTo = null;
    Date modifiedFrom = null;

    String actualQuery = _publishingService.findPublishedDocumentsQuery(availableDate, modifiedFrom, modifiedTo);

    assertEquals(expectedQuery, actualQuery);
  }

  @Test
  public void findPublishedDocuemntsQuery2() throws ParseException {
    String expectedQuery = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND vgr:dc\\.date\\.availablefrom:[MIN TO \"2013-04-10T12:00:00\"] AND (ISNULL:\"vgr:dc.date.availableto\" OR vgr:dc\\.date\\.availableto:[\"2013-04-10T12:00:00\" TO MAX]) AND ISNULL:\"vgr:pushed-for-publish\"";

    Date availableDate = parseDate("2013-04-10T12:00:00");

    String actualQuery = _publishingService.findPublishedDocumentsQuery(availableDate);

    assertEquals(expectedQuery, actualQuery);
  }

  private Date parseDate(String date) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    return sdf.parse(date);
  }

}
