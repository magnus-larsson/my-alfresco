package se.vgregion.alfresco.repo.rendition.executer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.FailedRenditionInfo;
import se.vgregion.alfresco.repo.storage.StorageService;

/**
 * @author Niklas Ekman
 */
public class NodeEligibleForRerenderingEvaluator extends ActionConditionEvaluatorAbstractBase {

  private static final Logger LOG = Logger.getLogger(NodeEligibleForRerenderingEvaluator.class);

  public final static String NAME = "node-eligible-for-rerendering-evaluator";

  public final static String PARAM_RENDITION_NAME = "rendition-name";

  public final static String PARAM_RETRY_PERIOD = "retry-period";
  public final static String PARAM_RETRY_COUNT = "retry-count";
  public final static String PARAM_QUIET_PERIOD = "quiet-period";
  public final static String PARAM_QUIET_PERIOD_RETRIES_ENABLED = "quiet-period-retries-enabled";

  protected NodeService _nodeService;

  protected RenditionService _renditionService;

  protected StorageService _storageService;

  /**
   * Add parameter definitions
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    paramList.add(new ParameterDefinitionImpl(PARAM_RENDITION_NAME, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_RENDITION_NAME)));

    paramList.add(new ParameterDefinitionImpl(PARAM_RETRY_PERIOD, DataTypeDefinition.LONG, true, getParamDisplayLabel(PARAM_RETRY_PERIOD)));
    paramList.add(new ParameterDefinitionImpl(PARAM_RETRY_COUNT, DataTypeDefinition.INT, true, getParamDisplayLabel(PARAM_RETRY_COUNT)));

    paramList.add(new ParameterDefinitionImpl(PARAM_QUIET_PERIOD, DataTypeDefinition.LONG, true, getParamDisplayLabel(PARAM_QUIET_PERIOD)));
    paramList.add(new ParameterDefinitionImpl(PARAM_QUIET_PERIOD_RETRIES_ENABLED, DataTypeDefinition.BOOLEAN, true, getParamDisplayLabel(PARAM_QUIET_PERIOD_RETRIES_ENABLED)));
  }

  /**
   * @see ActionConditionEvaluatorAbstractBase#evaluateImpl(ActionCondition,
   *      NodeRef)
   */
  public boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef) {
    if (!_nodeService.exists(actionedUponNodeRef)) {
      return false;
    }

    Serializable renditionName = actionCondition.getParameterValue(PARAM_RENDITION_NAME);

    Serializable paramRetryPeriod = actionCondition.getParameterValue(PARAM_RETRY_PERIOD);
    Serializable paramRetryCount = actionCondition.getParameterValue(PARAM_RETRY_COUNT);
    Serializable paramQuietPeriod = actionCondition.getParameterValue(PARAM_QUIET_PERIOD);

    Serializable parameterValue = actionCondition.getParameterValue(PARAM_QUIET_PERIOD_RETRIES_ENABLED);
    Serializable paramQuietPeriodRetriesEnabled = parameterValue != null ? parameterValue : true;

    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) renditionName);

    // If there are no previous failed rendering attempts for this rendition ,
    // then the node is always eligible for a first try.
    Map<String, FailedRenditionInfo> failures = _storageService.getFailedRenditions(actionedUponNodeRef);

    if (failures.isEmpty() || !failures.containsKey(renditionQName.getLocalName())) {
      if (LOG.isDebugEnabled()) {
        StringBuilder msg = new StringBuilder();

        msg.append("Node ").append(actionedUponNodeRef).append(" has no matching ").append(VgrModel.ASSOC_FAILED_RENDITION).append(" child.");

        LOG.debug(msg.toString());
      }

      return true;
    }

    FailedRenditionInfo failedRenditionInfo = failures.get(renditionQName.getLocalName());

    // There is a cm:failedRendition child, so there has been at least one
    // failed execution of the given
    // rendition at some point.
    if (failedRenditionInfo.getMostRecentFailure() == null) {
      // The property should never be null like this, but just in case.
      return true;
    }

    // So how long ago did the given rendition fail?
    long nowMs = new Date().getTime();

    long failureTimeMs = failedRenditionInfo.getMostRecentFailure().getTime();

    final long timeSinceLastFailureMs = nowMs - failureTimeMs;

    // And how many failures have there been?
    final int failureCount = failedRenditionInfo.getFailureCount();

    if (LOG.isDebugEnabled()) {
      StringBuilder msg = new StringBuilder();

      msg.append("Comparing failure time of ").append(failedRenditionInfo.getMostRecentFailure()).append(" to now. Difference is ").append(timeSinceLastFailureMs).append(" ms. ").append(failureCount)
          .append(" existing failures.");

      LOG.debug(msg.toString());
    }

    if (failureCount >= (Integer) paramRetryCount) {
      boolean quietPeriodRetriesEnabled = (Boolean) paramQuietPeriodRetriesEnabled;

      return quietPeriodRetriesEnabled && timeSinceFailureExceedsLimit(paramQuietPeriod, timeSinceLastFailureMs);
    } else {
      return timeSinceFailureExceedsLimit(paramRetryPeriod, timeSinceLastFailureMs);
    }
  }

  private boolean timeSinceFailureExceedsLimit(Serializable failurePeriod, long timeSinceFailure) {
    // Is that time period greater than the specified offset?
    // We'll allow for -ve offset values.
    Long offsetLong = (Long) failurePeriod;
    
    long absOffset = Math.abs(offsetLong);

    if (LOG.isDebugEnabled()) {
      StringBuilder msg = new StringBuilder();
      
      msg.append("Offset is ").append(offsetLong).append(" ms.");
      
      LOG.debug(msg.toString());
    }

    return timeSinceFailure > absOffset;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setRenditionService(RenditionService renditionService) {
    _renditionService = renditionService;
  }
  
  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

}
