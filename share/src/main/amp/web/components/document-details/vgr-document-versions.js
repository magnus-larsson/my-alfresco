/*
 * @overridden projects/slingshot/source/web/components/document-details/document-versions.js
 */

/**
 * TODO write some docs
 */
(function(onReady) {
	
	var Dom = YAHOO.util.Dom;
	
	var Selector = YAHOO.util.Selector;

	// define that if all versions need to be retrieved, use -1 as parameter
	var ALL_VERSIONS = -1;

	Alfresco.DocumentVersions.prototype.onReady = function() {
		// call the original onReady() method
		onReady.call(this);
		
		// Prefix some of the urls with values from the client
		var heading = Dom.get(this.id + "-heading");
		
		// select the span containing the actions
		var actions = Selector.query('span.alfresco-twister-actions', heading);
		
		// if the actions span is not there, create it, otherwise use it
		if (actions.length == 0) {
			var span = document.createElement('span');
			heading.appendChild(span);
		} else {
			var span = actions[0];
		}
		
		// create an anchor element
		var anchor = document.createElement('a');
		
		// set some valid stuff
		anchor.name = '.onShowAllVersionsClick';
		anchor.title = this.msg('label.showAllVersions');
		anchor.innerHTML = '&nbsp;';
		Dom.addClass(anchor, 'show')
		
		// append the anchor last in the <span>
		span.appendChild(anchor);
		
		// add an onclick event listener
		YAHOO.util.Event.addListener(anchor, "click", this.onShowAllVersions, this);
	};
	
	Alfresco.DocumentVersions.prototype.onShowAllVersions = function(event, scope) {
		// prevent the default on click
		event.preventDefault();

		// remove the number of version restriction
		scope.widgets.alfrescoDataTable.widgets.dataSource.liveData = scope.widgets.alfrescoDataTable.widgets.dataSource.liveData + '&total=' + ALL_VERSIONS;
		
		// reload the data table
		scope.widgets.alfrescoDataTable.reloadDataTable();
		
		var parent = Dom.get(this).parentNode;
		
		// remove the button, all versions has been fetched so this is not needed
		Dom.addClass(parent, 'hidden');
		
		if (parent.children.length == 0) {
			Dom.addClass(parent.parentNode, 'hidden');
		}
	};

}(Alfresco.DocumentVersions.prototype.onReady));
