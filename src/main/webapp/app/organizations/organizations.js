'use strict';

/* Controllers */

angular.module('mcp.organizations', ['ui.bootstrap'])

    .controller('OrganizationListController', ['$scope', 'OrganizationService',
      function($scope, OrganizationService) {
        $scope.organizations = OrganizationService.query();
        $scope.orderProp = 'age';
      }])

    .controller('OrganizationDetailsController', ['$scope', '$stateParams', 'OrganizationService',
      function($scope, $stateParams, OrganizationService) {
        $scope.organization = OrganizationService.get({organizationname: $stateParams.organizationname}, function(organization) {
        });
      }])

    .controller('OrganizationCreateController', ['$scope', '$location', 'OrganizationService',
      function($scope, $location, OrganizationService) {
        $scope.organization = {name: null, title: null};
        $scope.message = null;
        $scope.alertMessages = null;
        //$("#rPreferredLogin").focus();
        $scope.isTrue = true;

        /**
         * @returns true when there is enough data in the form 
         * to try to submit it. This is not to say that data is
         * valid. pressing submit will cause a series of 
         * validator to be evaluated
         */
        $scope.formIsSubmitable = function() {
          return ($scope.organization.name && $scope.organization.title);
        };

        $scope.submit = function() {
          $scope.message = null;
          $scope.alertMessages = null;

          // validate input values
          if ($scope.organization.name) {
            if ($scope.organization.name === "test") {
              $scope.alertMessages = ["Test? You have to be more visionary than that!"];
              return;
            }
          }

          // Send request
          $scope.message = "Sending request to create organization...";

          OrganizationService.create($scope.organization, function(data) {
            $location.path('/orgs/' + data.name).replace();
            $scope.message = ["Organization created: " + data];

          }, function(error) {
            // Error handler code
            $scope.message = null;
            $scope.alertMessages = ["Error on the serverside :( ", error];
          });
        };
      }]);

