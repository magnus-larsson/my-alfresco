package se.vgregion.alfresco.repo.ft;

import static com.jayway.restassured.RestAssured.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;

public class UploadFunctionalTest extends AbstractRepoFunctionalTest {
  
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
  public void testUpload() {
    String site = "testuser_" + System.currentTimeMillis();
    String filename = "test.pdf";
    
    createSite(site);
    
    try {
      uploadDocument(filename, site, "vgr:document");
    } finally {
      deleteSite(site);
    }
  }

}
