// @overridden projects/slingshot/source/web/components/people-finder/authority-finder.js

(function (_performSearch) {
  //Show translated names of site groups (roles)
  Alfresco.AuthorityFinder.prototype._performSearch = function (searchTerm) {
    var old = this.widgets.dataTable.onDataReturnInitializeTable;
    var scope = this;
    this.widgets.dataTable.onDataReturnInitializeTable = function customOnDataReturnInitializeTable (sRequest, oResponse, oPayload) {
      
      for (var i=0; i<oResponse.results.length; i++) {
        if (oResponse.results[i].displayName.indexOf("site_")===0) {
          var startIdx = oResponse.results[i].displayName.lastIndexOf("_");
          var roleName = oResponse.results[i].displayName.substr(startIdx + 1);
          oResponse.results[i].displayName = scope.msg("roles." + roleName.toLocaleLowerCase());
        }
      }

      old.call(this, sRequest, oResponse, oPayload);
    }

    _performSearch.call(this,searchTerm);
  };
}(Alfresco.AuthorityFinder.prototype._performSearch));