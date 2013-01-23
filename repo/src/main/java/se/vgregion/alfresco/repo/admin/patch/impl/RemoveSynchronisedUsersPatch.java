package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.Assert;

public class RemoveSynchronisedUsersPatch extends AbstractPatch implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(RemoveSynchronisedUsersPatch.class);

  private static final String MSG_SUCCESS = "vgr.patch.removeSynchronisedUsersPatch.result";

  private PersonService _personService;

  private SiteService _siteService;

  public void setPersonService(final PersonService personService) {
    _personService = personService;
  }

  public void setSiteService(final SiteService siteService) {
    _siteService = siteService;
  }

  @Override
  protected String applyInternal() throws Exception {
    // get all users in the system
    final Set<NodeRef> people = _personService.getAllPeople();

    int count = 0;

    // iterate through all the users
    for (final NodeRef person : people) {
      // get the username from the node
      final Serializable username = nodeService.getProperty(person, ContentModel.PROP_USERNAME);

      // if no username, continue
      if (username == null) {
        continue;
      }

      // if it's the guest or admin, skip as well
      if (username.toString().equals("guest") || username.toString().equals("admin")) {
        continue;
      }

      // list all sites that the user is excplicit member in
      final List<SiteInfo> sites = _siteService.listSites(username.toString());

      // the user is member of 1 site or more, continue
      if (sites.size() > 0) {
        continue;
      }

      // if this point is reached, the user is not a member of any site, so it
      // should be deleted
      LOG.error("Deleting user '" + username + "', user is not a member of any site");

      _personService.deletePerson(username.toString());

      count++;
    }

    LOG.error("Deleted " + count + " of " + people.size() + " users that wasn't a member in any sites.");

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_personService);
    Assert.notNull(_siteService);
  }

}
