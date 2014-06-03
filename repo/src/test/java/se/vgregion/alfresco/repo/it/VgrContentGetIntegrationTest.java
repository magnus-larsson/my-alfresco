package se.vgregion.alfresco.repo.it;

import static com.jayway.restassured.RestAssured.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

public class VgrContentGetIntegrationTest extends AbstractVgrRepoFunctionalTest {
  
  @Before
  public void setUp() {
    RestAssured.authentication = preemptive().basic("admin", "admin");
  }

  @After
  public void tearDown() {
    RestAssured.reset();
  }

  @Test
  public void test() {
    String site = "site_name_" + System.currentTimeMillis();
    String filename = "apelonserviceimpl_testgetkeywords.pdf";

    createSite(site);

    try {
      String fileNodeRef = uploadDocument(filename, site);
      
      given()
          .baseUri(BASE_URI)
          .pathParam("id", "")
          .expect().statusCode(200)
          .when().get("/vgr/storage/node/content/{id}?a={attach?}&amp;streamId={streamId?}&amp;version={version?}&amp;native={native?}");
    } finally {
      deleteSite(site);
    }
  }

}
