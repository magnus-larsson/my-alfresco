# encoding: UTF-8

Givet /^att jag är inloggad som admin$/ do
  visit '/share/page/site-index'
  fill_in 'username', :with => 'admin'
  fill_in 'password', :with => 'admin'
  click_button 'Logga in'
end

Givet /^att jag är på min anslagstavla$/ do
  visit('/share/page/user/admin/dashboard')
end

När /^jag klickar på länken "([^"]*)"$/ do |link|
  click_link link
end

Så /^ska jag se dialogen "([^"]*)"$/ do |title|
  find('div.yui-panel').find('div.hd').should have_content(title)
end

När /^klickar på knappen "([^"]*)"$/ do |button|
  click_button button
end

När /^jag klickar på knappen "([^"]*)"$/ do |button|
  click_button button
end

Så /^ska jag se notifieringen "([^"]*)"$/ do |txt|
    wait_until { page.has_content? txt }
end

Så /^ska jag se "([^"]*)"$/ do |txt|
  wait_until { page.has_content? txt }
end


Givet /^att jag har laddat upp ett test dokument$/ do
    path = File.expand_path $0
    #print "file: "+path
    visit  '/share/page/site/testplats/documentlibrary'
    sleep 2
    wait_until { page.has_content? 'Ladda upp' } #this part of the page is ajax loaded
    click_button 'Ladda upp'
    wait_until { page.has_content? 'Ladda upp fil' }
    page.attach_file('filedata', path)
    sleep 2
    click_button "Ladda upp fil"
    sleep 2
    wait_until { page.has_content? 'cucumber' }
end


Givet /^att jag är i dokumentbiblioteket$/ do
    visit  '/share/page/site/testplats/documentlibrary'
    wait_until { page.has_content? 'Ladda upp' } #this part of the page is ajax loaded
    sleep 2  
end

När /^jag klickar i checkboxen för dokument nr (\d+) i listan$/ do |index|
    #index is 1 based in the feature but 0 based in reality
    index = index.to_i - 1
    tr = all(:css,'.documents tbody.yui-dt-data tr')[index]
    tr.check('fileChecked') 
    sleep 1 #let the menus get a chance to react
end


Så /^se texten "([^"]*)"$/ do |txt|
    wait_until { page.has_content? txt }
end

Så /^ska inte se texten "([^"]*)"$/ do |txt|
    #since we're in an ajax world wee sleep a bit to give dialogs and stuff a chance
    sleep 2
    page.should have_no_content(txt)
end



#fungerar ej
#När /^jag klickar på "([^"]*)" ikonen$/ do |value|
    #ok first we load jquery
#    page.execute_script("YAHOO.util.Get.script('https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js',onSuccess=function(){ jQuery.noConflict(); });")
#    wait_until { page.evaluate_script('window.jQuery != undefined') }
    #then we trigger a mouse over
#    page.execute_script('jQuery.noConflict(); 
#                         jQuery("table").trigger("mouseenter");
#                         jQuery(".hidden").removeClass("hidden");
#                         alert(jQuery)')
#    sleep 5
#    first(:css, 'a[title="'+value+'"]').click
#end

När /^jag väljer "([^"]*)" ifrån rullgardinslistan "([^"]*)"$/ do |value, name|
    #find the label and find it's field from the "for" attribute
    labels = all(:css,'label')
    found_it = false
    #puts "#" + name + "#"
    for label in labels
        #puts "#" + label.text + "#"
        if label.text.strip == name 
            #print "found label: " + label['for'] 
            select(value, :from => label['for'])
            found_it = true
            break    
        end 
    end
    assert found_it
end


När /^väljer "([^"]*)" ifrån rullgardinslistan "([^"]*)"$/ do |arg1, arg2|
    När "jag väljer \"#{arg1}\" ifrån rullgardinslistan \"#{arg2}\""
end



Så /^ska rullgardinslistan "([^"]*)" fyllas med värden$/ do |name|
    sleep 2 #this is ajax loaded, so give it a bit of time
    #find the label and find it's field from the "for" attribute
    labels = all(:css,'label')
    found_it = false
    for label in labels
        #puts label.text
        if label.text.strip == name 
            #lets find the select and check that it has more than one option
            options = all(:css,'select[id="' + label['for'] + '"] option')
            #puts options
            assert options.length > 1
            found_it = true
            break    
        end 
    end
    assert found_it
end


När /^fyller i "([^"]*)" i fältet "([^"]*)"$/ do |value, name|
    labels = all(:css,'label')
    found_it = false
    for label in labels
        if label.text.strip == name 
            #lets find the select and check that it has more than one option
            fill_in label['for'],:with => value
            found_it = true
            break    
        end 
    end
    assert found_it

end



