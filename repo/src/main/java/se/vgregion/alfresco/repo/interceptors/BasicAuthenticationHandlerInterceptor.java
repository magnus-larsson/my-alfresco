package se.vgregion.alfresco.repo.interceptors;

import java.nio.charset.Charset;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.web.auth.AuthenticationListener;
import org.alfresco.repo.web.auth.BasicAuthCredentials;
import org.alfresco.repo.web.auth.TicketCredentials;
import org.alfresco.repo.webdav.auth.SharepointConstants;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;

/**
 * This customization is because of that basic auth is problematic in the VTI
 * module.
 * 
 * https://issues.alfresco.com/jira/browse/MNT-12437
 * 
 * This is just a temporary fix until the SSO is installed, or until Alfresco
 * 5.0 in which a new Sharepoint module is used.
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public @Aspect class BasicAuthenticationHandlerInterceptor {

  private final static Logger LOG = Logger.getLogger(BasicAuthenticationHandlerInterceptor.class);

  private final static String HEADER_AUTHORIZATION = "Authorization";

  private final static String BASIC_START = "Basic";

  @Pointcut("execution(* org.alfresco.web.sharepoint.auth.BasicAuthenticationHandler+.isUserAuthenticated(..))")
  private void hook() {
  }

  @Around("hook()")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    Object[] args = pjp.getArgs();

    if (!(args[0] instanceof ServletContext)) {
      return pjp.proceed();
    }

    if (!(args[1] instanceof HttpServletRequest)) {
      return pjp.proceed();
    }

    HttpServletRequest request = (HttpServletRequest) args[1];

    String authHdr = request.getHeader(HEADER_AUTHORIZATION);
    HttpSession session = request.getSession(false);
    SessionUser sessionUser = session == null ? null : (SessionUser) session.getAttribute(SharepointConstants.USER_SESSION_ATTRIBUTE);
    if (sessionUser == null) {
      if (authHdr != null && authHdr.length() > 5 && authHdr.substring(0, 5).equalsIgnoreCase(BASIC_START)) {
        byte[] decodeBase64 = Base64.decodeBase64(authHdr.substring(5).getBytes());

        String userAgent = request.getHeader("User-Agent").toLowerCase();
        String basicAuth;

        if (userAgent.indexOf("chrome/") >= 0) {
          basicAuth = new String(decodeBase64, Charset.forName("UTF-8"));
        } else {
          basicAuth = new String(decodeBase64, Charset.forName("ISO-8859-1"));
        }

        String username = null;
        String password = null;

        int pos = basicAuth.indexOf(":");
        if (pos != -1) {
          username = basicAuth.substring(0, pos);
          password = basicAuth.substring(pos + 1);
        } else {
          username = basicAuth;
          password = "";
        }

        try {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Authenticating user '" + username + "'");
          }

          getAuthenticationService().authenticate(username, password.toCharArray());

          // Normalize the user ID taking into account case sensitivity settings
          username = getAuthenticationService().getCurrentUserName();

          if (LOG.isDebugEnabled()) {
            LOG.debug("Authenticated user '" + username + "'");
          }

          getAuthenticationListener().userAuthenticated(new BasicAuthCredentials(username, password));

          request.getSession().setAttribute(SharepointConstants.USER_SESSION_ATTRIBUTE, new User(username, getAuthenticationService().getCurrentTicket(), getPersonService().getPerson(username)));

          return true;
        } catch (AuthenticationException ex) {
          getAuthenticationListener().authenticationFailed(new BasicAuthCredentials(username, password), ex);
        }
      } else {
        if (getRemoteUserMapper() != null && (!(getRemoteUserMapper() instanceof ActivateableBean) || ((ActivateableBean) getRemoteUserMapper()).isActive())) {
          String userId = getRemoteUserMapper().getRemoteUser(request);
          if (userId != null) {
            // authenticated by other
            getAuthenticationComponent().setCurrentUser(userId);

            request.getSession().setAttribute(SharepointConstants.USER_SESSION_ATTRIBUTE, new User(userId, getAuthenticationService().getCurrentTicket(), getPersonService().getPerson(userId)));
            return true;
          }
        }
      }
    } else {
      try {
        getAuthenticationService().validate(sessionUser.getTicket());
        getAuthenticationListener().userAuthenticated(new TicketCredentials(sessionUser.getTicket()));
        return true;
      } catch (AuthenticationException ex) {
        getAuthenticationListener().authenticationFailed(new TicketCredentials(sessionUser.getTicket()), ex);
        session.invalidate();
      }
    }

    return false;
  }

  private AuthenticationComponent getAuthenticationComponent() {
    return ApplicationContextHolder.getApplicationContext().getBean("AuthenticationComponent", AuthenticationComponent.class);
  }

  private PersonService getPersonService() {
    return ApplicationContextHolder.getApplicationContext().getBean("PersonService", PersonService.class);
  }

  private RemoteUserMapper getRemoteUserMapper() {
    return ApplicationContextHolder.getApplicationContext().getBean("RemoteUserMapper", RemoteUserMapper.class);
  }

  private AuthenticationListener getAuthenticationListener() {
    return ApplicationContextHolder.getApplicationContext().getBean("sharepointAuthenticationListener", AuthenticationListener.class);
  }

  private AuthenticationService getAuthenticationService() {
    return ApplicationContextHolder.getApplicationContext().getBean("AuthenticationService", AuthenticationService.class);
  }

  public static BasicAuthenticationHandlerInterceptor aspectOf() {
    return new BasicAuthenticationHandlerInterceptor();
  }

}
