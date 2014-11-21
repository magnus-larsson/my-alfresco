package se.vgregion.alfresco.repo.ft;

import static com.jayway.restassured.RestAssured.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;

public class StorageContentGetFunctionalTest extends AbstractVgrRepoFunctionalTest {
  
  @Before
  public void setUp() {
    RestAssured.defaultParser = Parser.JSON;
    RestAssured.authentication = preemptive().basic("admin", "admin");
  }

  @After
  public void tearDown() {
    RestAssured.reset();
  }

  @Test
  public void test() throws JSONException {
    String site = "site_name_" + System.currentTimeMillis();
    String filename = "test.pdf";

    createSite(site);

    try {
      Map<String, String> properties = new HashMap<String, String>();
      properties.put("vgr:dc.publisher.project-assignment", "Foobar");
      properties.put("vgr:dc.type.record", "Foobar Type Record");
      properties.put("vgr:dc.type.record.id", "foobar-type-record-id");
      
      String nodeRef = uploadDocument(filename, site);
      
      updateDocument(nodeRef, properties);
      
      String publishedNodeRef = publishDocument(nodeRef, true);
      
      String publishedId = StringUtils.replace(publishedNodeRef, "workspace://SpacesStore/", "");
      
      RestAssured.requestContentType(ContentType.JSON);
      RestAssured.responseContentType("application/pdf");

      Response response = given()
        .baseUri(getBaseUri())
        .pathParam("store_type", "workspace")
        .pathParam("store_id", "SpacesStore")
        .pathParam("id", publishedId)
        .parameters("a", true, "native", false)
        .header("User-Agent", "MSIE")
        .expect().statusCode(200)
        .when().get("/vgr/storage/node/content/{store_type}/{store_id}/{id}");
      
      assertEquals("application/pdf;charset=UTF-8", response.contentType());
      
      String contentDisposition = response.getHeader("Content-Disposition");
      
      System.out.println(contentDisposition);

      assertEquals("attachment; filename=\"\"test.pdf\"\"; filename*=UTF-8''%22test.pdf%22", contentDisposition);
    } finally {
      deleteSite(site);
    }
  }


}
