// @override projects/repository/source/java/org/alfresco/repo/admin/patch/impl/GenericEMailTemplateUpdatePatch.java
package se.vgregion.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.patch.impl.GenericEMailTemplateUpdatePatch;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.I18NUtil;

public class AddSwedishEmailTemplatesPatch extends GenericEMailTemplateUpdatePatch {
  private static final Logger logger = Logger.getLogger(AddSwedishEmailTemplatesPatch.class);
  private static final String[] LOCALES = new String[] {
    "sv"
  };
  private static final String[] PATHS = new String[] {
      "alfresco/templates/activities-email-templates/",
      "alfresco/bootstrap/notification/",
      "alfresco/templates/notify_email_templates/",
      "alfresco/templates/new-user-templates/",
      "alfresco/templates/invite-email-templates/",
      "alfresco/templates/following-email-templates/"
  };

  private static final String[] BASE_FILES = new String[] {
      "activities-email.ftl",
      "wf-email.html.ftl",
      "notify_user_email.html.ftl",
      "new-user-email.html.ftl",
      "invite-email.html.ftl",
      "following-email.html.ftl"
  };

  private static final String[] XPATHS = new String[] {
      "/app:company_home/app:dictionary/app:email_templates/cm:activities/cm:activities-email.ftl",
      "/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:invite-email.html.ftl",
      "/app:company_home/app:dictionary/app:email_templates/app:notify_email_templates/cm:notify_user_email.html.ftl",
      "/app:company_home/app:dictionary/app:email_templates/cm:invite/cm:new-user-email.html.ftl",
      "/app:company_home/app:dictionary/app:email_templates/cm:invite/cm:invite-email.html.ftl",
      "/app:company_home/app:dictionary/app:email_templates/app:following/cm:following-email.html.ftl"
  };

  @Override
  protected List<String> getSiblingFiles() {
    List<String> siblingFiles = new ArrayList<String>(getLocales().length);
    for (String locale : getLocales()) {
      siblingFiles.add(makeSiblingFileName2(getBaseFileName(), locale));
    }
    return siblingFiles;
  }

  /**
   * Bug in parent function which uses lastIndexOf to find name. Does not work
   * for .html.ftl files
   * 
   * @param baseFileName
   * @param locale
   * @return
   */
  private String makeSiblingFileName2(String baseFileName, String locale) {
    int index = baseFileName.indexOf(".");
    StringBuilder builder = new StringBuilder();
    builder.append(baseFileName.substring(0, index)).append("_").append(locale).append(baseFileName.substring(index));
    return builder.toString();
  }

  private int currentIndex = 0;

  private Repository repository;

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  @Override
  protected String getPath() {
    return PATHS[currentIndex];
  }

  @Override
  protected String getBaseFileName() {
    return BASE_FILES[currentIndex];
  }

  @Override
  protected String[] getLocales() {
    return LOCALES;
  }

  @Override
  protected NodeRef getBaseTemplate() {
    List<NodeRef> refs = searchService.selectNodes(repository.getRootHome(), XPATHS[currentIndex], null, namespaceService, false);
    if (refs.size() != 1) {
      logger.error("Could not bootstrap template. refs.size(): " + refs.size() + " xpath: " + XPATHS[currentIndex]);
      throw new AlfrescoRuntimeException(I18NUtil.getMessage("patch.addSwedishEmailTemplatesPatch.error"));
    }
    return refs.get(0);
  }

  /**
   * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
   */
  @Override
  protected String applyInternal() throws Exception {
    while (currentIndex < BASE_FILES.length) {
      updateTemplates();
      currentIndex++;
    }

    return I18NUtil.getMessage("patch.addSwedishEmailTemplatesPatch.result");
  }
}
