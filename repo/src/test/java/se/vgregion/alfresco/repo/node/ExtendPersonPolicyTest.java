package se.vgregion.alfresco.repo.node;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import se.vgregion.alfresco.repo.kivclient.KivWsClient;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.node.ExtendPersonPolicy.PersonInfoUpdater;

public class ExtendPersonPolicyTest {

  private static final String USERNAME = "kallekula";

  private static final String ORGANIZATION_DN = "ou=Bemanningsservice Poolmedarbetare,ou=Verksamhet Bemanningsservice,ou=Område 2,ou=Sahlgrenska Universitetssjukhuset,ou=Org,o=VGR";

  Mockery _context;

  NodeService _nodeService;

  KivWsClient _kivWsClient;

  BehaviourFilter _behaviourFilter;

  @Before
  public void setup() {
    _context = new JUnit4Mockery() {
      {
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
      }
    };

    _nodeService = _context.mock(NodeService.class);
    _kivWsClient = _context.mock(KivWsClient.class);
    _behaviourFilter = _context.mock(BehaviourFilter.class);
  }

  @Test
  public void test() throws IOException, JAXBException {
    final String expectedOrganisation = "VGR/Org/Sahlgrenska Universitetssjukhuset/Område 2/Verksamhet Bemanningsservice/Bemanningsservice Poolmedarbetare";

    _context.checking(new Expectations() {
      {
        allowing(_nodeService).getProperty(with(any(NodeRef.class)), with(equal(ContentModel.PROP_USERNAME)));
        will(returnValue(USERNAME));
        allowing(_nodeService).getProperty(with(any(NodeRef.class)), with(equal(VgrModel.PROP_PERSON_ORGANIZATION_DN)));
        will(returnValue(ORGANIZATION_DN));
        allowing(_behaviourFilter).disableBehaviour(with(any(NodeRef.class)));
        allowing(_nodeService).setProperty(with(any(NodeRef.class)), with(equal(ContentModel.PROP_ORGANIZATION)), with(equal(expectedOrganisation)));
        allowing(_nodeService).setProperty(with(any(NodeRef.class)), with(equal(ContentModel.PROP_ORGID)), with(equal(expectedOrganisation)));
        allowing(_behaviourFilter).enableBehaviour(with(any(NodeRef.class)));
      }
    });

    ExtendPersonPolicy policy = new ExtendPersonPolicy();
    policy.setNodeService(_nodeService);
    policy.setBehaviourFilter(_behaviourFilter);

    NodeRef personNodeRef = new NodeRef("workspace://SpaceStore/kallekula");

    PersonInfoUpdater updater = policy.new PersonInfoUpdater(personNodeRef);

    updater.runInternal();
  }

}
