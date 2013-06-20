# encoding: UTF-8

Givet /^har skapat en testplats$/ do
    #Check first if site is already created, then we need to remove it
    visit '/share/page/user/admin/dashboard'
    sleep 2 #ajax loading of sites often take time
    if page.has_content?('testplats')
        trs = page.all(:css, '.my-sites .yui-dt-data tr')
        for tr in trs
            link = tr.first(:css,'.site-title a')
            if link.text == 'testplats'
                #ok we got the right site, now lets find the "remove" link
                show = "YAHOO.util.Dom.addClass(YAHOO.util.Dom.get('" + tr['id'] + "'),'yui-dt-highlighted')"
                page.execute_script(show) #show the delete button
                tr.find(:css,'a.delete-site').click
                click_button 'Ta bort'
                click_button 'Ja'
                sleep 1
                break
            end
        end
    end 
    
    #create site
    find_link('Skapa webbplats').click
    fill_in 'alfresco-createSite-instance-title', :with => 'testplats'
    find_button('alfresco-createSite-instance-ok-button-button').click
    wait_until { page.has_no_content? 'Webbplats skapas' } #wait for dialog to close
    sleep 2
        
    
end

Givet /^har satt testplatsens sido standard till dokumentbiblioteket$/ do
  visit '/share/page/site/testplats/customise-site'
  sleep 1
  page.execute_script("CustomisePages.widgets.defaultpage.getMenu().show()") #shows the menu
  find_link('Dokumentbibliotek').click
  click_button 'OK'
  wait_until { page.has_no_content? 'Sparar konfiguration' }
end



När /^jag klickar på länken "testplats" under dashleten mina webplatser$/ do 
    visit '/share/page/site-index'
    click_link "testplats"
end

Så /^ska jag se sidan dokumentbiblioteket på testplatsen$/ do
  sleep 3
  current_path.should == "/share/page/site/testplats/documentlibrary"
end



