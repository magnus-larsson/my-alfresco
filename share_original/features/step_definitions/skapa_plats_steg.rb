# encoding: UTF-8

När /^jag fyller i namn fältet med "([^"]*)"$/ do |value|
  fill_in 'alfresco-createSite-instance-title', :with => value 
end

Så /^ska URL namn automatiskt fyllas med "([^"]*)"$/ do |value|
  find('#alfresco-createSite-instance-shortName').value.should == value
end

