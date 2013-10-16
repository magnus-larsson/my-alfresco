package se.vgregion.alfresco.repo.webdav;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.alfresco.repo.webdav.LockInfo;
import org.alfresco.repo.webdav.WebDAV;
import org.alfresco.repo.webdav.WebDAVHelper;
import org.alfresco.repo.webdav.WebDAVLockService;
import org.alfresco.repo.webdav.WebDAVMethod;
import org.alfresco.repo.webdav.WebDAVServerException;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.ReflectionUtils;

import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;

/**
 * This beast of an aspect overrides the default behaviour of WebDAVMethod.checkNode() to include the hotfix from 4.1.5.5. 
 * TODO: Remove this when the hotfix is in the product, maybe 4.1.7.
 * 
 * @author niklas
 */
@Aspect
public class WebDAVMethodAspect {

  @Pointcut("execution(* org.alfresco.repo.webdav.WebDAVMethod.checkNode(..))")
  private void hook() {
  }

  @Around("hook()")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    String method = pjp.getSignature().getName();

    if (!method.equalsIgnoreCase("checkNode")) {
      return pjp.proceed();
    }

    Object[] arguments = pjp.getArgs();

    // if it's not the 3 argument version, just exit
    if (arguments.length != 3) {
      return pjp.proceed();
    }

    // eliminate all calls that's not correct
    if (!(arguments[0] instanceof FileInfo)) {
      return pjp.proceed();
    }

    if (!(arguments[1] instanceof Boolean)) {
      return pjp.proceed();
    }

    if (!(arguments[2] instanceof Boolean)) {
      return pjp.proceed();
    }

    WebDAVMethod object = (WebDAVMethod) pjp.getThis();

    return checkNode(object, (FileInfo) arguments[0], (Boolean) arguments[1], (Boolean) arguments[2]);
  }

  /**
   * This method mimics the one in Alfresco Enterprise 4.1.5.5, which is a
   * hotfix for 4.1.5 and NOT included in 4.1.6. The difference apart from all
   * the gucky reflection stuff is isLockedAndNotLockOwner() which has to be
   * reimplemented, cause it's added in 4.1.5.5.
   * 
   * @param object
   * @param fileInfo
   * @param ignoreShared
   * @param lockMethod
   * @return
   * @throws WebDAVServerException
   */
  protected LockInfo checkNode(WebDAVMethod object, FileInfo fileInfo, boolean ignoreShared, boolean lockMethod) throws WebDAVServerException {
    LockInfo nodeLockInfo = getNodeLockInfo(object, fileInfo);

    String nodeLockToken = nodeLockInfo.getToken();

    String nodeETag = getDAVHelper().makeQuotedETag(fileInfo);

    NodeRef nodeRef = fileInfo.getNodeRef();

    // Regardless of WebDAV locks, if we can't write to this node, then it's
    // locked!
    if (isLockedAndNotLockOwner(nodeRef)) {
      throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
    }

    // Handle the case where there are no conditions and no lock token stored on
    // the node. Node just needs to be writable with no shared locks
    if (getConditions(object) == null) {
      // ALF-3681 fix. WebDrive 10 client doesn't send If header when locked
      // resource is updated so check the node by lockOwner.
      if (nodeLockToken == null || (getUserAgent(object) != null && getUserAgent(object).equals(WebDAV.AGENT_MICROSOFT_DATA_ACCESS_INTERNET_PUBLISHING_PROVIDER_DAV))) {
        if (!ignoreShared && nodeLockInfo.getSharedLockTokens() != null) {
          throw new WebDAVServerException(WebDAV.WEBDAV_SC_LOCKED);
        }

        return nodeLockInfo;
      }
    }

    // Checking of the If tag consists of two checks:
    // 1. Check the appropriate lock token for the node has been supplied (if
    // the node is locked)
    // 2. If there are conditions, check at least one condition (corresponding
    // to this node) is satisfied.
    checkLockToken(object, nodeLockInfo, ignoreShared, lockMethod);
    checkConditions(object, nodeLockToken, nodeETag);

    return nodeLockInfo;
  }

  private boolean isLockedAndNotLockOwner(NodeRef nodeRef) {
    LockStatus lockStatus = getLockService().getLockStatus(nodeRef);
    
    switch (lockStatus) {
    case NO_LOCK:
    case LOCK_EXPIRED:
    case LOCK_OWNER:
      return false;
    default:
      return true;
    }
  }

  private WebDAVLockService getLockService() {
    return (WebDAVLockService) ApplicationContextHolder.getApplicationContext().getBean("webDAVLockService");
  }

  private void checkConditions(WebDAVMethod object, String nodeLockToken, String nodeETag) {
    Method[] methods = ReflectionUtils.getAllDeclaredMethods(WebDAVMethod.class);

    for (Method method : methods) {
      if (!method.getName().equals("checkConditions")) {
        continue;
      }

      ReflectionUtils.makeAccessible(method);

      ReflectionUtils.invokeMethod(method, object, nodeLockToken, nodeETag);
    }
  }

  private void checkLockToken(WebDAVMethod object, LockInfo nodeLockInfo, boolean ignoreShared, boolean lockMethod) {
    Method[] methods = ReflectionUtils.getAllDeclaredMethods(WebDAVMethod.class);

    for (Method method : methods) {
      if (!method.getName().equals("checkLockToken")) {
        continue;
      }

      ReflectionUtils.makeAccessible(method);

      ReflectionUtils.invokeMethod(method, object, nodeLockInfo, ignoreShared, lockMethod);
    }
  }

  private String getUserAgent(WebDAVMethod object) {
    final Field field = ReflectionUtils.findField(WebDAVMethod.class, "m_userAgent");

    ReflectionUtils.makeAccessible(field);

    try {
      return (String) field.get(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Object getConditions(WebDAVMethod object) {
    final Field field = ReflectionUtils.findField(WebDAVMethod.class, "m_conditions");

    ReflectionUtils.makeAccessible(field);

    try {
      return field.get(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private WebDAVHelper getDAVHelper() {
    return (WebDAVHelper) ApplicationContextHolder.getApplicationContext().getBean("webDAVHelper");
  }

  private LockInfo getNodeLockInfo(WebDAVMethod object, FileInfo fileInfo) {
    Method[] methods = ReflectionUtils.getAllDeclaredMethods(WebDAVMethod.class);

    for (Method method : methods) {
      if (!method.getName().equals("getNodeLockInfo")) {
        continue;
      }

      ReflectionUtils.makeAccessible(method);

      return (LockInfo) ReflectionUtils.invokeMethod(method, object, fileInfo);
    }

    return null;
  }

  public static WebDAVMethodAspect aspectOf() {
    return new WebDAVMethodAspect();
  }

}
