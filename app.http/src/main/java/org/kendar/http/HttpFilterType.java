package org.kendar.http;

/**
 * Order of phases PRE_RENDER, PAGE (blocking if executed), simple proxy, PRE_CALL,calls the remote
 * server, POST_CALL,POST_RENDER (finally)
 */
public enum HttpFilterType {
  /** Never executed */
  NONE("NONE"),
  PRE_RENDER("PRE_RENDER"),
  /** Always blocking */
  API("API"),
  STATIC("STATIC"),
  PRE_CALL("PRE_CALL"),
  POST_CALL("POST_CALL"),
  POST_RENDER("POST_RENDER");
  private final String text;

  /**
   * Filter phase
   *
   * @param The text for phase
   */
  HttpFilterType(final String text) {
    this.text = text;
  }

  /* (non-Javadoc)
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return text;
  }
}
