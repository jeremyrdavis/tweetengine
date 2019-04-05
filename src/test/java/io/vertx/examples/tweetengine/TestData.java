package io.vertx.examples.tweetengine;

public enum TestData {

  JBOSSDEMO(702198386284433409L, "jbossdemo"), VERTXDEMO(1113238476600893445L, "vertxdemo");

  public final long id;

  public final String screen_name;

  private TestData(long id, String screen_name) {
    this.id = id;
    this.screen_name = screen_name;
  }
}
