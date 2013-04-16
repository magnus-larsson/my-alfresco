#language: sv
Egenskap: Standard sida för plats
  När man navigerar till en plats ifråm "Min anslagstavla" så ska man komma till 
  den sida som är vald att vara standard
  
  Bakgrund:
    Givet att jag är inloggad som admin 
    Och har skapat en testplats
    Och har satt testplatsens sido standard till dokumentbiblioteket 
    
  @javascript
  Scenario: Mina webbplatser dashlet
    Givet att jag är på min anslagstavla
    När jag klickar på länken "testplats" under dashleten mina webplatser
    Så ska jag se sidan dokumentbiblioteket på testplatsen
    


