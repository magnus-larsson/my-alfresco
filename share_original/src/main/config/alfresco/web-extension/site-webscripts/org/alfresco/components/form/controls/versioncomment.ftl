<#if form.mode == "edit">
    <div class="form-field">
          <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
          <textarea id="${fieldHtmlId}" class="versioncomment" name="-"></textarea>
    </div>
    <script type="text/javascript" language="javascript" charset="utf-8">
    // <![CDATA[
        YAHOO.Bubbling.on("afterFormRuntimeInit",function(layer,args){
            var old = args[1].runtime.ajaxSubmitHandlers.successCallback;
            args[1].runtime.ajaxSubmitHandlers.successCallback = {
                scope: args[1].component,
                fn: function(response){
                    //do an ajax call to save version comment
                    var value = YAHOO.util.Dom.get('${fieldHtmlId}').value;

                    if (value && value != "") {
                        var callback = function(){ old.fn.call(old.scope,response); };
                    
                        //save it with an ajax call
                        Alfresco.util.Ajax.request({
		                    url: Alfresco.constants.PROXY_URI + "vgr/versioncomment",
		                    method: Alfresco.util.Ajax.POST,
		                    requestContentType: Alfresco.util.Ajax.JSON,
		                    responseContentType: Alfresco.util.Ajax.JSON,
		                    dataObj: { comment: value, nodeRef: '${(nodeRef!page.url.args.nodeRef)?js_string}' }, 
		                    successCallback: { fn: callback, scope: this },
                            failureCallback: { fn: callback, scope: this }
		                });
     
                    } else {
                        //just call old successHandler
                        old.fn.call(old.scope,response);
                    }
                }
            };
           
                
        });
    // ]]>
    </script>
</#if>
