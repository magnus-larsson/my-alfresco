// @overridden projects/repository/source/java/org/alfresco/repo/version/VersionServiceImpl.java
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class extension makes the default versioning behaviour configurable.
 * Either, the first version is a minor or a major version. Standard Alfresco
 * behaviour as of 4.2 is to always generate a 1.0 version.
 * 
 * @author Marcus Svensson <marcus.svensson (at) redpill-linpro.com
 *
 */

public class CustomVersion2ServiceImpl extends Version2ServiceImpl {

  private static Log logger = LogFactory.getLog(CustomVersion2ServiceImpl.class);

  private boolean defaultVersionIsMajorVersion = true; // Default value

  /**
   * @see org.alfresco.cms.version.VersionService#ensureVersioningEnabled(NodeRef,Map)
   */
  public void ensureVersioningEnabled(NodeRef nodeRef, Map<QName, Serializable> versionProperties) {
    if (logger.isDebugEnabled()) {
      logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
      logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
    }

    // Don't alter the auditable aspect!
    boolean disableAuditable = policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE);
    if (disableAuditable) {
      policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
    }

    // Do we need to apply the aspect?
    if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
      // Only apply new properties that are version ones
      AspectDefinition versionable = dictionaryService.getAspect(ContentModel.ASPECT_VERSIONABLE);
      Set<QName> versionAspectProperties = versionable.getProperties().keySet();

      Map<QName, Serializable> props = new HashMap<QName, Serializable>();
      if (versionProperties != null && !versionProperties.isEmpty()) {
        for (QName prop : versionProperties.keySet()) {
          if (versionAspectProperties.contains(prop)) {
            // This property is one from the versionable aspect
            props.put(prop, versionProperties.get(prop));
          }
        }
      }

      // Add the aspect
      nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, props);
    }

    // Do we need to create the initial version history entry? By convention
    // this is always a major version.
    if (getVersionHistoryNodeRef(nodeRef) == null) {
      if (defaultVersionIsMajorVersion)
        createVersion(nodeRef, Collections.<String, Serializable> singletonMap(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR));
      else
        createVersion(nodeRef, Collections.<String, Serializable> singletonMap(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR));
    }

    // Put Auditable back
    if (disableAuditable) {
      policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
    }
  }

  public boolean isDefaultVersionIsMajorVersion() {
    return defaultVersionIsMajorVersion;
  }

  public void setDefaultVersionIsMajorVersion(boolean defaultVersionIsMajorVersion) {
    this.defaultVersionIsMajorVersion = defaultVersionIsMajorVersion;
  }

  /**
   * Initialise method
   */
  @Override
  public void initialise() {
    super.initialise();
    if (defaultVersionIsMajorVersion) {
      logger.debug("Initial versions will be a major version (eg. 1.0)");
    } else {
      logger.debug("Initial versions will be a minor version (eg. 0.1)");
    }
  }
}
