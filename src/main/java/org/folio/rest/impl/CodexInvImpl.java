package org.folio.rest.impl;

import static io.vertx.core.MultiMap.caseInsensitiveMultiMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.codex.inventory.InstanceConvert;
import org.folio.codex.inventory.LHeaders;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.folio.codex.inventory.IdMaps;
import org.folio.codex.inventory.QueryConvert;
import org.folio.okapi.common.XOkapiHeaders;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rest.jaxrs.model.ResultInfo;
import org.folio.rest.jaxrs.resource.CodexInstances;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;
import org.z3950.zing.cql.UnknownIndexException;
import org.z3950.zing.cql.UnknownRelationException;
import org.z3950.zing.cql.UnknownRelationModifierException;

public class CodexInvImpl implements CodexInstances {
  private static class HttpError401 extends Throwable {
    HttpError401() {
      super();
    }
  }

  private static class HttpError400 extends Throwable {
    HttpError400() {
      super();
    }
  }

  private static final Logger logger = LogManager.getLogger("codex.inventory");

  private void getUrl(String url, WebClient client,
    LHeaders okapiHeaders, Handler<AsyncResult<Buffer>> fut) {

    client.getAbs(url)
      .putHeader("Accept", "application/json")
      .putHeaders(getRequestHeaders(okapiHeaders))
      .send()
      .onSuccess(res -> {
        logger.info("getUrl " + url + " returned " + res.statusCode());
        client.close();
        if (res.statusCode() == 200) {
          fut.handle(Future.succeededFuture(res.body()));
        } else if (res.statusCode() == 404) {
          fut.handle(Future.succeededFuture(Buffer.buffer())); // empty buffer
        } else if (res.statusCode() == 401) {
          fut.handle(Future.failedFuture(new HttpError401()));
        } else if (res.statusCode() == 400) {
          fut.handle(Future.failedFuture(new HttpError400()));
        } else {
          fut.handle(Future.failedFuture("Get url " + url + " returned " + res.statusCode()));
        }
      })
      .onFailure(r -> {
        client.close();
        fut.handle(Future.failedFuture(r.getMessage()));
      });
  }

  static IdMaps idMaps = new IdMaps();

  private void getMap(Context vertxContext, LHeaders headers, Map<String, String> map,
    String path, String rootElement, Handler<AsyncResult<Void>> fut) {

    WebClient client = createWebClient(vertxContext);
    int offset = map.size();
    int chunk = 10;
    final String url = headers.get(XOkapiHeaders.URL) + path
      + "?limit=" + Integer.toString(chunk)
      + "&offset=" + Integer.toString(offset);
    logger.info("GetMap " + url);
    getUrl(url, client, headers, res -> {
      if (res.succeeded()) {
        try {
          JsonObject j = new JsonObject(res.result().toString());
          JsonArray a = j.getJsonArray(rootElement);
          if (a == null) {
            fut.handle(Future.failedFuture("missing " + rootElement + " got " + j.encodePrettily()));
            return;
          }
          logger.info(rootElement);
          for (int i = 0; i < a.size(); i++) {
            JsonObject ji = a.getJsonObject(i);
            final String id = ji.getString("id");
            final String name = ji.getString("name");
            logger.info(" " + id + "=" + name);
            map.put(id, name);
          }
          if (map.isEmpty()) {
            fut.handle(Future.failedFuture(rootElement + " is empty"));
          } else if (a.size() == chunk) {
            getMap(vertxContext, headers, map, path, rootElement, fut);
          } else {
            fut.handle(Future.succeededFuture());
          }
        } catch (Exception e) {
          fut.handle(Future.failedFuture(e));
        }
      } else {
        fut.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  private void getMaps(Context context, LHeaders headers, Handler<AsyncResult<Void>> fut) {
    if (idMaps.getContributorNameTypeIdMap().isEmpty()) {
      getMap(context, headers, idMaps.getContributorNameTypeIdMap(), "/contributor-name-types", "contributorNameTypes",
        res -> {
          if (res.succeeded()) {
            getMaps(context, headers, fut);
          } else {
            fut.handle(Future.failedFuture(res.cause()));
          }
        });
    } else if (idMaps.getInstanceTypeMap().isEmpty()) {
      getMap(context, headers, idMaps.getInstanceTypeMap(), "/instance-types", "instanceTypes",
        res -> {
          if (res.succeeded()) {
            getMaps(context, headers, fut);
          } else {
            fut.handle(Future.failedFuture(res.cause()));
          }
        });
    } else if (idMaps.getInstanceFormatMap().isEmpty()) {
      getMap(context, headers, idMaps.getInstanceFormatMap(), "/instance-formats", "instanceFormats",
        res -> {
          if (res.succeeded()) {
            getMaps(context, headers, fut);
          } else {
            fut.handle(Future.failedFuture(res.cause()));
          }
        });
    } else if (idMaps.getIdentifierTypeMap().isEmpty()) {
      getMap(context, headers, idMaps.getIdentifierTypeMap(), "/identifier-types", "identifierTypes",
        res -> {
          if (res.succeeded()) {
            getMaps(context, headers, fut);
          } else {
            fut.handle(Future.failedFuture(res.cause()));
          }
        });
    } else if (idMaps.getShelfLocationMap().isEmpty()) {
      getMap(context, headers, idMaps.getShelfLocationMap(), "/locations", "locations",
        res -> {
          if (res.succeeded()) {
            getMaps(context, headers, fut);
          } else {
            fut.handle(Future.failedFuture(res.cause()));
          }
        });
    } else {
      logger.info("All maps fetched");
      fut.handle(Future.succeededFuture());
    }
  }

  private void getQueryUrl(String query, int offset, int limit,
    LHeaders okapiHeaders, Handler<AsyncResult<String>> fut) {

    String url = okapiHeaders.get(XOkapiHeaders.URL) + "/instance-storage/instances?"
      + "offset=" + offset + "&limit=" + limit;
    if (query != null) {
      CQLNode qn = null;
      try {
        CQLParser parser = new CQLParser(CQLParser.V1POINT2);
        CQLNode top = parser.parse(query);
        QueryConvert v = new QueryConvert(idMaps);
        qn = v.convert(top);
      } catch (IOException ex) {
        fut.handle(Future.failedFuture(ex));
        return;
      } catch (CQLParseException ex) {
        fut.handle(Future.failedFuture("cql parse error: " + ex.getMessage()));
        return;
      } catch (IllegalArgumentException ex) {
        fut.handle(Future.failedFuture("cql: " + ex.getMessage()));
        return;
      } catch (UnknownIndexException ex) {
        fut.handle(Future.failedFuture("unknown index: " + ex.getMessage()));
        return;
      } catch (UnknownRelationModifierException ex) {
        fut.handle(Future.failedFuture("unknown relation modifier: " + ex.getMessage()));
        return;
      } catch (UnknownRelationException ex) {
        fut.handle(Future.failedFuture("unknown relation" + ex.getMessage()));
        return;
      }
      if (qn == null) { // not this source or diagnostic
        fut.handle(Future.succeededFuture(""));
        return;
      }
      final String query2 = qn.toCQL();
      logger.info("Resulting query = " + query2);
      try {
        url += "&query=" + URLEncoder.encode(query2, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        fut.handle(Future.failedFuture(ex.getMessage()));
        return;
      }
    }
    fut.handle(Future.succeededFuture(url));
  }

  private void getByQuery(Context vertxContext, String url,
    LHeaders okapiHeaders, InstanceCollection col,
    Handler<AsyncResult<Void>> fut) {

    logger.info("getByQuery url=" + url);
    WebClient client = createWebClient(vertxContext);
    getUrl(url, client, okapiHeaders, res -> {
      if (res.failed()) {
        logger.warn("getByQuery. getUrl failed " + res.cause());
        fut.handle(Future.failedFuture(res.cause()));
      } else {
        Buffer b = res.result();
        logger.info("getByQuery succeeded. Analyzing results");
        JsonObject j;
        try {
          j = new JsonObject(b.toString());
        } catch (Exception ex) {
          logger.warn(ex);
          fut.handle(Future.failedFuture(ex.getMessage()));
          return;
        }
        try {
          InstanceConvert.invToCollection(j, col, idMaps, "local");
        } catch (Exception ex) {
          fut.handle(Future.failedFuture("record conversion error: " + ex.getMessage()));
          return;
        }
        fut.handle(Future.succeededFuture());
      }
    });
  }

  private void getById(String id, Context vertxContext, LHeaders okapiHeaders,
    Instance instance, Handler<AsyncResult<Void>> fut) {

    WebClient client = createWebClient(vertxContext);
    final String url = okapiHeaders.get(XOkapiHeaders.URL) + "/instance-storage/instances/" + id;
    logger.info("getById url=" + url);
    getUrl(url, client, okapiHeaders, res -> {
      if (res.failed()) {
        logger.warn("getById. getUrl failed " + res.cause());
        fut.handle(Future.failedFuture(res.cause()));
      } else {
        try {
          if (res.result().length() > 0) {
            JsonObject j = new JsonObject(res.result().toString());
            InstanceConvert.invToCodex(j, instance, idMaps, "local");
          }
        } catch (Exception e) {
          logger.warn(e);
          fut.handle(Future.failedFuture(e.getMessage()));
          return;
        }
        fut.handle(Future.succeededFuture());
      }
    });
  }

  private void getCodexInstances2(AsyncResult<String> res2, Context vertxContext,
    LHeaders lHeaders, Handler<AsyncResult<Response>> handler) {

    InstanceCollection col = new InstanceCollection();
    ResultInfo resultInfo = new ResultInfo();
    resultInfo.setTotalRecords(0);
    col.setResultInfo(resultInfo);
    if (res2.result().isEmpty()) {
      handler.handle(Future.succeededFuture(
      CodexInstances.GetCodexInstancesResponse.respond200WithApplicationJson(col)));
    } else {
      getByQuery(vertxContext, res2.result(), lHeaders, col, res3 -> {
        if (res3.failed()) {
          if (res3.cause() instanceof HttpError401) {
            handler.handle(Future.succeededFuture(
              CodexInstances.GetCodexInstancesResponse.respond401WithTextPlain("")));
          } else if (res3.cause() instanceof HttpError400) {
            handler.handle(Future.succeededFuture(
              CodexInstances.GetCodexInstancesResponse.respond400WithTextPlain("")));
          } else {
            handler.handle(Future.succeededFuture(
              CodexInstances.GetCodexInstancesResponse.respond500WithTextPlain(res3.cause().getMessage())));
          }
        } else {
          handler.handle(Future.succeededFuture(
            CodexInstances.GetCodexInstancesResponse.respond200WithApplicationJson(col)));
        }
      });
    }
  }

  @Validate
  @Override
  public void getCodexInstances(String query, int offset, int limit, String lang,
    Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> handler,
    Context vertxContext) {

    logger.info("GetCodexInstances");
    LHeaders lHeaders = new LHeaders(okapiHeaders);

    getMaps(vertxContext, lHeaders, res1 -> {
      if (res1.failed()) {
        handler.handle(Future.succeededFuture(
          CodexInstances.GetCodexInstancesResponse.respond500WithTextPlain(res1.cause().getMessage())));
      } else {
        getQueryUrl(query, offset, limit, lHeaders, res2 -> {
          if (res2.failed()) {
            handler.handle(Future.succeededFuture(
              CodexInstances.GetCodexInstancesResponse.respond400WithTextPlain(res2.cause().getMessage())));
          } else {
            getCodexInstances2(res2, vertxContext, lHeaders, handler);
          }
        });
      }
    });
  }

  @Validate
  @Override
  public void getCodexInstancesById(String id, String lang,
    Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> handler,
    Context vertxContext) {

    logger.info("GetCodexInstancesById");
    if (!id.matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")) {
      handler.handle(Future.succeededFuture(
        CodexInstances.GetCodexInstancesByIdResponse.respond404WithTextPlain(id)));
      return;
    }
    LHeaders lHeaders = new LHeaders(okapiHeaders);
    getMaps(vertxContext, lHeaders, res1 -> {
      if (res1.failed()) {
        handler.handle(Future.succeededFuture(
          CodexInstances.GetCodexInstancesResponse.respond500WithTextPlain(res1.cause().getMessage())));
      } else {
        Instance instance = new Instance();
        getById(id, vertxContext, lHeaders, instance, res2 -> {
          if (res2.failed()) {
            if (res2.cause() instanceof HttpError401) {
              handler.handle(Future.succeededFuture(
                CodexInstances.GetCodexInstancesByIdResponse.respond401WithTextPlain(id)));
            } else {
              handler.handle(Future.succeededFuture(
                CodexInstances.GetCodexInstancesByIdResponse.respond500WithTextPlain(res2.cause().getMessage())));
            }
          } else {
            if (instance.getId() == null) {
              handler.handle(Future.succeededFuture(
                CodexInstances.GetCodexInstancesByIdResponse.respond404WithTextPlain(id)));
            } else {
              handler.handle(Future.succeededFuture(
                CodexInstances.GetCodexInstancesByIdResponse.respond200WithApplicationJson(instance)));
            }
          }
        });
      }
    });
  }

  private WebClient createWebClient(Context vertxContext) {
    return WebClient.create(vertxContext.owner());
  }

  private MultiMap getRequestHeaders(LHeaders headers) {
    final MultiMap headersForRequest = caseInsensitiveMultiMap();

    for (Map.Entry<String, String> e : headers.entrySet()) {
      if (!e.getKey().equalsIgnoreCase(XOkapiHeaders.URL)) {
        headersForRequest.add(e.getKey(), e.getValue());
      }
    }

    return headersForRequest;
  }
}
