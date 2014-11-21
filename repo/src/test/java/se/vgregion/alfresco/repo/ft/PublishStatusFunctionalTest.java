package se.vgregion.alfresco.repo.ft;

import static com.jayway.restassured.RestAssured.preemptive;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;

public class PublishStatusFunctionalTest extends AbstractVgrRepoFunctionalTest {

  @Before
  public void setUp() {
    RestAssured.defaultParser = Parser.JSON;
    RestAssured.authentication = preemptive().basic("admin", "admin");
    // RestAssured.proxy("localhost", 8888);
  }

  @After
  public void tearDown() {
    RestAssured.reset();
  }

  @Test
  public void testStatus() throws Exception {
    String site = "testsite_" + System.currentTimeMillis();

    createSite(site);
    try {
      String filename = "test.pdf";

      String nodeRef = uploadDocument(filename, site);

      JSONObject publishStatus = getPublishStatus(nodeRef);

      assertEquals("NOT_PUBLISHED", publishStatus.getString("result"));
    } finally {
      deleteSite(site);
    }
  }

  @Test
  public void testStatusFoo() throws Exception {
    String site = "testsite_" + System.currentTimeMillis();

    createSite(site);
    try {
      String filename = "test.pdf";
      String title = "This is a very fine title  åäöÅÄÖ";
      String dcTypeRecord = "Foobar Type Record";
      String dcTypeRecordId = "foobar-type-record-id";

      Map<String, String> properties = new HashMap<String, String>();
      properties.put("vgr:dc.publisher.project-assignment", "Foobar");
      properties.put("vgr:dc.type.record", dcTypeRecord);
      properties.put("vgr:dc.type.record.id", dcTypeRecordId);
      properties.put("vgr:dc.title", title);

      String nodeRef = uploadDocument(filename, site);

      updateDocument(nodeRef, properties);

      nodeRef = publishDocument(nodeRef, false);

      String downloadUrl = "vgr/storage/node/content/workspace/SpacesStore/" + StringUtils.replace(nodeRef, "workspace://SpacesStore/", "") + "/test.pdf?a=true&guest=true";

      InputStream inputStream = downloadDocument(downloadUrl, "application/pdf");

      PDDocument pdfDocument = PDDocument.load(inputStream);
      
      try {
        assertEquals(title, pdfDocument.getDocumentInformation().getTitle());
        assertEquals(dcTypeRecord, pdfDocument.getDocumentInformation().getCustomMetadataValue("DC.type.record"));
        assertEquals(dcTypeRecordId, pdfDocument.getDocumentInformation().getCustomMetadataValue("DC.type.record.id"));
      } finally {
        pdfDocument.close();
      }

      JSONObject publishStatus = getPublishStatus(nodeRef);

      assertEquals("PREVIOUS_VERSION_PUBLISHED", publishStatus.getString("result"));
    } finally {
      deleteSite(site);
    }
  }

}
