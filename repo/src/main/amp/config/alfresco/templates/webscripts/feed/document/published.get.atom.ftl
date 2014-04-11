<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:DC="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:VGR="http://purl.org/vgregion/elements/1.0/">
  <id>${absurl(url.service)?replace(':80/', '/')?replace(':443/', '/')}</id>
  <link href="${absurl(encodeuri(url.full))?xml?replace(':80/', '/')?replace(':443/', '/')}" rel="self" />  
  <generator version="${server.version}">Alfresco (${server.edition})</generator>
  <title>VGR Published documents feed for content modified between ${from} and ${to}</title> 
  <updated>${now}</updated>
  <icon>${absurl(url.context)?replace(':80/', '/')?replace(':443/', '/')}/images/logo/AlfrescoLogo16.ico</icon>
  <author> 
    <name><#if person??>${person.properties.userName}<#else>unknown</#if></name>
  </author> 
<#list data.items as row>
  <entry>
    <published>${row.published}</published>
    <requestId>${row.request_id}</requestId>
    <@outputSingle value=row.title tag="title" />
    <link href="${(absurl(row.downloadUrl)!"")?html?replace(':80/', '/')?replace(':443/', '/')}"/>
    <@outputSingle value=row.id tag="id" />
    <@outputSingle value=row.modified tag="updated" skip=true />
    <@outputSingle value=row.description tag="summary" skip=true />
    <@outputMultiple values=row.creator tag="DC.creator" /> 
    <@outputMultiple values=row.creator_id tag="DC.creator.id" />
    <@outputSingle value=row.title tag="DC.title" />
    <@outputMultiple values=row.alt_title tag="DC.title.alternative" />
    <@outputSingle value=row.title_filename tag="DC.title.filename" />
    <@outputSingle value=row.title_filename_native tag="DC.title.filename.native" />
    <@outputSingle value=row.saved tag="DC.date.saved" skip=true />
    <@outputSingle value=row.creator_freetext tag="DC.creator.freetext" />
    <@outputMultiple values=row.creator_document tag="DC.creator.document" />
    <@outputMultiple values=row.creator_document_id tag="DC.creator.document.id" />
    <@outputSingle value=row.creator_function tag="DC.creator.function" />
    <@outputMultiple values=row.creator_forunit tag="DC.creator.forunit" />
    <@outputMultiple values=row.creator_forunit_id tag="DC.creator.forunit.id" />
    <@outputMultiple values=row.creator_recordscreator tag="DC.creator.recordscreator" />
    <@outputMultiple values=row.creator_recordscreator_id tag="DC.creator.recordscreator.id" />
    <@outputSingle value=row.creator_project_assignment tag="DC.creator.project-assignment" />
    <@outputSingle value=row.publisher tag="DC.publisher" />
    <@outputSingle value=row.publisher_id tag="DC.publisher.id" />
    <@outputMultiple values=row.publisher_forunit tag="DC.publisher.forunit" />
    <@outputMultiple values=row.publisher_forunit_id tag="DC.publisher.forunit.id" />
    <@outputMultiple values=row.publisher_project_assignment tag="DC.publisher.project-assignment" />
    <@outputSingle value=row.date_issued tag="DC.date.issued" skip=true />
    <@outputSingle value=row.contributor_savedby tag="DC.contributor.savedby" />
    <@outputSingle value=row.contributor_savedby_id tag="DC.contributor.savedby.id" />
    <@outputMultiple values=row.contributor_acceptedby tag="DC.contributor.acceptedby" />
    <@outputMultiple values=row.contributor_acceptedby_id tag="DC.contributor.acceptedby.id" />
    <@outputSingle value=row.contributor_acceptedby_freetext tag="DC.contributor.acceptedby.freetext" />
    <@outputSingle value=row.date_accepted tag="DC.date.accepted" skip=true />
    <@outputSingle value=row.contributor_acceptedby_role tag="DC.contributor.acceptedby.role" />
    <@outputSingle value=row.contributor_acceptedby_unit_freetext tag="DC.contributor.acceptedby.unit.freetext" />
    <@outputMultiple values=row.contributor_controlledby tag="DC.contributor.controlledby" />
    <@outputMultiple values=row.contributor_controlledby_id tag="DC.contributor.controlledby.id" />
    <@outputSingle value=row.format tag="DC.format.extent.mimetype" />
    <@outputSingle value=row.format_native tag="DC.format.extent.mimetype.native" />
    <@outputMultiple values=row.language tag="DC.language" skip=true />
    <@outputSingle value=row.created tag="dcterms.created" skip=true />
    <@outputMultiple values=row.author_keywords tag="DC.subject.authorkeywords" />
    <@outputMultiple values=row.subject_keywords tag="DC.subject.keywords" />
    <@outputMultiple values=row.subject_keywords_id tag="DC.subject.keywords.id" />
    <@outputSingle value=row.description tag="DC.description" />
    <@outputSingle value=row.contributor_controlledby_freetext tag="DC.contributor.controlledby.freetext" />
    <@outputSingle value=row.date_controlled tag="DC.date.controlled" skip=true />
    <@outputSingle value=row.contributor_controlledby_role tag="DC.contributor.controlledby.role" />
    <@outputSingle value=row.contributor_controlledby_unit_freetext tag="DC.contributor.controlledby.unit.freetext" />
    <@outputSingle value=row.date_validfrom tag="DC.date.validfrom" skip=true />
    <@outputSingle value=row.date_validto tag="DC.date.validto" skip=true />
    <@outputSingle value=row.date_availablefrom tag="DC.date.availablefrom" skip=true />
    <@outputSingle value=row.date_availableto tag="DC.date.availableto" skip=true />
    <@outputSingle value=row.date_copyrighted tag="DC.date.copyrighted" skip=true />
    <@outputSingle value=row.type_document tag="DC.type.document" />
    <@outputSingle value=row.type_document_structure tag="DC.type.document.structure" />
    <@outputSingle value=row.type_document_structure_id tag="DC.type.document.structure.id" />
    <@outputSingle value=row.type_templatename tag="DC.type.templatename" />
    <@outputSingle value=row.type_record tag="DC.type.record" />
    <@outputSingle value=row.type_record_id tag="DC.type.record.id" />
    <@outputMultiple values=row.type_process_name tag="DC.type.process.name" />
    <@outputMultiple values=row.type_file_process tag="DC.type.file.process" />
    <@outputMultiple values=row.type_file tag="DC.type.file" />
    <@outputSingle value=row.type_document_serie tag="DC.type.document.serie" />
    <@outputSingle value=row.type_document_id tag="DC.type.document.id" />
    <@outputMultiple values=row.format_extent tag="DC.format.extent" />
    <@outputSingle value=row.format_extension tag="DC.format.extension" />
    <@outputSingle value=row.format_extension_native tag="DC.format.extension.native" />
    <@outputSingle value=row.identifier_diarie_id tag="DC.identifier.diarie.id" />
    <@outputSingle value=row.identifier tag="DC.identifier" />
    <@outputSingle value=row.identifier_native tag="DC.identifier.native" />
    <@outputSingle value=row.identifier_checksum tag="DC.identifier.checksum" />
    <@outputSingle value=row.identifier_checksum_native tag="DC.identifier.checksum.native" />
    <@outputSingle value=row.identifier_documentid tag="DC.identifier.documentid" />
    <@outputSingle value=row.identifier_version tag="DC.identifier.version" />
    <@outputSingle value=row.source tag="DC.source" />
    <@outputSingle value=row.source_documentid tag="DC.source.documentid" />
    <@outputSingle value=row.origin tag="DC.source.origin" />
    <@outputSingle value=row.relation_isversionof tag="DC.relation.isversionof" />
    <@outputMultiple values=row.relation_replaces tag="DC.relation.replaces" />
    <@outputMultiple values=row.coverage_hsacode tag="DC.coverage.hsacode" />
    <@outputMultiple values=row.coverage_hsacode_id tag="DC.coverage.hsacode.id" />
    <@outputMultiple values=row.audience tag="dcterms.audience" />
    <@outputMultiple values=row.audience_id tag="dcterms.audience.id" />
    <@outputSingle value=row.status_document tag="VGR.status.document" />
    <@outputSingle value=row.status_document_id tag="VGR.status.document.id" />
    <@outputSingle value=row.rights_accessrights tag="DC.rights.accessrights" />
    <@outputMultiple values=row.identifier_location tag="DC.identifier.location" />
  </entry>
</#list>
</feed>

<#-- Outputs a multi value value, an empty tag if no values found -->
<#macro outputMultiple tag values=[] skip=false>
  <#if values??>
    <#if (values?size > 0)>
      <#list values as value>
      <@outputSingle tag=tag value=value skip=skip/>  
      </#list>
    <#else>
      <@outputEmpty tag=tag skip=skip/>
    </#if>
  <#else>
    <@outputEmpty tag=tag skip=skip/>
  </#if>
</#macro>

<#-- Outputs a single value, an empty tag if no value found -->
<#macro outputSingle tag value="" skip=false>
  <#if value?? && value!="">
   <${tag}>${value?replace("&#44;", ",")?xml}</${tag}>
  <#else>
    <@outputEmpty tag=tag skip=skip />
  </#if>
</#macro>

<#macro outputEmpty tag skip=true>
  <#if !skip>
   <${tag} />
  </#if>
</#macro>