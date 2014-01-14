package se.vgregion.alfresco.repo.interceptors;

import java.util.Map;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import se.vgregion.alfresco.repo.model.VgrModel;

@Aspect
public class LDAPUserRegistryAspect {

  private static final Logger LOG = Logger.getLogger(LDAPUserRegistryAspect.class);

  @Pointcut("execution(* org.alfresco.repo.security.sync.ldap.LDAPUserRegistry.mapToNode(..))")
  private void hook() {
  }

  @Around("hook()")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    String method = pjp.getSignature().getName();

    if (!method.equalsIgnoreCase("mapToNode")) {
      LOG.warn("Expected mapToNode function");
      return pjp.proceed();
    }

    Object[] args = pjp.getArgs();

    if (args.length != 3) {
      LOG.warn("Expected 3 parameters to mapToNode function");
      
      return pjp.proceed();
    }

    if (!(args[0] instanceof Map)) {
      LOG.warn("First argument is not a Map in mapToNode function");
      
      return pjp.proceed();
    }
    
    @SuppressWarnings("unchecked")
    Map<String, String> attributeMapping = (Map<String, String>) args[0];

    if (!(args[1] instanceof Map)) {
      LOG.warn("Second argument is not a Map in mapToNode function");
      
      return pjp.proceed();
    }
    
    if (!(args[2] instanceof SearchResult)) {
      LOG.warn("Third argument is not a SearchResult in mapToNode function");
      
      return pjp.proceed();
    }
    
    SearchResult result = (SearchResult) args[2];

    // Proceed with switching thumbnail data to base64 encoded from byte array

    Attributes ldapAttributes = result.getAttributes();
    
    String key = VgrModel.VGR_SHORT + ":" + VgrModel.PROP_THUMBNAIL_PHOTO.getLocalName();
    
    if (attributeMapping.containsKey(key)) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Handling key: " + key);
      }
      
      String attributeName = attributeMapping.get(key);
      
      Attribute attribute = ldapAttributes.get(attributeName);
      
      if (attribute != null) {
        Object object = attribute.get(0);
        
        if (object instanceof byte[]) {
          LOG.trace("Type is byte array");
          
          byte[] byteArr = (byte[]) object;

          String base64String = Base64.encodeBase64String(byteArr);
          
          attribute.set(0, base64String);
          
          ldapAttributes.put(attribute);
          
          result.setAttributes(ldapAttributes);
        }
      }
    }

    return pjp.proceed();
  }

  public static LDAPUserRegistryAspect aspectOf() {
    return new LDAPUserRegistryAspect();
  }

}
