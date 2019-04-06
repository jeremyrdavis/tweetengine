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
    apiRouter.post("/directmessage").handler(this::directMessageHandler);
    apiRouter.post("/reply").handler(this::replyHandler);

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

  private void replyHandler(RoutingContext routingContext) {

    System.out.println("replyHandler");

    JsonObject requestJson = routingContext.getBodyAsJson();

    String replyText = new StringBuilder()
      .append("@")
      .append(requestJson.getJsonObject("user").getString("screen_name"))
      .append(" Thanks for the tweet!")
      .append(" Sent from Reactive Twitter MSA at")
      .append(Date.from(Instant.now())).toString();

    JsonObject message = new JsonObject()
      .put("reply", new JsonObject()
        .put(EventBusConstants.PARAMETERS_REPLY_TO_STATUS_ID, requestJson.getString("id"))
        .put(EventBusConstants.PARAMETERS_REPLY_STATUS, replyText));

    System.out.println("replyHandler: " + Json.encodePrettily(message));

    vertx.<JsonObject>eventBus().send(EventBusConstants.ADDRESS, message, ar -> {
      if (ar.succeeded()) {
        HttpServerResponse response = routingContext.response();
        response
          .putHeader("Content-Type", "application/json")
          .end(Json.encodePrettily(new JsonObject().put("outcome", "success").put("reply", replyText)));
      } else {
        HttpServerResponse response = routingContext.response();
        response
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("error", ar.cause().getMessage()).toBuffer());
      }
    });
  }

  private void directMessageHandler(RoutingContext routingContext) {

    JsonObject directMessageJson = routingContext.getBodyAsJson();

/*
    vertx.executeBlocking((Future<Object> future) -> {
      try {
        twitter.sendDirectMessage(directMessageJson.getLong("id"), directMessageJson.getString("message"));
        System.out.println("Sending direct message to " + directMessageJson.getString("screen_name"));
        future.complete(new JsonObject().put("result", "Success!"));
      } catch (TwitterException e) {
        future.fail(e.getMessage());
      }
    }, res -> {
      defaultResultHandler(res, routingContext, new JsonObject().put("outcome", "success"));
    });
*/

  }

  private void indexHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/html")
      .end("Hello, Tweetengine!");
  }

}
