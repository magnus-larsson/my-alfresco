package se.vgregion.alfresco.repo.action.executor;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import se.vgregion.alfresco.repo.storage.StorageService;

public class RecreatePdfaActionExecuter extends ActionExecuterAbstractBase {

  private StorageService _storageService;
  
  private NodeService _nodeService;

  @Override
  protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
    NodeRef pdfaRendition = _storageService.getPdfaRendition(actionedUponNodeRef);

    if (pdfaRendition != null) {
      _nodeService.deleteNode(pdfaRendition);
    }

    boolean result = _storageService.createPdfaRendition(actionedUponNodeRef, false);

    if (!result) {
      throw new RuntimeException("Failed to create PDF/A rendition for node '" + actionedUponNodeRef + "'.");
    }
  }

  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
  }

  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }
  
  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

}
