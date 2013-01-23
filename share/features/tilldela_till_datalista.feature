#language: sv
#
# Testerna här är tyvärr beroende på att en sökning på alla användare
# med termen ad* får mer än en träff. Dock har vi inget bra sätt att skapa
# en testanvändare för detta. Om du inte har en lämplig användare i databasen
# måste en skapas för testen.
#
# Vidare så kan inte editering av existerande list objekt testas då det krävs
# ett riktigt mouseover event för att skapa editeringsknappen
#
Egenskap: På datalistor ska man bara kunna tilldela uppgifter till site medlemmar

    Bakgrund:
        Givet att jag är inloggad som admin
        Och har skapat en privat testplats
        Och skapat en datalista kallad "test"
        Och är på datalist sidan för "test"

    @javascript
    Scenario: Bjuda in användare vid skapandet av nytt list objekt        
        När jag klickar på "Nytt objekt"
        Så ska jag se dialogen "Skapa nytt objekt"
        
        När jag klickar på knappen "Ändra"
        Så ska jag se en dialogen "Tilldelad"
        Och jag ska "Medlemmar i platsen"
        Och jag ska se "Alla användare"
        
        När jag fyller i "ad*" i sökfältet
        Och klickar på knappen "Sök"
        Så ska jag se ett resultat i listan
        
        När jag klickar i "Alla användare" radioknappen
        Och fyller i "ad*" i sökfältet
        Och klickar på knappen "Sök"
        Så ska jag se mer än ett resultat i listan
        
        När jag klickar på "Administrator (admin)" i listan
        Så ska jag inte se dialogen "Tilldelad"
        Och jag ska se "Administrator (admin)"
        
        När jag klickar på krysset till höger om "Administrator (admin)"
        Så ska jag inte se "Administrator (admin)"
        
        När jag klickar på knappen "Ändra"
        Så ska jag se en dialogen "Tilldelad"
            
        När fyller i "admin" i sökfältet
        Och klickar på knappen "Sök"
        Och klickar på "Administrator (admin)" i listan
        Så ska jag inte se dialogen "Tilldelad"
        Och jag ska se "Administrator (admin)"
        
        När jag fyller i "Test Objekt" i fältet "Titel"
        Och klickar på knappen "Skicka"
        Så ska jag inte se dialogen "Skapa nytt objekt"
        Och jag ska se raden "Test Objekt" tilldelad till "Administrator" i listan
        
