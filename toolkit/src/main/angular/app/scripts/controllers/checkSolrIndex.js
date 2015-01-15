'use strict';

var CheckSolrIndexController = function ($scope, Restangular, ngTableParams,
  $http, $q, splash, toaster) {

  $scope.refresh = function () {
    $http.get('/alfresco/service/vgr/toolkit/cache/refresh', {}).success(function (data, status) {
      toaster.pop('success', 'Refresh Success', 'Refresh cache job is started, it will take some time to finish.');
      node.repushed = true;
    }).error(function (data, status) {
      toaster.pop('error', 'Refresh Failure', 'Refresh if cache failed, message "' + data + '"');
    });
  };

  Restangular.one('cache', 'alfresco_orphans').get().then(function (data) {
    var result = [];

    for (var x = 0; x < data.orphans.length; x++) {
      result.push({
        nodeRef: data.orphans[x]
      });
    }

    $scope.alfrescoOrphans = result;
    $scope.alfrescoOrphansModified = data.cacheDate;
  }).then(function () {
    $scope.alfrescoOrphansTableParameters.reload();
  });

  Restangular.one('cache', 'solr_orphans').get().then(function (data) {
    var result = [];

    for (var x = 0; x < data.orphans.length; x++) {
      result.push({
        nodeRef: data.orphans[x]
      });
    }

    $scope.solrOrphans = result;
    $scope.solrOrphansModified = data.cacheDate;
  }).then(function () {
    $scope.solrOrphansTableParameters.reload();
  });

  $scope.alfrescoOrphansTableParameters = new ngTableParams({
    page: 1,
    count: 5
  }, {
    counts: [],
    total: 1,
    getData: function ($defer, params) {
      if (!$scope.alfrescoOrphans) {
        return;
      }

      $defer.resolve($scope.alfrescoOrphans);
    }
  });

  $scope.solrOrphansTableParameters = new ngTableParams({
    page: 1,
    count: 5
  }, {
    counts: [],
    total: 1,
    getData: function ($defer, params) {
      if (!$scope.solrOrphans) {
        return;
      }

      $defer.resolve($scope.solrOrphans);
    }
  });

  $scope.getMetadata = function (node) {
    $http.get('/alfresco/service/api/metadata?nodeRef=' + node.nodeRef)
      .success(function (result) {
        splash.open({
          title: 'Metadata',
          message: angular.toJson(result, 3)
        });
      });
  }

  $scope.repush = function (node) {
    $http.put('/alfresco/service/vgr/toolkit/repush', {
      nodeRef: node.nodeRef
    }).success(function (data, status) {
      var status = data.success;

      if (status === 'REPUSHED_FOR_PUBLISHED') {
        status = 'published';
      } else {
        status = 'unpublished';
      }

      toaster.pop('success', 'Repush Success', 'Repush of "' + node.nodeRef + '" as "' + status + '" successfull.');
      node.repushed = true;
    }).error(function (data, status) {
      var message = 'Repush of "' + node.nodeRef + '" failed.';

      if (status === 404) {
        message = data;
      }

      toaster.pop('error', 'Repush Failure', message);
    });
  }

};

CheckSolrIndexController.$inject = ['$scope', 'Restangular', 'ngTableParams', '$http', '$q', 'splash', 'toaster'];

angular.module('toolkitApp')

.config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider
      .state('checkSolrIndex', {
        url: '/checksolrindex',
        templateUrl: 'views/checkSolrIndex.html',
        controller: 'CheckSolrIndexController'
      });
  }
])

.controller('CheckSolrIndexController', CheckSolrIndexController);