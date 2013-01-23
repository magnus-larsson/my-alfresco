package se.vgregion.alfresco.repo.scripts;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;

import se.vgregion.alfresco.repo.storage.StorageService;

public class ScriptVgrDokTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  @Test
  public void testPublish() {
    final ScriptVgrDok script = new ScriptVgrDok();

    final SearchService searchService = context.mock(SearchService.class);
    script.setSearchService(searchService);

    final ResultSet searchResult = context.mock(ResultSet.class);

    final StorageService storageService = context.mock(StorageService.class);
    script.setStorageService(storageService);

    final ResultSetRow row1 = context.mock(ResultSetRow.class, "row1");
    final ResultSetRow row2 = context.mock(ResultSetRow.class, "row2");

    final List<ResultSetRow> nodes = new ArrayList<ResultSetRow>();
    nodes.add(row1);
    nodes.add(row2);

    final NodeRef nodeRef1 = new NodeRef("workspace://SpacesStore/nodeRef1");
    final NodeRef nodeRef2 = new NodeRef("workspace://SpacesStore/nodeRef2");
    final NodeRef publishedNodeRef1 = null;
    final NodeRef publishedNodeRef2 = new NodeRef("workspace://SpacesStore/publishedNodeRef2");

    context.checking(new Expectations() {
      {
        oneOf(searchService).query(with(any(SearchParameters.class)));
        will(returnValue(searchResult));
        oneOf(searchResult).iterator();
        will(returnIterator(nodes));
        oneOf(row1).getNodeRef();
        will(returnValue(nodeRef1));
        oneOf(storageService).getPublishedNodeRef(nodeRef1);
        will(returnValue(publishedNodeRef1));
        oneOf(storageService).publishToStorage(nodeRef1);
        oneOf(row2).getNodeRef();
        will(returnValue(nodeRef2));
        oneOf(storageService).getPublishedNodeRef(nodeRef2);
        will(returnValue(publishedNodeRef2));
      }
    });

    script.publish();

    context.assertIsSatisfied();
  }

}
