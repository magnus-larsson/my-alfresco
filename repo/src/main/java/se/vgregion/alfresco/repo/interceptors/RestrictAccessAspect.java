package se.vgregion.alfresco.repo.interceptors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.extensions.webscripts.WrappingWebScriptRequest;
import org.springframework.extensions.webscripts.WrappingWebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

@Aspect
public class RestrictAccessAspect {

  private static final Logger LOG = Logger.getLogger(RestrictAccessAspect.class);

  public static final String HTTP_HEADER_X_FORWARDED_FOR = "x-forwarded-for";

  // public static final String[] FORBIDDEN_IP_ADDRESSES = {"192.71.67.138", "192.71.67.139"};

  @Pointcut("execution(* org.alfresco.repo.web.scripts.content.StreamContent+.streamContent(..))")
  private void hook() {
  }

  @Around("hook()")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    Object[] args = pjp.getArgs();

    HttpServletRequest request = extractHttpServletRequest(args[0]);
    HttpServletResponse response = extractHttpServletResponse(args[1]);

    if (request == null) {
      return pjp.proceed();
    }

    if (!(args[2] instanceof NodeRef)) {
      return pjp.proceed();
    }

    NodeRef nodeRef = extractNodeRef(args[2]);

    String ipAddress = extractIpAddress(request);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Checking if streamContent on node '" + nodeRef + "' is allowed for IP '" + ipAddress + "'");
    }

    if (isForbidden(nodeRef, ipAddress)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("streamContent is forbidden, sending 403 to browser.");
      }

      // send the 403 error, Forbidden
      response.sendError(HttpServletResponse.SC_FORBIDDEN);

      return pjp.proceed();
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("streamContent is allowed, continuing with regular security.");
    }

    return pjp.proceed();
  }

  private HttpServletResponse extractHttpServletResponse(Object object) {
    if (object instanceof WebScriptServletResponse) {
      WebScriptServletResponse response = (WebScriptServletResponse) object;

      return response.getHttpServletResponse();
    } else if (object instanceof WrappingWebScriptResponse) {
      WrappingWebScriptResponse wrappingWebScriptResponse = (WrappingWebScriptResponse) object;

      WebScriptServletResponse response = (WebScriptServletResponse) wrappingWebScriptResponse.getNext();

      return response.getHttpServletResponse();
    }

    return null;
  }

  /**
   * Extracts the IP adress of the caller. If there's an "x-forwarded-for" header set,
   * that one is used cause that is set by proxies, load balancers and the like.
   *
   * @param request the http servlet request
   * @return The IP address of the caller
   */
  private String extractIpAddress(HttpServletRequest request) {
    String ipAddress = request.getRemoteAddr();

    Enumeration headerNames = request.getHeaderNames();

    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement().toString();

      if (!headerName.equalsIgnoreCase(HTTP_HEADER_X_FORWARDED_FOR)) {
        continue;
      }

      ipAddress = request.getHeader(headerName);
    }

    return ipAddress;
  }

  private boolean isForbidden(NodeRef nodeRef, String ipAddress) {
    NodeService nodeService = (NodeService) ApplicationContextHolder.getApplicationContext().getBean("nodeService");
    Properties globalProperties = (Properties) ApplicationContextHolder.getApplicationContext().getBean("global-properties");

    // first of all, if the node is not in the Storage, continue using the regular security
    if (!nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PUBLISHED)) {
      return false;
    }

    // if it's in the storage, extract vgr:dc.rights.accessrights
    @SuppressWarnings("unchecked")
    List<String> accessRights = (List<String>) nodeService.getProperty(nodeRef, VgrModel.PROP_ACCESS_RIGHT);

    // if no access right is set deny access, it's same as intranet
    if (accessRights.size() == 0) {
      return true;
    }

    // if access right contains "Internet" then it's also fine
    if (accessRights.contains(VgrModel.ACCESS_RIGHT_INTERNET)) {
      return false;
    }

    // if access rights contains "Intranät" (and probably only this value at this stage) then it's fine as long as the users IP is not among the forbidden ones
    if (accessRights.contains(VgrModel.ACCESS_RIGHT_INTRANET)) {
      List<String> forbiddenIpAddresses = Arrays.asList(StringUtils.split(globalProperties.getProperty("vgr.tmg_forbidden_ip_addresses", ""), ","));

      return forbiddenIpAddresses.contains(ipAddress);
    }

    // if we reach this stage then it ought to be good, continue with the regular security
    return false;
  }

  private NodeRef extractNodeRef(Object object) {
    NodeService nodeService = (NodeService) ApplicationContextHolder.getApplicationContext().getBean("nodeService");

    NodeRef nodeRef = new NodeRef(object.toString());

    if (!nodeService.exists(nodeRef)) {
      throw new RuntimeException("No node found with nodeRef " + object);
    }

    String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

    // if name is pdfa we need to resolv the parent node
    if (name.equalsIgnoreCase("pdfa")) {
      nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
    }

    return nodeRef;
  }

  private HttpServletRequest extractHttpServletRequest(Object object) {
    if (object instanceof WebScriptServletRequest) {
      WebScriptServletRequest request = (WebScriptServletRequest) object;

      return request.getHttpServletRequest();
    } else if (object instanceof WrappingWebScriptRequest) {
      WrappingWebScriptRequest wrappingWebScriptRequest = (WrappingWebScriptRequest) object;

      WebScriptServletRequest request = (WebScriptServletRequest) wrappingWebScriptRequest.getNext();

      return request.getHttpServletRequest();
    }

    return null;
  }

  public static RestrictAccessAspect aspectOf() {
    return new RestrictAccessAspect();
  }

}
