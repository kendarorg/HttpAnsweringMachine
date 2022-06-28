package swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class OpenAPIExtensions {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIExtensions.class);

  private static List<OpenAPIExtension> extensions = null;

  public static List<OpenAPIExtension> getExtensions() {
    return extensions;
  }

  public static void setExtensions(List<OpenAPIExtension> ext) {
    extensions = ext;
  }

  public static Iterator<OpenAPIExtension> chain() {
    return extensions.iterator();
  }


  public static void initialize(SwaggerLoader swaggerLoader) {
    extensions = new ArrayList<>();
    ServiceLoader<OpenAPIExtension> loader = ServiceLoader.load(OpenAPIExtension.class);
    for (OpenAPIExtension ext : loader) {
      LOGGER.debug("adding extension {}", ext);
      extensions.add(ext);
    }
    extensions.add(new DefaultParameterExtension(swaggerLoader));
  }
}
