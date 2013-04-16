#language: sv
Egenskap: Editering av metadata
    En användare ska kunna lägga till metadata för ett dokument
  
  Bakgrund:
    Givet att jag är inloggad som admin 
    Och har skapat en testplats 
    Och att jag har laddat upp ett test dokument
    
  @javascript
  Scenario: Lägg till obligatoriska fält
    Givet att jag är på test dokumentets ändra metadata formulär
    Och har öppnat alla flikar
    När jag väljer "Projektdokument" ifrån rullgardinslistan "Dokumenttyp:*"
    Så ska rullgardinslistan "Handlingstyp:*" fyllas med värden
    När jag väljer "Ospecificerat" ifrån rullgardinslistan "Handlingstyp:*"
    Och väljer "Arbetsmaterial" ifrån rullgardinslistan "Dokumentstatus:*"
    Och fyller i "foobar" i fältet "Projekt/Uppdrag/Grupp:" under "Publicering"
    Och klickar på knappen "Spara"
    Så ska jag se "Projektdokument"
    
