package se.vgregion.alfresco.repo.scripts;

import java.util.Date;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.report.ReportSiteUsage;

public class ScriptSiteReport extends BaseScopableProcessorExtension implements InitializingBean {

  protected SiteService _siteService;

  protected ReportSiteUsage _reportSiteUsage;

  public void setSiteService(SiteService siteService) {
    _siteService = siteService;
  }

  public void setReportSiteUsage(ReportSiteUsage reportSiteUsage) {
    _reportSiteUsage = reportSiteUsage;
  }

  public long getSiteSize(String shortName) {
    Assert.hasText(shortName, "Invalid site short name '" + shortName + "' for getting size site.");

    SiteInfo site = _siteService.getSite(shortName);

    return _reportSiteUsage.getSiteSize(site.getNodeRef());
  }

  public int getNumberOfSiteMembers(String shortName) {
    Assert.hasText(shortName, "Invalid site short name '" + shortName + "' for getting size site.");

    SiteInfo site = _siteService.getSite(shortName);

    return _reportSiteUsage.getNumberOfSiteMembers(site);
  }

  public Date getLastActivityOnSite(String shortName) {
    Assert.hasText(shortName, "Invalid site short name '" + shortName + "' for getting size site.");

    SiteInfo site = _siteService.getSite(shortName);

    try {
      return _reportSiteUsage.getLastActivityOnSite(site);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_siteService);
    Assert.notNull(_reportSiteUsage);
  }

}
