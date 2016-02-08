/**
 * Created by hesicong on 16/2/8.
 */
require("babel-register");

var MagSensor = require("../libs/sensors/mag.js");

var mag = new MagSensor();

setInterval(function(){
    console.log(mag.axeses);
}, 100);
