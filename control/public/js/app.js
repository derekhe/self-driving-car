var app = angular.module("app", []);
app.controller("carController", function ($scope, $window) {
    $scope.forward = function () {
        sock.send("forward");
    };

    $scope.backward = function () {
        sock.send("backward");
    };

    $scope.pause = function () {
        sock.send("pause");
    };

    $scope.left = function () {
        sock.send("left");
    };

    $scope.releaseSteer = function () {
        sock.send("releaseSteer");
    };

    $scope.right = function () {
        sock.send("right");
    };

    var sock = new SockJS($window.location.hostname + ":9999/echo");
    sock.onopen = function () {
        console.log('open');
    };

    sock.onmessage = function (e) {
        var data = JSON.parse(e.data);
        switch (data.type) {
            case "distance":
                $scope.dist = data.value;
                break;
            case "speed":
                $scope.speed = data.value;
                break;
            case "mag":
                $scope.mag = data.value;
                break;
            case "acc":
                $scope.acc = data.value;
                break;
        }
        $scope.$digest();
    };
    sock.onclose = function () {
        console.log('close');
    };

    var lastKeycode;
    $window.onkeydown = function (e) {
        if (e.keyCode == lastKeycode) {
            return;
        }

        lastKeycode = e.keyCode;
        switch (e.keyCode) {
            case 38:
                $scope.forward();
                break;
            case 40:
                $scope.backward();
                break;
            case 37:
                $scope.left();
                break;
            case 39:
                $scope.right();
                break;
        }
    };

    $window.onkeyup = function (e) {
        switch (e.keyCode) {
            case 38:
            case 40:
                $scope.pause();
                break;
            case 37:
            case 39:
                $scope.releaseSteer();
                break;
        }

        lastKeycode = null;
    };
});