import wiringpi2
from threading import Thread
import time
import math

class SpeedSensorThread(Thread):
    speed = 0
    distance = 0
    
    def __init__(self):
        super().__init__()

        self.speed = 0
        self.distance = 0
        pass

    def run(self):
        lastTimeRead = 0
        count = 0
        timeBegin = time.time()

        while True:
            read = wiringpi2.digitalRead(21)

            if lastTimeRead != read:
                if lastTimeRead == 1 and read == 0:
                    count = count + 1

                lastTimeRead = read

            now = time.time()
            timeElapsed = now - timeBegin
            if timeElapsed > 0.3:
                distance = count * 2 * math.pi * 5.4 / 100 / 4
                self.distance += distance
                self.speed = distance / timeElapsed

                timeBegin = time.time()
                count = 0

    def getSpeed(self):
        return self.speed

    def getDistance(self):
        return self.distance


class SpeedSensor:
    def __init__(self):
        thread = SpeedSensorThread()
        thread.start()
        self.thread = thread

    def speed(self):
        return self.thread.getSpeed()

    def distance(self):
        return self.thread.getDistance()

if __name__ == '__main__':

    from time import sleep

    wiringpi2.wiringPiSetupGpio()
    speedSensor = SpeedSensor()

    while True:
        print(speedSensor.speed(), speedSensor.distance())
        sleep(0.3)