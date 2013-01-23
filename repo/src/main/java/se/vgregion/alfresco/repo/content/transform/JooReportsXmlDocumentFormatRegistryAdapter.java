package se.vgregion.alfresco.repo.content.transform;

import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.jooreports.converter.DocumentFamily;
import net.sf.jooreports.converter.DocumentFormat;
import net.sf.jooreports.converter.XmlDocumentFormatRegistry;

import org.apache.commons.io.IOUtils;
import org.artofsolving.jodconverter.document.SimpleDocumentFormatRegistry;
import org.springframework.util.ReflectionUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * A JODConverter compatible document format registry that internally uses a
 * XML-based JOOReports document format registry to load and reuse mimetype and
 * export configuration from the Community Edition OOoDirect transformer
 * subsystem.
 *
 * @author <a href="mailto:axel.faust@prodyna.com">Axel Faust</a>, <a
 *         href="http://www.prodyna.com">PRODYNA AG</a>
 */
public class JooReportsXmlDocumentFormatRegistryAdapter extends SimpleDocumentFormatRegistry {

  /**
   * @see XmlDocumentFormatRegistry#DEFAULT_CONFIGURATION
   */
  private static final String DEFAULT_CONFIGURATION = "/" + XmlDocumentFormatRegistry.class.getPackage().getName().replace('.', '/')
      + "/document-formats.xml";

  private static final DocumentFamily[] DEFAULT_FAMILIES = { DocumentFamily.TEXT, DocumentFamily.SPREADSHEET, DocumentFamily.PRESENTATION };
  private static final org.artofsolving.jodconverter.document.DocumentFamily[] DEFAULT_FAMILY_EQUIVALENTS = {
      org.artofsolving.jodconverter.document.DocumentFamily.TEXT, org.artofsolving.jodconverter.document.DocumentFamily.SPREADSHEET,
      org.artofsolving.jodconverter.document.DocumentFamily.PRESENTATION };

  public JooReportsXmlDocumentFormatRegistryAdapter(final InputStream input) {
    load(input);
    enhanceJooConfigurationForJod();
  }

  public JooReportsXmlDocumentFormatRegistryAdapter() {
    load(getClass().getResourceAsStream(DEFAULT_CONFIGURATION));
    enhanceJooConfigurationForJod();
  }

  protected void enhanceJooConfigurationForJod() {
    /*
     * setup additional formats not properly supported via the jooreports
     * registry
     */

    final org.artofsolving.jodconverter.document.DocumentFormat csv = new org.artofsolving.jodconverter.document.DocumentFormat(
        "Comma Separated Values", "csv", "text/csv");
    csv.setInputFamily(org.artofsolving.jodconverter.document.DocumentFamily.SPREADSHEET);
    final Map<String, Object> csvLoadAndStoreProperties = new LinkedHashMap<String, Object>();
    csvLoadAndStoreProperties.put("FilterName", "Text - txt - csv (StarCalc)");
    csvLoadAndStoreProperties.put("FilterOptions", "44,34,0"); // Field
                                                               // Separator:
                                                               // ','; Text
                                                               // Delimiter: '"'
    csv.setLoadProperties(csvLoadAndStoreProperties);
    csv.setStoreProperties(org.artofsolving.jodconverter.document.DocumentFamily.SPREADSHEET, csvLoadAndStoreProperties);
    addFormat(csv);

    final org.artofsolving.jodconverter.document.DocumentFormat tsv = new org.artofsolving.jodconverter.document.DocumentFormat(
        "Tab Separated Values", "tsv", "text/tab-separated-values");
    tsv.setInputFamily(org.artofsolving.jodconverter.document.DocumentFamily.SPREADSHEET);
    final Map<String, Object> tsvLoadAndStoreProperties = new LinkedHashMap<String, Object>();
    tsvLoadAndStoreProperties.put("FilterName", "Text - txt - csv (StarCalc)");
    tsvLoadAndStoreProperties.put("FilterOptions", "9,34,0"); // Field
                                                              // Separator:
                                                              // '\t'; Text
                                                              // Delimiter: '"'
    tsv.setLoadProperties(tsvLoadAndStoreProperties);
    tsv.setStoreProperties(org.artofsolving.jodconverter.document.DocumentFamily.SPREADSHEET, tsvLoadAndStoreProperties);
    addFormat(tsv);

    /*
     * adapt the txt format export setting
     */
    final org.artofsolving.jodconverter.document.DocumentFormat txt = getFormatByExtension("txt");
    final Map<String, Object> txtLoadAndStoreProperties = new LinkedHashMap<String, Object>();
    txtLoadAndStoreProperties.put("FilterName", "Text (encoded)");
    txtLoadAndStoreProperties.put("FilterOptions", "utf8");
    txt.setLoadProperties(txtLoadAndStoreProperties);
    txt.setStoreProperties(org.artofsolving.jodconverter.document.DocumentFamily.TEXT, txtLoadAndStoreProperties);
  }

  /**
   * Overloaded method to provide mapping logic between JOOReports and
   * JODConverter specific document format types
   *
   * @param format
   */
  protected void addFormat(final DocumentFormat format) {
    final org.artofsolving.jodconverter.document.DocumentFormat mappedFormat = new org.artofsolving.jodconverter.document.DocumentFormat(
        format.getName(), format.getFileExtension(), format.getMimeType());

    for (int idx = 0; idx < DEFAULT_FAMILIES.length; idx++) {
      final DocumentFamily family = DEFAULT_FAMILIES[idx];
      final String exportFilter = format.getExportFilter(family);
      if (exportFilter != null) {
        final Map<?, ?> exportOptions = format.getExportOptions();

        if (exportOptions == null) {
          // standard case
          mappedFormat.setStoreProperties(DEFAULT_FAMILY_EQUIVALENTS[idx], Collections.singletonMap("FilterName", exportFilter));
        } else {
          final Map<String, Object> mappedOptions = new HashMap<String, Object>();
          mappedOptions.put("FilterName", exportFilter);
          mappedOptions.put("FilterData", exportOptions);
          mappedFormat.setStoreProperties(DEFAULT_FAMILY_EQUIVALENTS[idx], mappedOptions);
        }
      }
    }

    setDocumentFamily(format, mappedFormat);

    addFormat(mappedFormat);
  }

  private void setDocumentFamily(final DocumentFormat format, final org.artofsolving.jodconverter.document.DocumentFormat mappedFormat) {
    try {
      final DocumentFamily family = format.getFamily();

      if (family != null) {
        final Field field = DocumentFamily.class.getDeclaredField("name");

        ReflectionUtils.makeAccessible(field);

        final String name = (String) ReflectionUtils.getField(field, family);

        if (name.equalsIgnoreCase("Text")) {
          mappedFormat.setInputFamily(org.artofsolving.jodconverter.document.DocumentFamily.TEXT);
        } else if (name.equalsIgnoreCase("Spreadsheet")) {
          mappedFormat.setInputFamily(org.artofsolving.jodconverter.document.DocumentFamily.SPREADSHEET);
        } else if (name.equalsIgnoreCase("Presentation")) {
          mappedFormat.setInputFamily(org.artofsolving.jodconverter.document.DocumentFamily.PRESENTATION);
        }
      }
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * @see XmlDocumentFormatRegistry#load(InputStream)
   * @param inputStream
   */
  protected void load(final InputStream inputStream) {
    if (inputStream == null) {
      throw new IllegalArgumentException("inputStream is null");
    }
    final XStream xstream = createXStream();
    try {
      final ObjectInputStream in = xstream.createObjectInputStream(new InputStreamReader(inputStream));
      while (true) {
        try {
          addFormat((DocumentFormat) in.readObject());
        } catch (final EOFException endOfFile) {
          break;
        }
      }
    } catch (final Exception exception) {
      throw new RuntimeException("invalid registry configuration", exception);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * @see XmlDocumentFormatRegistry#createXStream()
   */
  protected XStream createXStream() {
    final XStream xstream = new XStream(new DomDriver());
    xstream.setMode(XStream.NO_REFERENCES);
    xstream.alias("document-format", DocumentFormat.class);
    xstream.aliasField("mime-type", DocumentFormat.class, "mimeType");
    xstream.aliasField("file-extension", DocumentFormat.class, "fileExtension");
    xstream.aliasField("export-filters", DocumentFormat.class, "exportFilters");
    xstream.aliasField("export-options", DocumentFormat.class, "exportOptions");
    xstream.alias("family", DocumentFamily.class);
    xstream.registerConverter(new AbstractSingleValueConverter() {
      @Override
      public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
        return type.equals(DocumentFamily.class);
      }

      @Override
      public Object fromString(final String name) {
        return DocumentFamily.getFamily(name);
      }
    });
    return xstream;
  }
}
