var wpi = require('wiring-pi');
wpi.wiringPiSetupGpio();

//drive
const drivePWMPin = 14;
const drivePin1 = 15;
const drivePin2 = 18;
wpi.pinMode(drivePin1, wpi.OUTPUT);
wpi.pinMode(drivePin2, wpi.OUTPUT);
wpi.pinMode(drivePWMPin, wpi.OUTPUT);
wpi.softPwmCreate(drivePWMPin, 0, 50);

function forward(speed) {
    wpi.digitalWrite(drivePin1, 1);
    wpi.digitalWrite(drivePin2, 0);
    wpi.softPwmWrite(drivePWMPin, speed);
}

function backward(speed) {
    wpi.digitalWrite(drivePin1, 0);
    wpi.digitalWrite(drivePin2, 1);
    wpi.softPwmWrite(drivePWMPin, speed);
}

function pause() {
    wpi.digitalWrite(drivePin1, 0);
    wpi.digitalWrite(drivePin2, 0);
    wpi.softPwmWrite(drivePWMPin, 0);
}

//steer
const steerPWMPin = 17;
const steerPin1 = 27;
const steerPin2 = 22;
wpi.pinMode(steerPin1, wpi.OUTPUT);
wpi.pinMode(steerPin2, wpi.OUTPUT);
wpi.pinMode(steerPWMPin, wpi.OUTPUT);

function left() {
    wpi.digitalWrite(steerPin1, 1);
    wpi.digitalWrite(steerPin2, 0);
    wpi.digitalWrite(steerPWMPin, 1);
}

function right() {
    wpi.digitalWrite(steerPin1, 0);
    wpi.digitalWrite(steerPin2, 1);
    wpi.digitalWrite(steerPWMPin, 1);
}

function straight() {
    wpi.digitalWrite(steerPin1, 0);
    wpi.digitalWrite(steerPin2, 0);
    wpi.digitalWrite(steerPWMPin, 0);
}

var speed = 35;
forward(speed);
left();
wpi.delay(1000);
straight();
pause();
wpi.delay(1000);
backward(speed);
right();
wpi.delay(1000);
pause();

process.on('exit', function () {
    wpi.softPwmWrite(drivePWMPin, 0);
    wpi.digitalWrite(steerPWMPin, 0);
});