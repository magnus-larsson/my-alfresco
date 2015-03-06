package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;

/**
 * Moves a document to the storage that is placed in a folder with the vgr:watched aspect.
 */
public class MoveWatchedDocumentsPolicy extends AbstractPolicy implements OnCreateNodePolicy {

  private final static Logger LOG = Logger.getLogger(MoveWatchedDocumentsPolicy.class);

  protected StorageService _storageService;

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef fileNodeRef = childAssocRef.getChildRef();
    final NodeRef folderNodeRef = childAssocRef.getParentRef();

    runSafe(new DefaultRunSafe(fileNodeRef) {

      @Override
      public void execute() {
        // if the node does not exist, don't do anything...
        if (!_nodeService.exists(fileNodeRef)) {
          return;
        }

        // if the node is not of type "vgr:document", exit
        if (!_nodeService.getType(fileNodeRef).isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
          return;
        }

        // if the node does not have the required aspect "vgr:standard", exit
        if (!_nodeService.hasAspect(fileNodeRef, VgrModel.ASPECT_STANDARD)) {
          return;
        }

        // if the nodes folder does not have the required aspect "watched", exit
        if (!_nodeService.hasAspect(folderNodeRef, VgrModel.ASPECT_WATCHED)) {
          return;
        }

        //First prepare the node for storage
        prepareForStorage(fileNodeRef);
        
        // if we reach this point, all is well and good so let's move the file
        _storageService.moveToStorage(fileNodeRef);

        if (LOG.isDebugEnabled()) {
          LOG.debug(this.getClass().getName());
        }
      }

    });
  }
  
  private void prepareForStorage(final NodeRef fileNodeRef) {
    _behaviourFilter.disableBehaviour(fileNodeRef);
    String title = _serviceUtils.getStringValue(fileNodeRef, VgrModel.PROP_TITLE);

    if (StringUtils.isNotBlank(title)) {
      title = trimIllegalCharacters(title);
      
      // if the title is set, replicate it to cm:title
      _nodeService.setProperty(fileNodeRef, VgrModel.PROP_TITLE, title);
      _nodeService.setProperty(fileNodeRef, ContentModel.PROP_TITLE, title);

    }
    _behaviourFilter.enableBehaviour(fileNodeRef);
  }
  
  private String trimIllegalCharacters(String str) {
    int lengthBefore = -1;
    int lengthAfter = -2;
    while (lengthBefore != lengthAfter) {
      lengthBefore = str.length();
      if (StringUtils.isNotBlank(str)) {
        // trim the string
        str = str.trim();
        // remove all trailing dots
        str = str.replaceAll("\\.+$", "");
      }
      lengthAfter = str.length();
    }
    return str;
  }
  
  @Required
  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

}
