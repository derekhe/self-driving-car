import math

import wiringpi2


class IRSensor:
    def __init__(self):
        wiringpi2.pcf8591Setup(200, 0x48)

    def distance(self):
        volts = wiringpi2.analogRead(200) * 3.3 / 256.0
        return 60.495 * math.pow(volts, -1.1904)


if __name__ == '__main__':
    irSensor = IRSensor()

    while True:
        print(irSensor.distance())
