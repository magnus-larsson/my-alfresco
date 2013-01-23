package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;

public class ExtendPersonPatch extends AbstractPatch implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(ExtendPersonPatch.class);

  private PersonService _personService;

  protected RetryingTransactionHelper _retryingTransactionHelper;

  public void setPersonService(final PersonService personService) {
    _personService = personService;
  }

  public void setRetryingTransactionHelper(final RetryingTransactionHelper retryingTransactionHelper) {
    _retryingTransactionHelper = retryingTransactionHelper;
  }

  @Override
  protected String applyInternal() throws Exception {
    // get all users in the system
    final Set<NodeRef> people = _personService.getAllPeople();

    // iterate through all the users
    for (final NodeRef person : people) {
      patchPerson(person);
    }

    return I18NUtil.getMessage("");
  }

  private Boolean patchPerson(final NodeRef person) {
    final RetryingTransactionHelper.RetryingTransactionCallback<Boolean> execution = new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {

      @Override
      public Boolean execute() throws Throwable {
        if (nodeService.hasAspect(person, VgrModel.ASPECT_PERSON)) {
          return Boolean.FALSE;
        }

        final Serializable username = nodeService.getProperty(person, ContentModel.PROP_USERNAME);

        nodeService.addAspect(person, VgrModel.ASPECT_PERSON, null);

        LOG.debug("Added aspect vgr:person to " + username);

        return Boolean.TRUE;
      }

    };

    return _retryingTransactionHelper.doInTransaction(execution, false, true);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_personService);
  }

}
