package se.vgregion.alfresco.repo.rendition.executer;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;

public class AddFailedRenditionActionExecuter extends ActionExecuterAbstractBase {

  private static final Logger LOG = Logger.getLogger(AddFailedRenditionActionExecuter.class);

  private NodeService _nodeService;

  private BehaviourFilter _behaviourFilter;

  private RenditionService _renditionService;

  /**
   * The action bean name.
   */
  public static final String NAME = "add-failed-rendition";

  /**
   * The name of the failed rendition e.g. pdfa or pdf.
   */
  public static final String PARAM_RENDITION_NAME = "rendition-name";

  /**
   * The parameter defines the failure datetime to be recorded against the
   * source node. We explicitly require a parameterised value for this (rather
   * than simply using 'now') because this action is executed asynchronously and
   * there is the possibility that the time of action execution is later than
   * the actual failure time.
   */
  public static final String PARAM_FAILURE_DATETIME = "failure-datetime";

  @Override
  protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
    // This logic must always be executed as the 'system' user as it is possible
    // that a user with read-only access to a node will trigger a rendition
    // failure and thereby trigger the execution of this action.
    AuthenticationUtil.pushAuthentication();

    try {
      doExecute(action, actionedUponNodeRef);
    } finally {
      AuthenticationUtil.popAuthentication();
    }
  }

  private void doExecute(Action action, NodeRef actionedUponNodeRef) {
    RunAsWork<Void> runAsWork = getUnitOfWork(action, actionedUponNodeRef);

    AuthenticationUtil.runAsSystem(runAsWork);
  }

  private RunAsWork<Void> getUnitOfWork(final Action action, final NodeRef actionedUponNodeRef) {
    return new RunAsWork<Void>() {
      
      @Override
      public Void doWork() throws Exception {
        if (!_nodeService.exists(actionedUponNodeRef)) {
          return null;
        }

        Map<String, Serializable> parameterValues = action.getParameterValues();

        Date failureDateTime = (Date) parameterValues.get(PARAM_FAILURE_DATETIME);

        String renditionName = (String) parameterValues.get(PARAM_RENDITION_NAME);

        QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, renditionName);

        if (LOG.isDebugEnabled()) {
          logInfo(actionedUponNodeRef, renditionName, renditionQName, failureDateTime);
        }

        if (!_nodeService.hasAspect(actionedUponNodeRef, VgrModel.ASPECT_FAILED_RENDITION_SOURCE)) {
          _behaviourFilter.disableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);

          try {
            _nodeService.addAspect(actionedUponNodeRef, VgrModel.ASPECT_FAILED_RENDITION_SOURCE, null);
          } finally {
            _behaviourFilter.enableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
          }
        }

        List<ChildAssociationRef> failedChildren = _nodeService.getChildAssocs(actionedUponNodeRef, VgrModel.ASSOC_FAILED_RENDITION, renditionQName);

        NodeRef childNode = failedChildren.isEmpty() ? null : failedChildren.get(0).getChildRef();

        // Does the actionedUponNodeRef already have a child for this rendition?
        if (childNode == null) {
          // No existing failedRendition child, so this is a first time
          // failure to render this source node with the current
          // rendition definition
          // We'll create a new failedRendition child under the source node.
          Map<QName, Serializable> props = new HashMap<QName, Serializable>();

          props.put(VgrModel.PROP_FAILED_RENDITION_TIME, failureDateTime);
          props.put(ContentModel.PROP_FAILURE_COUNT, 1);

          _behaviourFilter.disableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);

          try {
            // The association is named after the failed rendition.
            _nodeService.createNode(actionedUponNodeRef, VgrModel.ASSOC_FAILED_RENDITION, renditionQName, VgrModel.TYPE_FAILED_RENDITION, props);
          } finally {
            _behaviourFilter.enableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
          }
        } else {
          // There is already an existing failedRendition child, so this is a
          // repeat failure to perform the same rendition. Therefore we don't
          // need to create a new failedRendition child. But we do need to
          // update the failedRenditionTime property.
          _nodeService.setProperty(childNode, VgrModel.PROP_FAILED_RENDITION_TIME, failureDateTime);

          // and increment the failure count.
          int currentFailureCount = (Integer) _nodeService.getProperty(childNode, ContentModel.PROP_FAILURE_COUNT);

          _nodeService.setProperty(childNode, ContentModel.PROP_FAILURE_COUNT, currentFailureCount + 1);
        }

        return null;
      }
    };
  }

  protected void logInfo(NodeRef actionedUponNodeRef, String renditionName, QName renditionQName, Date failureDateTime) {
    ChildAssociationRef renditionChildAssociationRef = _renditionService.getRenditionByName(actionedUponNodeRef, renditionQName);

    NodeRef existingRendition = renditionChildAssociationRef != null ? renditionChildAssociationRef.getChildRef() : null;

    StringBuilder msg = new StringBuilder();
    msg.append("Adding ").append(VgrModel.ASPECT_FAILED_RENDITION_SOURCE).append(" to ").append(actionedUponNodeRef);
    LOG.debug(msg.toString());

    msg = new StringBuilder();
    msg.append("  failed rendition is ").append(renditionName);
    LOG.debug(msg.toString());

    msg = new StringBuilder();
    msg.append("  failed datetime is ").append(failureDateTime);
    LOG.debug(msg.toString());

    msg = new StringBuilder();
    msg.append("  existing rendition is ").append(existingRendition);
    LOG.debug(msg.toString());
  }

  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> parameterList) {
    parameterList.add(new ParameterDefinitionImpl(PARAM_RENDITION_NAME, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_RENDITION_NAME), false));
    parameterList.add(new ParameterDefinitionImpl(PARAM_FAILURE_DATETIME, DataTypeDefinition.DATETIME, true, getParamDisplayLabel(PARAM_FAILURE_DATETIME), false));
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void setRenditionService(RenditionService renditionService) {
    _renditionService = renditionService;
  }

}
