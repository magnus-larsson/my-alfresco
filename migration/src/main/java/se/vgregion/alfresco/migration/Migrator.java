package se.vgregion.alfresco.migration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Migrator {

  public static void main(final String[] args) {
    String[] files = { "/se/vgregion/alfresco/migration/applicationContext.xml", "/alfresco/module/vgr-repo/context/vgr-external-users-context.xml" };

    final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(files);

    final MigratorService migratorService = (MigratorService) applicationContext.getBean("migratorService");

    migratorService.migrate();
  }

}
