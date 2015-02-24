(function (_onReady) {
  Alfresco.DNDUpload.prototype.onReady = function() {
  	_onReady.call(this);
  	/**
  	Add fix for unset widgets.panel value. This value 
  	is set for the other uploaders but not for the DND
  	uploader which results in NPE when clicking the upload
  	link in the help widget for a site.
  	*/
  	this.widgets.panel = this.panel;
  }
}(Alfresco.DNDUpload.prototype.onReady));