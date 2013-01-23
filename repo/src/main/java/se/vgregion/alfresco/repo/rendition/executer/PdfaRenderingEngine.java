package se.vgregion.alfresco.repo.rendition.executer;

import org.alfresco.repo.rendition.executer.AbstractTransformationRenderingEngine;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;

import se.vgregion.alfresco.repo.content.transform.OpenOfficeTransformationOptions;

public class PdfaRenderingEngine extends AbstractTransformationRenderingEngine {

  public static final String NAME = "pdfa";

  @Override
  protected TransformationOptions getTransformOptions(final RenderingContext context) {
    final NodeRef sourceNode = context.getSourceNode();

    final NodeRef destinationNode = context.getDestinationNode();

    final OpenOfficeTransformationOptions transformationOptions = new OpenOfficeTransformationOptions(sourceNode, null, destinationNode, null);

    transformationOptions.setPdfVersion(1);

    return transformationOptions;
  }

}
