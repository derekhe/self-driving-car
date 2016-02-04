var wpi = require('wiring-pi');
wpi.wiringPiSetupGpio();
wpi.pcf8591Setup(200, 0x48);
while(true){
    var value = wpi.analogRead(200);
    volts = value * 5.0 / 256.0; // ("proxSens" is from analog read)
    cm = 60.495 * Math.pow(volts,-1.1904);
    console.log(cm);
}