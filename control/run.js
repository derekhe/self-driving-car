'use strict';

require('babel-core/register');
var Car = require("./libs/car.js");

var car = new Car();
car.forward(0.3);

console.log('Press any key to exit');

process.stdin.setRawMode(true);
process.stdin.resume();
process.stdin.on('data', process.exit.bind(process, 0));