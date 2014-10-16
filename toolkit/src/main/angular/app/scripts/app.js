'use strict';

/**
 * @ngdoc overview
 * @name toolkitApp
 * @description
 * # toolkitApp
 *
 * Main module of the application.
 */
angular
  .module('toolkitApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'restangular',
    'ngTable',
    'toaster',
    'angularSpinner',
    'ui.ladda',
    'ui.bootstrap',
    'ui.router'
  ])

.run([
  '$rootScope',
  '$state',
  '$stateParams',
  function($rootScope, $state, $stateParams) {
    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;
  }
])

.config([
  'RestangularProvider',
  function(RestangularProvider) {
    // add a response intereceptor
    RestangularProvider.addResponseInterceptor(function(data, operation) {
      var extractedData;

      // .. to look for getList operations
      if (operation === 'getList') {
        // .. and handle the data and meta data
        extractedData = data.data;
        extractedData.total = data.total;
      } else {
        extractedData = data;
      }

      return extractedData;
    });

    RestangularProvider.setBaseUrl('/alfresco/service/vgr/toolkit');
  }
])

.config([
  '$stateProvider',
  '$urlRouterProvider',
  function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('/');
  }
]);