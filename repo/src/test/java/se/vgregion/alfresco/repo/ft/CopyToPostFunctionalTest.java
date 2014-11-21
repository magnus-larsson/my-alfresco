package se.vgregion.alfresco.repo.ft;

import static com.jayway.restassured.RestAssured.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

public class CopyToPostFunctionalTest extends AbstractRepoFunctionalTest {
  
  @BeforeClass
  public static void setup() {
    
  }
  
  @Test
  public void foo() {
    /*
    RestAssured.requestContentType(ContentType.JSON);
    RestAssured.responseContentType(ContentType.JSON);
    
    given()
      .baseUri(BASE_URI)
      .pathParameter("source", zipFileNodeRef)
      .pathParameter("target", documentLibraryNodeRef)
      .pathParameter("async", async ? "true" : "false")
      .expect()
      .contentType(ContentType.JSON).and().statusCode(200)
      .when().post("/org/redpill/unzip?source={source}&target={target}&async={async}");
      */
  }

}
