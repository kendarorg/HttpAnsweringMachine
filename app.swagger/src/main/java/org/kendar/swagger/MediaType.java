package org.kendar.swagger;

public enum MediaType {
  APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
  MULTIPART_FORM_DATA("multipart/form-data");

  private final String value;

  MediaType(String s) {
    this.value = s;
  }

  String getValue(){
    return this.value;
  }
}
