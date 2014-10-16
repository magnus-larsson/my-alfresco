'use strict';

var MainController = function($scope, Restangular, ngTableParams, toaster, $timeout, usSpinnerService, splash, $q, $sce) {

  if (!$scope.timeout) {
    $scope.timeout = 300;
  }

  function syncLoop(iterations, process, exit) {
    var index = 0;
    var done = false;
    var shouldExit = false;

    var loop = {
      next: function() {
        if (done) {
          if (shouldExit && exit) {
            exit(); // Exit if we're done
          }

          return; // Stop the loop if we're done
        }

        // If we're not finished
        if (index < iterations) {
          index++; // Increment our index

          process(loop); // Run our process, pass in the loop
          // Otherwise we're done
        } else {
          done = true; // Make sure we say we're done

          if (exit) {
            exit(); // Call the callback on exit
          }
        }
      },

      iteration: function() {
        return index - 1; // Return the loop number we're on
      },

      break: function(end) {
        done = true; // End the loop

        shouldExit = end; // Passing end as true means we still call the exit callback
      }

    };

    loop.next();

    return loop;
  }

  $scope.tableParams = new ngTableParams({
    page: 1,
    count: 5
  }, {
    counts: [],
    total: 1,
    getData: function($defer, params) {
      Restangular.all('failed').getList().then(function(result) {
        params.total(result.total);
        $defer.resolve(result);
      }, function(error) {
        $defer.reject(error);
      });
    }
  });

  function getExitCodeMessage(exitCode) {
    var message;

    switch (exitCode) {
      case 100:
        message = 'Not serialized (no valid serialization found or keycode expired)';
        break;
      case 101:
        message = 'Command line parameter error';
        break;
      case 102:
        message = 'Command line syntax error (illegal command)';
        break;
      case 103:
        message = 'Unknown error (internal error)';
        break;
      case 104:
        message = 'A file could not be opened';
        break;
      case 105:
        message = 'An encrypted PDF file could not be opened for writing';
        break;
      case 106:
        message = 'A file could not be saved';
        break;
      case 110:
        message = 'Action is not distributable';
        break;
      case 111:
        message = 'No Dispatcher was found';
        break;
      case 112:
        message = 'No Satellite was found or is ready for execution';
        break;
      case 130:
        message = 'Execution is cancelled after timeout';
        break;
      default:
        message = 'Unknown exit code "' + exitCode + '"';
    }

    return message;
  }

  function getErrorCodeMessage(errorCode) {
    var message;

    switch (errorCode) {
      case 1000:
        message = 'Unknown reason';
        break;
      case 1001:
        message = 'A parameter is wrong';
        break;
      case 1002:
        message = 'A requested file could not be found';
        break;
      case 1003:
        message = 'A requested folder could not be found';
        break;
      case 1004:
        message = 'A requested folder is a file';
        break;
      case 1005:
        message = 'A requested file is a folder';
        break;
      case 1006:
        message = '30 days trial period expired';
        break;
      case 1007:
        message = 'Time limited keycode expired';
        break;
      case 1008:
        message = 'Invalid activation';
        break;
      case 1009:
        message = 'PDF does not contain ICC profiles';
        break;
      case 1010:
        message = 'A file could not be opened';
        break;
      case 1011:
        message = 'An encrypted PDF file could not be opened for writing';
        break;
      case 1012:
        message = 'A file could not be saved';
        break;
      default:
        message = 'Unknown error code "' + errorCode + '"';
    }

    return message;
  }

  $scope.rerender = function(node) {
    var deferred = $q.defer();
    $scope.error = null;
    node.stacktrace = null;
    $scope.working = true;
    node.working = true;

    var start = new Date().getTime();

    usSpinnerService.spin('spinner-1');

    node.put({
      timeout: $scope.timeout ? $scope.timeout : 300
    }).then(function(response) {
      usSpinnerService.stop('spinner-1');
      var end = new Date().getTime();
      var time = (end - start) / 1000;
      $scope.working = false;
      node.working = false;

      if (response.success) {
        toaster.pop('success', 'PDF/A render', 'PDF/A rendering successfull (' + time + ' seconds)');
        node.success = true;
      } else {
        toaster.pop('error', 'PDF/A render', 'PDF/A rendering failed (' + time + ' seconds)');
      }

      deferred.resolve();
    }, function(error) {
      usSpinnerService.stop('spinner-1');
      var end = new Date().getTime();

      $scope.working = false;
      node.working = false;
      $scope.time = (end - start) / 1000;
      $scope.error = error.data;
      node.stacktrace = error.data;

      var exitCode = error.data.match(/(exit\scode:\s+)(\d+)/);
      var errorCode = error.data.match(/(Error\s+)(\d+)/);

      if (exitCode && exitCode.length >= 2) {
        node.error = getExitCodeMessage(parseInt(exitCode[2])) + ' (exit code: ' + exitCode[2] + ')';
      }

      if (errorCode && errorCode.length >= 2) {
        if (node.error) {
          node.error += '<br/>';
        }

        node.error += getErrorCodeMessage(parseInt(errorCode[2])) + ' (error code: ' + errorCode[2] + ')';
      }

      node.error = $sce.trustAsHtml(node.error);

      // remove the document from the list if the error message is no permission...
      // node.success = error.data.indexOf('No permission to edit document') >= 0;

      toaster.pop('error', 'PDF/A render', 'PDF/A rendering failed (' + $scope.time + ' seconds)');

      deferred.reject();
    });

    return deferred.promise;
  };

  $scope.rerenderAll = function() {
    var total = $scope.tableParams.data.length;

    syncLoop(total, function(loop) {
      var node = $scope.tableParams.data[loop.iteration()];

      var promise = $scope.rerender(node);

      promise.then(function() {
        loop.next();
      }, function() {
        loop.next();
      });

    }, function() {
      toaster.pop('success', 'PDF/A render', 'Rerender all finished');
    });
  };

  $scope.showError = function(error) {
    splash.open({
      title: 'Error',
      message: error
    });
  };

};

MainController.$inject = ['$scope', 'Restangular', 'ngTableParams', 'toaster', '$timeout', 'usSpinnerService', 'splash', '$q', '$sce'];

angular.module('toolkitApp')

.config([
  '$stateProvider',
  function($stateProvider) {
    $stateProvider
      .state('main', {
        url: '/',
        templateUrl: 'views/main.html',
        controller: 'MainController'
      });
  }
])

.controller('MainController', MainController);