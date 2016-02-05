'use strict';
require('babel-core/register');
var express = require('express');
//var Car = require("./libs/car.js");

var app = express();


app.use(express.static(__dirname  + '/public'));
app.use(express.static(__dirname  + '/node_modules'));

//var car = new Car();

app.listen(3000, function () {
    console.log('Example app listening on port 3000!');
});