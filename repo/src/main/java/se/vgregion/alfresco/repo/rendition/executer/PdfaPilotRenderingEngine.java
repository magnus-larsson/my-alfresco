package se.vgregion.alfresco.repo.rendition.executer;

import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rendition.executer.AbstractTransformationRenderingEngine;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.lang.StringUtils;
import org.redpill.alfresco.repo.content.transform.PdfaPilotTransformationOptions;

public class PdfaPilotRenderingEngine extends AbstractTransformationRenderingEngine {

  public static final String NAME = "pdfapilot";

  public static final String PARAM_LEVEL = "level";

  public static final String PARAM_OPTIMIZE = "optimize";

  public static final String PARAM_FAIL_SILENTLY = "failSilently";

  private BehaviourFilter _behaviourFilter;

  @Override
  protected TransformationOptions getTransformOptions(RenderingContext context) {
    return getTransformOptionsImpl(new PdfaPilotTransformationOptions(), context);
  }

  @Override
  protected TransformationOptions getTransformOptionsImpl(TransformationOptions options, RenderingContext context) {
    _behaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);

    try {
      options.setSourceNodeRef(context.getSourceNode());
    } finally {
      _behaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
    }

    PdfaPilotTransformationOptions transformationOptions = (PdfaPilotTransformationOptions) options;

    String level = context.getCheckedParam(PARAM_LEVEL, String.class);

    if (StringUtils.isNotBlank(level)) {
      ContentData content = (ContentData) nodeService.getProperty(context.getSourceNode(), ContentModel.PROP_CONTENT);

      if (content != null && StringUtils.isNotBlank(content.getMimetype())) {
        level = content.getMimetype().equalsIgnoreCase(MimetypeMap.MIMETYPE_PDF) ? "2b" : level;
      }

      transformationOptions.setLevel(level);
    }

    Boolean optimize = context.getCheckedParam(PARAM_OPTIMIZE, Boolean.class);

    if (optimize != null) {
      transformationOptions.setOptimize(optimize);
    }

    Boolean failSilently = context.getCheckedParam(PARAM_FAIL_SILENTLY, Boolean.class);

    if (failSilently != null) {
      transformationOptions.setFailSilently(failSilently);
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

  public void setPolicyBehaviourFilter(BehaviourFilter _behaviourFilter) {
    this._behaviourFilter = _behaviourFilter;
  }
}
