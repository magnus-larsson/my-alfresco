'use strict';

var CheckSolrIndexController = function($scope, Restangular, ngTableParams, $http, $q) {

  function getSolrNodes(rows, start) {
    var defer = $q.defer();

    var url = 'http://solr-index.vgregion.se:8080/solr/core0/select';

    $http({
      method: 'JSONP',
      url: url,
      params: {
        fq: 'source:p-facet.source.pubsub',
        fl: 'dc.identifier.documentid',
        rows: rows,
        start: start,
        wt: 'json',
        'json.wrf': 'JSON_CALLBACK'
      }
    }).success(function(data) {
      var nodes = [];
      angular.forEach(data.response.docs, function(value) {
        nodes.push(value['dc.identifier.documentid'][0]);
      });

      defer.resolve({
        total: data.response.numFound,
        nodes: nodes,
        rows: rows,
        start: start
      });
    }).error(function() {
      defer.reject();
    });

    return defer.promise;
  }

  function getAllSolrNodes() {
    var defer = $q.defer();
    var result = [];

    getSolrNodes(1, 0).then(function(initial) {
      // console.log(initial.total);

      var pages = Math.ceil(initial.total / 100);

      for (var page = 0; page < pages; page++) {
        getSolrNodes(100, page * 100).then(function(nodes) {
          result = result.concat(nodes.nodes);

          console.log('A: ' + result.length);
          console.log('B: ' + nodes.start);

          if ((nodes.start + 100) > initial.total) {
            console.log('C: ' + result.length);
            console.log('D: ' + nodes.start);

            defer.resolve(result);
          }
        });
      }
    });

    return defer.promise;
  }

  Restangular.all('published').getList().then(function(aNodes) {
    getAllSolrNodes().then(function(sNodes) {
      console.log('E: ' + aNodes.length);
      console.log('F: ' + sNodes.length);
    });
  }, function(error) {});

  $scope.alfrescoPublishedTableParameters = new ngTableParams({
    page: 1,
    count: 5
  }, {
    counts: [],
    total: 1,
    getData: function($defer, params) {}
  });

};

CheckSolrIndexController.$inject = ['$scope', 'Restangular', 'ngTableParams', '$http', '$q'];

angular.module('toolkitApp')

.config([
  '$stateProvider',
  function($stateProvider) {
    $stateProvider
      .state('checkSolrIndex', {
        url: '/checksolrindex',
        templateUrl: 'views/checkSolrIndex.html',
        controller: 'CheckSolrIndexController'
      });
  }
])

.controller('CheckSolrIndexController', CheckSolrIndexController);