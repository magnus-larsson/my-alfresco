package se.vgregion.alfresco.repo.ft;

import static com.jayway.restassured.RestAssured.*;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractVgrRepoFunctionalTest extends AbstractRepoFunctionalTest {
  
  private static final Logger LOG = Logger.getLogger(AbstractVgrRepoFunctionalTest.class);

  protected String publishDocument(String nodeRef, boolean async) throws JSONException {
    RestAssured.requestContentType(ContentType.JSON);
    RestAssured.responseContentType(ContentType.JSON);

    JSONArray nodes = new JSONArray();
    nodes.put(nodeRef);

    JSONObject json = new JSONObject();
    json.put("nodes", nodes);
    json.put("sites", new JSONArray());
    json.put("action", "publish");
    json.put("async", false);

    Response response = given()
        .baseUri(getBaseUri())
        .request()
        .body(json.toString())
        // .expect().statusCode(200)
        .when().post("/vgr/publishtostorage");
    
    if (LOG.isDebugEnabled()) {
      response.prettyPrint();
    }

    response = given()
        .baseUri(getBaseUri())
        .pathParam("nodeRef", nodeRef)
        .when().get("/api/metadata?nodeRef={nodeRef}&shortQNames=true");

    if (LOG.isDebugEnabled()) {
      response.prettyPrint();
    }

    Map<String, String> properties = response.path("properties");

    return properties.get("vgr:dc.identifier.documentid");
  }

  protected JSONObject preUploadDocument(String filename, String nodeRef, boolean majorVersion) throws JSONException {
    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

    return preUploadDocument(inputStream, filename, nodeRef, majorVersion);
  }

  protected JSONObject preUploadDocument(InputStream inputStream, String filename, String nodeRef, boolean majorVersion) throws JSONException {
    RestAssured.requestContentType("multipart/form-data");
    RestAssured.responseContentType(ContentType.JSON);

    String updateNodeRef = null;
    String username = null;

    Response response = given()
        .baseUri(getBaseUri())
        .multiPart("filedata", filename, inputStream)
        .formParam("filename", filename)
        .formParam("destination", nodeRef)
        .formParam("majorVersion", majorVersion)
        .formParam("updateNodeRef", updateNodeRef)
        .formParam("username", username)
        .expect().statusCode(200)
        .when().post("/vgr/preupload");

    if (LOG.isDebugEnabled()) {
      response.prettyPrint();
    }

    return new JSONObject(response.asString());
  }

  public String confirmDocument(String status, String filename, String tempFilename, String nodeRef, boolean majorVersion) throws JSONException {
    RestAssured.requestContentType(ContentType.JSON);
    RestAssured.responseContentType(ContentType.JSON);
    
    JSONObject json = new JSONObject();
    
    json.put("status", status);
    json.put("tempFilename", tempFilename);
    json.put("filename", filename);
    json.put("nodeRef", nodeRef);
    json.put("updateNodeRef", nodeRef);
    json.put("majorVersion", majorVersion);
    json.put("description", "");
    
    Response response = given()
        .baseUri(getBaseUri())
        .body(json.toString())
        .expect().statusCode(200)
        .when().post("/vgr/preupload/confirm");

    if (LOG.isDebugEnabled()) {
      response.prettyPrint();
    }

    return response.path("nodeRef");
  }
  
  public JSONObject getPublishStatus(String nodeRef) throws JSONException {
    RestAssured.requestContentType(ContentType.JSON);
    RestAssured.responseContentType(ContentType.JSON);
    
    String id = StringUtils.replace(nodeRef, "workspace://SpacesStore/", "");
    
    Response response = given()
        .baseUri(getBaseUri())
        .pathParam("store_type", "workspace")
        .pathParam("store_id", "SpacesStore")
        .pathParam("node_id", id)
        .expect().statusCode(200)
        .when().get("/vgr/publishstatus/{store_type}/{store_id}/{node_id}");
    
    if (LOG.isDebugEnabled()) {
      response.prettyPrint();
    }

    return new JSONObject(response.asString());
  }
  
  @Override
  protected String getBaseUri() {
    return "https://alfresco.vgregion.se/alfresco/service";
  }

}
