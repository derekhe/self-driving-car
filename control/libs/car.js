'use strict';

var wpi = require('wiring-pi');
var drivePWMPin = 18;
var drivePin1 = 23;
var drivePin2 = 24;

var steerPWMPin = 17;
var steerPin1 = 27;
var steerPin2 = 22;

class Car {
    constructor() {
        wpi.wiringPiSetupGpio();

        this.maxPwm = 50;
        wpi.pinMode(drivePin1, wpi.OUTPUT);
        wpi.pinMode(drivePin2, wpi.OUTPUT);
        wpi.pinMode(drivePWMPin, wpi.OUTPUT);
        wpi.softPwmCreate(drivePWMPin, 0, this.maxPwm);

        wpi.pinMode(steerPin1, wpi.OUTPUT);
        wpi.pinMode(steerPin2, wpi.OUTPUT);
        wpi.pinMode(steerPWMPin, wpi.OUTPUT);
    }

    forward(speed = 0.5) {
        wpi.digitalWrite(drivePin1, 1);
        wpi.digitalWrite(drivePin2, 0);
        wpi.softPwmWrite(drivePWMPin, speed * this.maxPwm);
    }

    backward(speed = 0.5) {
        wpi.digitalWrite(drivePin1, 0);
        wpi.digitalWrite(drivePin2, 1);
        wpi.softPwmWrite(drivePWMPin, speed * this.maxPwm);
    }

    pause() {
        wpi.digitalWrite(drivePin1, 0);
        wpi.digitalWrite(drivePin2, 0);
        wpi.softPwmWrite(drivePWMPin, 0);
    }

    left() {
        wpi.digitalWrite(steerPin1, 1);
        wpi.digitalWrite(steerPin2, 0);
        wpi.digitalWrite(steerPWMPin, 1);
    }

    right() {
        wpi.digitalWrite(steerPin1, 0);
        wpi.digitalWrite(steerPin2, 1);
        wpi.digitalWrite(steerPWMPin, 1);
    }

    straight() {
        wpi.digitalWrite(steerPin1, 0);
        wpi.digitalWrite(steerPin2, 0);
        wpi.digitalWrite(steerPWMPin, 0);
    }
}

function cleanUp() {
    wpi.digitalWrite(drivePin1, 0);
    wpi.digitalWrite(drivePin2, 0);
    wpi.softPwmWrite(drivePWMPin, 0);

    wpi.digitalWrite(steerPin1, 0);
    wpi.digitalWrite(steerPin2, 0);
    wpi.digitalWrite(steerPWMPin, 0);

    console.log("Exiting");
    process.exit();
};

//do something when app is closing
process.on('exit', cleanUp);

//catches ctrl+c event
process.on('SIGINT', cleanUp);

//catches uncaught exceptions
process.on('uncaughtException', (ex)=> {
    console.log(`uncaughtException ${ex}`);
    console.log(ex, ex.stack.split("\n"))
    cleanUp();
});

module.exports = Car;