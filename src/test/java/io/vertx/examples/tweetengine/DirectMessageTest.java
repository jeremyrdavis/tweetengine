package io.vertx.examples.tweetengine;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Date;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class DirectMessageTest {

  @Test
  @DisplayName("Test Sending a direct message to @vertxdemo")
  public void testSendingADirectMessageToVertxdemo(Vertx vertx, VertxTestContext tc) {

    WebClient webClient = WebClient.create(vertx);
    Checkpoint deploymentCheckpoint = tc.checkpoint();
    Checkpoint requestCheckpoint = tc.checkpoint();

    vertx.deployVerticle(new MainVerticle(), tc.succeeding(id -> {

      deploymentCheckpoint.flag();

      webClient.post(8080, "localhost", "/api/directmessage")
        .as(BodyCodec.string())
        .sendJsonObject(new JsonObject()
            .put("message", "Test sent using " + TestData.JBOSSDEMO.screen_name + " authentication values from Vert.x Twitter MSA Demo at " + Date.from(Instant.now()).toString())
            .put("id", TestData.VERTXDEMO.id)
            .put("screen_name", TestData.VERTXDEMO.screen_name),
          tc.succeeding(resp -> {
            tc.verify(() -> {
              assertThat(resp.statusCode()).isEqualTo(200);
              assertThat(resp.body()).isNotEmpty();
              assertThat(resp.body()).contains("success");
              requestCheckpoint.flag();
            });
          }));
    }));
  }
}
