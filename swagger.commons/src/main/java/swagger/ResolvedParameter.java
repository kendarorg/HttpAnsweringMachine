package swagger;

import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;

public class ResolvedParameter {
  public List<Parameter> parameters = new ArrayList<>();
  public Parameter requestBody;
  public List<Parameter> formParameters = new ArrayList<>();
}
