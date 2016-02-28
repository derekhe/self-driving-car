'use strict';

var wpi = require('wiring-pi');

class SpeedSensor {
    constructor() {
        this._speed = 0;
        wpi.wiringPiSetupGpio();

        var lastTimeRead = 0;
        var count = 0;
        var timeBegin = new Date();
        this._distance = 0;
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
            if (timeElapsed > 0.3) {
                var distance = count * 2 * Math.PI * 5.4 / 100 / 4;
                self._distance += distance;
                self._speed = distance / timeElapsed;
                timeBegin = now;
                count = 0;
            }

            setTimeout(detectSpeed, 0);
        };

        detectSpeed();
    }

    get speed() {
        return this._speed;
    }

    get distance(){
        return this._distance;
    }
}

module.exports = SpeedSensor;