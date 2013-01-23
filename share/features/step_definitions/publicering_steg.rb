# encoding: UTF-8

När /^väljer "Publicera till lagret" i menyn "Valda objekt"$/ do 
    #show menu
    page.execute_script("Alfresco.util.ComponentManager.find({name:'Alfresco.DocListToolbar'})[0].widgets.selectedItems._menu.show()")
    click_link "Publicera till lagret"
end


Givet /^jag har fyllt i alla dess obligatoriska fält$/ do
    Givet "att jag är på test dokumentets ändra metadata formulär"
    Givet "har öppnat alla flikar"
    När 'jag väljer "Projektdokument" ifrån rullgardinslistan "Dokumenttyp:*"'
    Så 'ska rullgardinslistan "Handlingstyp:*" fyllas med värden'
    När 'jag väljer "Ospecificerat" ifrån rullgardinslistan "Handlingstyp:*"'
    När 'väljer "Arbetsmaterial" ifrån rullgardinslistan "Dokumentstatus:*"'
    När 'fyller i "foobar" i fältet "Projekt/Uppdrag/Grupp:" under "Publicering"'
    När 'klickar på knappen "Spara"'
    Så 'ska jag se "Projektdokument"'
end

