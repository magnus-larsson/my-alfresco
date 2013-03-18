package se.vgregion.alfresco.repo.scripts;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.vgregion.alfresco.repo.report.ReportSiteUsage;

public class SitesReport extends DeclarativeWebScript {

	private static ServiceRegistry serviceRegistry;

	@Override
	protected Map<String, Object> executeImpl(final WebScriptRequest req,
			final Status status, final Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>();

		String sites = req.getParameter("sites");

		ReportSiteUsage rsu = new ReportSiteUsage();
		SiteService siteService = serviceRegistry.getSiteService();
		List<SiteInfo> allSites = siteService.listSites(null, null);
		List<Map<String, Serializable>> sitesResult = new ArrayList<Map<String, Serializable>>();
		try {
			for (SiteInfo site : allSites) {
				NodeRef nodeRef = site.getNodeRef();
				//long siteSize = rsu.getSiteSize(nodeRef);
				//long siteMembers = rsu.getNumberOfSiteMembers(site);
				//Date lastActivity = rsu.getLastActivityOnSite(site);
				Map<String, Serializable> siteMap = new HashMap<String, Serializable>();
				siteMap.put("shortName", site.getShortName());
				siteMap.put("title", site.getTitle());
				// siteMap.put("size", siteSize);
				// siteMap.put("members", siteMembers);
				DateFormat df = new SimpleDateFormat();
				//if (lastActivity != null) {
					// siteMap.put("lastActivity", df.format(lastActivity));
				//} else {
					// siteMap.put("lastActivity", "");
				//}
				for (int i=0; i < 2000;i++) {
					sitesResult.add(siteMap);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		model.put("recordsReturned", sitesResult.size());
		model.put("totalRecords", sitesResult.size());
		model.put("startIndex", 0);
		model.put("pageSize", sitesResult.size());
		model.put("sites", sitesResult);
		return model;
	}

	public ServiceRegistry getServiceRegistry() {
		return SitesReport.serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		SitesReport.serviceRegistry = serviceRegistry;
	}
}
