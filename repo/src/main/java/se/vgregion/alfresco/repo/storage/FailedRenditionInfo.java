package se.vgregion.alfresco.repo.storage;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

public class FailedRenditionInfo {

  private Date _mostRecentFailure;

  private int _failureCount;

  private String _renditionName;

  private NodeRef _failedRenditionNode;

  public FailedRenditionInfo(String renditionName, Date failureDate, int failureCount, NodeRef failedRenditionNode) {
    _renditionName = renditionName;
    _mostRecentFailure = failureDate;
    _failureCount = failureCount;
    _failedRenditionNode = failedRenditionNode;
  }

  public Date getMostRecentFailure() {
    return _mostRecentFailure;
  }

  public void setMostRecentFailure(Date mostRecentFailure) {
    _mostRecentFailure = mostRecentFailure;
  }

  public int getFailureCount() {
    return _failureCount;
  }

  public void setFailureCount(int failureCount) {
    _failureCount = failureCount;
  }

  public String getRenditionName() {
    return _renditionName;
  }

  public void setRenditionName(String renditionName) {
    _renditionName = renditionName;
  }

  public NodeRef getFailedRenditionNode() {
    return _failedRenditionNode;
  }

  public void setFailedRenditionNode(NodeRef failedRenditionNode) {
    _failedRenditionNode = failedRenditionNode;
  }

}
