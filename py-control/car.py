import wiringpi2

OUTPUT = 1
INPUT = 0
HIGH = 1
LOW = 0

class Car:
    def __init__(self):        
        self.drivePWMPin = 18
        self.drivePin1 = 23
        self.drivePin2 = 24
        
        self.steerPWMPin = 17
        self.steerPin1 = 27
        self.steerPin2 = 22

        wiringpi2.wiringPiSetupGpio()

        self.maxPwm = 50
        wiringpi2.pinMode(self.drivePin1, OUTPUT)
        wiringpi2.pinMode(self.drivePin2, OUTPUT)
        wiringpi2.pinMode(self.drivePWMPin, OUTPUT)
        wiringpi2.softPwmCreate(self.drivePWMPin, 0, self.maxPwm)

        wiringpi2.pinMode(self.steerPin1, OUTPUT)
        wiringpi2.pinMode(self.steerPin2, OUTPUT)
        wiringpi2.pinMode(self.steerPWMPin, OUTPUT)

    def forward(self, speed = 0.5):
        wiringpi2.digitalWrite(self.drivePin1, 1)
        wiringpi2.digitalWrite(self.drivePin2, 0)
        wiringpi2.softPwmWrite(self.drivePWMPin, speed * self.maxPwm)

    def backward(self, speed = 0.5):
        wiringpi2.digitalWrite(self.drivePin1, 0)
        wiringpi2.digitalWrite(self.drivePin2, 1)
        wiringpi2.softPwmWrite(self.drivePWMPin, speed * self.maxPwm)

    def pause(self):
        wiringpi2.digitalWrite(self.drivePin1, 0)
        wiringpi2.digitalWrite(self.drivePin2, 0)
        wiringpi2.softPwmWrite(self.drivePWMPin, 0)

    def left(self):
        wiringpi2.digitalWrite(self.steerPin1, 1)
        wiringpi2.digitalWrite(self.steerPin2, 0)
        wiringpi2.digitalWrite(self.steerPWMPin, 1)


    def right(self):
        wiringpi2.digitalWrite(self.steerPin1, 0)
        wiringpi2.digitalWrite(self.steerPin2, 1)
        wiringpi2.digitalWrite(self.steerPWMPin, 1)


    def straight(self):
        wiringpi2.digitalWrite(self.steerPin1, 0)
        wiringpi2.digitalWrite(self.steerPin2, 0)
        wiringpi2.digitalWrite(self.steerPWMPin, 0)