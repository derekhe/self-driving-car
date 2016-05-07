var app = angular.module("app", []);
app.controller("carController", function ($scope, $window, $interval) {
    var canvas = new fabric.StaticCanvas('c');
    var size = 500;
    canvas.setDimensions({width: size, height: size});

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

    var sock = new SockJS($window.location.hostname + ":3000/echo");
    sock.onopen = function () {
        console.log('open');
    };

    var rangefinder = [];

    sock.onmessage = function (e) {
        var data = JSON.parse(e.data);
        switch (data.type) {
            case "distance":
                $scope.dist = data.value;
                break;
            case "speed":
                $scope.speed = data.value.speed;
                $scope.distance = data.value.distance;
                break;
            case "mag":
                $scope.mag = data.value;
                break;
            case "acc":
                $scope.acc = data.value;
                break;
            case "rangefinder":
                rangefinder = data.value;
        }
        $scope.$digest();
    };

    var maxDist = 100;
    var widthPixels = 320;
    $interval(function () {
        canvas.clear();

        var middle = size / 2;
        var angle = Math.tan(25 / 180 * Math.PI);
        canvas.add(new fabric.Line([middle, size, middle + size * angle, 0], {
                stroke: 'red'
            }
        ));

        canvas.add(new fabric.Line([middle, size, middle - size * angle, 0], {
                stroke: 'red'
            }
        ));

        function getYPixels(dist) {
            return (dist / maxDist * size);
        }

        function getXPixes(column, distYPixels) {
            return middle + (2 * column / widthPixels - 1 ) * distYPixels * angle;
        }

        rangefinder.forEach(function (data) {
            var distYPixels = getYPixels(data[1]);
            var distXPixels = getXPixes(data[0], distYPixels);
            var rangeY = size - distYPixels;

            //canvas.add(new fabric.Line([middle, size, distXPixels, rangeY], {
            //    stroke: '#DDD'
            //}));

            canvas.add(new fabric.Circle({
                radius: 3, fill: 'green', left: distXPixels, top: rangeY
            }))
        })
    }, 50);

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
}).directive('myTouchstart', [function () {
    return function (scope, element, attr) {

        element.on('touchstart', function (event) {
            scope.$apply(function () {
                scope.$eval(attr.myTouchstart);
            });
        });
    };
}]).directive('myTouchend', [function () {
    return function (scope, element, attr) {

        element.on('touchend', function (event) {
            scope.$apply(function () {
                scope.$eval(attr.myTouchend);
            });
        });
    };
}]);