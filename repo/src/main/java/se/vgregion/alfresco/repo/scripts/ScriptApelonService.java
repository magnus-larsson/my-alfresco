package se.vgregion.alfresco.repo.scripts;

import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;

import se.vgregion.alfresco.repo.constraints.ApelonService;
import se.vgregion.alfresco.repo.jobs.ClusteredExecuter;
import se.vgregion.alfresco.repo.model.ApelonNode;

public class ScriptApelonService extends BaseScopableProcessorExtension {

  private ApelonService _apelonService;

  private List<ClusteredExecuter> _synchronisations;

  private ServiceRegistry _serviceRegistry;

  public void setApelonService(final ApelonService apelonService) {
    _apelonService = apelonService;
  }

  public void setSynchronisations(final List<ClusteredExecuter> synchronisations) {
    _synchronisations = synchronisations;
  }

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    _serviceRegistry = serviceRegistry;
  }

  public List<ApelonNode> getVocabulary(final String path, final boolean sort) {
    return _apelonService.getVocabulary(path, sort);
  }

  public List<ApelonNode> findNodes(final String namespace, final String propertyName, final String propertyValue, final boolean sort) {
    return _apelonService.findNodes(namespace, propertyName, propertyValue, sort, 100000);
  }

  public List<NodeRef> getNodes(final String apelonType) {
    return _apelonService.getNodes(apelonType);
  }

  public List<NodeRef> getDocumentTypeList() {
    return _apelonService.getDocumentTypeList();
  }

  public List<NodeRef> getRecordTypeList(final String documentTypeId) {
    return _apelonService.getRecordTypeList(documentTypeId);
  }

  public String[] getKeywords(String sNodeRef) {
    NodeRef nodeRef = new NodeRef(sNodeRef);

    ScriptNode document = new ScriptNode(nodeRef, _serviceRegistry, getScope());

    ScriptNode textScriptNode = document.transformDocument("text/plain");

    if (textScriptNode == null) {
      return new String[0];
    }

    String text;

    try {
      text = textScriptNode.getContent();

      if (StringUtils.isBlank(text)) {
        return new String[0];
      }
    } finally {
      textScriptNode.remove();
    }

    List<ApelonNode> keywords = _apelonService.getKeywords(text);

    String[] result = new String[keywords.size()];

    for (int x = 0; x < keywords.size(); x++) {
      ApelonNode apelonNode = keywords.get(x);

      result[x] = apelonNode.getInternalId();
    }

    return result;
  }

  public void synchronise() {
    for (final ClusteredExecuter synchronisation : _synchronisations) {
      synchronisation.execute();
    }
  }

}
