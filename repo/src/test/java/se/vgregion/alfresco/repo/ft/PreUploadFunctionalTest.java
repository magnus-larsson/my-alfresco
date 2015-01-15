package se.vgregion.alfresco.repo.ft;

import static com.jayway.restassured.RestAssured.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;

public class PreUploadFunctionalTest extends AbstractVgrRepoFunctionalTest {

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
  public void testPreUpload() throws JSONException, InterruptedException, IOException {
    String site = "testite_" + System.currentTimeMillis();
    String filename = "test.pdf";
    boolean majorVersion = false;
    
    createSite(site);
    
    try {
      String nodeRef = uploadDocument(filename, site, "vgr:document");

      JSONObject checkedoutJson = checkoutDocument(nodeRef).getJSONArray("results").getJSONObject(0);
      
      String workingCopyNodeRef = checkedoutJson.getString("nodeRef");
      
      assertHasAspect(workingCopyNodeRef, "cm:workingcopy");
      assertHasAspect(nodeRef, "cm:checkedOut");      
      
      String downloadUrl = checkedoutJson.getString("downloadUrl");
      
      System.out.println(downloadUrl);
      
      InputStream workingCopy = downloadDocument(downloadUrl, "application/pdf");
      
      JSONObject json = preUploadDocument(workingCopy, filename, workingCopyNodeRef, majorVersion);
      
      cancelCheckoutDocument(workingCopyNodeRef);
      
      assertDocumentNotExist(workingCopyNodeRef);
      
      String status = json.getString("status");
      String tempFilename = json.getString("tempFilename");
      String description = json.getString("description");
      
      assertEquals(filename, json.getString("filename"));
      assertNotNull(tempFilename);
      assertEquals(majorVersion, json.getBoolean("majorVersion"));
      assertEquals("match", status);
      assertEquals("", description);
    } catch (Throwable ex) {
      ex.printStackTrace();
    } finally {
      deleteSite(site);
    }
  }

}
