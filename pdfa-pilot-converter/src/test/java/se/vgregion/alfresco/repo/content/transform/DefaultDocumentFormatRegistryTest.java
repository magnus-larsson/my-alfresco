package se.vgregion.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultDocumentFormatRegistryTest {

  private ApplicationContext _applicationContext;

  private DocumentFormatRegistry _documentFormatRegistry;

  @Before
  public void setUp() {
    _applicationContext = new ClassPathXmlApplicationContext(new String[]{"test-pdfa-pilot-convert-context.xml"});

    _documentFormatRegistry = (DocumentFormatRegistry) _applicationContext.getBean("pdfaPilot.documentFormatRegistry");

    assertNotNull(_documentFormatRegistry);
  }

  @Test
  public void testGetFormatByExtension() {
    DocumentFormat pdfDocumentFormat = _documentFormatRegistry.getFormatByExtension("pdf");
    DocumentFormat docDocumentFormat = _documentFormatRegistry.getFormatByExtension("doc");
    DocumentFormat xlsDocumentFormat = _documentFormatRegistry.getFormatByExtension("xls");
    DocumentFormat pptDocumentFormat = _documentFormatRegistry.getFormatByExtension("ppt");
    DocumentFormat odtDocumentFormat = _documentFormatRegistry.getFormatByExtension("odt");

    assertNotNull(pdfDocumentFormat);
    assertNotNull(docDocumentFormat);
    assertNotNull(xlsDocumentFormat);
    assertNotNull(pptDocumentFormat);
    assertNotNull(odtDocumentFormat);
  }

  @Test
  public void testGetFormatByMediaType() {
    DocumentFormat pdfDocumentFormat = _documentFormatRegistry.getFormatByMediaType(MimetypeMap.MIMETYPE_PDF);
    DocumentFormat docDocumentFormat = _documentFormatRegistry.getFormatByMediaType(MimetypeMap.MIMETYPE_WORD);
    DocumentFormat xlsDocumentFormat = _documentFormatRegistry.getFormatByMediaType(MimetypeMap.MIMETYPE_EXCEL);
    DocumentFormat pptDocumentFormat = _documentFormatRegistry.getFormatByMediaType(MimetypeMap.MIMETYPE_PPT);

    assertNotNull(pdfDocumentFormat);
    assertNotNull(docDocumentFormat);
    assertNotNull(xlsDocumentFormat);
    assertNotNull(pptDocumentFormat);
  }

  @Test
  public void testGetOutputFormats() {
    Set<DocumentFormat> outputFormats = _documentFormatRegistry.getOutputFormats(DocumentFamily.TEXT);

    assertTrue(outputFormats.size() == 1);

    outputFormats = _documentFormatRegistry.getOutputFormats(DocumentFamily.PRESENTATION);

    assertTrue(outputFormats.size() == 1);

    outputFormats = _documentFormatRegistry.getOutputFormats(DocumentFamily.SPREADSHEET);

    assertTrue(outputFormats.size() == 1);
  }

}
