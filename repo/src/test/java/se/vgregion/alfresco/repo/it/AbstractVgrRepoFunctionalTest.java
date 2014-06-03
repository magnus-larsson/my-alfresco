package se.vgregion.alfresco.repo.it;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

public abstract class AbstractVgrRepoFunctionalTest extends AbstractRepoFunctionalTest {
  
  public void publishDocument(String nodeRef) {
    try {
      RestAssured.requestContentType(ContentType.JSON);
      RestAssured.responseContentType(ContentType.JSON);

      JSONArray nodes = new JSONArray();
      nodes.put(nodeRef);

      JSONObject json = new JSONObject();
      json.put("nodes", nodes);
      json.put("action", "publish");
    
      given()
        .baseUri(BASE_URI)
        .request().body(json.toString())
        .when().post("/vgr/publishtostorage");
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
