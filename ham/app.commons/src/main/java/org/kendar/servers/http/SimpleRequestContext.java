package org.kendar.servers.http;

import org.apache.commons.fileupload.RequestContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class SimpleRequestContext implements RequestContext {
  private final Charset charset;
  private final String contentType;
  private final byte[] content;

  public SimpleRequestContext(Charset charset, String contentType, byte[] content) {
    this.charset = charset;
    this.contentType = contentType;
    this.content = content;
  }

  public String getCharacterEncoding() {
    return charset.displayName();
  }

  public String getContentType() {
    return contentType;
  }

  @Deprecated
  public int getContentLength() {
    return content.length;
  }

  public InputStream getInputStream() {
    return new ByteArrayInputStream(content);
  }
}
