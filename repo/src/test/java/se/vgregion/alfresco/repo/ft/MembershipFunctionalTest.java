package se.vgregion.alfresco.repo.ft;

import static com.jayway.restassured.RestAssured.*;
import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;

public class MembershipFunctionalTest extends AbstractRepoFunctionalTest {

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
  public void checkInternalZone() throws JSONException {
    RestAssured.requestContentType(ContentType.JSON);
    RestAssured.responseContentType(ContentType.JSON);
    
    String site = "tempsite_" + System.currentTimeMillis();
    String username = "tempuser_" + System.currentTimeMillis();
    
    createSite(site);
    
    createUser(username, null, "Temporary", "User", "temporary.user@redpill-linpro.com");
    
    try {
      addSiteMembership(site, username, "SiteManager");
      
      Response response = given()
        .baseUri(BASE_URI)
        .pathParam("shortname", site)
        .expect().statusCode(200)
        .when().get("/api/sites/{shortname}/memberships?nf=" + username);
      
      JSONArray array = new JSONArray(response.body().asString());
      JSONObject jsonAuthority = (JSONObject) ((JSONObject) array.get(0)).get("authority");

      assertEquals(username, jsonAuthority.get("userName"));

      assertEquals("internal", jsonAuthority.get("zone"));
    } finally {
      deleteSite(site);
      deleteUser(username);
    }
  }

  @Test
  public void checkExternalZone() throws JSONException {
    // can't really test this yet as there's no REST API for adding a user to a zone as of today
  }

  @Test
  public void checkAdminZone() throws JSONException {
    RestAssured.requestContentType(ContentType.JSON);
    RestAssured.responseContentType(ContentType.JSON);
    
    String site = "tempsite_" + System.currentTimeMillis();
    
    createSite(site);
    
    try {
      addSiteMembership(site, "admin", "SiteManager");
      
      Response response = given()
        .baseUri(BASE_URI)
        .pathParam("shortname", site)
        .expect().statusCode(200)
        .when().get("/api/sites/{shortname}/memberships?nf=admin");
      
      JSONArray array = new JSONArray(response.body().asString());
      JSONObject jsonAuthority = (JSONObject) ((JSONObject) array.get(0)).get("authority");

      assertEquals("admin", jsonAuthority.get("userName"));

      assertEquals("admin", jsonAuthority.get("zone"));
    } finally {
      deleteSite(site);
    }
  }

}
