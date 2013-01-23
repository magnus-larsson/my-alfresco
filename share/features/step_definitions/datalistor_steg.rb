# encoding: UTF-8

Givet /^har skapat två listor av typen "Att göra lista"$/ do
    visit "/share/page/site/testplats/data-lists"
    wait_until { page.has_content? 'Skapa ny datalista' }
    #first time we don't have a datalist so the create datalist dialog pops up automatically
    find(:css, '.yui-panel-container h4').click
    fill_in 'prop_cm_title', :with => 'Testlista 1'
    click_button 'Skicka'
    wait_until { page.has_content? 'Lyckades skapa ny datalista' }

    #create the second list
    visit "/share/page/site/testplats/data-lists"
    click_button "Ny lista"
    wait_until { page.has_content? 'Skapa ny datalista' }
    sleep 2
    find(:css, '.yui-panel-container h4').click
    fill_in 'prop_cm_title', :with => 'Testlista 2'
    click_button 'Skicka'
    wait_until { page.has_content? 'Lyckades skapa ny datalista' }
end

Givet /^att jag är på testplatsens sida "Datalistor" och har valt listan "([^"]*)"$/ do |list|
    visit "/share/page/site/testplats/data-lists"
    click_link list
    wait_until { page.has_content? 'Tilldelad' } #wait for the ajax load
    sleep 1
end

Givet /^att jag har en rad i listan med värdet "([^"]*)"$/ do |value|
    click_button "Nytt objekt"
    wait_until { page.has_content? 'Skapa nytt objekt' }
    fill_in 'prop_dl_todoTitle', :with => value
    click_button 'Skicka'
    wait_until { page.has_content? 'Lyckades lägga till nytt objekt' }
end


När /^jag i dialogen väljer listan "Testlista 2"$/ do 
    sleep 1
    find(:css,'.move-dialog-datalists li').click
    sleep 2 #the button has to undisable itselt
end


När /^jag klickar i checkboxen för första raden$/ do
  check 'fileChecked'
  sleep 2
end


När /^väljer "Flytta" i menyn "Valda objekt"$/ do
    click_button 'Valda objekt'
    sleep 1
    click_link 'Flytta'
end


