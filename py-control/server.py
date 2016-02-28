import tornado.ioloop
import tornado.web
from sockjs.tornado import SockJSRouter, SockJSConnection

from car import Car

car = Car()


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
        print(info)

if __name__ == "__main__":
    EchoRouter = SockJSRouter(EchoConnection, '/echo')
    app = tornado.web.Application(
        [(r"/app/(.*)", tornado.web.StaticFileHandler, {"path": "./public/"})] + EchoRouter.urls)
    app.listen(3000)
    print("Server stared")
    tornado.ioloop.IOLoop.current().start()
