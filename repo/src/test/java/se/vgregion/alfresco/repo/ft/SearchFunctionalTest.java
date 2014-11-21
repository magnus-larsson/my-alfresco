package se.vgregion.alfresco.repo.ft;

import static com.jayway.restassured.RestAssured.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;

public class SearchFunctionalTest extends AbstractVgrRepoFunctionalTest {
  
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
  public void testSearch() throws JSONException {
    String site = "testsite_" + System.currentTimeMillis();
    String filename = "test.pdf";
    String title = "test";
    
    createSite(site);
    
    try {
      String nodeRef = uploadDocument(filename, site);
      
      Map<String, String> properties = new HashMap<String, String>();
      properties.put("cm:title", title);
      updateDocument(nodeRef, properties);
      
      String term = title;
      
      JSONObject result = search(term, null, null, null, null, null, null);
      
      JSONArray items = result.getJSONArray("items");
      
      JSONObject item = items.getJSONObject(0);
      
      JSONObject permissions = item.getJSONObject("permissions");
      
      assertTrue(permissions.getString("published-before").equals("false"));
    } finally {
      deleteSite(site);
    }
  }

}
