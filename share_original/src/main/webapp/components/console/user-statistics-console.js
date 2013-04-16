/**
 * RL root namespace.
 * 
 * @namespace RL
 */
// Ensure RL root object exists
if (typeof RL == "undefined" || !RL) {
	var RL = {};
}

/**
 * Admin Console User Statistics Console
 * 
 * @namespace Alfresco
 * @class RL.UserStatisticsConsole
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;

	var $html = Alfresco.util.encodeHTML;

	var Bubbling = YAHOO.Bubbling;

	/**
	 * UserStatisticsConsole constructor.
	 * 
	 * @param {String}
	 *            htmlId The HTML id of the parent element
	 * @return {RL.UserStatisticsConsole} The new UserStatisticsConsole instance
	 * @constructor
	 */
	RL.UserStatisticsConsole = function(htmlId) {
		this.name = "RL.UserStatisticsConsole";

		RL.UserStatisticsConsole.superclass.constructor.call(this, htmlId);

		/* Register this component */
		Alfresco.util.ComponentManager.register(this);

		/* Load YUI Components */
		Alfresco.util.YUILoaderHelper.require([ "button", "container",
				"datasource", "datatable", "paginator", "json", "history" ],
				this.onComponentsLoaded, this);

		/* Define panel handlers */
		var parent = this;

		this.data = {
			"internalData" : new Object(),
			"externalData" : new Object()
		};

		// loading message function
		this.loadingMessage = null;
		this.fnShowLoadingMessage = function() {
			this.loadingMessage = Alfresco.util.PopupManager.displayMessage({
				displayTime : 0,
				text : '<span class="wait">'
						+ $html(this.msg("message.loading")) + '</span>',
				noEscape : true
			});
		};

		// slow data webscript message
        this.timerShowLoadingMessage = null;
		/* File List Panel Handler */
		ListPanelHandler = function ListPanelHandler_constructor() {
			ListPanelHandler.superclass.constructor.call(this, "main");
		};

		this.LoadUserActivity = function() {
			// slow data webscript message
			this.timerShowLoadingMessage = YAHOO.lang.later(2000, this, this.fnShowLoadingMessage);
			Alfresco.util.Ajax.request({
				url : Alfresco.constants.PROXY_URI + "vgr/reports/activeUsers",
				responseContentType : Alfresco.util.Ajax.JSON,
				method : Alfresco.util.Ajax.GET,
				successCallback : {
					fn : this.LoadUserActivity_success,
					scope : this
				},
				failureCallback : {
					fn : this.LoadUserActivity_failure,
					scope : this
				}
			});
		};

		this.LoadUserActivity_success = function(response) {
			if (this.timerShowLoadingMessage) {
				this.timerShowLoadingMessage.cancel();
            }
			if (this.loadingMessage) {
				this.loadingMessage.destroy();
			}
			if (response != null && response.json != null) {
				this.data.internalData = response.json.internal;
				this.data.externalData = response.json.external;
				// this.events.internalDataEvent.fire(this.dInternalata.internalData);
				var successHandlerInternal = function(request, response,
						payload) {

					parent.widgets.dataTableInternal.onDataReturnInitializeTable
							.call(parent.widgets.dataTableInternal, request,
									response, payload);

				};
				var successHandlerExternal = function(request, response,
						payload) {
					parent.widgets.dataTableExternal.onDataReturnInitializeTable
							.call(parent.widgets.dataTableExternal, request,
									response, payload);
				};
				var oCallbackInternal = {
					success : successHandlerInternal,
					failure : successHandlerInternal,
					scope : this.widgets.dataTableInternal,
					argument : this.widgets.dataTableInternal.getState()
				// data payload that will be returned to the callback function
				};
				this.widgets.dataSourceInternal.sendRequest("",
						oCallbackInternal);
				var oCallbackExternal = {
					success : successHandlerExternal,
					failure : successHandlerExternal,
					scope : this.widgets.dataTableExternal,
					argument : this.widgets.dataTableExternal.getState()
				// data payload that will be returned to the callback function
				};
				this.widgets.dataSourceExternal.sendRequest("",
						oCallbackExternal);
			}
		};

		YAHOO
				.extend(
						ListPanelHandler,
						Alfresco.ConsolePanelHandler,
						{
							/**
							 * Called by YAHOO.lang.JSON.stringify(the
							 * ConsolePanelHandler when this panel shall be
							 * loaded
							 * 
							 * @method onLoad
							 */
							onLoad : function onLoad() {
								// Buttons
								parent.widgets.refreshButton = Alfresco.util
										.createYUIButton(parent,
												"refresh-button-users",
												parent.onRefreshClick);
								// DataTable and DataSource setup
								parent.widgets.dataSourceInternal = new YAHOO.util.FunctionDataSource(
										function() {
											return YAHOO.lang.JSON
													.stringify(parent.data.internalData);
										},
										{
											"responseType" : YAHOO.util.FunctionDataSource.TYPE_JSON,
											responseSchema : {
												resultsList : "users",
												metaFields : {
													recordOffset : "startIndex",
													totalRecords : "totalRecords"
												}
											}
										});

								parent.widgets.dataSourceExternal = new YAHOO.util.FunctionDataSource(
										function() {
											return YAHOO.lang.JSON
													.stringify(parent.data.externalData);
										},
										{
											"responseType" : YAHOO.util.FunctionDataSource.TYPE_JSON,
											responseSchema : {
												resultsList : "users",
												metaFields : {
													recordOffset : "startIndex",
													totalRecords : "totalRecords"
												}
											}
										});
								// Setup the main datatable
								this._setupDataTable();
								parent.LoadUserActivity();

							},
							_setupDataTable : function() {
								var renderCellUserName = function(cell, record,
										column, data) {
									cell.innerHTML = $html(data);
								};

								var renderCellFullName = function(cell, record,
										column, data) {
									cell.innerHTML = $html(data);
								};

								var renderCellLogins = function(cell, record,
										column, data) {
									cell.innerHTML = $html(data);
								};

								var renderCellLastActivity = function(cell,
										record, column, data) {
									if (data == "") {
										data = Alfresco.util
												.message(
														"statistics-console.noactivity",
														"RL.UserStatisticsConsole");
									}
									cell.innerHTML = $html(data);
								};

								var columnDefinitions = [ {
									key : "userName",
									label : parent._msg("label.userName"),
									sortable : false,
									formatter : renderCellUserName
								}, {
									key : "fullName",
									label : parent._msg("label.fullName"),
									sortable : false,
									formatter : renderCellFullName
								}, {
									key : "logins",
									label : parent._msg("label.logins"),
									sortable : false,
									formatter : renderCellLogins
								}, {
									key : "lastActivity",
									label : parent._msg("label.lastActivity"),
									sortable : false,
									formatter : renderCellLastActivity
								} ];

								parent.widgets.dataTableInternal = new YAHOO.widget.DataTable(
										parent.id
												+ "-statistics-internal-users-list",
										columnDefinitions,
										parent.widgets.dataSourceInternal, {
											MSG_EMPTY : parent
													._msg("message.empty"),
											MSG_ERROR : parent
													._msg("message.empty")
										});
								parent.widgets.dataTableExternal = new YAHOO.widget.DataTable(
										parent.id
												+ "-statistics-external-users-list",
										columnDefinitions,
										parent.widgets.dataSourceExternal, {
											MSG_EMPTY : parent
													._msg("message.empty"),
											MSG_ERROR : parent
													._msg("message.empty")
										});
							}

						});

		new ListPanelHandler();

		return this;
	};

	YAHOO.extend(RL.UserStatisticsConsole, Alfresco.ConsoleTool, {

		/**
		 * Fired by YUI when parent element is available for scripting.
		 * Component initialisation, including instantiation of YUI widgets and
		 * event listener binding.
		 * 
		 * @method onReady
		 */
		onReady : function() {
			var self = this;

			// Call super-class onReady() method
			RL.UserStatisticsConsole.superclass.onReady.call(this);

		},

		onRefreshClick : function() {
			this.LoadUserActivity();
		},

		/**
		 * Gets a custom message
		 * 
		 * @method _msg
		 * @param messageId
		 *            {string} The messageId to retrieve
		 * @return {string} The custom message
		 * @private
		 */
		_msg : function(messageId) {
			return Alfresco.util.message.call(this, messageId,
					"RL.UserStatisticsConsole", Array.prototype.slice.call(
							arguments).slice(1));
		},

		/**
		 * Resets the YUI DataTable errors to our custom messages
		 * 
		 * NOTE: Scope could be YAHOO.widget.DataTable, so can't use "this"
		 * 
		 * @method _setDefaultDataTableErrors
		 * @param dataTable
		 *            {object} Instance of the DataTable
		 */
		_setDefaultDataTableErrors : function(dataTable) {
			var msg = Alfresco.util.message;
			dataTable.set("MSG_EMPTY", msg("message.empty",
					"RL.UserStatisticsConsole"));
			dataTable.set("MSG_ERROR", msg("message.error",
					"RL.UserStatisticsConsole"));
		}

	});
})();
