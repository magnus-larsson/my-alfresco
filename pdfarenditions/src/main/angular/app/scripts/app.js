'use strict';

/**
 * @ngdoc overview
 * @name failedPdfaRenditionsApp
 * @description
 * # failedPdfaRenditionsApp
 *
 * Main module of the application.
 */
angular
  .module('failedPdfaRenditionsApp', [
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
    'ui.bootstrap'
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

    // RestangularProvider.setBaseUrl('/alfresco/service/vgr/pdfarenditions');
    RestangularProvider.setBaseUrl('/alfresco/service/vgr/pdfarenditions');
  }
])

.config(function($routeProvider) {
  $routeProvider
    .when('/', {
      templateUrl: 'views/main.html',
      controller: 'MainCtrl'
    })
    .otherwise({
      redirectTo: '/'
    });
});