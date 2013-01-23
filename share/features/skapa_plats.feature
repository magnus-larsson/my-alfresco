#language: sv
Egenskap: Automatiskt url vid skapandet av plats
  När man skapar en plats får man automatiskt en trevlig url satt utefter
  platsens namn
    
  Bakgrund:
    Givet att jag är inloggad som admin
    
  @selenium
  Scenario: Skapa plats
    Givet att jag är på min anslagstavla
    När jag klickar på länken "Skapa webbplats"
    Så ska jag se dialogen "Skapa webbplats"
    När jag fyller i namn fältet med "Härliga tider!"
    Så ska URL namn automatiskt fyllas med "hrligatider"


