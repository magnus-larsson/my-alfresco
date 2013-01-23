package se.vgregion.alfresco.repo.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.sync.NodeDescription;
import org.alfresco.repo.security.sync.UserRegistry;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;

public class UserSynchronizer implements InitializingBean, ApplicationEventPublisherAware {

  private static final Log logger = LogFactory.getLog(UserSynchronizer.class);

  private ChildApplicationContextManager _applicationContextManager;

  private TransactionService _transactionService;

  private ApplicationEventPublisher _applicationEventPublisher;

  private AuthorityService _authorityService;

  private PersonService _personService;

  private RuleService _ruleService;

  private int _workerThreads = 2;

  private int _loggingInterval = 100;

  private String _zone;

  public void setApplicationContextManager(final ChildApplicationContextManager applicationContextManager) {
    _applicationContextManager = applicationContextManager;
  }

  public void setZone(final String zone) {
    _zone = zone;
  }

  @Override
  public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
    _applicationEventPublisher = applicationEventPublisher;
  }

  public void setAuthorityService(final AuthorityService authorityService) {
    _authorityService = authorityService;
  }

  public void setLoggingInterval(final int loggingInterval) {
    _loggingInterval = loggingInterval;
  }

  public void setPersonService(final PersonService personService) {
    _personService = personService;
  }

  public void setRuleService(final RuleService ruleService) {
    _ruleService = ruleService;
  }

  public void setTransactionService(final TransactionService transactionService) {
    _transactionService = transactionService;
  }

  public void setWorkerThreads(final int workerThreads) {
    _workerThreads = workerThreads;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_applicationContextManager);
    Assert.notNull(_applicationEventPublisher);
    Assert.notNull(_authorityService);
    Assert.notNull(_personService);
    Assert.notNull(_ruleService);
    Assert.notNull(_transactionService);
    Assert.hasText(_zone);
  }

  private Collection<NodeDescription> findPersons(final Date modifiedSince, final String username) {
    final ApplicationContext applicationContext = _applicationContextManager.getApplicationContext(_zone);

    final UserRegistry userRegistry = (UserRegistry) applicationContext.getBean("userRegistry");

    final Collection<NodeDescription> persons = userRegistry.getPersons(modifiedSince);

    final Collection<NodeDescription> result = new ArrayList<NodeDescription>();

    for (final NodeDescription person : persons) {
      final String personUsername = (String) person.getProperties().get(ContentModel.PROP_USERNAME);

      if (personUsername.equals(username)) {
        result.add(person);
      }
    }

    return result;
  }

  public int synchronizeUser(final Date modifiedSince, final String username) {
    final Collection<NodeDescription> personList = findPersons(modifiedSince, username);

    @SuppressWarnings("deprecation")
    final BatchProcessor<NodeDescription> personProcessor = new BatchProcessor<NodeDescription>(_zone + " User Creation and Association",
        _transactionService.getRetryingTransactionHelper(), personList, _workerThreads, 10, _applicationEventPublisher, logger, _loggingInterval);

    // Create a prefixed zone ID for use with the authority service
    final String zoneId = AuthorityService.ZONE_AUTH_EXT_PREFIX + _zone;

    // The set of zones we associate with new objects (default plus registry
    // specific)
    final Set<String> zoneSet = getZones(zoneId);

    final Collection<String> visitedZoneIds = new ArrayList<String>();

    final Collection<String> allZoneIds = new ArrayList<String>();
    allZoneIds.add(zoneId);

    final PersonWorker persons = new PersonWorker(modifiedSince.getTime(), allZoneIds, visitedZoneIds, zoneSet, zoneId, _zone);

    return personProcessor.process(persons, true);
  }

  class PersonWorker extends BaseBatchProcessWorker<NodeDescription> {

    private long _latestTime;

    private final Collection<?> _allZoneIds;

    private final Collection<?> _visitedZoneIds;

    private final Set<String> _zoneSet;

    private final String _zoneId;

    private final String _zone;

    public PersonWorker(final long latestTime, final Collection<?> allZoneIds, final Collection<?> visitedZoneIds, final Set<String> zoneSet,
        final String zoneId, final String zone) {
      _latestTime = latestTime;
      _allZoneIds = allZoneIds;
      _visitedZoneIds = visitedZoneIds;
      _zoneSet = zoneSet;
      _zoneId = zoneId;
      _zone = zone;
    }

    public long getLatestTime() {
      return _latestTime;
    }

    @Override
    public String getIdentifier(final NodeDescription entry) {
      return entry.getSourceId();
    }

    @Override
    public void process(final NodeDescription person) throws Throwable {
      // Make a mutable copy of the person properties, since they get written
      // back to by person service
      final HashMap<QName, Serializable> personProperties = new HashMap<QName, Serializable>(person.getProperties());

      final String personName = (String) personProperties.get(ContentModel.PROP_USERNAME);

      final Set<String> zones = _authorityService.getAuthorityZones(personName);

      if (zones == null) {
        // The person did not exist at all
        if (logger.isDebugEnabled()) {
          logger.debug("Creating user '" + personName + "'");
        }

        _personService.createPerson(personProperties, _zoneSet);
      } else if (zones.contains(_zoneId)) {
        // The person already existed in this zone: update the person
        if (logger.isDebugEnabled()) {
          logger.debug("Updating user '" + personName + "'");
        }

        _personService.setPersonProperties(personName, personProperties, false);
      } else {
        // Check whether the user is in any of the authentication chain zones
        final Set<String> intersection = new TreeSet<String>(zones);

        intersection.retainAll(_allZoneIds);

        if (intersection.size() == 0) {
          // The person exists, but not in a zone that's in the authentication
          // chain. May be due
          // to upgrade or zone changes. Let's re-zone them
          if (logger.isWarnEnabled()) {
            logger.warn("Updating user '" + personName + "'. This user will in future be assumed to originate from user registry '" + _zone + "'.");
          }

          _authorityService.removeAuthorityFromZones(personName, zones);

          _authorityService.addAuthorityToZones(personName, _zoneSet);

          _personService.setPersonProperties(personName, personProperties, false);
        } else {
          // Check whether the user is in any of the higher priority
          // authentication chain zones
          intersection.retainAll(_visitedZoneIds);

          if (intersection.size() > 0) {
            // A person that exists in a different zone with higher precedence
            // - ignore
            return;
          }

          // The person existed, but in a zone with lower precedence
          if (logger.isWarnEnabled()) {
            logger.warn("Recreating occluded user '" + personName
                + "'. This user was previously created through synchronization with a lower priority user registry.");
          }
          _personService.deletePerson(personName);

          _personService.createPerson(personProperties, _zoneSet);
        }
      }

      synchronized (this) {
        // Maintain the last modified date
        final Date personLastModified = person.getLastModified();

        if (personLastModified != null) {
          _latestTime = Math.max(_latestTime, personLastModified.getTime());
        }
      }
    }
  }

  protected abstract class BaseBatchProcessWorker<T> implements BatchProcessWorker<T> {
    @Override
    public final void beforeProcess() throws Throwable {
      // Disable rules
      _ruleService.disableRules();

      // Authentication
      AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
    }

    @Override
    public final void afterProcess() throws Throwable {
      // Enable rules
      _ruleService.enableRules();

      // Clear authentication
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }

  private Set<String> getZones(final String zoneId) {
    final Set<String> zones = new HashSet<String>(5);

    zones.add(AuthorityService.ZONE_APP_DEFAULT);
    zones.add(zoneId);

    return zones;
  }

}
