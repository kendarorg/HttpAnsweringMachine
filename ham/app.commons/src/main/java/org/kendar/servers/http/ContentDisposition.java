package org.kendar.servers.http;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ContentDisposition {

  private static final Pattern BASE64_ENCODED_PATTERN =
      Pattern.compile("=\\?([\\da-zA-Z-_]+)\\?B\\?([+/\\da-zA-Z]+=*)\\?=");

  private static final String INVALID_HEADER_FIELD_PARAMETER_FORMAT =
      "Invalid header field parameter format (as defined in RFC 5987)";

  @Nullable private final String type;

  @Nullable private final String name;

  @Nullable private final String filename;

  @Nullable private final String charset;

  @Nullable private final Long size;

  @Nullable private final String creationDate;

  @Nullable private final String modificationDate;

  @Nullable private final String readDate;

  /** Private constructor. See static factory methods in this class. */
  private ContentDisposition(
      @Nullable String type,
      @Nullable String name,
      @Nullable String filename,
      @Nullable String charset,
      @Nullable Long size,
      @Nullable String creationDate,
      @Nullable String modificationDate,
      @Nullable String readDate) {

    this.type = type;
    this.name = name;
    this.filename = filename;
    this.charset = charset;
    this.size = size;
    this.creationDate = creationDate;
    this.modificationDate = modificationDate;
    this.readDate = readDate;
  }

  /**
   * Return a builder for a {@code ContentDisposition} of type {@literal "attachment"}.
   *
   * @since 5.3
   */
  public static Builder attachment() {
    return builder("attachment");
  }

  /**
   * Return a builder for a {@code ContentDisposition} of type {@literal "form-data"}.
   *
   * @since 5.3
   */
  public static Builder formData() {
    return builder("form-data");
  }

  /**
   * Return a builder for a {@code ContentDisposition} of type {@literal "inline"}.
   *
   * @since 5.3
   */
  public static Builder inline() {
    return builder("inline");
  }

  /**
   * Return a builder for a {@code ContentDisposition}.
   *
   * @param type the disposition type like for example {@literal inline}, {@literal attachment}, or
   *     {@literal form-data}
   * @return the builder
   */
  public static Builder builder(String type) {
    return new BuilderImpl(type);
  }

  /** Return an empty content disposition. */
  public static ContentDisposition empty() {
    return new ContentDisposition("", null, null, null, null, null, null, null);
  }

  /**
   * Parse a {@literal Content-Disposition} header value as defined in RFC 2183.
   *
   * @param contentDisposition the {@literal Content-Disposition} header value
   * @return the parsed content disposition
   * @see #toString()
   */
  public static ContentDisposition parse(String contentDisposition) {
    List<String> parts = tokenize(contentDisposition);
    String type = parts.get(0);
    String name = null;
    String filename = null;
    Charset charset;
    String charsetString = null;
    Long size = null;
    String creationDate = null;
    String modificationDate = null;
    String readDate = null;
    for (int i = 1; i < parts.size(); i++) {
      String part = parts.get(i);
      int eqIndex = part.indexOf('=');
      if (eqIndex != -1) {
        String attribute = part.substring(0, eqIndex);
        String value =
            (part.startsWith("\"", eqIndex + 1) && part.endsWith("\"")
                ? part.substring(eqIndex + 2, part.length() - 1)
                : part.substring(eqIndex + 1));
        if (attribute.equals("name")) {
          name = value;
        } else if (attribute.equals("filename*")) {
          int idx1 = value.indexOf('\'');
          int idx2 = value.indexOf('\'', idx1 + 1);
          if (idx1 != -1 && idx2 != -1) {
            charsetString = value.substring(0, idx1).trim();
            charset = Charset.forName(charsetString);
            Assert.isTrue(
                UTF_8.equals(charset) || ISO_8859_1.equals(charset),
                "Charset should be UTF-8 or ISO-8859-1");
            filename = decodeFilename(value.substring(idx2 + 1), charset);
          } else {
            // US ASCII
            filename = decodeFilename(value, StandardCharsets.US_ASCII);
          }
        } else if (attribute.equals("filename") && (filename == null)) {
          if (value.startsWith("=?")) {
            Matcher matcher = BASE64_ENCODED_PATTERN.matcher(value);
            if (matcher.find()) {
              String match1 = matcher.group(1);
              String match2 = matcher.group(2);
              filename = new String(Base64.getDecoder().decode(match2), Charset.forName(match1));
            } else {
              filename = value;
            }
          } else {
            filename = value;
          }
        } else if (attribute.equals("size")) {
          size = Long.parseLong(value);
        } else if (attribute.equals("creation-date")) {
          creationDate = value;
        } else if (attribute.equals("modification-date")) {
          modificationDate = value;
        } else if (attribute.equals("read-date")) {
          readDate = value;
        }
      } else {
        throw new IllegalArgumentException("Invalid content disposition format");
      }
    }
    return new ContentDisposition(
        type, name, filename, charsetString, size, creationDate, modificationDate, readDate);
  }

  private static List<String> tokenize(String headerValue) {
    int index = headerValue.indexOf(';');
    String type = (index >= 0 ? headerValue.substring(0, index) : headerValue).trim();
    if (type.isEmpty()) {
      throw new IllegalArgumentException("Content-Disposition header must not be empty");
    }
    List<String> parts = new ArrayList<>();
    parts.add(type);
    if (index >= 0) {
      do {
        int nextIndex = index + 1;
        boolean quoted = false;
        boolean escaped = false;
        while (nextIndex < headerValue.length()) {
          char ch = headerValue.charAt(nextIndex);
          if (ch == ';') {
            if (!quoted) {
              break;
            }
          } else if (!escaped && ch == '"') {
            quoted = !quoted;
          }
          escaped = (!escaped && ch == '\\');
          nextIndex++;
        }
        String part = headerValue.substring(index + 1, nextIndex).trim();
        if (!part.isEmpty()) {
          parts.add(part);
        }
        index = nextIndex;
      } while (index < headerValue.length());
    }
    return parts;
  }

  /**
   * Decode the given header field param as described in RFC 5987.
   *
   * <p>Only the US-ASCII, UTF-8 and ISO-8859-1 charsets are supported.
   *
   * @param filename the filename
   * @param charset the charset for the filename
   * @return the encoded header field param
   * @see <a href="https://tools.ietf.org/html/rfc5987">RFC 5987</a>
   */
  private static String decodeFilename(String filename, Charset charset) {
    Assert.notNull(filename, "'input' String` should not be null");
    Assert.notNull(charset, "'charset' should not be null");
    byte[] value = filename.getBytes(charset);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int index = 0;
    while (index < value.length) {
      byte b = value[index];
      if (isRFC5987AttrChar(b)) {
        baos.write((char) b);
        index++;
      } else if (b == '%' && index < value.length - 2) {
        char[] array = new char[] {(char) value[index + 1], (char) value[index + 2]};
        try {
          baos.write(Integer.parseInt(String.valueOf(array), 16));
        } catch (NumberFormatException ex) {
          throw new IllegalArgumentException(INVALID_HEADER_FIELD_PARAMETER_FORMAT, ex);
        }
        index += 3;
      } else {
        throw new IllegalArgumentException(INVALID_HEADER_FIELD_PARAMETER_FORMAT);
      }
    }
    return StreamUtils.copyToString(baos, charset);
  }

  private static boolean isRFC5987AttrChar(byte c) {
    return (c >= '0' && c <= '9')
        || (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || c == '!'
        || c == '#'
        || c == '$'
        || c == '&'
        || c == '+'
        || c == '-'
        || c == '.'
        || c == '^'
        || c == '_'
        || c == '`'
        || c == '|'
        || c == '~';
  }

  private static String escapeQuotationsInFilename(String filename) {
    if (filename.indexOf('"') == -1 && filename.indexOf('\\') == -1) {
      return filename;
    }
    boolean escaped = false;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < filename.length(); i++) {
      char c = filename.charAt(i);
      if (!escaped && c == '"') {
        sb.append("\\\"");
      } else {
        sb.append(c);
      }
      escaped = (!escaped && c == '\\');
    }
    // Remove backslash at the end..
    if (escaped) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  /**
   * Encode the given header field param as describe in RFC 5987.
   *
   * @param input the header field param
   * @param charset the charset of the header field param string, only the US-ASCII, UTF-8 and
   *     ISO-8859-1 charsets are supported
   * @return the encoded header field param
   * @see <a href="https://tools.ietf.org/html/rfc5987">RFC 5987</a>
   */
  private static String encodeFilename(String input, Charset charset) {
    Assert.notNull(input, "`input` is required");
    Assert.notNull(charset, "`charset` is required");
    Assert.isTrue(!StandardCharsets.US_ASCII.equals(charset), "ASCII does not require encoding");
    Assert.isTrue(
        UTF_8.equals(charset) || ISO_8859_1.equals(charset),
        "Only UTF-8 and ISO-8859-1 supported.");
    byte[] source = input.getBytes(charset);
    int len = source.length;
    StringBuilder sb = new StringBuilder(len << 1);
    sb.append(charset.name());
    sb.append("''");
    for (byte b : source) {
      if (isRFC5987AttrChar(b)) {
        sb.append((char) b);
      } else {
        sb.append('%');
        char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
        char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
        sb.append(hex1);
        sb.append(hex2);
      }
    }
    return sb.toString();
  }

  /**
   * Return whether the {@link #getType() type} is {@literal "attachment"}.
   *
   * @since 5.3
   */
  public boolean isAttachment() {
    return (this.type != null && this.type.equalsIgnoreCase("attachment"));
  }

  /**
   * Return whether the {@link #getType() type} is {@literal "form-data"}.
   *
   * @since 5.3
   */
  public boolean isFormData() {
    return (this.type != null && this.type.equalsIgnoreCase("form-data"));
  }

  /**
   * Return whether the {@link #getType() type} is {@literal "inline"}.
   *
   * @since 5.3
   */
  public boolean isInline() {
    return (this.type != null && this.type.equalsIgnoreCase("inline"));
  }

  /**
   * Return the disposition type.
   *
   * @see #isAttachment()
   * @see #isFormData()
   * @see #isInline()
   */
  @Nullable
  public String getType() {
    return this.type;
  }

  /** Return the value of the {@literal name} parameter, or {@code null} if not defined. */
  @Nullable
  public String getName() {
    return this.name;
  }

  /**
   * Return the value of the {@literal filename} parameter, possibly decoded from BASE64 encoding
   * based on RFC 2047, or of the {@literal filename*} parameter, possibly decoded as defined in the
   * RFC 5987.
   */
  @Nullable
  public String getFilename() {
    return this.filename;
  }

  /**
   * Return the charset defined in {@literal filename*} parameter, or {@code null} if not defined.
   */
  @Nullable
  public String getCharset() {
    return this.charset;
  }

  /**
   * Return the value of the {@literal size} parameter, or {@code null} if not defined.
   *
   * @deprecated since 5.2.3 as per <a href="<a href="https://tools.ietf.org/html/rfc6266#appendix-B">https://tools.ietf.org/html/rfc6266#appendix-B</a>">RFC
   *     6266, Appendix B</a>, to be removed in a future release.
   */
  @Deprecated
  @Nullable
  public Long getSize() {
    return this.size;
  }

  /**
   * Return the value of the {@literal creation-date} parameter, or {@code null} if not defined.
   *
   * @deprecated since 5.2.3 as per <a href="<a href="https://tools.ietf.org/html/rfc6266#appendix-B">https://tools.ietf.org/html/rfc6266#appendix-B</a>">RFC
   *     6266, Appendix B</a>, to be removed in a future release.
   */
  @Deprecated
  @Nullable
  public String getCreationDate() {
    return this.creationDate;
  }

  /**
   * Return the value of the {@literal modification-date} parameter, or {@code null} if not defined.
   *
   * @deprecated since 5.2.3 as per <a href="<a href="https://tools.ietf.org/html/rfc6266#appendix-B">https://tools.ietf.org/html/rfc6266#appendix-B</a>">RFC
   *     6266, Appendix B</a>, to be removed in a future release.
   */
  @Deprecated
  @Nullable
  public String getModificationDate() {
    return this.modificationDate;
  }

  /**
   * Return the value of the {@literal read-date} parameter, or {@code null} if not defined.
   *
   * @deprecated since 5.2.3 as per <a href="<a href="https://tools.ietf.org/html/rfc6266#appendix-B">https://tools.ietf.org/html/rfc6266#appendix-B</a>">RFC
   *     6266, Appendix B</a>, to be removed in a future release.
   */
  @Deprecated
  @Nullable
  public String getReadDate() {
    return this.readDate;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ContentDisposition)) {
      return false;
    }
    ContentDisposition otherCd = (ContentDisposition) other;
    return (ObjectUtils.nullSafeEquals(this.type, otherCd.type)
        && ObjectUtils.nullSafeEquals(this.name, otherCd.name)
        && ObjectUtils.nullSafeEquals(this.filename, otherCd.filename)
        && ObjectUtils.nullSafeEquals(this.charset, otherCd.charset)
        && ObjectUtils.nullSafeEquals(this.size, otherCd.size)
        && ObjectUtils.nullSafeEquals(this.creationDate, otherCd.creationDate)
        && ObjectUtils.nullSafeEquals(this.modificationDate, otherCd.modificationDate)
        && ObjectUtils.nullSafeEquals(this.readDate, otherCd.readDate));
  }

  @Override
  public int hashCode() {
    int result = ObjectUtils.nullSafeHashCode(this.type);
    result = 31 * result + ObjectUtils.nullSafeHashCode(this.name);
    result = 31 * result + ObjectUtils.nullSafeHashCode(this.filename);
    result = 31 * result + ObjectUtils.nullSafeHashCode(this.charset);
    result = 31 * result + ObjectUtils.nullSafeHashCode(this.size);
    result = 31 * result + (this.creationDate != null ? this.creationDate.hashCode() : 0);
    result = 31 * result + (this.modificationDate != null ? this.modificationDate.hashCode() : 0);
    result = 31 * result + (this.readDate != null ? this.readDate.hashCode() : 0);
    return result;
  }

  /**
   * Return the header value for this content disposition as defined in RFC 6266.
   *
   * @see #parse(String)
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (this.type != null) {
      sb.append(this.type);
    }
    if (this.name != null) {
      sb.append("; name=\"");
      sb.append(this.name).append('\"');
    }
    if (this.filename != null) {
      if (this.charset == null || StandardCharsets.US_ASCII.equals(Charset.forName(this.charset))) {
        sb.append("; filename=\"");
        sb.append(escapeQuotationsInFilename(this.filename)).append('\"');
      } else {
        sb.append("; filename*=");
        sb.append(encodeFilename(this.filename, Charset.forName(this.charset)));
      }
    }
    if (this.size != null) {
      sb.append("; size=");
      sb.append(this.size);
    }
    if (this.creationDate != null) {
      sb.append("; creation-date=\"");
      sb.append(this.creationDate);
      sb.append('\"');
    }
    if (this.modificationDate != null) {
      sb.append("; modification-date=\"");
      sb.append(this.modificationDate);
      sb.append('\"');
    }
    if (this.readDate != null) {
      sb.append("; read-date=\"");
      sb.append(this.readDate);
      sb.append('\"');
    }
    return sb.toString();
  }

  /** A mutable builder for {@code ContentDisposition}. */
  public interface Builder {

    /** Set the value of the {@literal name} parameter. */
    Builder name(String name);

    /**
     * Set the value of the {@literal filename} parameter. The given filename will be formatted as
     * quoted-string, as defined in RFC 2616, section 2.2, and any quote characters within the
     * filename value will be escaped with a backslash, e.g. {@code "foo\"bar.txt"} becomes {@code
     * "foo\\\"bar.txt"}.
     */
    Builder filename(String filename);

    /**
     * Set the value of the {@literal filename*} that will be encoded as defined in the RFC 5987.
     * Only the US-ASCII, UTF-8 and ISO-8859-1 charsets are supported.
     *
     * <p><strong>Note:</strong> Do not use this for a {@code "multipart/form-data"} requests as per
     * <a link="<a href="https://tools.ietf.org/html/rfc7578#section-4.2">https://tools.ietf.org/html/rfc7578#section-4.2</a>">RFC 7578, Section 4.2</a> and also
     * RFC 5987 itself mentions it does not apply to multipart requests.
     */
    Builder filename(String filename, String charset);

    /**
     * Set the value of the {@literal size} parameter.
     *
     * @deprecated since 5.2.3 as per <a href="<a href="https://tools.ietf.org/html/rfc6266#appendix-B">https://tools.ietf.org/html/rfc6266#appendix-B</a>">RFC
     *     6266, Appendix B</a>, to be removed in a future release.
     */
    @Deprecated
    Builder size(Long size);

    /**
     * Set the value of the {@literal creation-date} parameter.
     *
     * @deprecated since 5.2.3 as per <a href="<a href="https://tools.ietf.org/html/rfc6266#appendix-B">https://tools.ietf.org/html/rfc6266#appendix-B</a>">RFC
     *     6266, Appendix B</a>, to be removed in a future release.
     */
    @Deprecated
    Builder creationDate(String creationDate);

    /**
     * Set the value of the {@literal modification-date} parameter.
     *
     * @deprecated since 5.2.3 as per <a href="<a href="https://tools.ietf.org/html/rfc6266#appendix-B">https://tools.ietf.org/html/rfc6266#appendix-B</a>">RFC
     *     6266, Appendix B</a>, to be removed in a future release.
     */
    @Deprecated
    Builder modificationDate(String modificationDate);

    /**
     * Set the value of the {@literal read-date} parameter.
     *
     * @deprecated since 5.2.3 as per <a href="<a href="https://tools.ietf.org/html/rfc6266#appendix-B">https://tools.ietf.org/html/rfc6266#appendix-B</a>">RFC
     *     6266, Appendix B</a>, to be removed in a future release.
     */
    @Deprecated
    Builder readDate(String readDate);

    /** Build the content disposition. */
    ContentDisposition build();
  }

  private static class BuilderImpl implements Builder {

    private final String type;

    @Nullable private String name;

    @Nullable private String filename;

    @Nullable private String charset;

    @Nullable private Long size;

    @Nullable private String creationDate;

    @Nullable private String modificationDate;

    @Nullable private String readDate;

    public BuilderImpl(String type) {
      Assert.hasText(type, "'type' must not be not empty");
      this.type = type;
    }

    @Override
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    @Override
    public Builder filename(String filename) {
      Assert.hasText(filename, "No filename");
      this.filename = filename;
      return this;
    }

    @Override
    public Builder filename(String filename, String charset) {
      Assert.hasText(filename, "No filename");
      this.filename = filename;
      this.charset = charset;
      return this;
    }

    @Override
    public Builder size(Long size) {
      this.size = size;
      return this;
    }

    @Override
    public Builder creationDate(String creationDate) {
      this.creationDate = creationDate;
      return this;
    }

    @Override
    public Builder modificationDate(String modificationDate) {
      this.modificationDate = modificationDate;
      return this;
    }

    @Override
    public Builder readDate(String readDate) {
      this.readDate = readDate;
      return this;
    }

    @Override
    public ContentDisposition build() {
      return new ContentDisposition(
          this.type,
          this.name,
          this.filename,
          this.charset,
          this.size,
          this.creationDate,
          this.modificationDate,
          this.readDate);
    }
  }
}
