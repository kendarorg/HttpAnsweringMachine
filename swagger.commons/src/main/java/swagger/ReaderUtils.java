package swagger;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

public abstract class ReaderUtils {
  private static final String GET_METHOD = "get";
  private static final String POST_METHOD = "post";
  private static final String PUT_METHOD = "put";
  private static final String DELETE_METHOD = "delete";
  private static final String HEAD_METHOD = "head";
  private static final String OPTIONS_METHOD = "options";
  private static final String PATH_DELIMITER = "/";

  private static boolean isContext(List<Annotation> annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof Context) {
        return true;
      }
    }
    return false;
  }
  /**
   * Collects constructor-level parameters from class.
   *
   * @param cls is a class for collecting
   * @param components
   * @return the collection of supported parameters
   */
  public static List<Parameter> collectConstructorParameters(
      Class<?> cls,
      Components components,
      List<String> classConsumes,
      JsonView jsonViewAnnotation) {
    if (cls.isLocalClass() || (cls.isMemberClass() && !Modifier.isStatic(cls.getModifiers()))) {
      return Collections.emptyList();
    }

    List<Parameter> selected = Collections.emptyList();
    int maxParamsCount = 0;

    for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
      if (!ReflectionUtils.isConstructorCompatible(constructor)
          && !ReflectionUtils.isInject(Arrays.asList(constructor.getDeclaredAnnotations()))) {
        continue;
      }

      final Type[] genericParameterTypes = constructor.getGenericParameterTypes();
      final Annotation[][] annotations = constructor.getParameterAnnotations();

      int paramsCount = 0;
      final List<Parameter> parameters = new ArrayList<>();
      for (int i = 0; i < genericParameterTypes.length; i++) {
        final List<Annotation> tmpAnnotations = Arrays.asList(annotations[i]);
        if (isContext(tmpAnnotations)) {
          paramsCount++;
        } else {
          final Type genericParameterType = genericParameterTypes[i];
          final List<Parameter> tmpParameters =
              collectParameters(
                  genericParameterType,
                  tmpAnnotations,
                  components,
                  classConsumes,
                  jsonViewAnnotation);
          if (!tmpParameters.isEmpty()) {
            for (Parameter tmpParameter : tmpParameters) {
              Parameter processedParameter =
                  ParameterProcessor.applyAnnotations(
                      tmpParameter,
                      genericParameterType,
                      tmpAnnotations,
                      components,
                      classConsumes == null ? new String[0] : classConsumes.toArray(new String[0]),
                      null,
                      jsonViewAnnotation);
              if (processedParameter != null) {
                parameters.add(processedParameter);
              }
            }
            paramsCount++;
          }
        }
      }

      if (paramsCount >= maxParamsCount) {
        maxParamsCount = paramsCount;
        selected = parameters;
      }
    }

    return selected;
  }

  /**
   * Collects field-level parameters from class.
   *
   * @param cls is a class for collecting
   * @param components
   * @return the collection of supported parameters
   */
  public static List<Parameter> collectFieldParameters(
      Class<?> cls, Components components, List<String> classConsumes, JsonView jsonViewAnnotation) {
    final List<Parameter> parameters = new ArrayList<>();
    for (Field field : ReflectionUtils.getDeclaredFields(cls)) {
      final List<Annotation> annotations = Arrays.asList(field.getAnnotations());
      final Type genericType = field.getGenericType();
      parameters.addAll(
          collectParameters(
              genericType, annotations, components, classConsumes, jsonViewAnnotation));
    }
    return parameters;
  }

  private static List<Parameter> collectParameters(
      Type type,
      List<Annotation> annotations,
      Components components,
      List<String> classConsumes,
      JsonView jsonViewAnnotation) {
    final Iterator<OpenAPIExtension> chain = OpenAPIExtensions.chain();
    return chain.hasNext()
        ? chain
            .next()
            .extractParameters(
                annotations,
                type,
                new HashSet<>(),
                components,
                classConsumes,
                null,
                false,
                jsonViewAnnotation,
                chain)
            .parameters
        : Collections.emptyList();
  }

  public static Optional<List<String>> getStringListFromStringArray(String[] array) {
    if (array == null) {
      return Optional.empty();
    }
    List<String> list = new ArrayList<>();
    boolean isEmpty = true;
    for (String value : array) {
      if (StringUtils.isNotBlank(value)) {
        isEmpty = false;
      }
      list.add(value);
    }
    if (isEmpty) {
      return Optional.empty();
    }
    return Optional.of(list);
  }

  public static boolean isIgnored(String path, OpenAPIConfiguration config) {
    if (config.getIgnoredRoutes() == null) {
      return false;
    }
    for (String item : config.getIgnoredRoutes()) {
      final int length = item.length();
      if (path.startsWith(item)
          && (path.length() == length || path.startsWith(PATH_DELIMITER, length))) {
        return true;
      }
    }
    return false;
  }

  public static String getPath(
      String classLevelPath, String methodLevelPath, String parentPath, boolean isSubresource) {
    if (classLevelPath == null && methodLevelPath == null && StringUtils.isEmpty(parentPath)) {
      return null;
    }
    StringBuilder b = new StringBuilder();
    appendPathComponent(parentPath, b);
    if (classLevelPath != null && !isSubresource) {
      appendPathComponent(classLevelPath, b);
    }
    if (methodLevelPath != null) {
      appendPathComponent(methodLevelPath, b);
    }
    return b.length() == 0 ? "/" : b.toString();
  }

  /**
   * appends a path component string to a StringBuilder guarantees:
   *
   * <ul>
   *   <li>nulls, empty strings and "/" are nops
   *   <li>output will always start with "/" and never end with "/"
   * </ul>
   *
   * @param component component to be added
   * @param to output
   */
  private static void appendPathComponent(String component, StringBuilder to) {
    if (component == null || component.isEmpty() || "/".equals(component)) {
      return;
    }
    if (!component.startsWith("/") && (to.length() == 0 || '/' != to.charAt(to.length() - 1))) {
      to.append("/");
    }
    if (component.endsWith("/")) {
      to.append(component, 0, component.length() - 1);
    } else {
      to.append(component);
    }
  }


  public static String extractOperationMethod(SwaggerLoader loader,Method method, Iterator<OpenAPIExtension> chain) {
    var getMethodAnnotation = loader.getHttpMethodLowerCase(method);
    if (getMethodAnnotation.equalsIgnoreCase("get")) {
      return GET_METHOD;
    } else if (getMethodAnnotation.equalsIgnoreCase("put")) {
      return PUT_METHOD;
    } else if (getMethodAnnotation.equalsIgnoreCase("post")) {
      return POST_METHOD;
    } else if (getMethodAnnotation.equalsIgnoreCase("delete")) {
      return DELETE_METHOD;
    } else if (getMethodAnnotation.equalsIgnoreCase("options")) {
      return OPTIONS_METHOD;
    } else if (getMethodAnnotation.equalsIgnoreCase("header")) {
      return HEAD_METHOD;
    } else if (getMethodAnnotation != null) {
      return getMethodAnnotation.toLowerCase();
    } else if (!StringUtils.isEmpty(getHttpMethodFromCustomAnnotations(loader,method))) {
      return getHttpMethodFromCustomAnnotations(loader,method);
    }else if ((ReflectionUtils.getOverriddenMethod(method)) != null) {
      return extractOperationMethod(loader,ReflectionUtils.getOverriddenMethod(method), chain);
    } else if (chain != null && chain.hasNext()) {
      return chain.next().extractOperationMethod(method, chain);
    } else {
      return null;
    }
  }

  public static String getHttpMethodFromCustomAnnotations(SwaggerLoader loader,Method method) {
    for (Annotation methodAnnotation : method.getAnnotations()) {
      String stringMethod = loader.getHttpMethoFromCustomAnnotation(methodAnnotation.annotationType());
      if (stringMethod != null) {
        return stringMethod.toLowerCase(Locale.ROOT);
      }
    }
    return null;
  }
}
