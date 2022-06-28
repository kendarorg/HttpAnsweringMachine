package swagger;

public enum SwaggerMediaType {
  APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
  MULTIPART_FORM_DATA("multipart/form-data");

  private final String value;

  SwaggerMediaType(String s) {
    this.value = s;
  }

  String getValue(){
    return this.value;
  }
}
