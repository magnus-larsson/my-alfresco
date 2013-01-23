package se.vgregion.alfresco.repo.security;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.springframework.beans.factory.InitializingBean;

public class EsbSecurityInit implements InitializingBean {

  @Override
  public void afterPropertiesSet() throws Exception {
    final SpringBusFactory bf = new SpringBusFactory();

    final Bus bus = bf.createBus("alfresco/module/vgr-repo/context/vgr-external-users-security.xml");

    BusFactory.setDefaultBus(bus);
  }

}
