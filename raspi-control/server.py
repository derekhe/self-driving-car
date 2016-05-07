import json
from threading import Thread

import tornado.ioloop
import tornado.web
from sockjs.tornado import SockJSRouter, SockJSConnection

from controller.car import Car
from sensors import rangefinder, LSM303, speedSensor

car = Car()

finder = rangefinder.RangeFinder()
finder.start()

lsm303 = LSM303.LSM303DLHC()
speed = speedSensor.SpeedSensor()

class EchoConnection(SockJSConnection):
    actions = {
        "forward": car.forward,
        "backward": car.backward,
        "pause": car.pause,
        "left": car.left,
        "right": car.right,
        "releaseSteer": car.straight}

    def on_message(self, msg):
        if not self.actions[msg] == None:
            self.actions[msg]()

    def on_open(self, info):
        Thread(target=self.update, args=()).start()

    def update(self):
        while True:
            self.send(json.dumps({"type": "rangefinder", "value": finder.values}))
            self.send(json.dumps({"type": "mag", "value": lsm303.read()[0:3]}))
            self.send(json.dumps({"type": "acc", "value": lsm303.read()[3:]}))
            self.send(json.dumps({"type": "speed", "value": { "speed": speed.speed(), "distance": speed.distance()}}))
            import time
            time.sleep(0.1)


if __name__ == "__main__":
    EchoRouter = SockJSRouter(EchoConnection, '/echo')
    app = tornado.web.Application(
        [(r"/app/(.*)", tornado.web.StaticFileHandler, {"path": "./public/"})] + EchoRouter.urls)
    app.listen(3000)
    print("Server stared")
    tornado.ioloop.IOLoop.current().start()
