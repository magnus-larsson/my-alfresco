package se.vgregion.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

public class FixDcSourceDocumentIdPatchTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Test
  public void testApplyInternal() throws Exception {
    final String link = "http://alfresco.vgregion.se/alfresco/service/api/node/content/workspace/SpacesStore/7e0f0291-570f-4676-84c3-00cfb0a1ffe6?a=true";

    final String documentId = "workspace://SpacesStore/7e0f0291-570f-4676-84c3-00cfb0a1ffe6";

    final SearchService searchService = context.mock(SearchService.class);

    final NodeService nodeService = context.mock(NodeService.class);

    final ResultSet searchResult = context.mock(ResultSet.class);

    final ResultSetRow row = context.mock(ResultSetRow.class);
    
    final BehaviourFilter behaviourFilter = context.mock(BehaviourFilter.class);

    final List<ResultSetRow> nodes = new ArrayList<ResultSetRow>();
    nodes.add(row);

    final NodeRef publishedNodeRef = new NodeRef("workspace://SpacesStore/07586684-19fe-4ab9-aa84-d39f518005f2");
    
    final Collection<String> linkList = new ArrayList<String>();
    linkList.add(link);
    
    final FixDcSourceDocumentIdPatch patch = new FixDcSourceDocumentIdPatch();
    patch.setSearchService(searchService);
    patch.setNodeService(nodeService);
    patch.setBehaviourFilter(behaviourFilter);
    context.checking(new Expectations() {
      {
        oneOf(searchService).query(with(any(SearchParameters.class)));
        will(returnValue(searchResult));
        oneOf(searchResult).iterator();
        will(returnIterator(nodes));
        oneOf(searchResult).close();
        oneOf(row).getValue(ContentModel.PROP_REFERENCE);
        will(returnValue(null));
        oneOf(row).getValue(VgrModel.PROP_IDENTIFIER);
        will(returnValue(linkList));
        oneOf(row).getNodeRef();
        will(returnValue(publishedNodeRef));
        oneOf(nodeService).setProperty(publishedNodeRef, VgrModel.PROP_SOURCE_DOCUMENTID, documentId);
        oneOf(behaviourFilter).disableBehaviour();
        oneOf(behaviourFilter).enableBehaviour();
      }
    });

    patch.applyInternal();

    context.assertIsSatisfied();
  }

}
