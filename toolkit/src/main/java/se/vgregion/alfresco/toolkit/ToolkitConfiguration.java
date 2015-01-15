package se.vgregion.alfresco.toolkit;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.util.CronTriggerBean;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailBean;

@Configuration
public class ToolkitConfiguration {
  
  @Autowired
  private RefreshPublishedCaches _refreshPublishedCaches;

  @Resource(name = "schedulerFactory")
  private Scheduler _scheduler;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Bean(name = "vgr.refreshPublishedCachesJobDetail")
  public JobDetailBean refreshPublishedCachesJobDetail() {
    JobDetailBean detail = new JobDetailBean();

    Map jobDataAsMap = new HashMap();
    jobDataAsMap.put("refreshPublishedCaches", _refreshPublishedCaches);

    detail.setJobClass(RefreshPublishedCachesJob.class);
    detail.setJobDataAsMap(jobDataAsMap);

    return detail;
  }

  @Bean(name = "vgr.refreshPublishedCachesTrigger")
  public CronTriggerBean refreshPublishedCachesTrigger() {
    CronTriggerBean trigger = new CronTriggerBean();

    trigger.setJobDetail(refreshPublishedCachesJobDetail());
    trigger.setScheduler(_scheduler);
    trigger.setCronExpression("0 0 4 1/1 * ? *");

    return trigger;
  }
}
