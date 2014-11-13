package se.vgregion.alfresco.repo.interceptors;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

public @Aspect class NodeServiceLoggerAspect {

  private final static Logger LOG = Logger.getLogger(NodeServiceLoggerAspect.class);

  @Pointcut("execution(* org.alfresco.service.cmr.repository.NodeService.setProperty(..))")
  private void hook() {
  }

  @Around("hook()")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    Object[] args = pjp.getArgs();

    NodeRef nodeRef = (NodeRef) args[0];
    QName qname = (QName) args[1];
    Serializable value = (Serializable) args[2];

    // LOG.debug("NodeService.setProperty(" + nodeRef + ", " + qname + ", " + value + ")");

    return pjp.proceed();
  }

  public static NodeServiceLoggerAspect aspectOf() {
    return new NodeServiceLoggerAspect();
  }

}
