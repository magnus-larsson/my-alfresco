package se.vgregion.alfresco.repo.it;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

public class AddPdfaPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

  private static final Logger LOG = Logger.getLogger(AddPdfaPolicyIntegrationTest.class);

  private static final String DEFAULT_USERNAME = "testuser";

  @Test
  public void test() {
    LOG.debug("Starting test...");

    try {
      createUser(DEFAULT_USERNAME);

      AuthenticationUtil.runAs(new RunAsWork<Void>() {

        @Override
        public Void doWork() throws Exception {
          testAsUser();

          return null;
        }

      }, DEFAULT_USERNAME);
    } finally {
      deleteUser(DEFAULT_USERNAME);
    }

    LOG.debug("Ending test...");
  }

  protected void testAsUser() {
    SiteInfo site = createSite();

    try {
      testInSite(site);
    } finally {
      deleteSite(site);
    }
  }

  private void testInSite(SiteInfo site) {
    FileInfo fileInfo = uploadDocument(null, "apelonserviceimpl_testgetkeywords.pdf");

    NodeRef parentNode = fileInfo.getNodeRef();

    _nodeService.setType(parentNode, VgrModel.TYPE_VGR_DOCUMENT);
    _nodeService.addAspect(parentNode, VgrModel.ASPECT_PUBLISHED, null);

    _nodeService.setProperty(parentNode, VgrModel.PROP_TYPE_RECORD, "type record");
    _nodeService.setProperty(parentNode, VgrModel.PROP_TYPE_RECORD_ID, "dc.type.record");
    _nodeService.setProperty(parentNode, VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, "publisher project assignment");
    _nodeService.setProperty(parentNode, VgrModel.PROP_TITLE, "title");
    _nodeService.setProperty(parentNode, VgrModel.PROP_SOURCE_DOCUMENTID, "source.document.id");

    QName assocType = RenditionModel.ASSOC_RENDITION;
    QName renditionName = VgrModel.RD_PDFA;
    QName nodeType = ContentModel.TYPE_CONTENT;
    Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>();
    nodeProps.put(ContentModel.PROP_NAME, renditionName.getLocalName());
    nodeProps.put(ContentModel.PROP_CONTENT_PROPERTY_NAME, nodeType);

    ChildAssociationRef pdfNode = _nodeService.createNode(parentNode, assocType, renditionName, nodeType, nodeProps);

    _nodeService.addAspect(pdfNode.getChildRef(), RenditionModel.ASPECT_HIDDEN_RENDITION, nodeProps);

    ContentWriter writer = _contentService.getWriter(pdfNode.getChildRef(), ContentModel.PROP_CONTENT, true);

    writer.putContent(Thread.currentThread().getContextClassLoader().getResourceAsStream("apelonserviceimpl_testgetkeywords.pdf"));
    writer.guessMimetype("apelonserviceimpl_testgetkeywords.pdf");
    writer.guessEncoding();
    
    Serializable checksum = _nodeService.getProperty(parentNode, VgrModel.PROP_CHECKSUM_NATIVE);
    Serializable filename = _nodeService.getProperty(parentNode, VgrModel.PROP_TITLE_FILENAME_NATIVE);
    Serializable identifier = _nodeService.getProperty(parentNode, VgrModel.PROP_IDENTIFIER_NATIVE);
    Serializable mimetype = _nodeService.getProperty(parentNode, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE_NATIVE);
    Serializable extension = _nodeService.getProperty(parentNode, VgrModel.PROP_FORMAT_EXTENT_EXTENSION_NATIVE);

    Assert.assertNotNull(checksum);
    Assert.assertNotNull(filename);
    Assert.assertNotNull(identifier);
    Assert.assertNotNull(mimetype);
    Assert.assertNotNull(extension);
  }

}
