#language: sv
Egenskap: Flytta rad mellan datalistor av samma typ
  
  Bakgrund:
    Givet att jag är inloggad som admin 
    Och har skapat en testplats
    Och har skapat två listor av typen "Att göra lista"
    
  @javascript
  Scenario: Flytta rad mellan listor
    Givet att jag är på testplatsens sida "Datalistor" och har valt listan "Testlista 1"
    Och att jag har en rad i listan med värdet "Test av flytt"
    När jag klickar i checkboxen för första raden
    Och väljer "Flytta" i menyn "Valda objekt"
    Så ska jag se dialogen "Flytta objekt"
    När jag i dialogen väljer listan "Testlista 2" 
    Och klickar på knappen "Välj"
    Så ska jag se notifieringen "Dataobjekt flyttat"
    När jag klickar på länken "Testlista 2"
    Så ska jag se "Test av flytt"
