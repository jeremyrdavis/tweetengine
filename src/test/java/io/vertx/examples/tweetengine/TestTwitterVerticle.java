package io.vertx.examples.tweetengine;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class TestTwitterVerticle {


  @Test
//  @Timeout(6000)
  @DisplayName("Test Twitter Verticle")
  public void testReplyingToATweet(Vertx vertx, VertxTestContext tc) {

    Checkpoint deploymentCheckpoint = tc.checkpoint();
    Checkpoint messageSentCheckpoint = tc.checkpoint();

    String replyText = new StringBuilder()
      .append("@")
      .append(TestData.VERTXDEMO.screen_name)
      .append(" Thanks for the tweet!")
      .append(" Sent from Reactive Twitter MSA at")
      .append(Date.from(Instant.now())).toString();

    JsonObject message = new JsonObject()
      .put(EventBusConstants.MESSAGE_KEY, new JsonObject()
        .put(EventBusConstants.ACTION, EventBusConstants.ACTIONS_REPLY)
        .put(EventBusConstants.PARAMETERS_REPLY_TO_STATUS_ID, TestData.VERTXDEMO.reply_to_status_id)
        .put(EventBusConstants.PARAMETERS_REPLY_STATUS, TestData.VERTXDEMO.screen_name));

    System.out.println("testReplyingToATweet: " + Json.encodePrettily(message));

    vertx.deployVerticle(new TwitterVerticle(), tc.succeeding(id -> {
      deploymentCheckpoint.flag();

      vertx.<JsonObject>eventBus().send( EventBusConstants.ADDRESS, message, ar -> {
        if (ar.succeeded()) {
          messageSentCheckpoint.flag();
          assertThat(ar.result().body()).isNotNull();
          tc.completeNow();
        }else{
          assertThat(ar.succeeded());
          tc.completeNow();
        }
      });


    }));
  }


}
