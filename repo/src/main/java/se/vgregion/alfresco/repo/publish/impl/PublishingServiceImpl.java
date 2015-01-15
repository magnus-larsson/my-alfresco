package se.vgregion.alfresco.repo.publish.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.publish.NodeRefCallbackHandler;
import se.vgregion.alfresco.repo.publish.PublishingService;
import se.vgregion.alfresco.repo.utils.ServiceUtils;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class PublishingServiceImpl implements PublishingService, InitializingBean {

  private final static Logger LOG = Logger.getLogger(PublishingServiceImpl.class);

  private ServiceUtils _serviceUtils;

  private NodeService _nodeService;

  @Override
  public List<NodeRef> findUnpublishedDocuments(Date availableDate) {
    ResultSet result = _serviceUtils.query(findUnpublishedDocumentsQuery(availableDate), null, null);

    List<NodeRef> nodeRefs = new ArrayList<NodeRef>();

    try {
      for (NodeRef nodeRef : result.getNodeRefs()) {
        Date availableto = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_TO);

        if (availableto == null) {
          continue;
        }

        if (availableDate.getTime() >= availableto.getTime()) {
          nodeRefs.add(nodeRef);
        }
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }

    return nodeRefs;
  }

  @Override
  public void findUnpublishedDocuments(Date availableDate, Date modifiedFrom, Date modifiedTo, NodeRefCallbackHandler callback, boolean excludeAlreadyPushed, Integer maxItems, Integer skipCount) {
    ResultSet result = _serviceUtils.query(findUnpublishedDocumentsQuery(availableDate, modifiedFrom, modifiedTo, excludeAlreadyPushed), maxItems, skipCount);

    try {
      for (NodeRef nodeRef : result.getNodeRefs()) {
        Date availableto = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_TO);

        if (availableto == null) {
          continue;
        }

        if (availableDate.getTime() >= availableto.getTime()) {
          callback.processNodeRef(nodeRef);
        }
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }
  }

  @Override
  public String findUnpublishedDocumentsQuery(Date availableDate) {
    return findUnpublishedDocumentsQuery(availableDate, null, null);
  }

  @Override
  public String findUnpublishedDocumentsQuery(Date availableDate, Date modifiedFrom, Date modifiedTo) {
    return findUnpublishedDocumentsQuery(availableDate, modifiedFrom, modifiedTo, true);
  }

  @Override
  public String findUnpublishedDocumentsQuery(Date availableDate, Date modifiedFrom, Date modifiedTo, boolean excludeAlreadyPushed) {
    ParameterCheck.mandatory("availableDate", availableDate);

    String sDate = formatDate(availableDate);

    StringBuffer query = new StringBuffer();

    query.append("TYPE:\"vgr:document\" AND ");
    query.append("ASPECT:\"vgr:published\" AND ");
    query.append("ISNOTNULL:\"vgr:dc.date.availableto\" AND ");
    query.append("vgr:dc\\.date\\.availableto:[MIN TO \"" + sDate + "\"] AND ");
    query.append("ISNOTNULL:\"vgr:pushed-for-publish\"");

    if (excludeAlreadyPushed) {
      query.append(" AND ISNULL:\"vgr:pushed-for-unpublish\"");
    }

    if (modifiedFrom != null && modifiedTo != null) {
      String utcFrom = formatDate(modifiedFrom);
      String utcTo = formatDate(modifiedTo);

      query.append(" AND cm:modified:[\"" + utcFrom + "\" TO \"" + utcTo + "\"]");
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Query for finding unpushed unpublished documents: " + query.toString());
    }

    return query.toString();
  }

  @Override
  public List<NodeRef> findPublishedDocuments(Date availableDate) {
    ResultSet result = _serviceUtils.query(findPublishedDocumentsQuery(availableDate), null, null);

    try {
      return result.getNodeRefs();
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }
  }

  @Override
  public void findPublishedDocuments(Date availableDate, Date modifiedFrom, Date modifiedTo, NodeRefCallbackHandler callback, boolean excludeAlreadyPushed, Integer maxItems, Integer skipCount) {
    ResultSet result = _serviceUtils.query(findPublishedDocumentsQuery(availableDate, modifiedFrom, modifiedTo, excludeAlreadyPushed), maxItems, skipCount);

    try {
      for (NodeRef nodeRef : result.getNodeRefs()) {
        callback.processNodeRef(nodeRef);
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }
  }

  @Override
  public String findPublishedDocumentsQuery(Date availableDate) {
    return findPublishedDocumentsQuery(availableDate, null, null);
  }

  @Override
  public String findPublishedDocumentsQuery(Date availableDate, Date modifiedFrom, Date modifiedTo) {
    return findPublishedDocumentsQuery(availableDate, modifiedFrom, modifiedTo, true);
  }

  @Override
  public String findPublishedDocumentsQuery(Date availableDate, Date modifiedFrom, Date modifiedTo, boolean excludeAlreadyPushed) {
    ParameterCheck.mandatory("availableDate", availableDate);

    String sDate = formatDate(availableDate);

    StringBuffer query = new StringBuffer();

    query.append("TYPE:\"vgr:document\" AND ");
    query.append("ASPECT:\"vgr:published\" AND ");
    query.append("vgr:dc\\.date\\.availablefrom:[MIN TO \"" + sDate + "\"] AND ");
    query.append("(ISNULL:\"vgr:dc.date.availableto\" OR vgr:dc\\.date\\.availableto:[\"" + sDate + "\" TO MAX])");

    if (excludeAlreadyPushed) {
      query.append(" AND ISNULL:\"vgr:pushed-for-publish\"");
    }

    if (modifiedFrom != null && modifiedTo != null) {
      String utcFrom = formatDate(modifiedFrom);
      String utcTo = formatDate(modifiedTo);

      query.append(" AND cm:modified:[\"" + utcFrom + "\" TO \"" + utcTo + "\"]");
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Query for finding unpushed published documents: " + query.toString());
    }

    return query.toString();
  }

  private String formatDate(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    return sdf.format(date);
  }

  @Override
  public boolean isPublished(NodeRef nodeRef) {
    return isPublished(nodeRef, false);
  }

  @Override
  public boolean isPublished(NodeRef nodeRef, boolean excludeAlreadyPushed) {
    return isPublished(nodeRef, excludeAlreadyPushed, false);
  }

  @Override
  public boolean isPublished(NodeRef nodeRef, boolean excludeAlreadyPushed, boolean onlyOK) {
    if (!_nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PUBLISHED)) {
      return false;
    }

    if (!_nodeService.getType(nodeRef).isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
      return false;
    }

    final long now = new Date().getTime();

    final Date availableFrom = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_FROM);
    final Date availableTo = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_DATE_AVAILABLE_TO);

    final long safeAvailableFrom = availableFrom != null ? availableFrom.getTime() : 0;
    final long safeAvailableTo = availableTo != null ? availableTo.getTime() : now;

    final boolean published = now <= safeAvailableTo && now >= safeAvailableFrom;

    if (!published) {
      return false;
    }

    if (excludeAlreadyPushed) {
      Date pushedForPublish = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_PUBLISH);
      Date pushedForUnpublish = (Date) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUSHED_FOR_UNPUBLISH);

      boolean alreadyPushed = pushedForPublish != null || pushedForUnpublish != null;

      if (alreadyPushed) {
        return false;
      }
    }
    
    if (onlyOK) {
      String status = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_PUBLISH_STATUS);
      
      if (StringUtils.isNotBlank(status)) {
        return "ok".equalsIgnoreCase(status);
      }
    }

    return true;
  }

  @Override
  public boolean isPublished(String nodeRef) {
    return isPublished(nodeRef, false);
  }

  @Override
  public boolean isPublished(String nodeRef, boolean excludeAlreadyPushed) {
    return isPublished(new NodeRef(nodeRef), excludeAlreadyPushed);
  }

  @Override
  public boolean isPublished(String nodeRef, boolean excludeAlreadyPushed, boolean onlyOK) {
    return isPublished(new NodeRef(nodeRef), excludeAlreadyPushed, onlyOK);
  }

  public void setServiceUtils(ServiceUtils serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("serviceUtils", _serviceUtils);
  }


}
