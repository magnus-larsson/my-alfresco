/*
 * Copyright 2005-2010 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package se.vgregion.alfresco.repo.content.transform;

import org.alfresco.enterprise.repo.content.JodConverter;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.repo.content.transform.OOoContentTransformerHelper;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.office.OfficeException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;
import se.vgregion.alfresco.repo.model.VgrModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Makes use of the {@link http://code.google.com/p/jodconverter/} library and
 * an installed OpenOffice application to perform OpenOffice-driven conversions.
 *
 * @author Neil McErlean
 */
public class JodContentTransformer extends OOoContentTransformerHelper implements ContentTransformerWorker, InitializingBean {
  /** Logger */
  private static Log logger = LogFactory.getLog(JodContentTransformer.class);

  private String documentFormatsConfiguration;
  private DocumentFormatRegistry formatRegistry;
  private JodConverter jodconverter;
  private NodeService _nodeService;

  /**
   * Set a non-default location from which to load the document format mappings.
   *
   * @param path
   *          a resource location supporting the <b>file:</b> or
   *          <b>classpath:</b> prefixes
   */
  public void setDocumentFormatsConfiguration(final String path) {
    this.documentFormatsConfiguration = path;
  }

  @Override
  protected Log getLogger() {
    return logger;
  }

  @Override
  protected String getTempFilePrefix() {
    return "JodContentTransformer";
  }

  public void setJodConverter(final JodConverter jodc) {
    this.jodconverter = jodc;
  }

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  @Override
  public boolean isAvailable() {
    return jodconverter.isAvailable();
  }

  @Override
  protected void convert(File tempFromFile, net.sf.jooreports.converter.DocumentFormat sourceFormat, File tempToFile, net.sf.jooreports.converter.DocumentFormat targetFormat) {
    OfficeDocumentConverter converter = new OfficeDocumentConverter(jodconverter.getOfficeManager());

    converter.convert(tempFromFile, tempToFile);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // load the document conversion configuration
    if (this.documentFormatsConfiguration != null) {
      final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
      try {
        final InputStream is = resourceLoader.getResource(this.documentFormatsConfiguration).getInputStream();
        this.formatRegistry = new JooReportsXmlDocumentFormatRegistryAdapter(is);
        // We do not need to explicitly close this InputStream as it is closed
        // for us within the XmlDocumentFormatRegistry
      } catch (final IOException e) {
        throw new AlfrescoRuntimeException("Unable to load document formats configuration file: " + this.documentFormatsConfiguration);
      }
    } else {
      this.formatRegistry = new JooReportsXmlDocumentFormatRegistryAdapter();
    }
  }

  /**
   * @see DocumentFormatRegistry
   */
  @Override
  public boolean isTransformable(final String sourceMimetype, final String targetMimetype, final TransformationOptions options) {
    if (this.isAvailable() == false) {
      return false;
    }

    if (isTransformationBlocked(sourceMimetype, targetMimetype)) {
      if (logger.isDebugEnabled()) {
        final StringBuilder msg = new StringBuilder();
        msg.append("Transformation from ").append(sourceMimetype).append(" to ").append(targetMimetype)
            .append(" is blocked and therefore unavailable.");
        logger.debug(msg.toString());
      }
      return false;
    }

    final MimetypeService mimetypeService = getMimetypeService();
    final String sourceExtension = mimetypeService.getExtension(sourceMimetype);
    final String targetExtension = mimetypeService.getExtension(targetMimetype);
    // query the registry for the source format
    final DocumentFormat sourceFormat = this.formatRegistry.getFormatByExtension(sourceExtension);
    if (sourceFormat == null) {
      // no document format
      return false;
    }
    // query the registry for the target format
    final DocumentFormat targetFormat = this.formatRegistry.getFormatByExtension(targetExtension);
    if (targetFormat == null) {
      // no document format
      return false;
    }

    // get the family of the target document
    final DocumentFamily sourceFamily = sourceFormat.getInputFamily();
    // does the format support the conversion
    if (targetFormat.getStoreProperties(sourceFamily) == null) {
      // unable to export from source family of documents to the target format
      return false;
    } else {
      return true;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.alfresco.repo.content.transform.ContentTransformerWorker#getVersionString
   * ()
   */
  @Override
  public String getVersionString() {
    return "";
  }

  @Override
  public void transform(final ContentReader reader, final ContentWriter writer, final TransformationOptions options) throws Exception {
    if (this.isAvailable() == false) {
      throw new ContentIOException("Content conversion failed (unavailable): \n" + "   reader: " + reader + "\n" + "   writer: " + writer);
    }

    if (logger.isDebugEnabled()) {
      final StringBuilder msg = new StringBuilder();
      msg.append("transforming content from ").append(reader.getMimetype()).append(" to ").append(writer.getMimetype());
      logger.debug(msg.toString());
    }

    final String sourceMimetype = getMimetype(reader);
    final String targetMimetype = getMimetype(writer);

    final MimetypeService mimetypeService = getMimetypeService();

    final String sourceExtension = mimetypeService.getExtension(sourceMimetype);
    final String targetExtension = mimetypeService.getExtension(targetMimetype);
    // query the registry for the source format
    final DocumentFormat sourceFormat = this.formatRegistry.getFormatByExtension(sourceExtension);
    if (sourceFormat == null) {
      // source format is not recognised
      throw new ContentIOException("No OpenOffice document format for source extension: " + sourceExtension);
    }
    // query the registry for the target format
    final DocumentFormat targetFormat = this.formatRegistry.getFormatByExtension(targetExtension);
    if (targetFormat == null) {
      // target format is not recognised
      throw new ContentIOException("No OpenOffice document format for target extension: " + targetExtension);
    }
    // get the family of the target document
    final DocumentFamily sourceFamily = sourceFormat.getInputFamily();
    // does the format support the conversion
    if (targetFormat.getStoreProperties(sourceFamily) == null) {
      throw new ContentIOException("OpenOffice conversion not supported: \n" + "   reader: " + reader + "\n" + "   writer: " + writer);
    }

    // create temporary files to convert from and to
    final File tempFromFile = getTempFromFile(options.getSourceNodeRef(), sourceExtension);

    final File tempToFile = TempFileProvider.createTempFile("JodContentTransformer-target-", "." + targetExtension);

    // download the content from the source reader
    reader.getContent(tempFromFile);

    try {
      final OfficeDocumentConverter converter = new OfficeDocumentConverter(jodconverter.getOfficeManager(), this.formatRegistry);

      if (options instanceof OpenOfficeTransformationOptions) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> storeProperties = (Map<String, Object>) targetFormat.getStoreProperties(sourceFamily);

        // add all option properties as FilterData (Note: may be override by
        // global configuration)
        @SuppressWarnings("unchecked")
        final Map<String, Object> storedFilterData = (Map<String, Object>) storeProperties.get("FilterData");

        final Map<String, Object> filterData = new HashMap<String, Object>();

        if (storedFilterData != null) {
          for (final String key : storedFilterData.keySet()) {
            final Object value = storedFilterData.get(key);
            filterData.put(key, value);
          }
        }

        final Map<String, Object> optionMap = options.toMap();

        for (final String option : OpenOfficeTransformationOptions.ALL_OPTIONS) {
          final Object value = optionMap.get(option);

          if (value != null) {
            filterData.put(option, value);
          }
        }

        storeProperties.put("FilterData", filterData);

        targetFormat.setStoreProperties(sourceFamily, storeProperties);
      }

      converter.convert(tempFromFile, tempToFile, targetFormat);
    } catch (final OfficeException ox) {
      throw new ContentIOException("OpenOffice server conversion failed: \n" + "   reader: " + reader + "\n" + "   writer: " + writer + "\n"
          + "   from file: " + tempFromFile + "\n" + "   to file: " + tempToFile, ox);
    }
    // conversion success

    // upload the temp output to the writer given us
    writer.putContent(tempToFile);

    if (logger.isDebugEnabled()) {
      logger.debug("transformation successful");
    }

  }

  private File getTempFromFile(NodeRef nodeRef, String extension) {
    if (nodeRef == null) {
      return TempFileProvider.createTempFile("JodContentTransformer-source-", "." + extension);
    }

    try {
      final File systemTempDir = TempFileProvider.getTempDir();

      final File tempDir = new File(systemTempDir + "/" + System.nanoTime());

      FileUtils.forceMkdir(tempDir);

      String filename = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME);

      if (StringUtils.isBlank(filename)) {
        filename = (String) _nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      }

      filename = FilenameUtils.getBaseName(filename) + "." + extension;

      return new File(tempDir, filename);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
