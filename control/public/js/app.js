var app = angular.module("app", []);
app.controller("carController", function ($scope, $window) {
    $scope.forward =function(){
        sock.send("forward");
    };

    $scope.backward =function(){
        sock.send("backward");
    };

    $scope.pause = function(){
        sock.send("pause");
    };

    $scope.left =function(){
        sock.send("left");
    };

    $scope.releaseSteer = function(){
        sock.send("releaseSteer");
    };

    $scope.right =function(){
        sock.send("right");
    };

    var sock = new SockJS($window.location.hostname +":9999/echo");
    sock.onopen = function() {
        console.log('open');
    };
    sock.onmessage = function(e) {
        $scope.dist = e.data;
        $scope.$digest();
    };
    sock.onclose = function() {
        console.log('close');
    };

    var lastKeycode;
    $window.onkeydown = function(e){
        if(e.keyCode == lastKeycode){
            return;
        }

        lastKeycode = e.keyCode;
        switch(e.keyCode){
            case 38:
                $scope.forward();
                break;
            case 40:
                $scope.backward();
                break;
        }
    };

    $window.onkeyup = function(e){
        switch(e.keyCode){
            case 38:
            case 40:
                $scope.pause();
                break;
        }

        lastKeycode = null;
    };
});