@api
Feature: Move a data list item from one list to another of the same type
  In order to move a data list item 
  As a regular user
  I want to be able to do this with a REST POST command
  
  Backgrounder:
    Given a site with ID "cucumber"
    And I click on the "Datalistor" module
    And I create a data list of type "Att göra lista" with the name "lista 1"
    And I create a data list of type "Att göra lista" with the name "lista 2"
  
  Scenario: Move the item
    Given I want to move an item
    And I set parameter "srcs" to "kalle"
    And I set parameter "target" to "kula"
    When I send the JSONP request to "/vgr/data-lists/move"
    Then I should get a "200" response
    And I should see "success"
