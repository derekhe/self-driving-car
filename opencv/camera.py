# import the necessary packages
from picamera.array import PiRGBArray
from picamera import PiCamera
import time
import cv2
import numpy as np

# initialize the camera and grab a reference to the raw camera capture
camera = PiCamera()
camera.resolution = (320, 240)
camera.framerate = 30
camera.brightness = 40
camera.drc_strength = 'high'
camera.contrast = 100
camera.awb_mode = 'cloudy'

camera.vflip = True

rawCapture = PiRGBArray(camera, size=(320, 240))

hl = 168
sl = 80
vl = 200

hh = 178
sh = 254
vh = 255

# allow the camera to warmup
time.sleep(0.1)

# capture frames from the camera
for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
    # grab the raw NumPy array representing the image, then initialize the timestamp
    # and occupied/unoccupied text
    image = frame.array

    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

    lower_blue = np.array([hl, sl, vl])
    upper_blue = np.array([hh, sh, vh])

    mask = cv2.inRange(hsv, lower_blue, upper_blue)

    # show the frame
    cv2.imshow('frame', image)
    cv2.imshow('mask', mask)

    key = cv2.waitKey(1) & 0xFF

    # clear the stream in preparation for the next frame
    rawCapture.truncate(0)

    # if the `q` key was pressed, break from the loop
    if key == ord("q"):
        hl = hl + 1
    if key == ord("Q"):
        hl = hl - 1
    if key == ord("a"):
        hh = hh - 1
    if key == ord("A"):
        hh = hh + 1

    if key == ord("w"):
        sl = sl + 1
    if key == ord("W"):
        sl = sl - 1
    if key == ord("s"):
        sh = sh - 1
    if key == ord("S"):
        sh = sh + 1

    if key == ord("e"):
        vl = vl + 1
    if key == ord("E"):
        vl = vl - 1
    if key == ord("d"):
        vh = vh - 1
    if key == ord("D"):
        vh = vh + 1

    print((hl,sl,vl, hh, sh, vh))