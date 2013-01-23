package se.vgregion.alfresco.repo.rendition.executer;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.rendition.executer.AbstractTransformationRenderingEngine;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.lang.StringUtils;
import se.vgregion.alfresco.repo.content.transform.PdfaPilotTransformationOptions;

import java.util.Collection;

public class PdfaPilotRenderingEngine extends AbstractTransformationRenderingEngine {

  public static final String NAME = "pdfapilot";

  public static final String PARAM_LEVEL = "level";

  public static final String PARAM_OPTIMIZE = "optimize";

  @Override
  protected TransformationOptions getTransformOptions(RenderingContext context) {
    return getTransformOptionsImpl(new PdfaPilotTransformationOptions(), context);
  }

  @Override
  protected TransformationOptions getTransformOptionsImpl(TransformationOptions options, RenderingContext context) {
    options.setSourceNodeRef(context.getSourceNode());
    options.setTargetNodeRef(context.getDestinationNode());

    PdfaPilotTransformationOptions transformationOptions = (PdfaPilotTransformationOptions) options;

    String level = context.getCheckedParam(PARAM_LEVEL, String.class);

    if (StringUtils.isNotBlank(level)) {
      transformationOptions.setLevel(level);
    }

    Boolean optimize = context.getCheckedParam(PARAM_OPTIMIZE, Boolean.class);

    if (optimize != null) {
      transformationOptions.setOptimize(optimize);
    }

    return super.getTransformOptionsImpl(options, context);
  }

  @Override
  protected Collection<ParameterDefinition> getParameterDefinitions() {
    Collection<ParameterDefinition> paramList = super.getParameterDefinitions();

    paramList.add(new ParameterDefinitionImpl(PARAM_LEVEL, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_LEVEL)));

    paramList.add(new ParameterDefinitionImpl(PARAM_OPTIMIZE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_OPTIMIZE)));

    return paramList;
  }
}
