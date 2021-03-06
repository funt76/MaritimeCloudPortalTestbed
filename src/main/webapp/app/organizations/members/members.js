'use strict';

/* Controllers */

angular.module('mcp.organizations.members', ['ui.bootstrap'])

    .controller('MembershipUserStatusController', ['$scope', 'UserContext',
      function ($scope, UserContext) {
        UserContext.initAndThen(function (user) {
          $scope.membershipStatus = user.membershipStatus($scope.membership.organizationId);
        });
      }])

    .controller('OrganizationMembersSummaryController', ['$scope', '$stateParams', 'UserContext', 'OrganizationService', 'AlmanacOrganizationMemberService',
      function ($scope, $stateParams, UserContext, OrganizationService, AlmanacOrganizationMemberService) {

        var isActive = function (m) {
          return m.active;
        };
        var isApplying = function (m) {
          return !(m.active || m.acceptedByOrganization) && m.acceptedByUser;
        };
        var isInvited = function (m) {
          return !m.active && m.acceptedByOrganization && !m.acceptedByUser;
        };

        UserContext.refresh();

        $scope.organization = OrganizationService.get({organizationId: $stateParams.organizationId}, function (organization) {
          AlmanacOrganizationMemberService.query({organizationId: organization.organizationId}, function (members) {
            $scope.organization.members = members;
            $scope.organization.activeMembers = members.filter(isActive);
            $scope.organization.membersApplying = members.filter(isApplying);
            $scope.organization.membersInvited = members.filter(isInvited);
          });
          UserContext.initAndThen(function (user) {
            $scope.userHasWriteAccess = user.hasWriteAccessTo(organization.organizationId);
            $scope.membershipStatus = user.membershipStatus(organization.organizationId);
          });
        });

      }])

    .controller('OrganizationMembersController', ['$scope', '$stateParams', 'UserContext', 'OrganizationService', 'AlmanacOrganizationMemberService',
      function ($scope, $stateParams, UserContext, OrganizationService, AlmanacOrganizationMemberService) {

        $scope.organization = OrganizationService.get({organizationId: $stateParams.organizationId}, function (organization) {
          $scope.organization.members = AlmanacOrganizationMemberService.query({organizationId: organization.organizationId});
          UserContext.initAndThen(function (user) {
            $scope.userHasWriteAccess = user.hasWriteAccessTo(organization.organizationId);
          });

        });

        $scope.canBeRemoved = function (membership) {
          return isNotCurrentUser(membership);
        };

        $scope.canBeAccepted = function (membership) {
          return isNotCurrentUser(membership) && isApplying(membership);
        };

        $scope.isInvited = function (membership) {
          return isInvited(membership);
        };

        var isNotCurrentUser = function (membership) {
          return !$scope.isCurrentUser(membership);
        };

        $scope.isCurrentUser = function (membership) {
          return UserContext.currentUser() && membership.username === UserContext.currentUser().name;
        };

        var isApplying = function (m) {
          return !(m.active || m.acceptedByOrganization) && m.acceptedByUser;
        };

        var isInvited = function (m) {
          return !m.active && m.acceptedByOrganization && !m.acceptedByUser;
        };

        $scope.viewState = 'list';

        $scope.confirmRemove = function (username) {
          $scope.viewState = 'confirm-remove';
          $scope.selectedMember = username;
        };

        $scope.cancel = function () {
          $scope.viewState = 'list';
          $scope.selectedMember = null;
        };

        $scope.remove = function (member) {
          $scope.busyPromise = OrganizationService.RemoveUserFromOrganization($scope.organization, member.membershipId, function () {
            $scope.viewState = 'remove-success';
          }, function (error) { /*reportError*/
            $scope.viewState = 'remove-failed';
            console.log("Error, dammit!", error);
            //$scope.viewState = 'error';
          });
        };

      }])

    .controller('MembershipAcceptInviteController', ['$scope', '$stateParams', 'UserContext', 'OrganizationService',
      function ($scope, $stateParams, UserContext, OrganizationService) {

        $scope.viewState = 'accept-invite-try';
        $scope.busyPromise = OrganizationService.get({organizationId: $stateParams.organizationId}, function (organization) {
          $scope.organization = organization;
          var membership = UserContext.membershipOf(organization.organizationId);
          $scope.busyPromise = OrganizationService.acceptMembershipToOrganization(
              membership.organizationId,
              membership.membershipId,
              function () {
                $scope.viewState = 'accept-success';
              },
              function (error) { /*reportError*/
                console.log("Error, dammit!", error);
                $scope.viewState = 'error';
              }
          );
        });
      }])

    .controller('MembershipAcceptApplicationController', ['$scope', '$stateParams', 'UserContext', 'OrganizationService',
      function ($scope, $stateParams, UserContext, OrganizationService) {

        $scope.viewState = 'accept-application-try';
        $scope.busyPromise = OrganizationService.get({organizationId: $stateParams.organizationId}, function (organization) {
          $scope.organization = organization;
          $scope.busyPromise = OrganizationService.acceptUsersMembershipApplication(
              organization.organizationId,
              $stateParams.membershipId,
              function () {
                $scope.viewState = 'accept-application-success';
              },
              function (error) { /*reportError*/
                console.log("Error, dammit!", error);
                $scope.viewState = 'error';
              }
          );
        });
      }])

    .controller('UserLeaveOrganizationController', ['$scope', '$stateParams', 'UserContext', 'OrganizationService',
      function ($scope, $stateParams, UserContext, OrganizationService) {

        $scope.busyPromise = OrganizationService.get({organizationId: $stateParams.organizationId}, function (organization) {
          $scope.viewState = 'confirm-leave';
          $scope.organization = organization;
          $scope.membership = UserContext.membershipOf(organization.organizationId);

          $scope.leave = function (membership) {
            $scope.busyPromise = OrganizationService.dropMembershipToOrganization(
                membership.organizationId,
                membership.membershipId,
                function () {
                  $scope.viewState = 'leave-success';
                },
                function (error) {
                  $scope.viewState = 'error';
                }
            );
          };

        });
      }])

    .controller('OrganizationInviteMemberController', ['$scope', '$stateParams', 'UserService', 'UserContext', 'OrganizationService', 'UUID', 'AlmanacOrganizationMemberService',
      function ($scope, $stateParams, UserService, UserContext, OrganizationService, UUID, AlmanacOrganizationMemberService) {

        $scope.filter_query = '';
        $scope.viewState = 'invite';
        $scope.orderProp = 'username';

        $scope.organization = OrganizationService.get({organizationId: $stateParams.organizationId}, function (organization) {
          $scope.organization.members = AlmanacOrganizationMemberService.query({organizationId: organization.organizationId});
        });

        $scope.isNewMember = function (user) {
          for (var i = 0; i < $scope.organization.members.length; i++) {
            if (user.username === $scope.organization.members[i].username) {
              return false;
            }
          }
          return true;
        };

        $scope.updateSearch = function (pattern) {
          if (pattern.trim().length > 0) {
            $scope.busyPromiseSearch = UserService.query({usernamePattern: pattern, size: 10}, function (page) {
              $scope.page = page;
              $scope.people = page.content;
            }).$promise;
          } else {
            $scope.page = null;
            $scope.people = [];
          }
        };

        $scope.confirm = function (member) {
          $scope.invitedMember = member;
          $scope.viewState = 'invite-confirm';
        };

        $scope.invite = function (member) {

          // fetch and assign a new UUID from the server
          UUID.get({name: "identifier"}, function (newMembershipId) {

            // call server with an invite-request !
            $scope.busyPromise = OrganizationService.InviteUserToOrganization($scope.organization, newMembershipId.identifier, member, function () {
              $scope.viewState = 'invite-success';
            }, function (error) { /*reportError*/
              console.log("Error, dammit!", error);
              $scope.viewState = 'error';
            });
          });

        };
      }])

    .controller('UserJoinOrganizationController', ['$scope', '$stateParams', 'OrganizationService', 'UUID',
      function ($scope, $stateParams, OrganizationService, UUID) {
        $scope.viewState = 'apply-form';
        $scope.organization = OrganizationService.get({organizationId: $stateParams.organizationId});
        $scope.join = function (addtionalMessage) {

          // fetch and assign a new UUID from the server
          UUID.get({name: "identifier"}, function (newMembershipId) {

            // call server with a join-request !
            $scope.busyPromise = OrganizationService.applyForMembershipToOrganization(
                $scope.organization,
                newMembershipId.identifier,
                $scope.currentUser.name,
                !addtionalMessage ? "" : addtionalMessage,
                function () {
                  $scope.viewState = 'apply-success';
                },
                function (error) { /*reportError*/
                  console.log("Error, dammit!", error);
                  $scope.viewState = 'error';
                }
            );
          });
        };
      }])

    ;

