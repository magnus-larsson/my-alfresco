<#-- @overridden projects/repository/config/alfresco/templates/invite-email-templates/invite-email.html.ftl -->
<html>
   <#assign inviterPersonRef=args["inviterPersonRef"]/>
   <#assign inviterPerson=companyhome.nodeByReference[inviterPersonRef]/>
   <#assign inviteePersonRef=args["inviteePersonRef"]/>
   <#assign inviteePerson=companyhome.nodeByReference[inviteePersonRef]/>

   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }
      
      a, a:visited
      {
         color: #0072cf;
      }
      --></style>
   </head>
   
   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 10px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td>
                                                   <img src="${shareUrl}/res/components/site-finder/images/site-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      Du har blivit inbjuden till samarbetsytan ${args["siteName"]}
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hej ${inviteePerson.properties["cm:firstName"]!""},</p>
      
                                             <p>${inviterPerson.properties["cm:firstName"]!""} ${inviterPerson.properties["cm:lastName"]!""} 
                                             har bjudit in dig till samarbetsytan <b>${args["siteName"]}</b> med rollen ${args["inviteeSiteRole"]}.</p>
                                             
                                             <p>Klicka på denna länk för att <span style="font-weight: bolder; color: green;">acceptera</span> ${inviterPerson.properties["cm:firstName"]!""}'s inbjudan:<br />
                                             <br /><a href="${args["acceptLink"]}">${args["acceptLink"]}</a></p>
                                             
                                             <#if args["inviteeGenPassword"]?exists>
                                             <p>Ett konto har skapats för dig. Dina inloggningsuppgifter är:<br />
                                             <br />Användarnamn: <b>${args["inviteeUserName"]}</b>
                                             <br />Lösenord: <b>${args["inviteeGenPassword"]}</b>
                                             </p>
                                             
                                             <p><b>Vi rekommenderar kraftigt att du ändrar ditt lösenord när du loggar in första gången.</b><br />
                                             Du kan göra detta genom att gå till <b>Min profil</b> och välj <b>Ändra lösenord</b>.</p>
                                             </#if>
                                             
                                             <p>Om du vill <span style="color: red;">avböja</span> ${inviterPerson.properties["cm:firstName"]!""}’s inbjudan, klicka på denna länken:<br />
                                             <br /><a href="${args["rejectLink"]}">${args["rejectLink"]}</a></p>
                                             
                                             <p>Med vänlig hälsningar,<br />
                                             ${inviterPerson.properties["cm:firstName"]} ${inviterPerson.properties["cm:lastName"]}
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-top: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 0px 30px; font-size: 13px;">
                                 För att få reda på mer om Alfresco ${productName!""} gå till <a href="http://www.vgregion.se/alfresco">http://www.vgregion.se/alfresco</a>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 10px 30px;">
                                 <img src="${shareUrl}/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>