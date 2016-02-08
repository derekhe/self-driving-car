/**
 * Created by hesicong on 16/2/8.
 */
/**
 * Created by hesicong on 16/2/8.
 */

var wpi = require('wiring-pi');

const LSM303_REGISTER_ACCEL_CTRL_REG1_A = 0x20;
const LSM303_REGISTER_ACCEL_CTRL_REG4_A = 0x23;

class AccSensor {
    constructor() {
        wpi.wiringPiSetupGpio();
        this.mag = wpi.wiringPiI2CSetup(0x18);
        wpi.wiringPiI2CWriteReg8(this.mag, LSM303_REGISTER_ACCEL_CTRL_REG1_A, 0x27);    //normal mode
        wpi.wiringPiI2CWriteReg8(this.mag, LSM303_REGISTER_ACCEL_CTRL_REG4_A, 0); //low res mode

    }

    get axeses() {
        var baseAddr = 0x28 | 0x80;
        var bufferX = [wpi.wiringPiI2CReadReg8(this.mag, baseAddr + 0), wpi.wiringPiI2CReadReg8(this.mag, baseAddr + 1)];
        var bufferY = [wpi.wiringPiI2CReadReg8(this.mag, baseAddr + 2), wpi.wiringPiI2CReadReg8(this.mag, baseAddr + 3)];
        var bufferZ = [wpi.wiringPiI2CReadReg8(this.mag, baseAddr + 4), wpi.wiringPiI2CReadReg8(this.mag, baseAddr + 5)];

        var self = this;
        function convert(buffer) {
            return self._trueRound(self._twoscomp(((buffer[1] << 8) | buffer[0]) >> 4, 12) / 1000.0, 5);
        }

        return {
            x: convert(bufferX),
            y: convert(bufferY),
            z: convert(bufferZ)
        }
    }

    _twoscomp(value, no_of_bits) {
        var upper = Math.pow(2, no_of_bits);
        if (value > upper / 2) {
            return value - upper;
        }
        else {
            return value;
        }
    }

    _trueRound(value, digits) {
        return parseFloat((Math.round((value * Math.pow(10, digits)).toFixed(digits - 1)) / Math.pow(10, digits)).toFixed(digits));
    }
}

module.exports = AccSensor;