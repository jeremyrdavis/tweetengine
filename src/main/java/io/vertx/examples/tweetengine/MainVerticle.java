package io.vertx.examples.tweetengine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * {
 *     "tweet": {
 *         "status_id" : 123456789,
 *         "reply_message" : "@twitteruser Thanks for the tweet!"
 *     }
 * }
 */
public class MainVerticle extends AbstractVerticle {

  Map<Integer, JsonObject> processedTweets = new HashMap<>();

  WebClient webClient;

  JsonObject config;

  @Override
  public void start(Future<Void> startFuture) {

    webClient = WebClient.create(vertx);

    Router baseRouter = Router.router(vertx);
    baseRouter.route("/").handler(this::indexHandler);

    Router apiRouter = Router.router(vertx);
    apiRouter.route("/*").handler(BodyHandler.create());

    baseRouter.mountSubRouter("/api", apiRouter);

    vertx.deployVerticle(new TwitterStreamVerticle(), ar -> {
      if (ar.succeeded()) {
        vertx
          .createHttpServer()
          .requestHandler(baseRouter::accept)
          .listen(8080, result -> {
            if (result.succeeded()) {
              startFuture.complete();
            } else {
              startFuture.fail(result.cause());
            }
          });
      }else{
        startFuture.fail(ar.cause());
      }
    });

  }

  private void defaultResultHandler(final AsyncResult result, final RoutingContext routingContext, final JsonObject successMessage) {
    if (result.succeeded()) {
      HttpServerResponse response = routingContext.response();
      response
        .putHeader("Content-Type", "application/json")
        .end(Json.encodePrettily(successMessage));
    } else {
      HttpServerResponse response = routingContext.response();
      response
        .putHeader("Content-Type", "application/json")
        .end(new JsonObject().put("error", result.cause().getMessage()).toBuffer());
    }

  }

  private void indexHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/html")
      .end("Hello, Tweetengine!");
  }

}
