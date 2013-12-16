package se.vgregion.alfresco.repo.push.impl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.push.PuSHAtomFeedUtil;
import se.vgregion.alfresco.repo.push.PushJmsService;
import se.vgregion.docpublishing.publishdocumentevent.v1.PublishDocument;
import se.vgregion.docpublishing.unpublishdocumentevent.v1.UnpublishDocument;

/**
 * Service class for sending push messages to Mule
 * 
 * @author Marcus Svensson <marcus.svensson (at) redpill-linpro.com>
 * 
 */
public class PushJmsServiceImpl implements PushJmsService, InitializingBean {

  private final static Logger LOG = Logger.getLogger(PushJmsServiceImpl.class);
  private String _queueName;
  private String _consumerRemoteUrl;
  private String _producerLocalUrl;
  private String _vgrHdrSenderId;
  private String _vgrHdrReceiverId;
  private String _vgrHdrMessageTypeVersion;
  private NodeService _nodeService;
  private PuSHAtomFeedUtil _pushAtomFeedUtil;

  // Public for testing
  public static final String VGR_HDR_SENDER_ID = "vgrHdr_senderId";
  public static final String VGR_HDR_RECEIVER_ID = "vgrHdr_receiverId";
  public static final String VGR_HDR_MESSAGE_TYPE = "vgrHdr_messageType";
  public static final String PUBLISH_DOCUMENT_EVENT = "PublishDocumentEvent";
  public static final String UNPUBLISH_DOCUMENT_EVENT = "UnpublishDocumentEvent";
  public static final String DOCUMENT_STATUS_EVENT = "DocumentStatusEvent";
  public static final String FEED = "feed";

  public static final se.vgregion.docpublishing.publishdocumentevent.v1.ObjectFactory documentPublishObjectFactory = new se.vgregion.docpublishing.publishdocumentevent.v1.ObjectFactory();
  public static final se.vgregion.docpublishing.unpublishdocumentevent.v1.ObjectFactory documentUnPublishObjectFactory = new se.vgregion.docpublishing.unpublishdocumentevent.v1.ObjectFactory();

  // private BrokerService producerBroker;
  
  //To maintain cdata tags use something like this: https://jaxb.java.net/faq/JaxbCDATASample.java
  private String ObjectToXml(Object object) throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(object.getClass());

    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    OutputStream os = new ByteArrayOutputStream();

    m.marshal(object, os);

    return os.toString();
  }

  @Override
  public boolean pushToJms(List<NodeRef> nodeRefs, QName property) {
    if (nodeRefs == null || nodeRefs.size() == 0) {
      LOG.debug("No nodes available for sending");
      return false;
    } else if (property == null || !(VgrModel.PROP_PUSHED_FOR_PUBLISH.equals(property) || VgrModel.PROP_PUSHED_FOR_UNPUBLISH.equals(property))) {
      LOG.error("Type of JMS event missing or of unexpected type: " + property);
      return false;
    }

    try {

      if (LOG.isDebugEnabled())
        LOG.debug("Creating connection to " + _consumerRemoteUrl);
      ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(_consumerRemoteUrl);
      Connection connection = connectionFactory.createConnection();

      if (LOG.isDebugEnabled())
        LOG.debug("Creating session on queue " + _queueName);
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination destination = session.createQueue(_queueName);

      if (LOG.isDebugEnabled())
        LOG.debug("Starting producer");
      MessageProducer producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

      for (NodeRef nodeRef : nodeRefs) {
        TextMessage message = null;
        String requestId;
        if (VgrModel.PROP_PUSHED_FOR_PUBLISH.equals(property)) {
          // Publish event
          Date publishDate = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_PUBLISH);
          requestId = "publish_" + nodeRef.toString() + "_" + publishDate.getTime();
          PublishDocument createPublishDocument = documentPublishObjectFactory.createPublishDocument();
          createPublishDocument.setDocumentId(nodeRef.toString());
          createPublishDocument.setSource((String) _nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_ORIGIN));
          createPublishDocument.setSourceDocumentId((String) _nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_DOCUMENTID));
          createPublishDocument.setRequestId(requestId);
          String feedXml = _pushAtomFeedUtil.createPublishDocumentFeed(nodeRef);
          feedXml = "<![CDATA[" + feedXml +"]]>";
          createPublishDocument.setFeed(feedXml);
          if (LOG.isDebugEnabled()) {
            LOG.debug("PublishDocument contents: " + ObjectToXml(createPublishDocument));
          }
          message = session.createTextMessage(ObjectToXml(createPublishDocument));
          message.setJMSCorrelationID(requestId);
          message.setStringProperty(VGR_HDR_SENDER_ID, _vgrHdrSenderId);
          message.setStringProperty(VGR_HDR_RECEIVER_ID, _vgrHdrReceiverId);
          message.setStringProperty(VGR_HDR_MESSAGE_TYPE, PUBLISH_DOCUMENT_EVENT);
          if (LOG.isDebugEnabled())
            LOG.debug("Sending publishEvent: " + message.toString());
        } else if (VgrModel.PROP_PUSHED_FOR_UNPUBLISH.equals(property)) {
          // Unpublish event
          Date unpublishDate = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);
          requestId = "unpublish_" + nodeRef.toString() + "_" + unpublishDate.getTime();
          UnpublishDocument createUnpublishDocument = documentUnPublishObjectFactory.createUnpublishDocument();
          createUnpublishDocument.setDocumentId(nodeRef.toString());
          createUnpublishDocument.setSource((String) _nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_ORIGIN));
          createUnpublishDocument.setSourceDocumentId((String) _nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_DOCUMENTID));
          createUnpublishDocument.setRequestId(requestId);
          String feedXml = _pushAtomFeedUtil.createUnPublishDocumentFeed(nodeRef);
          feedXml = "<![CDATA[" + feedXml +"]]>";
          createUnpublishDocument.setFeed(feedXml);
          if (LOG.isDebugEnabled()) {
            LOG.debug("UnpublishDocument contents: " + ObjectToXml(createUnpublishDocument));
          }
          message = session.createTextMessage(ObjectToXml(createUnpublishDocument));
          message.setJMSCorrelationID(requestId);
          message.setStringProperty(VGR_HDR_SENDER_ID, _vgrHdrSenderId);
          message.setStringProperty(VGR_HDR_RECEIVER_ID, _vgrHdrReceiverId);
          message.setStringProperty(VGR_HDR_MESSAGE_TYPE, UNPUBLISH_DOCUMENT_EVENT);
          if (LOG.isDebugEnabled())
            LOG.debug("Sending unPublishEvent: " + message.toString());
        }
        if (message != null) {

          producer.send(message);
          if (LOG.isDebugEnabled())
            LOG.debug("Sent message");
        } else {
          LOG.error("No message to send");
        }
      }
      producer.close();
      if (LOG.isDebugEnabled())
        LOG.debug("Closed producer");
      session.close();
      if (LOG.isDebugEnabled())
        LOG.debug("Closed session");
      connection.close();
      if (LOG.isDebugEnabled())
        LOG.debug("Closed connection");

      return true;
    } catch (Exception e) {
      LOG.error("Error when sending JMS message", e);
      return false;
    }
  }

  public void setQueueName(String _queueName) {
    this._queueName = _queueName;
  }

  public void setProducerLocalUrl(String _producerLocalUrl) {
    this._producerLocalUrl = _producerLocalUrl;
  }

  public void setVgrHdrSenderId(String _vgrHdrSenderId) {
    this._vgrHdrSenderId = _vgrHdrSenderId;
  }

  public void setVgrHdrReceiverId(String _vgrHdrReceiverId) {
    this._vgrHdrReceiverId = _vgrHdrReceiverId;
  }

  public void setVgrHdrMessageTypeVersion(String _vgrHdrMessageTypeVersion) {
    this._vgrHdrMessageTypeVersion = _vgrHdrMessageTypeVersion;
  }

  public void setPushAtomFeedUtil(PuSHAtomFeedUtil pushAtomFeedUtil) {
    this._pushAtomFeedUtil = pushAtomFeedUtil;
  }

  public void setConsumerRemoteUrl(String _consumerRemoteUrl) {
    this._consumerRemoteUrl = _consumerRemoteUrl;
  }

  public void setNodeService(NodeService _nodeService) {
    this._nodeService = _nodeService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_queueName);
    Assert.notNull(_producerLocalUrl);
    Assert.notNull(_vgrHdrReceiverId);
    Assert.notNull(_vgrHdrSenderId);
    Assert.notNull(_vgrHdrMessageTypeVersion);
    Assert.notNull(_pushAtomFeedUtil);
    Assert.notNull(_consumerRemoteUrl);
    Assert.notNull(_nodeService);
  }

}
