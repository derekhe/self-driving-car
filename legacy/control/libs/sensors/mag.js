/**
 * Created by hesicong on 16/2/8.
 */

var wpi = require('wiring-pi');

const LSM303_REGISTER_MAG_CRA_REG_M = 0x00;
const LSM303_REGISTER_MAG_MR_REG_M = 0x02;

class MagSensor {
    constructor(options = {offset:{x:0,y:0,z:0}}) {
        wpi.wiringPiSetupGpio();
        this.mag = wpi.wiringPiI2CSetup(0x1e);

        this.offset = options.offset;
    }

    get axeses() {
        wpi.wiringPiI2CWriteReg8(this.mag, LSM303_REGISTER_MAG_CRA_REG_M, 0x10);
        wpi.wiringPiI2CWriteReg8(this.mag, LSM303_REGISTER_MAG_MR_REG_M, 0x03);
        wpi.wiringPiI2CWriteReg8(this.mag, LSM303_REGISTER_MAG_MR_REG_M, 0x00);

        var buffer = this._readBytes(0x03, 6);

        return {
            x: this._twoscomp((buffer[0] << 8) | buffer[1], 16) - this.offset.x,
            y: this._twoscomp((buffer[2] << 8) | buffer[3], 16) - this.offset.y,
            z: this._twoscomp((buffer[4] << 8) | buffer[5], 16) - this.offset.z
        }
    }

    _readBytes(baseAddr, bytesToRead) {
        var bytes = [];
        for (var i = 0; i < bytesToRead; i++) {
            bytes.push(wpi.wiringPiI2CReadReg8(this.mag, 0x03 + i));
        }

        return bytes;
    }

    get heading() {
        var axeses = this.axeses;
        return this._toPolar(axeses.x, axeses.y);
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

    _toPolar(x, y) {
        // returns polar coordinates as an object (radians)
        var polarCoords = {};
        polarCoords.r = Math.sqrt(x * x + y * y);
        polarCoords.theta = Math.PI / 2 - Math.atan2(y, x);
        if (polarCoords.theta < 0) {
            polarCoords.theta += 2 * Math.PI;
        }
        polarCoords.theta = 2 * Math.PI - polarCoords.theta;
        polarCoords.theta = (180 / Math.PI * polarCoords.theta);
        return ((polarCoords.theta != 360) ? polarCoords.theta : 0);
    }
}

module.exports = MagSensor;