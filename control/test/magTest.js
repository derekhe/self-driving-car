/**
 * Created by hesicong on 16/2/8.
 */
require("babel-register");

var MagSensor = require("../libs/sensors/mag.js");

var mag = new MagSensor({
    offset: {
        x: 12.5,
        y: -642,
        z: 0
    }
});

var min = {
    x: 9999,
    y: 9999,
    z: 9999
};

var max = {
    x: -9999,
    y: -9999,
    z: -9999
};

function assignMinMax(value) {
    if (mag.axeses[value] < min[value]) {
        min[value] = mag.axeses[value];
    }

    if (mag.axeses[value] > max[value]) {
        max[value] = mag.axeses[value];
    }
}

function offset(value) {
    return (max[value] + min[value]) / 2.0
}

setInterval(function () {
    console.log(mag.axeses, mag.heading);
    assignMinMax('x');
    assignMinMax('y');
    assignMinMax('z');

    console.log("Min", min);
    console.log("Max", max);
    console.log("Offset",
        {
            x: offset('x'),
            y: offset('y'),
            z: offset('z')
        });
}, 10);
