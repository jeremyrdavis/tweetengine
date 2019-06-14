package io.vertx.examples.tweetengine;

import io.vertx.core.AbstractVerticle;

import io.vertx.core.Future;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterStreamVerticle extends AbstractVerticle {

  private TwitterStream twitterStream;

  @Override
  public void start(Future<Void> startFuture) {

    try {

      TwitterStream twitterStream = new TwitterStreamFactory().getInstance().addListener(new StatusListener() {
        @Override
        public void onStatus(Status status) {
          System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
          System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
          System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
          System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
        }

        @Override
        public void onStallWarning(StallWarning warning) {
          System.out.println("Got stall warning:" + warning);
        }

        @Override
        public void onException(Exception ex) {
          ex.printStackTrace();
        }
      });

      FilterQuery fq = new FilterQuery();
      fq.follow(702198386284433409L);
      twitterStream.filter(fq);

    } catch (Exception e) {
      startFuture.fail(e.getCause());
    }finally {
      startFuture.complete();
    }



  }

}
