package se.vgregion.alfresco.repo.it;

import static com.jayway.restassured.RestAssured.*;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractVgrRepoFunctionalTest extends AbstractRepoFunctionalTest {
  
  public String publishDocument(String nodeRef) {
    try {
      RestAssured.requestContentType(ContentType.JSON);
      RestAssured.responseContentType(ContentType.JSON);

      JSONArray nodes = new JSONArray();
      nodes.put(nodeRef);

      JSONObject json = new JSONObject();
      json.put("nodes", nodes);
      json.put("sites", new JSONArray());
      json.put("action", "publish");
    
      Response response = given()
        .baseUri(BASE_URI)
        .request().body(json.toString())
        .expect().statusCode(200)
        .when().post("/vgr/publishtostorage");

      response = given()
        .baseUri(BASE_URI)
        .pathParam("nodeRef", nodeRef)
        .when().get("/api/metadata?nodeRef={nodeRef}&shortQNames=true");
      
      Map<String, String> properties = response.path("properties");
      
      return properties.get("vgr:dc.identifier.documentid");
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public void updateDocument(String nodeRef, Map<String, String> properties) {
    try {
      RestAssured.requestContentType(ContentType.JSON);
      RestAssured.responseContentType(ContentType.JSON);
      
      JSONObject json = new JSONObject();
      json.put("properties", properties);
      
      String id = StringUtils.replace(nodeRef, "workspace://SpacesStore/", "");

      Response response = given()
        .baseUri(BASE_URI)
        .pathParam("store_type", "workspace")
        .pathParam("store_id", "SpacesStore")
        .pathParam("id", id)
        .request().body(json.toString())
        .expect().statusCode(200)
        .when().post("/api/metadata/node/{store_type}/{store_id}/{id}");
      
      response.prettyPrint();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
