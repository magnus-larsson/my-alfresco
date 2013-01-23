package se.vgregion.alfresco.repo.thumbnail;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import se.vgregion.alfresco.repo.content.transform.OpenOfficeTransformationOptions;
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

    if (!(args[0] instanceof OpenOfficeTransformationOptions)) {
      return pjp.proceed();
    }

    return PdfaRenderingEngine.NAME;
  }

  public static ThumbnailServiceAspect aspectOf() {
    return new ThumbnailServiceAspect();
  }

}
