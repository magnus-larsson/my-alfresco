package se.vgregion.alfresco.repo.report;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ISO8601DateFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ReportSiteUsage {

  private static ServiceRegistry serviceRegistry;
  private static ActivityService activityService;
  public static final String DOCUMENT_LIBRARY = "documentLibrary";

  /**
   * Get the total size for all files and folders in a site in bytes
   *
   * @param siteNodeRef
   * @return
   */
  public long getSiteSize(NodeRef siteNodeRef) {
    NodeService nodeService = serviceRegistry.getNodeService();

    FileFolderService fileFolderService = serviceRegistry.getFileFolderService();

    long size = 0;

    if (nodeService.exists(siteNodeRef)) {
      NodeRef dlNodeRef = fileFolderService.searchSimple(siteNodeRef, DOCUMENT_LIBRARY);

      if (nodeService.exists(dlNodeRef)) {
        // List files in document library root and calculate size
        List<FileInfo> listFiles = fileFolderService.listFiles(dlNodeRef);

        for (FileInfo fileInfo : listFiles) {
          ContentData contentData = fileInfo.getContentData();

          if (contentData == null) {
            continue;
          }

          size = size + contentData.getSize();
        }

        // List all folders and calculate site for all files
        List<FileInfo> allFolders = fileFolderService.listDeepFolders(dlNodeRef, null);

        for (FileInfo folderInfo : allFolders) {
          NodeRef folderNodeRef = folderInfo.getNodeRef();

          listFiles = fileFolderService.listFiles(folderNodeRef);

          for (FileInfo fileInfo : listFiles) {
            ContentData contentData = fileInfo.getContentData();

            if (contentData == null) {
              continue;
            }

            size = size + contentData.getSize();
          }
        }
      }

      return size;
    } else {
      throw new InvalidNodeRefException("Site could not be found", siteNodeRef);
    }
  }

  /**
   * Get the number of members of a site
   *
   * @param site
   * @return
   */
  public int getNumberOfSiteMembers(SiteInfo site) {
    NodeService nodeService = serviceRegistry.getNodeService();
    SiteService siteService = serviceRegistry.getSiteService();
    if (nodeService.exists(site.getNodeRef())) {
      Map<String, String> listMembers = siteService.listMembers(
          site.getShortName(), null, null, 0, true);
      return listMembers.size();
    } else {
      throw new InvalidNodeRefException("Site could not be found",
          site.getNodeRef());
    }
  }

  /**
   * Get last activity on site (date)
   *
   * @param siteNodeRef
   * @return
   * @throws Exception
   */
  public Date getLastActivityOnSite(SiteInfo siteInfo) throws Exception {
    List<String> siteFeedEntries = activityService.getSiteFeedEntries(
        siteInfo.getShortName(), "json");
    JSONParser p = new JSONParser();

    if (siteFeedEntries.size() == 0) {
      return null;
    } else {
      Date lastDate = null;
      for (String feedEntry : siteFeedEntries) {
        JSONObject parsedObject = (JSONObject) p.parse(feedEntry);
        if (parsedObject != null) {
          String stringDate = (String) parsedObject.get("postDate");
          Date entryDate = ISO8601DateFormat.parse(stringDate);
          if (lastDate == null || entryDate.compareTo(lastDate) > 0) {
            lastDate = entryDate;
          }
        }
      }
      return lastDate;
    }
  }

  public ServiceRegistry getServiceRegistry() {
    return ReportSiteUsage.serviceRegistry;
  }

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    ReportSiteUsage.serviceRegistry = serviceRegistry;
  }

  public ActivityService getActivityService() {
    return activityService;
  }

  public void setActivityService(ActivityService activityService) {
    ReportSiteUsage.activityService = activityService;
  }

}
