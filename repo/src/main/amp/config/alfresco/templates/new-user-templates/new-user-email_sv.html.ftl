<#-- @overridden /home/mars/Documents/alfresco/alfresco-source/ENTERPRISE/4.2.4/projects/repository/config/alfresco/templates/new-user-templates/new-user-email.html -->
<html>
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
                                                   <img src="${shareUrl}/res/components/images/no-user-photo-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      Ditt nya Alfresco ${productName!""}-konto
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hej ${firstname},</p>

                                             <p>${creator.firstname} ${creator.lastname} har skapat ett Alfresco ${productName!""}-konto åt dig.</p>
                                             
                                             <p>Klicka på följande länk för att logga in:<br />
                                             <br /><a href="${shareUrl}">${shareUrl}</a></p>
                                             
                                             <p>Dina inloggningsuppgifter är:<br />
                                             <br />Användarnamn: <b>${username}</b>
                                             <br />Lösenord: <b>${password}</b>
                                             </p>
                                             
                                             <p><b>Vi rekommenderar kraftigt att du ändrar ditt lösenord när du loggar in första gången.</b><br />
                                             Du kan göra detta genom att gå till <b>Min profil</b> och välj <b>Ändra lösenord</b>.</p>
                                             </#if>
                                             
                                             <p>Med vänliga hälsningar,<br />
                                             Alfresco ${productName!""}</p>
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
                                 <img src="${shareUrl}/res/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
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
