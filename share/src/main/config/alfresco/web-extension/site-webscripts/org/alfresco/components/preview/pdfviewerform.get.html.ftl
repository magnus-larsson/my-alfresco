<#-- This file is part of the Share Extras PdfJs Viewer project -->
<#assign el=args.htmlid?html>
    <div id="controls">
      <button id="previous" onclick="PDFView.page--;" oncontextmenu="return false;">
        <img src="${url.context}/res/components/images/back-arrow.png" align="top" height="16"/>
        ${msg("button.previous")}
      </button>

      <button id="next" onclick="PDFView.page++;" oncontextmenu="return false;">
        <img src="${url.context}/res/components/images/forward-arrow-16.png" align="top" height="16"/>
        ${msg("button.next")}
      </button>



      <input type="number" id="pageNumber" onchange="PDFView.page = this.value;" value="1" size="4" min="1" />

      <span>/</span>
      <span id="numPages">--</span>



      <button id="zoomOut" title="${msg("button.zoomout")}" onclick="PDFView.zoomOut();" oncontextmenu="return false;">
        <img src="${url.context}/res/components/preview/pdfjs/images/zoom-out.svg" align="top" height="16"/>
      </button>
      <button id="zoomIn" title="${msg("button.zoomin")}" onclick="PDFView.zoomIn();" oncontextmenu="return false;">
        <img src="${url.context}/res/components/preview/pdfjs/images/zoom-in.svg" align="top" height="16"/>
      </button>



      <select id="scaleSelect" onchange="PDFView.parseScale(this.value);" oncontextmenu="return false;">
        <option id="customScaleOption" value="custom"></option>
        <option value="0.5">50%</option>
        <option value="0.75">75%</option>
        <option value="1">100%</option>
        <option value="1.25">125%</option>
        <option value="1.5">150%</option>
        <option value="2">200%</option>
        <option id="pageWidthOption" value="page-width">${msg("select.pagewidth")}</option>
        <option id="pageFitOption" value="page-fit">${msg("select.pagefit")}</option>
        <option id="pageAutoOption" value="auto" selected="selected">${msg("select.auto")}</option>
      </select>



      <button id="print" onclick="window.print();" oncontextmenu="return false;">
        <img src="${url.context}/res/components/images/printer-16.png" align="top" height="16"/>
        ${msg("button.print")}
      </button>

      <#-- hidden the download button for the moment, only confuses -->
      <button class="hidden" id="download" title="${msg("button.download")}" onclick="PDFView.download();" oncontextmenu="return false;">
        <img src="${url.context}/res/components/documentlibrary/images/download-16.png" align="top" height="16"/>
        ${msg("button.download")}
      </button>



      <button id="fullpage" title="${msg("button.fullpage")}" onclick="window.open(window.location, '_blank');" oncontextmenu="return false;">
        <img src="${url.context}/res/components/documentlibrary/images/generic-16.png" align="top" height="16"/>
        ${msg("button.fullpage")}
      </button>

    </div>
    <div id="errorWrapper" hidden='true'>
      <div id="errorMessageLeft">
        <span id="errorMessage"></span>
        <button id="errorShowMore" onclick="" oncontextmenu="return false;">
          ${msg("error.moreinformation")}
        </button>
        <button id="errorShowLess" onclick="" oncontextmenu="return false;" hidden='true'>
          ${msg("error.lessinformation")}
        </button>
      </div>
      <div id="errorMessageRight">
        <button id="errorClose" oncontextmenu="return false;">
          ${msg("error.close")}
        </button>
      </div>
      <div class="clearBoth"></div>
      <textarea id="errorMoreInfo" hidden='true' readonly="readonly"></textarea>
    </div>

    <div id="sidebar">
      <div id="sidebarBox">
        <div id="pinIcon" onClick="PDFView.pinSidebar()"></div>
        <div id="sidebarScrollView">
          <div id="sidebarView"></div>
        </div>
        <div id="outlineScrollView" hidden='true'>
          <div id="outlineView"></div>
        </div>
        <div id="sidebarControls">
          <button id="thumbsSwitch" title="${msg("sidebar.showthumbnails")}" onclick="PDFView.switchSidebarView('thumbs')" data-selected>
            <img src="${url.context}/res/components/preview/pdfjs/images/nav-thumbs.svg" align="top" height="16" alt="${msg("sidebar.showthumbnails.alt")}" />
          </button>
          <button id="outlineSwitch" title="${msg("sidebar.showoutline")}" onclick="PDFView.switchSidebarView('outline')" disabled>
            <img src="${url.context}/res/components/preview/pdfjs/images/nav-outline.svg" align="top" height="16" alt="${msg("sidebar.showoutline.alt")}" />
          </button>
        </div>
      </div>
    </div>

    <div id="loadingBox">
        <div id="loading">${msg("loading")}... 0%</div>
        <div id="loadingBar"><div class="progress"></div></div>
    </div>
    <div id="viewer"></div>
    