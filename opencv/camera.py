# import the necessary packages
from picamera.array import PiRGBArray
from picamera import PiCamera
import time
import cv2
import numpy as np
import math

# initialize the camera and grab a reference to the raw camera capture
camera = PiCamera()
w = 320
h = 240
camera.resolution = (w, h)
camera.framerate = 60
camera.brightness = 70
camera.contrast = 100

rawCapture = PiRGBArray(camera, size=(w, h))

# allow the camera to warmup
time.sleep(0.1)

mtx = np.matrix([[313.1251541, 0., 157.36763381],
                 [0., 311.84837219, 130.36209271],
                 [0., 0., 1.]])
dist = np.matrix([[-0.42159111, 0.44966352, -0.00877638, 0.00070653, -0.43508731]])
newcameramtx, roi = cv2.getOptimalNewCameraMatrix(mtx, dist, (w, h), 0, (w, h))


def distance(pixel):
    return 84134.5 - 0.0390032 * pixel - 53553.8 * np.arctan(pixel ** 2)


def drawReferenceLines():
    # 90cm
    cv2.line(mask, (0, 26), (w, 26), (255))
    # 60cm
    cv2.line(mask, (0, 33), (w, 33), (180))
    # 30cm
    cv2.line(mask, (0, 55), (w, 55), (128))


def drawLine(row, column):
    cv2.line(mask, (column, row), (column, h), (255))


# capture frames from the camera
mapx, mapy = cv2.initUndistortRectifyMap(mtx, dist, None, newcameramtx, (w, h), 5)

last = cv2.getTickCount()
for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
    # Improve performance
    image = cv2.remap(frame.array, mapx, mapy, cv2.INTER_LINEAR)
    grayImg = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    mask = cv2.threshold(grayImg, 160, 255, cv2.THRESH_BINARY)[1]

    for columnIndex in range(0, w, 4):
        column = mask[:, columnIndex]
        row = np.nonzero(column)[0]

        if not row.size == 0:
            distance(row[0])
            drawLine(row[0], columnIndex)

    drawReferenceLines()
    cv2.imshow("image", grayImg)
    cv2.imshow('mask', mask)

    timeSpend = cv2.getTickCount() - last
    last = cv2.getTickCount()
    time = timeSpend / cv2.getTickFrequency()
    print(time, 1 / time)

    key = cv2.waitKey(1) & 0xFF

    # clear the stream in preparation for the next frame
    rawCapture.truncate(0)