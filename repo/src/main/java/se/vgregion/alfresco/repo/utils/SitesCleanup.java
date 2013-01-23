package se.vgregion.alfresco.repo.utils;

import java.util.concurrent.ExecutionException;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.stereotype.Service;

@Service
public class SitesCleanup extends AbstractTransactionJob {

  private SearchService _searchService;
  
  @Override
  protected void doExecute() throws ExecutionException {
    SearchParameters searchParameters = new SearchParameters();

    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLanguage("cmis-alfresco");
    searchParameters.setQuery("SELECT * FROM cmis:folder where cmis:name = 'Sites'");

    _searchService.query(searchParameters);
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }
  
}
