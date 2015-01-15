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

public class ConfirmFunctionalTest extends AbstractVgrRepoFunctionalTest {

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
  public void testConfirm() throws JSONException, InterruptedException, IOException {
    String site = "testite_" + System.currentTimeMillis();
    String filename = "test.pdf";
    boolean majorVersion = false;
    
    createSite(site);
    
    try {
      String nodeRef = uploadDocument(filename, site, "vgr:document");

      JSONObject checkedoutJson = checkoutDocument(nodeRef).getJSONArray("results").getJSONObject(0);
      
      String workingCopyNodeRef = checkedoutJson.getString("nodeRef");
      
      String downloadUrl = checkedoutJson.getString("downloadUrl");
      
      InputStream workingCopy = downloadDocument(downloadUrl, "application/pdf");
      
      JSONObject json = preUploadDocument(workingCopy, filename, workingCopyNodeRef, majorVersion);
      
      String status = json.getString("status");
      String tempFilename = json.getString("tempFilename");
      
      String confirmedNodeRef = confirmDocument(status, filename, tempFilename, workingCopyNodeRef, majorVersion);
      
      assertDocumentNotExist(workingCopyNodeRef);
      
      assertEquals(nodeRef, confirmedNodeRef);      
    } catch (Throwable ex) {
      ex.printStackTrace();
    } finally {
      deleteSite(site);
    }
  }

}
