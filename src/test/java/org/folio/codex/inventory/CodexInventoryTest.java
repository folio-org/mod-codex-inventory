package org.folio.codex.inventory;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.LinkedHashMap;
import java.util.Map;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Diagnostic;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CodexInventoryTest {

  static {
    System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, "io.vertx.core.logging.Log4jLogDelegateFactory");
  }

  private final int portInventory = 9030;
  private final int portCodex = 9031;
  private final Logger logger = LoggerFactory.getLogger("codex.inventory");
  private String failMap; // for non-null value signals provoked failure
  private String failInventory; // for non-null value signals provoked failure

  private final String ID1 = "e54b1f4d-7d05-4b1a-9368-3c36b75d8ac6";
  private final String ID2 = "e54b1f4d-7d05-4b1a-9368-3c36b75d8ac8";
  private final String ID_404 = "e54b1f4d-7d05-4b1a-9368-3c36b75d8ac7";

  Vertx vertx;

  public CodexInventoryTest() {
  }

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();

    // Register the context exception handler to catch assertThat
    vertx.exceptionHandler(context.exceptionHandler());

    setupMux(context, context.async());

  }

  private void setupMux(TestContext context, Async async) {
    JsonObject conf = new JsonObject();
    conf.put("http.port", portCodex);
    DeploymentOptions opt = new DeploymentOptions()
      .setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt,
      r -> {
        context.assertTrue(r.succeeded());
        setupInventory(context, async);
      });
  }

  private void setupInventory(TestContext context, Async async) {
    failMap = null;

    Router router = Router.router(vertx);
    router.get("/instance-storage/instances").handler(this::handlerGetByQuery);
    router.get("/instance-storage/instances/:id").handler(this::handlerGetById);
    router.get("/contributor-name-types").handler(this::handlerContributorNameTypes);
    router.get("/instance-types").handler(this::handlerInstanceTypes);
    router.get("/instance-formats").handler(this::handlerInstanceFormats);
    router.get("/identifier-types").handler(this::handlerIdentifierTypes);
    router.get("/locations").handler(this::handlerShelfLocations);

    HttpServerOptions so = new HttpServerOptions().setHandle100ContinueAutomatically(true);
    vertx.createHttpServer(so)
      .requestHandler(router::accept)
      .listen(
        portInventory,
        result -> {
          if (result.failed()) {
            context.fail(result.cause());
          }
          async.complete();
        }
      );
  }

  private final String[] records = {""
    + "  {\n"
    + "    \"id\" : \"" + ID1 + "\",\n"
    + "    \"source\" : \"Sample\",\n"
    + "    \"title\" : \"Transparent water\",\n"
    + "    \"alternativeTitles\" : [ \"alternative titles\" ],\n"
    + "    \"edition\" : \"1st edition\",\n"
    + "    \"series\" : [ \"first series\" ],\n"
    + "    \"identifiers\" : [ {\n"
    + "      \"value\" : \"ocn968777846\",\n"
    + "      \"identifierTypeId\" : \"5d164f4b-0b15-4e42-ae75-cfcf85318ad9\"\n"
    + "    }, {\n"
    + "      \"value\" : \"9786316800312\",\n"
    + "      \"identifierTypeId\" : \"8261054f-be78-422d-bd51-4ed9f33c3422\"\n"
    + "    }, {\n"
    + "      \"value\" : \"6316800312\",\n"
    + "      \"identifierTypeId\" : \"8261054f-be78-422d-bd51-4ed9f33c3422\"\n"
    + "    }, {\n"
    + "      \"value\" : \"OTA-1031 Otá Records\",\n"
    + "      \"identifierTypeId\" : \"b5d8cdc4-9441-487c-90cf-0c7ec97728eb\"\n"
    + "    }, {\n"
    + "      \"value\" : \"(OCoLC)968777846\",\n"
    + "      \"identifierTypeId\" : \"7e591197-f335-4afb-bc6d-a6d76ca3bace\"\n"
    + "    } ],\n"
    + "    \"contributors\" : [ {\n"
    + "      \"name\" : \"Sosa, Omar\",\n"
    + "      \"contributorTypeId\" : \"2b94c631-fca9-a892-c730-03ee529ffe2b\",\n"
    + "      \"contributorNameTypeId\" : \"2b94c631-fca9-a892-c730-03ee529ffe2a\",\n"
    + "      \"primary\" : true\n"
    + "    }, {\n"
    + "      \"name\" : \"Keita, Seckou, 1977-\",\n"
    + "      \"contributorTypeId\" : \"2b94c631-fca9-a892-c730-03ee529ffe2b\",\n"
    + "      \"contributorNameTypeId\" : \"2b94c631-fca9-a892-c730-03ee529ffe2a\",\n"
    + "      \"primary\" : false\n"
    + "    } ],\n"
    + "    \"subjects\" : [ \"World music.\", \"Jazz\" ],\n"
    + "    \"classifications\" : [ {\n"
    + "      \"classificationNumber\" : \"M1366.S67\",\n"
    + "      \"classificationTypeId\" : \"ce176ace-a53e-4b4d-aa89-725ed7b2edac\"\n"
    + "    } ],\n"
    + "    \"publication\" : [ {\n"
    + "      \"publisher\" : \"Otá Records, \",\n"
    + "      \"place\" : \"[Place of publication not identified]: \",\n"
    + "      \"dateOfPublication\" : \"[2017]\"\n"
    + "    } ],\n"
    + "    \"urls\" : [ ],\n"
    + "    \"instanceTypeId\" : \"2e48e713-17f3-7c13-09f8-23845bb210ac\",\n"
    + "    \"instanceFormatId\" : \"309c3a3d-d54c-4519-b978-2c5c2de78d95\",\n"
    + "    \"physicalDescriptions\" : [ \"1 audio disc: digital; 4 3/4 in.\" ],\n"
    + "    \"languages\" : [ \"und\" ],\n"
    + "    \"notes\" : [ \"Title from disc label.\", \"All compositions written by Omar Sosa and Seckou Keita, except tracks 6, 8 and 10 written by Omar Sosa.\", \"Produced by Steve Argüelles and Omar Sosa.\", \"Omar Sosa, grand piano, Fender Rhodes, sampler, microKorg, vocals ; Seckou Keita, kora, talking drum, djembe, sabar, vocals ; Wu Tong, sheng, bawu ; Mieko Miyazaki, koto ; Gustavo Ovalles, bata drums, culo'e puya, maracas, guataca, calabaza, clave ; E'Joung-Ju, geojungo ; Mosin Khan Kawa, nagadi ; Dominique Huchet, bird effects.\" ],\n"
    + "    \"metadata\" : {\n"
    + "      \"createdDate\" : \"2018-02-02T03:40:46.084+0000\",\n"
    + "      \"createdByUserId\" : \"1ad737b0-d847-11e6-bf26-cec0c932ce01\",\n"
    + "      \"updatedDate\" : \"2018-02-02T03:40:46.084+0000\",\n"
    + "      \"updatedByUserId\" : \"1ad737b0-d847-11e6-bf26-cec0c932ce01\"\n"
    + "    }\n"
    + "  }"
  };

  private void handlerGetByQuery(RoutingContext ctx) {
    final String query = ctx.request().getParam("query");
    ctx.request().endHandler(res -> {
      JsonArray instances = new JsonArray();
      JsonObject rec = new JsonObject(records[0]);
      if (failInventory != null) {
        if (failInventory.startsWith("-")) {
          rec.remove(failInventory.substring(1));
        }
        String[] v = failInventory.split("=");
        if (v.length == 2) {
          rec.put(v[0], v[1]);
        }
      }
      instances.add(rec);
      JsonObject j = new JsonObject();
      if (!"instances".equals(failInventory)) {
        j.put("instances", instances);
      }
      if (!"totalRecords".equals(failInventory)) {
        j.put("totalRecords", instances.size());
      }
      if (failInventory != null && failInventory.matches("^[0-9]+")) {
        ctx.response().setStatusCode(Integer.parseInt(failInventory));
        ctx.response().end();
      } else {
        ctx.response().headers().add("Content-Type", "application/json");
        ctx.response().setStatusCode(200);
        if ("badJson".equals(failInventory)) {
          ctx.response().end("{");
        } else {
          ctx.response().end(j.encodePrettily());
        }
      }
    });
    ctx.request().exceptionHandler(res -> {
      ctx.response().setStatusCode(500);
      ctx.response().end(res.getMessage());
    });
  }

  private void handlerGetById(RoutingContext ctx) {
    final String id = ctx.request().getParam("id");
    ctx.request().endHandler(res -> {
      if (ID1.equals(id)) {
        JsonObject rec = new JsonObject(records[0]);
        if (failInventory != null && failInventory.matches("^[0-9]+")) {
          ctx.response().setStatusCode(Integer.parseInt(failInventory));
          ctx.response().end();
        } else {
          ctx.response().headers().add("Content-Type", "application/json");
          ctx.response().setStatusCode(200);
          if ("badJson".equals(failInventory)) {
            ctx.response().end("{");
          } else {
            ctx.response().end(rec.encodePrettily());
          }
        }
      } else {
        ctx.response().setStatusCode(404);
        ctx.response().end("not found");
      }
    });
    ctx.request().exceptionHandler(res -> {
      ctx.response().setStatusCode(500);
      ctx.response().end(res.getMessage());
    });
  }

  private void handleTypeMaps(RoutingContext ctx, Map<String, String> maps, String n) {
    logger.info("failMap=" + failMap + " n=" + n);
    if (failMap != null) {
      if (failMap.equals(n) || failMap.equals("*")) {
        logger.info("failMap in action");
        ctx.request().endHandler(res -> {
          ctx.response().setStatusCode(500);
          ctx.response().end(failMap);
        });
        ctx.request().exceptionHandler(res -> {
          ctx.response().setStatusCode(500);
          ctx.response().end(res.getMessage());
        });
        return;
      }
    }
    final String limitStr = ctx.request().getParam("limit");
    final String offsetStr = ctx.request().getParam("offset");

    int limit = limitStr != null ? Integer.parseInt(limitStr) : 100;
    int offset = offsetStr != null ? Integer.parseInt(offsetStr) : 0;

    JsonArray a = new JsonArray();

    if (!"emptyArray".equals(failMap)) {
      int pos = 0;
      for (Map.Entry<String, String> entry : maps.entrySet()) {
        if (pos >= offset && pos < offset + limit) {
          JsonObject r = new JsonObject();
          r.put("id", entry.getKey());
          r.put("name", entry.getValue());
          if ("locations".equals(n)) { // simulate more elements for locations
            r.put("code", entry.getKey());
          }
          a.add(r);
        }
        pos++;
      }
    }
    JsonObject j = new JsonObject();
    if ("badRoot".equals(failMap)) {
      j.put("foo", a);
    } else {
      j.put(n, a);
    }
    j.put("totalRecords", maps.size());
    ctx.request().endHandler(res -> {
      ctx.response().setStatusCode(200);
      ctx.response().headers().add("Content-Type", "application/json");
      if ("badJson".equals(failMap)) {
        ctx.response().end("{");
      } else {
        ctx.response().end(j.encodePrettily());
      }
    });
    ctx.request().exceptionHandler(res -> {
      ctx.response().setStatusCode(500);
      ctx.response().end(res.getMessage());
    });
  }

  private void handlerContributorNameTypes(RoutingContext ctx) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("e8b311a6-3b21-03f2-2269-dd9310cb2d0a", "Meeting name");
    map.put("2b94c631-fca9-a892-c730-03ee529ffe2a", "Personal name");
    map.put("2e48e713-17f3-7c13-09f8-23845bb210aa", "Corporate name");
    handleTypeMaps(ctx, map, "contributorNameTypes");
  }

  private void handlerInstanceTypes(RoutingContext ctx) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("e8b311a6-3b21-03f2-2269-dd9310cb2d0c", "Mixed Material");
    map.put("53571e16-2277-4252-9d60-7ec31850672c", "Web Resources");
    map.put("a385b165-b103-4436-bbb3-02f9ef6f8a2c", "Spoken Record");
    map.put("2b94c631-fca9-a892-c730-03ee529ffe2c", "Books");
    map.put("c2e873be-4c99-4070-9133-76314941e166", "Music (MMS)");
    map.put("ac011474-5e2c-49d4-a6aa-42193cbd8213", "Charts Posters");
    map.put("d02e6f4b-be20-44eb-b500-daa49eef0c3a", "Error");
    map.put("c2677271-188e-46c2-9593-71ba82b901e4", "Music (Scores)");
    map.put("20d12382-98a6-4e3a-ba50-dc35445b35c0", "Kits");
    map.put("85c58579-a833-4624-904a-754973374679", "Videorecording");
    map.put("1b492cb2-326e-4a9c-8904-f49d5fe193fb", "Old Code");
    map.put("2e48e713-17f3-7c13-09f8-23845bb210ac", "Music (Audio)");
    map.put("a56c42e2-8b57-47d6-bb93-aa6267c28fb4", "Theses");
    map.put("a35947c6-795f-41d4-9c34-e5668d925d3f", "Serials");
    map.put("447bea04-0b30-4f0d-8841-a9164ee5d9cb", "eBooks");
    map.put("e2f217ff-95ba-47d7-9150-b044f275e06e", "Maps");
    map.put("4c72d83d-2625-4bde-8209-93abe245362e", "Computer Files");
    map.put("464102a7-1527-4bd6-9bca-886597cebf29", "3-D Objects");
    handleTypeMaps(ctx, map, "instanceTypes");
  }

  private void handlerInstanceFormats(RoutingContext ctx) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("182d9673-bbd8-4774-971e-99304dd705f2", "Notated music");
    map.put("309c3a3d-d54c-4519-b978-2c5c2de78d95", "Sound recording");
    map.put("266d8f18-11c9-478b-85cf-99051ed0c91a", "Remoted-sensing image");
    map.put("5824fc05-b672-4e16-9807-89f221c56c45", "Projected graphic");
    map.put("1657333c-ae05-430f-8e52-95e9107cdda0", "Unspecified");
    map.put("aa7a2802-9a29-438b-8b81-7019c7543859", "Videorecording");
    map.put("3087d369-522c-4128-9a55-7b2744810faf", "globe");
    map.put("4eba6d19-5fef-4d2f-a6da-9f2176ccf851", "Tactile material");
    map.put("c37704b8-3ba4-409e-9ebd-a93372ef43e4", "Motion picture");
    map.put("1b9f2518-dfa5-4113-88fa-b6a35b8f9451", "Nonprojected graphic");
    map.put("e0474071-2d1d-4898-b226-226bd060aa55", "Electronic resource");
    map.put("7f525502-5cf5-46c9-b835-75ecc1b2b5b7", "Microform");
    map.put("3ffeb708-64ba-4d48-a778-0a300be56782", "Text");
    map.put("27694cb9-eb08-46e0-81f7-8530ab84a07c", "map");
    map.put("b8fb5108-c49f-4b73-879b-55085cd0538f", "kit");
    handleTypeMaps(ctx, map, "instanceFormats");
  }

  private void handlerIdentifierTypes(RoutingContext ctx) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("8e3dd25e-db82-4b06-8311-90d41998c109", "Standard Technical Report Number");
    map.put("913300b2-03ed-469a-8179-c1092c991227", "ISSN");
    map.put("351ebc1c-3aae-4825-8765-c6d50dbf011f", "GPO Item Number");
    map.put("c858e4f2-2b6b-4385-842b-60732ee14abb", "LCCN");
    map.put("b5d8cdc4-9441-487c-90cf-0c7ec97728eb", "Publisher Number");
    map.put("3187432f-9434-40a8-8782-35a111a1491e", "BNB");
    map.put("650ef996-35e3-48ec-bf3a-a0d078a0ca37", "UkMac");
    map.put("8261054f-be78-422d-bd51-4ed9f33c3422", "ISBN");
    map.put("5d164f4b-0b15-4e42-ae75-cfcf85318ad9", "Control Number");
    map.put("3fbacad6-0240-4823-bce8-bb122cfdf229", "StEdNL");
    map.put("593b78cb-32f3-44d1-ba8c-63fd5e6989e6", "CODEN");
    map.put("7f907515-a1bf-4513-8a38-92e1a07c539d", "ASIN");
    map.put("37b65e79-0392-450d-adc6-e2a1f47de452", "Report Number");
    map.put("439bfbae-75bc-4f74-9fc7-b2a2d47ce3ef", "OCLC");
    map.put("2e8b3b6c-0e7d-4e48-bca2-b0b23b376af5", "Other Standard Identifier");
    map.put("7e591197-f335-4afb-bc6d-a6d76ca3bace", "System Control Number");
    map.put("5130aed5-1095-4fb6-8f6f-caa3d6cc7aae", "Local Identifier");
    handleTypeMaps(ctx, map, "identifierTypes");
  }

  private void handlerShelfLocations(RoutingContext ctx) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("fcd64ce1-6995-48f0-840e-89ffa2288371", "Main Library");
    map.put("b241764c-1466-4e1d-a028-1a3684a5da87", "Popular Reading Collection");
    map.put("758258bc-ecc1-41b8-abca-f7b610822ffd", "ORWIG ETHNO CD");
    map.put("f34d27c6-a8eb-461b-acd6-5dea81771e70", "SECOND FLOOR");
    map.put("53cf956f-c1df-410b-8bea-27f712cca7c0", "Annex");
    handleTypeMaps(ctx, map, "locations");
  }

  @After
  public void tearDown(TestContext context) {
    Async async = context.async();
    vertx.close(context.asyncAssertSuccess(res -> {
      async.complete();
    }));
  }

  @Test
  public void testInventory(TestContext context) {
    Response r;
    String b;
    JsonObject j;
    JsonArray a;

    RestAssured.port = portInventory;
    r = RestAssured.given()
      .get("/instance-storage/instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    j = new JsonObject(b);
    context.assertEquals(1, j.getInteger("totalRecords"));
    context.assertEquals("Sample", j.getJsonArray("instances").getJsonObject(0).getString("source"));

    r = RestAssured.given()
      .get("/instance-storage/instances/" + ID1)
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    j = new JsonObject(b);
    context.assertEquals(ID1, j.getString("id"));

    RestAssured.given()
      .get("/instance-storage/instances/" + ID_404)
      .then()
      .log().ifValidationFails()
      .statusCode(404);

    RestAssured.given()
      .get("/instance-storage/instances/1234")
      .then()
      .log().ifValidationFails()
      .statusCode(404);

    r = RestAssured.given()
      .get("/contributor-name-types")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    j = new JsonObject(b);
    context.assertEquals(3, j.getInteger("totalRecords"));
    context.assertEquals(3, j.getJsonArray("contributorNameTypes").size());

    r = RestAssured.given()
      .get("/instance-types")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    j = new JsonObject(b);
    context.assertEquals(18, j.getInteger("totalRecords"));

    r = RestAssured.given()
      .get("/instance-formats")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    j = new JsonObject(b);
    context.assertEquals(15, j.getInteger("totalRecords"));

    r = RestAssured.given()
      .get("/identifier-types")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    j = new JsonObject(b);
    context.assertEquals(17, j.getInteger("totalRecords"));

    r = RestAssured.given()
      .get("/locations?offset=1&limit=2")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    j = new JsonObject(b);
    context.assertEquals(5, j.getInteger("totalRecords"));
    context.assertEquals(2, j.getJsonArray("locations").size());

    context.assertEquals("b241764c-1466-4e1d-a028-1a3684a5da87",
      j.getJsonArray("locations").getJsonObject(0).getString("id"));
    context.assertEquals("Popular Reading Collection",
      j.getJsonArray("locations").getJsonObject(0).getString("name"));
    context.assertEquals("ORWIG ETHNO CD",
      j.getJsonArray("locations").getJsonObject(1).getString("name"));
  }

  @Test
  public void testCodex1(TestContext context) {
    InstanceCollection col;
    Instance inst;
    Diagnostic diag;
    Response r;
    String b;
    JsonObject j;
    JsonArray a;
    Header tenantHeader = new Header("X-Okapi-Tenant", "testlib");
    Header urlHeader = new Header("X-Okapi-Url", "http://localhost:" + portInventory);
    RestAssured.port = portCodex;

    failMap = "*";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances/" + ID1)
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    failMap = "contributorNameTypes";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    failMap = "instanceTypes";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    failMap = "instanceFormats";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    failMap = "identifierTypes";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    failMap = "locations";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    failMap = "badJson";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    failMap = "badRoot";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    failMap = "emptyArray";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    // no type map failures anymore
    failMap = null;
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    col = Json.decodeValue(b, InstanceCollection.class);
    context.assertEquals(1, col.getResultInfo().getTotalRecords());

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water)")
      .then()
      .log().ifValidationFails()
      .statusCode(400).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("cql parse error"));

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=foo=bar")
      .then()
      .log().ifValidationFails()
      .statusCode(400).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("unknown index: foo"));

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=language>bar")
      .then()
      .log().ifValidationFails()
      .statusCode(400).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("unknown relation"));

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=identifier=/type=isbn 6316800312")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    col = Json.decodeValue(b, InstanceCollection.class);
    context.assertEquals(1, col.getResultInfo().getTotalRecords());

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=identifier=/type<isbn 6316800312")
      .then()
      .log().ifValidationFails()
      .statusCode(400).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("unknown relation"));

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=identifier=/x=y 6316800312")
      .then()
      .log().ifValidationFails()
      .statusCode(400).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("unknown relation modifier"));

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=source=foo")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    col = Json.decodeValue(b, InstanceCollection.class);
    context.assertEquals(0, col.getResultInfo().getTotalRecords());

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=source=local")
      .then()
      .log().ifValidationFails()
      .statusCode(400).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("cql:"));

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=source=local and kurt")
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    col = Json.decodeValue(b, InstanceCollection.class);
    context.assertEquals(1, col.getResultInfo().getTotalRecords());

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances/" + ID1)
      .then()
      .log().ifValidationFails()
      .statusCode(200).extract().response();
    b = r.getBody().asString();
    inst = Json.decodeValue(b, Instance.class);
    context.assertEquals(ID1, inst.getId());

    RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances/" + ID_404)
      .then()
      .log().ifValidationFails()
      .statusCode(404);

    RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances/1234")
      .then()
      .log().ifValidationFails()
      .statusCode(404);

    // should be captured with the @validate
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water&limit=-1")
      .then()
      .log().ifValidationFails()
      .statusCode(400).extract().response();
    b = r.getBody().asString();

    // should be captured with the @validate
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?lang=123")
      .then()
      .log().ifValidationFails()
      .statusCode(400).extract().response();
    b = r.getBody().asString();

    failInventory = "401";
    RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances/" + ID1)
      .then()
      .log().ifValidationFails()
      .statusCode(401);

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(401).extract().response();
    b = r.getBody().asString();

    failInventory = "400";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(400).extract().response();
    b = r.getBody().asString();

    failInventory = "500";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances/" + ID1)
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    failInventory = "instances";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("instances missing"));

    failInventory = "totalRecords";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("totalRecords missing"));

    failInventory = "-id";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("id missing"));

    failInventory = "-title";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("title missing"));

    failInventory = "-instanceTypeId";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("instanceTypeId missing"));

    failInventory = "instanceFormatId=112233";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("instanceFormatId "));
    context.assertTrue(b.contains("does not exist"));

    failInventory = "instanceTypeId=112233";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();
    context.assertTrue(b.contains("instanceTypeId "));
    context.assertTrue(b.contains("does not exist"));

    failInventory = "badJson";
    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances/e54b1f4d-7d05-4b1a-9368-3c36b75d8ac6")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

    r = RestAssured.given()
      .header(tenantHeader)
      .header(urlHeader)
      .get("/codex-instances?query=water")
      .then()
      .log().ifValidationFails()
      .statusCode(500).extract().response();
    b = r.getBody().asString();

  }
}
