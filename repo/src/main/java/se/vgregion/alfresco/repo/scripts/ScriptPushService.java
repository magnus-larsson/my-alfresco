package se.vgregion.alfresco.repo.scripts;

import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;

public class ScriptPushService extends BaseScopableProcessorExtension implements InitializingBean {

  protected NodeService _nodeService;

  protected BehaviourFilter _behaviourFilter;

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void send(final Scriptable documents) {
    _behaviourFilter.disableAllBehaviours();

    final ValueConverter converter = new ValueConverter();

    @SuppressWarnings("unchecked")
    final List<NodeRef> files = (List<NodeRef>) converter.convertValueForJava(documents);

    for (final NodeRef file : files) {
      _nodeService.setProperty(file, ContentModel.PROP_MODIFIED, new Date());
      _nodeService.setProperty(file, VgrModel.PROP_PUSHED_FOR_PUBLISH, null);
      _nodeService.setProperty(file, VgrModel.PROP_PUSHED_FOR_UNPUBLISH, null);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_nodeService);
    Assert.notNull(_behaviourFilter);
  }

}
