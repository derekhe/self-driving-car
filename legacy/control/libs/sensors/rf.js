'use strict';

var wpi = require('wiring-pi');

class RFSensor {
    constructor(){
        wpi.wiringPiSetupGpio();
        wpi.pcf8591Setup(200, 0x48);
    }

    get distance(){
        var volts = wpi.analogRead(200) * 3.3 / 256.0;
        return 60.495 * Math.pow(volts,-1.1904);
    }
}

module.exports = RFSensor;