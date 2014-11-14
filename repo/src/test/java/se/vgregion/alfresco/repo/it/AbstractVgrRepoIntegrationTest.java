package se.vgregion.alfresco.repo.it;

import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration({ "classpath:alfresco/application-context.xml", "classpath:alfresco/remote-api-context.xml", "classpath:alfresco/web-scripts-application-context.xml" })
public abstract class AbstractVgrRepoIntegrationTest extends AbstractRepoIntegrationTest {
  
}
