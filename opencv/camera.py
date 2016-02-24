# import the necessary packages
from picamera.array import PiRGBArray
from picamera import PiCamera
import time
import cv2
import numpy as np

# initialize the camera and grab a reference to the raw camera capture
camera = PiCamera()
w=320
h=240
camera.resolution = (w, h)
camera.framerate = 60
camera.brightness = 60
camera.contrast = 100

rawCapture = PiRGBArray(camera, size=(w, h))

# allow the camera to warmup
time.sleep(0.1)

mtx = np.matrix( [[313.1251541, 0., 157.36763381],
       [0., 311.84837219, 130.36209271],
       [0., 0., 1.]])
dist = np.matrix([[-0.42159111, 0.44966352, -0.00877638, 0.00070653, -0.43508731]])
newcameramtx, roi = cv2.getOptimalNewCameraMatrix(mtx, dist, (w, h), 0, (w, h))

# capture frames from the camera
for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
    image = cv2.undistort(frame.array, mtx, dist, None, newcameramtx)
    grayImg = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    mask = cv2.threshold(grayImg, 180, 255, cv2.THRESH_BINARY)[1]

    cv2.imshow('frame', image)
    cv2.imshow('mask', mask)

    key = cv2.waitKey(1) & 0xFF

    # clear the stream in preparation for the next frame
    rawCapture.truncate(0)

    center = w / 2

    for y in range(0,h,1):
        if mask[y, center] > 0:
            print y
            break