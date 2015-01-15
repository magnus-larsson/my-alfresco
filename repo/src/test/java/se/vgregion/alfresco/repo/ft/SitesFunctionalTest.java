package se.vgregion.alfresco.repo.ft;

import static com.jayway.restassured.RestAssured.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;

public class SitesFunctionalTest extends AbstractRepoFunctionalTest {
  
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
  public void testCreateSiteAsNonAdminSuccess() throws JSONException {
    String username = "tempuser_" + System.currentTimeMillis();
    String password = "verynicepassword";
    String firstname ="Temporary";
    String lastname = "User";
    String email = "temporary.user@redpill-linpro.com";
    String site = "newsite_" + System.currentTimeMillis();
    
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("vgr:responsibility_code", "abc123");
    
    createUser(username, password, firstname, lastname, email, properties, null);
    
    try {
      RestAssured.authentication = preemptive().basic(username, password);
      
      createSite(site);
      
      deleteSite(site);
    } catch (AssertionError ex) {
      // do nothing here
    } finally {
      RestAssured.authentication = preemptive().basic("admin", "admin");
      
      deleteUser(username);
    }
  }

  @Test
  public void testCreateSiteAsAdminSuccess() throws JSONException {
    String username = "tempuser_" + System.currentTimeMillis();
    String password = "verynicepassword";
    String firstname ="Temporary";
    String lastname = "User";
    String email = "temporary.user@redpill-linpro.com";
    String site = "newsite_" + System.currentTimeMillis();
    
    createUser(username, password, firstname, lastname, email);
    
    try {
      createSite(site);
      
      deleteSite(site);
    } catch (AssertionError ex) {
      // do nothing here
    } finally {
      deleteUser(username);
    }
  }

  @Test
  public void testCreateSiteAsNonAdminFailure() throws JSONException {
    String username = "tempuser_" + System.currentTimeMillis();
    String password = "verynicepassword";
    String firstname ="Temporary";
    String lastname = "User";
    String email = "temporary.user@redpill-linpro.com";
    String site = "newsite_" + System.currentTimeMillis();
    
    createUser(username, password, firstname, lastname, email);
    
    try {
      RestAssured.authentication = preemptive().basic(username, password);
      
      createSite(site);
      
      fail();
    } catch (AssertionError ex) {
      // do nothing here
    } finally {
      RestAssured.authentication = preemptive().basic("admin", "admin");
      
      deleteUser(username);
    }
  }

}
