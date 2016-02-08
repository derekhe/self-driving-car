/**
 * Created by hesicong on 16/2/8.
 */

var wpi = require('wiring-pi');

const LSM303_REGISTER_MAG_MR_REG_M = 0x02;

class MagSensor {
    constructor(options = {}) {
        wpi.wiringPiSetupGpio();
        this.mag = wpi.wiringPiI2CSetup(0x1e);
        wpi.wiringPiI2CWriteReg8(this.mag, LSM303_REGISTER_MAG_MR_REG_M, 0x00);

        this.offset = options.offset;
    }

    get axeses() {
        var bufferX = [wpi.wiringPiI2CReadReg8(this.mag, 0x03), wpi.wiringPiI2CReadReg8(this.mag, 0x04)];
        var bufferY = [wpi.wiringPiI2CReadReg8(this.mag, 0x05), wpi.wiringPiI2CReadReg8(this.mag, 0x06)];
        var bufferZ = [wpi.wiringPiI2CReadReg8(this.mag, 0x07), wpi.wiringPiI2CReadReg8(this.mag, 0x08)];

        return {
            x: this._twoscomp((bufferX[0] << 8) | bufferX[1],16) - this.offset.x,
            y: this._twoscomp((bufferY[0] << 8) | bufferY[1],16) - this.offset.y,
            z: this._twoscomp((bufferZ[0] << 8) | bufferZ[1],16) - this.offset.z
        }
    }

    get heading(){
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
        if ( polarCoords.theta < 0 ) {
            polarCoords.theta += 2 * Math.PI;
        }
        polarCoords.theta = 2 * Math.PI - polarCoords.theta;
        polarCoords.theta = (180 / Math.PI * polarCoords.theta);
        return ((polarCoords.theta != 360) ? polarCoords.theta : 0);
    }
}

module.exports = MagSensor;