package se.vgregion.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.thumbnail.ThumbnailRenditionConvertor;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails;
import org.redpill.alfresco.repo.content.transform.PdfaPilotTransformationOptions;

import se.vgregion.alfresco.repo.content.transform.OpenOfficeTransformationOptions;
import se.vgregion.alfresco.repo.rendition.executer.PdfaPilotRenderingEngine;

public class CustomThumbnailRenditionConvertor extends ThumbnailRenditionConvertor {

  @Override
  public Map<String, Serializable> convert(TransformationOptions transformationOptions, ThumbnailParentAssociationDetails assocDetails) {
    Map<String, Serializable> parameters = super.convert(transformationOptions, assocDetails);

    if (transformationOptions instanceof OpenOfficeTransformationOptions) {
      OpenOfficeTransformationOptions ooTransformationOptions = (OpenOfficeTransformationOptions) transformationOptions;

      parameters.put(OpenOfficeTransformationOptions.OPT_EXPORT_NOTES, ooTransformationOptions.getExportNotes());
      parameters.put(OpenOfficeTransformationOptions.OPT_EXPORT_NOTES_PAGES, ooTransformationOptions.getExportNotesPages());
      parameters.put(OpenOfficeTransformationOptions.OPT_PDF_VERSION, ooTransformationOptions.getPdfVersion());
      parameters.put(OpenOfficeTransformationOptions.OPT_USE_LOSSLESS_COMPRESSION, ooTransformationOptions.getUseLosslessCompression());
    } else if (transformationOptions instanceof PdfaPilotTransformationOptions) {
      PdfaPilotTransformationOptions pdfaPilotTransformationOptions = (PdfaPilotTransformationOptions) transformationOptions;

      parameters.put(PdfaPilotRenderingEngine.PARAM_LEVEL, pdfaPilotTransformationOptions.getLevel());
      parameters.put(PdfaPilotRenderingEngine.PARAM_OPTIMIZE, pdfaPilotTransformationOptions.isOptimize());
    }

    return parameters;
  }
}
