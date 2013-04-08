package se.vgregion.alfresco.repo.thumbnail;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redpill.alfresco.repo.content.transform.PdfaPilotTransformationOptions;

import se.vgregion.alfresco.repo.content.transform.OpenOfficeTransformationOptions;
import se.vgregion.alfresco.repo.rendition.executer.PdfaPilotRenderingEngine;
import se.vgregion.alfresco.repo.rendition.executer.PdfaRenderingEngine;

@Aspect
public class ThumbnailServiceAspect {

  @Pointcut("execution(* org.alfresco.repo.thumbnail.ThumbnailServiceImpl.getRenderingEngineNameFor(..))")
  private void hook() {
  }

  @Around("hook()")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    String method = pjp.getSignature().getName();

    if (!method.equalsIgnoreCase("getRenderingEngineNameFor")) {
      return pjp.proceed();
    }

    Object[] args = pjp.getArgs();

    if (args.length != 1) {
      return pjp.proceed();
    }

    if (args[0] instanceof OpenOfficeTransformationOptions) {
      return PdfaRenderingEngine.NAME;
    }

    if (args[0] instanceof PdfaPilotTransformationOptions) {
      return PdfaPilotRenderingEngine.NAME;
    }

    return pjp.proceed();
  }

  public static ThumbnailServiceAspect aspectOf() {
    return new ThumbnailServiceAspect();
  }

}
