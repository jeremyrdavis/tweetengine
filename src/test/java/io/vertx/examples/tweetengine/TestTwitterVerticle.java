package io.vertx.examples.tweetengine;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class TestTwitterVerticle {


  @Test
  @Timeout(2000)
  @DisplayName("Test Twitter Verticle")
  public void testReplyingToATweet(Vertx vertx, VertxTestContext tc) {

    Checkpoint deploymentCheckpoint = tc.checkpoint();
    Checkpoint messageSentCheckpoint = tc.checkpoint();

    JsonObject message = new JsonObject()
      .put(EventBusConstants.ACTION, EventBusConstants.ACTIONS_REPLY)
      .put(EventBusConstants.PARAMETERS_REPLY_TO_STATUS_ID, TestData.JBOSSDEMO.reply_to_status_id)
      .put(EventBusConstants.PARAMETERS_REPLY_STATUS, TestData.generateReply(TestData.JBOSSDEMO));

    System.out.println(message);

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
