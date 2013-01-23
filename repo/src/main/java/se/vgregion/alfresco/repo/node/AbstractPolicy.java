package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPolicy implements InitializingBean {

  protected NodeService _nodeService;

  protected PermissionService _permissionService;

  protected PolicyComponent _policyComponent;

  protected ServiceUtils _serviceUtils;

  protected BehaviourFilter _behaviourFilter;

  protected LockService _lockService;

  public void setPolicyComponent(final PolicyComponent policyComponent) {
    _policyComponent = policyComponent;
  }

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setPermissionService(final PermissionService permissionService) {
    _permissionService = permissionService;
  }

  public void setServiceUtils(final ServiceUtils serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void setLockService(final LockService lockService) {
    _lockService = lockService;
  }

  public boolean isDocumentLibrary(final NodeRef nodeRef) {
    return _serviceUtils.isDocumentLibrary(nodeRef);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_nodeService);
    Assert.notNull(_permissionService);
    Assert.notNull(_policyComponent);
    Assert.notNull(_serviceUtils);
    Assert.notNull(_behaviourFilter);
    Assert.notNull(_lockService, "You must provide an instance of the LockService.");
  }

  protected boolean shouldSkipPolicy(final NodeRef nodeRef) {
    // if the node does not exist, exit
    if (!_nodeService.exists(nodeRef)) {
      return true;
    }

    // don't do this for working copies
    if (_nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
      return true;
    }

    // if it's not the spaces store, exit
    if (!StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef())) {
      return true;
    }

    // if it's anything but locked, don't do anything
    if (_lockService.getLockStatus(nodeRef) != LockStatus.NO_LOCK) {
      return true;
    }

    // if it's not the document library, exit
    if (!isDocumentLibrary(nodeRef)) {
      return true;
    }

    if (!_nodeService.getType(nodeRef).isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
      return true;
    }

    return false;
  }

  public void runSafe(final RunSafe runSafe) {
    List<QName> classNames = runSafe.getClassNames();

    // disable behaviours for the list of classnames for this node
    for (QName className : classNames) {
      _behaviourFilter.disableBehaviour(runSafe.getNodeRef(), className);
    }

    try {
      String user = StringUtils.isNotBlank(runSafe.getUser()) ? runSafe.getUser() : AuthenticationUtil.getSystemUserName();

      AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
        @Override
        public Object doWork() throws Exception {
          runSafe.execute();

          return null;
        }
      }, user);
    } finally {
      // re-enable behaviours for the list of classnames for this node
      for (QName className : classNames) {
        _behaviourFilter.enableBehaviour(runSafe.getNodeRef(), className);
      }
    }
  }

}

interface RunSafe {

  NodeRef getNodeRef();

  String getUser();

  List<QName> getClassNames();

  void execute();

}

abstract class DefaultRunSafe implements RunSafe {

  private NodeRef _nodeRef;

  private String _user;

  private List<QName> _classNames;

  public DefaultRunSafe(NodeRef nodeRef) {
    this(nodeRef, null);
  }

  public DefaultRunSafe(NodeRef nodeRef, String user) {
    this(nodeRef, user, null);
  }

  public DefaultRunSafe(NodeRef nodeRef, String user, List<QName> classNames) {
    _nodeRef = nodeRef;
    _user = user;
    _classNames = classNames;

    if (_classNames == null) {
      _classNames = new ArrayList<QName>();
      _classNames.add(ContentModel.ASPECT_AUDITABLE);
      _classNames.add(ContentModel.ASPECT_VERSIONABLE);
    }
  }

  @Override
  public NodeRef getNodeRef() {
    return _nodeRef;
  }

  @Override
  public String getUser() {
    return _user;
  }

  @Override
  public List<QName> getClassNames() {
    return _classNames;
  }


}
