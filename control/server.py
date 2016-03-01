import tornado.ioloop
import tornado.web
from threading import Thread
from sockjs.tornado import SockJSRouter, SockJSConnection
from car import Car
import json

from sensors.RangeFinder import rangefinder

car = Car()

finder = rangefinder.RangeFinder()
finder.start()

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
            self.send(json.dumps(finder.values))
            import time
            time.sleep(0.1)

if __name__ == "__main__":
    EchoRouter = SockJSRouter(EchoConnection, '/echo')
    app = tornado.web.Application(
        [(r"/app/(.*)", tornado.web.StaticFileHandler, {"path": "./public/"})] + EchoRouter.urls)
    app.listen(3000)
    print("Server stared")
    tornado.ioloop.IOLoop.current().start()
