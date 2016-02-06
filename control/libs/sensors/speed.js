'use strict';

var wpi = require('wiring-pi');

class SpeedSensor {
    constructor() {
        this._speed = 0;
        wpi.wiringPiSetupGpio();

        var lastTimeRead = 0;
        var count = 0;
        var timeBegin = new Date();
        var self = this;
        var detectSpeed = function () {
            var read = wpi.digitalRead(21);

            if (lastTimeRead != read) {
                if(lastTimeRead == 1 && read == 0){
                    count++;
                }
                lastTimeRead = read;
            }

            var now = new Date();
            var timeElapsed = (now - timeBegin) / 1000;
            if (timeElapsed > 0.2) {
                self._speed = count * 5.5 /100.0 * Math.PI / timeElapsed;
                timeBegin = now;
                console.log(self._speed);
                count = 0;
            }

            setTimeout(detectSpeed, 0);
        };

        detectSpeed();
    }

    get speed() {
        return this._speed;
    }
}

module.exports = SpeedSensor;