# encoding: UTF-8

Givet /^att jag är på test dokumentets ändra metadata formulär$/ do
    #we dont have the document id here so we need to go through the documentlibtrary
    Givet "att jag är i dokumentbiblioteket"
    click_link 'cucumber'
    sleep 2
    click_link 'Ändra metadata'
    wait_until { page.has_content? 'Välj dokumenttyp'} #wait until ajax requests has loaded
end

Givet /^har öppnat alla flikar$/ do
    #we open the entire accordion so that we can easily fill in forms and click links
    page.evaluate_script "YAHOO.util.Dom.setStyle(document.getElementsByTagName('dd'),'display','block')"
end

När /^fyller i "([^"]*)" i fältet "Projekt\/Uppdrag\/Grupp:" under "Publicering"$/ do |value|
    #there are two fields with the same name, so we need to be a bit more smart
    labels = all(:css,'label')
    found_it = false
    for label in labels
        if label.text.strip == "Projekt/Uppdrag/Grupp:"
            #lets find the select and check that it has more than one option
            if label['for'].include? 'publisher'
                input  = label['for'] + '_0' #this is the first in a multivalue field        
                fill_in input,:with => value
                found_it = true
            end
        end 
    end
    assert found_it
end
