<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/upload/file-upload.get.head.ftl -->

<#include "../component.head.inc">
<!-- File-Upload -->
<@script type="text/javascript" src="${page.url.context}/res/components/upload/file-upload.js"></@script>

<!-- CheckIn, since it inherits upload we place it here after upload has been included -->
<@script type="text/javascript" src="${page.url.context}/res/components/checkin/checkin.js"></@script>