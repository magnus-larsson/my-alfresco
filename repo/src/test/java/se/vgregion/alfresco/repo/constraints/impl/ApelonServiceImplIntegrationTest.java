package se.vgregion.alfresco.repo.constraints.impl;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import keywordservices.wsdl.metaservice_vgr_se.v2.KeywordService;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.ApelonNode;
import vocabularyservices.wsdl.metaservice_vgr_se.v2.VocabularyService;

public class ApelonServiceImplIntegrationTest {

  @Test
  public void testDocumentType() {
    JaxWsProxyFactoryBean vocabularyServiceProxyFactory = new JaxWsProxyFactoryBean();
    vocabularyServiceProxyFactory.setServiceClass(VocabularyService.class);
    vocabularyServiceProxyFactory.setAddress("http://metadataservice.vgregion.se/vocabularyservice/VocabularyService");

    VocabularyService vocabularyService = (VocabularyService) vocabularyServiceProxyFactory.create();

    Client client = ClientProxy.getClient(vocabularyService);
    HTTPConduit conduit = (HTTPConduit) client.getConduit();
    HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
    httpClientPolicy.setAllowChunking(false);
    conduit.setClient(httpClientPolicy);

    ApelonServiceImpl apelonService = new ApelonServiceImpl();

    apelonService.setVocabularyService(vocabularyService);

    List<ApelonNode> result = apelonService.getVocabulary("Dokumenttyp VGR/Dokumenttyp VGR", true);

    System.out.println(result.size());

    Assert.assertTrue(result.size() > 0);
  }

  @Test
  public void testLanguage() {
    JaxWsProxyFactoryBean vocabularyServiceProxyFactory = new JaxWsProxyFactoryBean();
    vocabularyServiceProxyFactory.setServiceClass(VocabularyService.class);
    vocabularyServiceProxyFactory.setAddress("http://metadataservice.vgregion.se/vocabularyservice/VocabularyService");

    VocabularyService vocabularyService = (VocabularyService) vocabularyServiceProxyFactory.create();

    ApelonServiceImpl apelonService = new ApelonServiceImpl();

    apelonService.setVocabularyService(vocabularyService);

    List<ApelonNode> result = apelonService.getVocabulary("Språk/Språk");

    System.out.println(result.size());

    Assert.assertTrue(result.size() > 0);
  }

  @Test
  public void testVerksamhetsKod() {
    JaxWsProxyFactoryBean vocabularyServiceProxyFactory = new JaxWsProxyFactoryBean();
    vocabularyServiceProxyFactory.setServiceClass(VocabularyService.class);
    vocabularyServiceProxyFactory.setAddress("http://metadataservice.vgregion.se/vocabularyservice/VocabularyService");

    VocabularyService vocabularyService = (VocabularyService) vocabularyServiceProxyFactory.create();

    ApelonServiceImpl apelonService = new ApelonServiceImpl();

    apelonService.setVocabularyService(vocabularyService);

    List<ApelonNode> result = apelonService.getVocabulary("Verksamhetskod/Verksamhetskod");

    System.out.println(result.size());

    Assert.assertTrue(result.size() > 0);
  }

  @Test
  public void testStatus() {
    JaxWsProxyFactoryBean vocabularyServiceProxyFactory = new JaxWsProxyFactoryBean();
    vocabularyServiceProxyFactory.setServiceClass(VocabularyService.class);
    vocabularyServiceProxyFactory.setAddress("http://metadataservice.vgregion.se/vocabularyservice/VocabularyService");

    VocabularyService vocabularyService = (VocabularyService) vocabularyServiceProxyFactory.create();

    ApelonServiceImpl apelonService = new ApelonServiceImpl();

    apelonService.setVocabularyService(vocabularyService);

    List<ApelonNode> result = apelonService.getVocabulary("Dokumentstatus/Dokumentstatus");

    System.out.println(result.size());

    Assert.assertTrue(result.size() > 0);
  }

  @Test
  public void testRecordType() {
    JaxWsProxyFactoryBean vocabularyServiceProxyFactory = new JaxWsProxyFactoryBean();
    vocabularyServiceProxyFactory.setServiceClass(VocabularyService.class);
    vocabularyServiceProxyFactory.setAddress("http://metadataservice.vgregion.se/vocabularyservice/VocabularyService");

    VocabularyService vocabularyService = (VocabularyService) vocabularyServiceProxyFactory.create();

    ApelonServiceImpl apelonService = new ApelonServiceImpl();

    apelonService.setVocabularyService(vocabularyService);

    List<ApelonNode> result = apelonService.getVocabulary("Handlingstyp/Handlingstyp");

    System.out.println(result.size());

    Assert.assertTrue(result.size() > 0);
  }

  @Test
  public void testRecordTypeWithFilterOnDocumentTypeId() {
    JaxWsProxyFactoryBean vocabularyServiceProxyFactory = new JaxWsProxyFactoryBean();
    vocabularyServiceProxyFactory.setServiceClass(VocabularyService.class);
    vocabularyServiceProxyFactory.setAddress("http://metadataservice.vgregion.se/vocabularyservice/VocabularyService");

    VocabularyService vocabularyService = (VocabularyService) vocabularyServiceProxyFactory.create();

    ApelonServiceImpl apelonService = new ApelonServiceImpl();

    apelonService.setVocabularyService(vocabularyService);

    List<ApelonNode> result = apelonService.findNodes("Handlingstyp", "Dokumenttyp", "1717562289");

    System.out.println(result.size());

    Assert.assertTrue(result.size() > 0);
  }

  @Test
  public void testRecordTypeWithFindNodes() {
    JaxWsProxyFactoryBean vocabularyServiceProxyFactory = new JaxWsProxyFactoryBean();
    vocabularyServiceProxyFactory.setServiceClass(VocabularyService.class);
    vocabularyServiceProxyFactory.setAddress("http://metadataservice.vgregion.se/vocabularyservice/VocabularyService");

    VocabularyService vocabularyService = (VocabularyService) vocabularyServiceProxyFactory.create();

    ApelonServiceImpl apelonService = new ApelonServiceImpl();

    apelonService.setVocabularyService(vocabularyService);

    List<ApelonNode> result = apelonService.findNodes("Handlingstyp", null, null);

    System.out.println(result.size());

    Assert.assertTrue(result.size() > 0);
  }

  @Test
  public void testGetKeywords() throws IOException {
    JaxWsProxyFactoryBean keywordServiceProxyFactory = new JaxWsProxyFactoryBean();
    keywordServiceProxyFactory.setServiceClass(KeywordService.class);
    keywordServiceProxyFactory.setAddress("http://metadataservice.vgregion.se/keywordservice/KeywordService");

    KeywordService keywordService = (KeywordService) keywordServiceProxyFactory.create();

    ApelonServiceImpl apelonService = new ApelonServiceImpl();

    apelonService.setKeywordService(keywordService);

    PDDocument document = PDDocument.load(this.getClass().getResourceAsStream("/apelonserviceimpl_testgetkeywords.pdf"));

    PDFTextStripper stripper = new PDFTextStripper();

    String text = stripper.getText(document);

    apelonService.getKeywords(text);
  }

}
