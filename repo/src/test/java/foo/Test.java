package foo;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.metadata.MetadataExtracterLimits;
import org.alfresco.repo.content.metadata.PoiMetadataExtracter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;


public class Test {
  
  @org.junit.Test
  public void foo() {
    MetadataExtracterLimits metadataExtracterLimit = new MetadataExtracterLimits();
    metadataExtracterLimit.setTimeoutMs(2000);
    
    Map<String, MetadataExtracterLimits> mimetypeLimits = new HashMap<String, MetadataExtracterLimits>();
    mimetypeLimits.put("application/vnd.ms-word.document.macroenabled.12", metadataExtracterLimit);
    
    PoiMetadataExtracter extracter = new PoiMetadataExtracter();
    extracter.register();
    extracter.setMimetypeLimits(mimetypeLimits);
    
    File file = new File("/Users/niklas/Downloads/test2.doc");
    ContentReader reader = new FileContentReader(file);
    reader.setMimetype("application/vnd.ms-word.document.macroenabled.12");
    Map<QName, Serializable> destination = null;
    
    extracter.extract(reader, destination);
  }

}
