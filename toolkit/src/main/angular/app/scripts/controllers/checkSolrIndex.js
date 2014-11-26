'use strict';

var CheckSolrIndexController = function($scope, Restangular, ngTableParams,
  $http, $q) {

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

  function mockAllSolrNodes() {
    var defer = $q.defer();

    var url = 'http://localhost:9000/data/solr_lagret.json';

    $http({
      method: 'GET',
      url: url
    }).success(function(data) {
      var nodes = [];
      angular.forEach(data.response.docs, function(value) {
        nodes.push(value['dc.identifier.documentid'][0]);
      });

      defer.resolve(nodes);
    }).error(function() {
      defer.reject();
    });

    return defer.promise;
  }

  function getAllSolrNodes() {
    var batch = 1000;
    var defer = $q.defer();
    var result = [];

    getSolrNodes(1, 0).then(function(initial) {
      var pages = Math.ceil(initial.total / batch);

      for (var page = 0; page < pages; page++) {
        getSolrNodes(batch, page * batch).then(function(nodes) {
          result = result.concat(nodes.nodes);

          if (nodes.total === result.length) {
            defer.resolve(result);
          }
        });
      }
    });

    return defer.promise;
  }

  function getAlfrescoNodes(rows, start) {
    var defer = $q.defer();

    Restangular.all('published').getList({
      rows: rows,
      start: start
    }).then(function(nodes) {
      defer.resolve(nodes);
    }, function(error) {
      defer.reject(error);
    });

    return defer.promise;
  }

  function mockAllAlfrescoNodes() {
    var defer = $q.defer();

    var url = 'http://localhost:9000/data/alfresco_lagret.json';

    $http({
      method: 'GET',
      url: url
    }).success(function(data) {
      defer.resolve(data.data);
    }).error(function() {
      defer.reject();
    });

    return defer.promise;
  }

  function getAllAlfrescoNodes() {
    var batch = 1000;
    var defer = $q.defer();
    var result = [];
    var more = true;
    var start = 0;

    async.whilst(function() {
      return more;
    }, function(callback) {
      getAlfrescoNodes(batch, start).then(function(nodes) {
        start += nodes.total;

        result = result.concat(nodes);

        console.log(nodes.length);

        if (nodes.total === 0) {
          more = false;
        }

        callback();
      });
    }, function(callback) {
      defer.resolve(result);
    });

    return defer.promise;
  }

  mockAllAlfrescoNodes().then(function(nodes) {
    // getAllSolrNodes().then(function(nodes) {
    console.log(nodes.length);
  });

  mockAllSolrNodes().then(function(nodes) {
    // getAllSolrNodes().then(function(nodes) {
    console.log(nodes.length);
  });

  function foo() {
    getAllAlfrescoNodes().then(function(aNodes) {
      getAllSolrNodes().then(function(sNodes) {
        var onlyAlfresco = [];

        for (var x = 0; x < aNodes.length; x++) {
          if (!aNodes[x].published) {
            continue;
          }

          var alfrescoNode = aNodes[x].nodeRef;

          var found = false;

          for (var y = 0; y < sNodes.length; y++) {
            var solrNode = sNodes[y];

            if (solrNode === alfrescoNode) {
              found = true;
              sNodes.splice(y, 1);
              break;
            }
          }

          if (!found) {
            onlyAlfresco.push(alfrescoNode);
          }
        }

        console.log('Only Alfresco');
        console.log(onlyAlfresco.length);

        console.log('');

        console.log('Only Solr');
        console.log(sNodes.length);
      });
    });

  }

  foo();

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
