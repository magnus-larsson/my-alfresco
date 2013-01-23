package se.vgregion.alfresco.repo.thumbnail;

import org.alfresco.repo.thumbnail.ThumbnailRenditionConvertor;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails;
import se.vgregion.alfresco.repo.content.transform.OpenOfficeTransformationOptions;

import java.io.Serializable;
import java.util.Map;

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
    }

    return parameters;
  }
}
