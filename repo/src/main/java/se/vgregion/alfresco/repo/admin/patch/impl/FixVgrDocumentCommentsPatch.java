package se.vgregion.alfresco.repo.admin.patch.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class FixVgrDocumentCommentsPatch extends AbstractPatch implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(FixVgrDocumentCommentsPatch.class);

  private static final String MSG_SUCCESS = "vgr.patch.fixVgrDocumentCommentsPatch.result";

  private BehaviourFilter _behaviourFilter;

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  @Override
  protected String applyInternal() throws Exception {
    return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {

      @Override
      public String doWork() throws Exception {
        return doApply();
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  private String doApply() {
    _behaviourFilter.disableBehaviour();

    final ResultSet documents = queryDocuments();

    try {
      for (final ResultSetRow document : documents) {
        if (!nodeService.exists(document.getNodeRef())) {
          continue;
        }

        patchDocument(document.getNodeRef());

        LOG.debug("Patched comments " + document.getNodeRef());
      }
    } finally {
      ServiceUtils.closeQuietly(documents);
    }

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private void patchDocument(final NodeRef document) {
    final Set<QName> types = new HashSet<QName>();
    types.add(ForumModel.TYPE_FORUM);

    final List<ChildAssociationRef> forums = nodeService.getChildAssocs(document, types);

    for (final ChildAssociationRef forum : forums) {
      patchForum(forum.getChildRef());
    }
  }

  private void patchForum(final NodeRef forum) {
    nodeService.removeAspect(forum, VgrModel.ASPECT_METADATA);

    LOG.info("Removed aspect 'vgr:metadata' from forum node '" + forum + "'");

    final Set<QName> types = new HashSet<QName>();
    types.add(ForumModel.TYPE_TOPIC);

    final List<ChildAssociationRef> topics = nodeService.getChildAssocs(forum, types);

    for (final ChildAssociationRef topic : topics) {
      patchTopic(topic.getChildRef());
    }
  }

  private void patchTopic(final NodeRef topic) {
    nodeService.removeAspect(topic, VgrModel.ASPECT_METADATA);

    LOG.info("Removed aspect 'vgr:metadata' from topic node '" + topic + "'");

    final Set<QName> types = new HashSet<QName>();
    types.add(VgrModel.TYPE_VGR_DOCUMENT);

    final List<ChildAssociationRef> posts = nodeService.getChildAssocs(topic, types);

    for (final ChildAssociationRef post : posts) {
      patchPost(post.getChildRef());
    }
  }

  private void patchPost(final NodeRef post) {
    nodeService.setType(post, ForumModel.TYPE_POST);

    LOG.info("Changed type for post node '" + post + "' to 'fm:post'");

    final Set<QName> aspects = nodeService.getAspects(post);

    for (final QName aspect : aspects) {
      if (aspect.isMatch(ContentModel.ASPECT_AUDITABLE) || aspect.isMatch(ContentModel.ASPECT_REFERENCEABLE)
          || aspect.isMatch(ContentModel.ASPECT_TITLED)) {
        continue;
      }

      nodeService.removeAspect(post, aspect);

      LOG.info("Removed aspect '" + aspect + "' from post node '" + post + "'");
    }
  }

  private ResultSet queryDocuments() {
    final String query = "TYPE:\"vgr:document\" AND ASPECT:\"fm:discussable\"";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    return searchService.query(searchParameters);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_behaviourFilter);
  }

}
