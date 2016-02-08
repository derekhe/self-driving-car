var Car = require("./car.js");
var RFSensor = require("./sensors/rf.js");
var SpeedSensor = require("./sensors/speed.js");
var MagSensor = require("./sensors/mag.js");

class CarController {
    constructor() {
        this.car = new Car();
        this.rfSensor = new RFSensor();
        this.rfCount = 0;
        this.rfMaxCount = 10;
        this.forwarding = false;
        this.speedSensor = new SpeedSensor();
        this.magSensor = new MagSensor();
        setTimeout(this._detectRange.bind(this), 1);
    }

    _detectRange() {
        var dist = this.rfSensor.distance;
        if (dist < 50) {
            this.rfCount++;
            if (this.rfCount > this.rfMaxCount && (this.speedSensor.speed != 0) && this.forwarding) {
                console.log("pause", dist);
                this.car.pause();
                this.forwarding = false;
                this.car.backward(1);
                setTimeout(this.car.pause, 500);
            }
        }
        else {
            this.rfCount = 0;
        }

        setTimeout(this._detectRange.bind(this), 1);
    }

    processCommand(message) {
        console.log(message);
        switch (message) {
            case "forward":
                this.forwarding = true;
                this.car.forward();
                break;
            case "backward":
                this.car.backward();
                break;
            case "pause":
                this.car.pause();
                break;
            case "left":
                this.car.left();
                break;
            case "right":
                this.car.right();
                break;
            case "releaseSteer":
                this.car.straight();
                break;
        }
    }

    registerDistanceNotify(func) {
        var self = this;
        setInterval(function () {
            func(self.rfSensor.distance);
        }, 100);
    }

    registerSpeedNotify(func){
        var self = this;
        setInterval(function () {
            func(self.speedSensor.speed);
        }, 100);
    }

    registerMagNotify(func){
        var self = this;
        setInterval(function () {
            func(self.magSensor.axeses);
        }, 100);
    }
}

module.exports = CarController;
