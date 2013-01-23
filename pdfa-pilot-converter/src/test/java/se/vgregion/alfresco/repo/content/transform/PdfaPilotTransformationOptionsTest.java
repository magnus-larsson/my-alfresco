package se.vgregion.alfresco.repo.content.transform;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@ContextConfiguration(locations = {"classpath:test-pdfa-pilot-convert-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class PdfaPilotTransformationOptionsTest {

  @Autowired
  private ApplicationContext _applicationContext;

  public void testClass() {
    PdfaPilotTransformationOptions options = new PdfaPilotTransformationOptions();
  }

  @Test
  public void testPdfaOptions() {
    PdfaPilotTransformationOptions options = (PdfaPilotTransformationOptions) _applicationContext.getBean("pdfaPilot.pdfaOptions");

    assertEquals("1b", options.getLevel());
    assertFalse(options.isOptimize());
  }

  @Test
  public void testPdfOptions() {
    PdfaPilotTransformationOptions options = (PdfaPilotTransformationOptions) _applicationContext.getBean("pdfaPilot.pdfOptions");

    assertTrue(StringUtils.isBlank(options.getLevel()));
    assertTrue(options.isOptimize());
  }

}
