require 'capybara' 
require 'capybara/dsl' 
require 'capybara/cucumber'
require 'test/unit/assertions'
require 'httpclient'
require 'crack'
require 'cucumber/formatter/unicode'
require 'test/unit/assertions'

Capybara.run_server = false

HOST = 'http://localhost:8080'
Capybara.app_host = HOST 

require 'selenium-webdriver'
Selenium::WebDriver.for :firefox
Capybara.default_driver = :selenium

Capybara.default_selector = :css

World(Capybara, Test::Unit::Assertions)
