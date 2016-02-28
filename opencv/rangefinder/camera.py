import time
from threading import Thread
import cv2
import numpy as np
from picamera import PiCamera
from picamera.array import PiRGBArray

class CameraThread:
    def __init__(self, w, h):
        self.camera = PiCamera()
        self.image = None
        self.camera.resolution = (w, h)
        self.camera.framerate = 60
        self.camera.brightness = 70
        self.camera.contrast = 100
        self.rawCapture = PiRGBArray(self.camera, size=(w, h))

        time.sleep(0.1)

        mtx = np.matrix([[313.1251541, 0., 157.36763381],
                         [0., 311.84837219, 130.36209271],
                         [0., 0., 1.]])
        dist = np.matrix([[-0.42159111, 0.44966352, -0.00877638, 0.00070653, -0.43508731]])
        newcameramtx, roi = cv2.getOptimalNewCameraMatrix(mtx, dist, (w, h), 0, (w, h))

        self.mapx, self.mapy = cv2.initUndistortRectifyMap(mtx, dist, None, newcameramtx, (w, h), 5)
        self.stream = self.camera.capture_continuous(self.rawCapture, format="bgr", use_video_port=True)
        self.stopped = False

    def start(self):
        Thread(target=self.update, args=()).start()
        return self

    def update(self):
        for f in self.stream:
            self.image = cv2.remap(f.array, self.mapx, self.mapy, cv2.INTER_LINEAR)
            self.rawCapture.truncate(0)

            if self.stopped:
                self.stream.close()
                self.rawCapture.close()
                self.camera.close()
                return

    def read(self):
        return self.image

    def stop(self):
        self.stopped = True
