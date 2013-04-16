#language: sv
Egenskap: Publicering av dokument
    När man publicerat och avpublicerat dokument, ändrar metadata etc så ska
    alfresco hålla koll på status och visa detta med status ikoner
  
  Bakgrund:
    Givet att jag är inloggad som admin 
    Och har skapat en testplats 
    
  @javascript
  Scenario: Publicering endast tillåten om alla obligatoriska fält ifyllda
    Givet att jag har laddat upp ett test dokument
    Och att jag är i dokumentbiblioteket
    När jag klickar i checkboxen för dokument nr 1 i listan
    Och väljer "Publicera till lagret" i menyn "Valda objekt"
    Så ska jag se dialogen "Publicera till lagret"
    Och se texten "Saknar giltig dokumentbeskrivning"
    
  @javascript
  Scenario: Publicera ett dokument
    Givet att jag har laddat upp ett test dokument
    Och jag har fyllt i alla dess obligatoriska fält
    Och att jag är i dokumentbiblioteket
    När jag klickar i checkboxen för dokument nr 1 i listan
    Och väljer "Publicera till lagret" i menyn "Valda objekt"
    Så ska jag se dialogen "Publicera till lagret"
    Och ska inte se texten "Saknar giltig dokumentbeskrivning"
    När jag klickar på knappen "OK"
    Så ska jag se "Publicerad"
