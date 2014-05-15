package se.vgregion.alfresco.repo.push.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.push.PuSHAtomFeedUtil;

public class PushJmsServiceTest {

  /**
   * The URLs used by the producer and consumer to connect to their local
   * brokers
   */
  final static String PRODUCER_LOCAL_URL = "vm://producer", CONSUMER_LOCAL_URL = "vm://consumer";
  /**
   * The URL used by a remote broker to connect to the consumer broker
   */
  final static String CONSUMER_REMOTE_URL = "tcp://localhost:12345";

  final static String DEV_CONSUMER_REMOTE_URL = "failover:(tcp://vgms0141:61616)";

  final static String QUEUE_NAME = "DOCPUBLISHING.PUBLISHINGSTATUS.IN";

  final static String SENDER_ID = "Alfresco";
  final static String RECEIVER_ID = "Docpublishing";
  final static String MESSAGE_TYPE_VERSION = "1.0";

  final Logger logger = Logger.getLogger(PushJmsServiceTest.class);

  BrokerService consumerBroker, producerBroker;
  Mockery context;

  NodeService nodeService;
  PuSHAtomFeedUtil pushAtomFeedUtil;
  
  private static final String WORKSPACE_AND_STORE = "workspace://SpacesStore/";
  private static final String DUMMY_NODE_ID_PUBLISH = "cafebabe-cafe-babe-cafe-babecafebab1";
  private static final String DUMMY_NODE_ID_UNPUBLISH = "cafebabe-cafe-babe-cafe-babecafebab2";

  /**
   * Setup the producer and consumer broker
   * 
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {

    context = new JUnit4Mockery() {
      {
        setThreadingPolicy(new Synchroniser());
      }
    };

    nodeService = context.mock(NodeService.class);
    pushAtomFeedUtil = context.mock(PuSHAtomFeedUtil.class);
    
    context.checking(new Expectations() {
      {
        // siteService.getSite
        allowing(nodeService).getProperty(with(any(NodeRef.class)), with(equal(VgrModel.PROP_SOURCE_ORIGIN)));
        will(returnValue("Alfresco"));
        allowing(nodeService).getProperty(with(any(NodeRef.class)), with(equal(VgrModel.PROP_SOURCE_DOCUMENTID)));
        will(returnValue(WORKSPACE_AND_STORE + DUMMY_NODE_ID_PUBLISH));
        allowing(nodeService).getProperty(with(any(NodeRef.class)), with(equal(VgrModel.PROP_PUSHED_FOR_PUBLISH)));
        will(returnValue(new Date()));
        allowing(nodeService).getProperty(with(any(NodeRef.class)), with(equal(VgrModel.PROP_PUSHED_FOR_UNPUBLISH)));
        will(returnValue(new Date()));
        allowing(pushAtomFeedUtil).createPublishDocumentFeed(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_PUBLISH));
        will(returnValue("<xmlPub></xmlPub>"));
        allowing(pushAtomFeedUtil).createUnPublishDocumentFeed(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_UNPUBLISH));
        will(returnValue("<xmlUnPub></xmlUnPub>"));
      }
    });
    this.logger.info("Starting consumer broker");

    // Set memory usage, should be configured in activemq.xml
    // <systemUsage>
    // <systemUsage>
    // <memoryUsage>
    // <memoryUsage limit="20 mb"/>
    // </memoryUsage>
    // <storeUsage>
    // <storeUsage limit="1 gb"/>
    // </storeUsage>
    // <tempUsage>
    // <tempUsage limit="100 mb"/>
    // </tempUsage>
    // </systemUsage>
    // </systemUsage>
    SystemUsage memoryManager = new SystemUsage();

    MemoryUsage memoryUsage = new MemoryUsage();
    memoryUsage.setLimit(20 * 1024 * 1024); // 20mb
    memoryManager.setMemoryUsage(memoryUsage);

    StoreUsage storeUsage = new StoreUsage();
    storeUsage.setLimit(500 * 1024 * 1024); // 500mb
    memoryManager.setStoreUsage(storeUsage);

    TempUsage tempUsage = new TempUsage();
    tempUsage.setLimit(100 * 1024 * 1024); // 100mb
    memoryManager.setTempUsage(tempUsage);

    this.consumerBroker = new BrokerService();

    this.consumerBroker.setSystemUsage(memoryManager);

    this.consumerBroker.setBrokerName("consumer");
    this.consumerBroker.addConnector(PushJmsServiceTest.CONSUMER_LOCAL_URL);
    /* Explicitly add the remote URL so the broker is reachable via TCP */
    this.consumerBroker.addConnector(PushJmsServiceTest.CONSUMER_REMOTE_URL);
    this.consumerBroker.start();
  }

  @After
  public void tearDown() throws Exception {
    this.logger.info("Stopping consumer broker");
    this.consumerBroker.stop();
  }

  @Test
  public void testLocal() throws Exception {

    class ConsumerThread extends Thread {

      final Logger LOG = Logger.getLogger(ConsumerThread.class);
      /**
       * Since threads cannot throw exceptions (including AssertionError) use
       * this flag to determine if the test was successful
       */
      public boolean successPublish = false;
      public boolean successUnpublish = false;

      @Override
      public void run() {
        try {
          this.LOG.info("Creating connection");
          ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(CONSUMER_LOCAL_URL);
          Connection connection = connectionFactory.createConnection();
          connection.start();

          this.LOG.info("Creating session");
          Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          Destination destination = session.createQueue(QUEUE_NAME);

          this.LOG.info("Starting consumer");
          MessageConsumer consumer = session.createConsumer(destination);

          for (int i = 0; i < 2; i++) {
            this.LOG.info("Waiting for message");
            Message message = consumer.receive();

            Assert.assertNotNull("Did not receive message in time", message);
            Assert.assertTrue(message instanceof TextMessage);
            TextMessage textMessage = (TextMessage) message;
            this.LOG.info("Received message: " + textMessage);
            this.LOG.info("Message contents: " + textMessage.getText());
            String result = textMessage.getText();
            Assert.assertTrue(result.length() > 0);

            if (result.indexOf("<PublishDocument") > 0 && result.indexOf("<feed>&lt;xmlPub&gt;&lt;/xmlPub&gt;</feed>") > 0) {
              successPublish = true;
            } else if (result.indexOf("<UnpublishDocument") > 0 && result.indexOf("<feed>&lt;xmlUnPub&gt;&lt;/xmlUnPub&gt;</feed>") > 0) {
              successUnpublish = true;
            }

          }

        } catch (Exception e) {
          logger.error(e);
          e.printStackTrace();
        }
      }
    }

    class ProducerThread extends Thread {

      @Override
      public void run() {
        PushJmsServiceImpl pushJmsService = new PushJmsServiceImpl();
        pushJmsService.setQueueName(QUEUE_NAME);
        pushJmsService.setVgrHdrSenderId(SENDER_ID);
        pushJmsService.setVgrHdrReceiverId(RECEIVER_ID);
        pushJmsService.setProducerLocalUrl("vm://producerTest");
        pushJmsService.setConsumerRemoteUrl("failover:(" + CONSUMER_REMOTE_URL + ")");
        pushJmsService.setNodeService(nodeService);
        pushJmsService.setVgrHdrMessageTypeVersion(MESSAGE_TYPE_VERSION);
        pushJmsService.setPushAtomFeedUtil(pushAtomFeedUtil);
        try {
          pushJmsService.afterPropertiesSet();
        } catch (Exception e) {
          e.printStackTrace();
          Assert.assertTrue(false);
        }
        // Send some messages
        // Send publish event
        List<NodeRef> list = new ArrayList<NodeRef>();
        list.add(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_PUBLISH));
        pushJmsService.pushToJms(list, VgrModel.PROP_PUSHED_FOR_PUBLISH);
        // Send unpublish event
        list = new ArrayList<NodeRef>();
        list.add(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_UNPUBLISH));
        pushJmsService.pushToJms(list, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
      }
    }

    ConsumerThread consumer = new ConsumerThread();
    consumer.start();

    Thread producer = new ProducerThread();
    producer.start();
    producer.join();

    consumer.join(5000);

    Assert.assertTrue("Test failed", consumer.successPublish);
    Assert.assertTrue("Test failed", consumer.successUnpublish);
  }
/*
 * Uncomment to test against dev jms queue
  @Test
  public void testRemoteDev() throws Exception {
    PushJmsServiceImpl pushJmsService = new PushJmsServiceImpl();
    pushJmsService.setQueueName(QUEUE_NAME);
    pushJmsService.setMemoryUsageLimit(20 * 1024 * 1024);
    pushJmsService.setStoreUsageLimit(500 * 1024 * 1024);
    pushJmsService.setTempUsageLimit(100 * 1024 * 1024);
    pushJmsService.setProducerLocalUrl(PRODUCER_LOCAL_URL);
    pushJmsService.setVgrHdrSenderId(SENDER_ID);
    pushJmsService.setVgrHdrReceiverId(RECEIVER_ID);
    pushJmsService.setConsumerRemoteUrl(DEV_CONSUMER_REMOTE_URL);
    pushJmsService.setNodeService(nodeService);
    pushJmsService.setVgrHdrMessageTypeVersion(MESSAGE_TYPE_VERSION);

    try {
      pushJmsService.afterPropertiesSet();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.assertTrue(false);
    }
    // Send some messages
    // Send publish event
    List<NodeRef> list = new ArrayList<NodeRef>();
    list.add(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_PUBLISH));
    pushJmsService.pushToJms(list, VgrModel.PROP_PUSHED_FOR_PUBLISH);
    // Send unpublish event
    list = new ArrayList<NodeRef>();
    list.add(new NodeRef(WORKSPACE_AND_STORE + DUMMY_NODE_ID_UNPUBLISH));
    pushJmsService.pushToJms(list, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
  }
  */
}
