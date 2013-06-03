<#compress>

<#-- This file is part of the Share Extras PdfJs Viewer project -->
    <#assign el=args.htmlid?html>

<script type="text/javascript">
    new Alfresco.thirdparty.PdfMaximise("${el}").setOptions({
        nodeRef: '${nodeRef}',
        name: '${name}'
    });
</script>

<div id="pdf-maximise">
    <div id="toolbar">
        <a href="javascript:window.close();"><img src="${page.url.context}/res/components/preview/close.png" /></a>
    </div>

    <div id="pdf" />
</div>

</#compress>
