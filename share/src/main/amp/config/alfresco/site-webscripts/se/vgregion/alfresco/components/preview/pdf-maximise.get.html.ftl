<@markup id="css" >
  <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/preview/pdf-maximise.css" group="pdfMaximise" />
</@>

<@markup id="js">
  <@script type="text/javascript" src="${url.context}/res/components/preview/pdf-maximise.js"  group="pdfMaximise" />
  <@script type="text/javascript" src="${url.context}/res/components/preview/pdfobject.js" group="pdfMaximise" />
</@>

<@markup id="widgets">
  <@createWidgets group="pdfMaximize"/>
</@>

<@markup id="html">
	<#compress>
	
	<#-- This file is part of the Share Extras PdfJs Viewer project -->
	    <#assign el=args.htmlid?html>
	
	<div id="pdf-maximise">
	    <div id="toolbar">
	        <a href="javascript:window.close();"><img src="${url.context}/res/components/preview/close.png" /></a>
	    </div>
	
	    <div id="pdf" />
	</div>
	
	</#compress>
</@>