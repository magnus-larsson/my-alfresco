package se.vgregion.alfresco.repo.solr;

import static org.junit.Assert.*;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

public class InvalidSolrJsonChildAssocsTest {

  
  @Test
  public void childAssocRefToStringTest() {
    String name = "Test.docx";
    QName assocTypeQName = ContentModel.ASSOC_CONTAINS;
    NodeRef parentRef = new NodeRef("workspace://SpacesStore/parentNodeRef");
    QName childQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name);
    NodeRef childRef = new NodeRef("workspace://SpacesStore/childNodeRef");
    
    ChildAssociationRef car = new ChildAssociationRef(assocTypeQName, parentRef, childQName, childRef);
    
    assertEquals("workspace://SpacesStore/parentNodeRef|workspace://SpacesStore/childNodeRef|{http://www.alfresco.org/model/content/1.0}contains|{http://www.alfresco.org/model/content/1.0}Test.docx|false|-1", car.toString());
    
  }
  
  @Test
  public void childAssocRefToString_UTF8NameTest() {
    String name = "RF \u0015 154-2011 Riktlinjer medlemskap i f\u00f6rening o organisation.pdf";
    QName assocTypeQName = ContentModel.ASSOC_CONTAINS;
    NodeRef parentRef = new NodeRef("workspace://SpacesStore/parentNodeRef");
    QName childQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name);
    NodeRef childRef = new NodeRef("workspace://SpacesStore/childNodeRef");
    
    ChildAssociationRef car = new ChildAssociationRef(assocTypeQName, parentRef, childQName, childRef);
    
    assertEquals("workspace://SpacesStore/parentNodeRef|workspace://SpacesStore/childNodeRef|{http://www.alfresco.org/model/content/1.0}contains|{http://www.alfresco.org/model/content/1.0}RF \u0015 154-2011 Riktlinjer medlemskap i f\u00f6rening o organisation.pdf|false|-1", car.toString());
    
  }
}
