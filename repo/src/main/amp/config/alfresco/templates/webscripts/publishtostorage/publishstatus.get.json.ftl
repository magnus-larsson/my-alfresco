{ 
	"result": "${result}", 
	"publish_status": <#if publishStatus??>"${publishStatus}"<#else>""</#if>,
	"unpublish_status": <#if unpublishStatus??>"${unpublishStatus}"<#else>""</#if>, 
	"pushed_for_publish": <#if pushed_for_publish??>"${pushed_for_publish?datetime?iso_utc_ms}"<#else>""</#if>,
	"pushed_for_unpublish": <#if pushed_for_unpublish??>"${pushed_for_unpublish?datetime?iso_utc_ms}"<#else>""</#if>,
	"nodeRef": <#if nodeRef??>"${nodeRef}"<#else>""</#if>
}
