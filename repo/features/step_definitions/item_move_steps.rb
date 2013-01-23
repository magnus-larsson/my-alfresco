##############
# Background #
##############

Given /^a site with ID "([^"]*)"$/ do |site_id|
  visit 'http://localhost:8081/share/page/site-index'
  fill_in 'username', :with => 'admin'
  fill_in 'password', :with => 'admin'
  click_button 'Logga in'
  visit 'http://localhost:8081/share/page/site/demo/dashboard'
end

Given /^I click on the "([^"]*)" module$/ do |module_id|
  click_link module_id
end

Given /^I want to move an item$/ do
  @query = {}
end

Given /^I create a data list of type "([^"]*)" with the name "([^"]*)"$/ do |data_list_type, data_list_name|
  click_button 'Ny lista'
  find('h4', :text => data_list_type).click
  fill_in 'Titel', :with => data_list_name
  click_button 'Skicka'
end

Given /^I set parameter "([^"]*)" to "([^"]*)"$/ do |param, value|
  @query.merge!({param => value})
end

When /^I send the JSONP request to "([^"]*)"$/ do |url|
  @http_client = HTTPClient.new
  @http_client.set_auth("http://localhost:8080/alfresco/service", "admin", "admin")
  @response = @http_client.post("http://localhost:8080/alfresco/service" + url, @query, :header => {'Content-type' => 'application/json', 'Accept' => 'application/json'})
end

Then /^I should get a "([^"]*)" response$/ do |code|
  assert_equal code.to_i, @response.code, @response.content
end

Then /^I should see "([^"]*)"$/ do |arg1|
  
end