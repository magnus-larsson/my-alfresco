package se.vgregion.alfresco.repo.interceptors;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;

public @Aspect class CTFootnotesAspect {

  private final static Logger LOG = Logger.getLogger(CTFootnotesAspect.class);

  private final static long DEFAULT_LIMIT = 10;

  @Pointcut("execution(* org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFootnotes.getFootnoteList(..))")
  private void hook1() {
  }

  @Pointcut("execution(* org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFootnotes.sizeOfFootnoteArray(..))")
  private void hook2() {
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Around("hook1()")
  public Object around1(ProceedingJoinPoint pjp) throws Throwable {
    List list = (List) pjp.proceed();

    if (list.size() <= getLimit()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("List size is less than or equal to limit.");
      }

      return list;
    }

    List result = new ArrayList();

    LOG.warn("List before limit is " + list.size());
    System.out.println("List before limit is " + list.size());

    for (int x = 0; x < getLimit(); x++) {
      result.add(list.get(x));
    }

    LOG.warn("List after limit is " + result.size());
    System.out.println("List after limit is " + result.size());

    for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
      LOG.warn(stackTraceElement.toString());
    }

    return result;
  }

  @Around("hook2()")
  public Object around2(ProceedingJoinPoint pjp) throws Throwable {
    Integer size = (Integer) pjp.proceed();

    if (size <= getLimit()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("List size is less than or equal to limit.");
      }

      return size;
    }

    LOG.warn("List before limit is " + size);

    LOG.warn("List after limit is " + getLimit());

    for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
      LOG.warn(stackTraceElement.toString());
    }

    return getLimit();
  }

  private long getLimit() {
    Properties properties = ApplicationContextHolder.getApplicationContext().getBean("global-properties", Properties.class);

    String limit = properties.getProperty("ctfootnotes.footnoteslist.limit", String.valueOf(DEFAULT_LIMIT));

    return Long.valueOf(limit);
  }

  public static CTFootnotesAspect aspectOf() {
    return new CTFootnotesAspect();
  }

}
