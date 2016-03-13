import time
from multiprocessing import Process, Queue
from threading import Thread

import cv2
import numpy as np
from picamera import PiCamera
from picamera.array import PiRGBArray

w = 320
h = 240

q = Queue(3)

class CameraThread:
    def start(self):
        self.p = Process(target=self.update, args=(q,))
        self.p.start()
        return self

    def update(self, queue):
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

        for f in self.stream:
            self.image = cv2.remap(f.array, self.mapx, self.mapy, cv2.INTER_LINEAR)
            self.rawCapture.truncate(0)

            if not q.full():
                queue.put(self.image, False)

            if self.stopped:
                self.stream.close()
                self.rawCapture.close()
                self.camera.close()
                return

    def stop(self):
        self.stopped = True


class RangeFinder:
    cameraThread = CameraThread()
    cameraThread.start()

    def start(self):
        Thread(target=self.update, args=()).start()

    def update(self, DEBUG=False, ShowPerformance=False):
        def distance(pixel):
            return 84134.5 - 0.0390032 * pixel - 53553.8 * np.arctan(pixel ** 2)

        def drawReferenceLines():
            # 90cm
            cv2.line(outputImg, (0, 26), (w, 26), (255))
            # 60cm
            cv2.line(outputImg, (0, 33), (w, 33), (180))
            # 30cm
            cv2.line(outputImg, (0, 55), (w, 55), (128))

        def drawLine(row, column):
            cv2.line(outputImg, (column, row), (column, h), (255))

        last = cv2.getTickCount()

        while True:
            image = q.get()

            if image is None:
                time.sleep(0.1)
                continue

            grayImg = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
            outputImg = cv2.threshold(grayImg, 160, 255, cv2.THRESH_BINARY)[1]

            values = []
            for columnIndex in range(0, w, 2):
                column = outputImg[:, columnIndex]
                row = np.nonzero(column)[0]

                if not row.size == 0:
                    values.append((columnIndex, distance(row[0])))

                    if DEBUG:
                        drawLine(row[0], columnIndex)

                else:
                    values.append((columnIndex, 300))

            self.values = values
            if DEBUG:
                drawReferenceLines()

                cv2.imshow("image", grayImg)
                cv2.imshow('outputImg', outputImg)
                cv2.waitKey(1)

            if ShowPerformance:
                timeSpend = cv2.getTickCount() - last
                last = cv2.getTickCount()
                timeSec = timeSpend / cv2.getTickFrequency()
                print(timeSec, 1 / timeSec)

    def getValues(self):
        return self.values


if __name__ == "__main__":
    rangeFinder = RangeFinder()
    rangeFinder.update(DEBUG=False, ShowPerformance=True)
