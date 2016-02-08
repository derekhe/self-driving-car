/**
 * Created by hesicong on 16/2/8.
 */
require("babel-register");

var AccSensor = require("../libs/sensors/acc.js");

var acc = new AccSensor();

setInterval(function(){
    console.log(acc.axeses);
}, 0);
