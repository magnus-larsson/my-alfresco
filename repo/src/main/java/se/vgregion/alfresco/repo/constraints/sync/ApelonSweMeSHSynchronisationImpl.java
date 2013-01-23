package se.vgregion.alfresco.repo.constraints.sync;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import se.vgregion.alfresco.repo.model.ApelonNode;

import java.util.List;

public class ApelonSweMeSHSynchronisationImpl extends ApelonSynchronisationImpl {

  @Override
  protected void synchroniseNodeList(List<ApelonNode> nodeList, Parent parent) {
    for (ApelonNode node : nodeList) {
      if (parent != null) {
        node.setParent(parent.node);
      }

      synchroniseNode(node, parent);

      if (node.isHasChildren() && _hierarchy) {
        List<ApelonNode> children = _apelonService.getVocabulary(_path + "/" + node.getPath(), false, 10000);

        NodeRef nodeRef = findNodeRef(node);

        if (!matchingNode(parent, node)) {
          continue;
        }

        synchroniseNodeList(children, new Parent(nodeRef, node));
      }
    }
  }

  private boolean matchingNode(Parent parent, ApelonNode node) {
    if (parent == null) {
      return true;
    }

    if (StringUtils.isBlank(parent.mn)) {
      return true;
    }

    if (!node.getProperties().containsKey("MN")) {
      return true;
    }

    if (!parent.mn.contains(".")) {
      return true;
    }

    String mn = node.getProperties().get("MN").iterator().next();

    return mn.startsWith(parent.mn + ".");
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Assert.hasText(_path);

    _hierarchy = true;
  }

}
