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
camera.brightness = 60
camera.contrast = 100

rawCapture = PiRGBArray(camera, size=(320, 240))

# allow the camera to warmup
time.sleep(0.1)

criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)

# 6x9 chess board, prepare object points, like (0,0,0), (1,0,0), (2,0,0) ....,(6,5,0)
object_point = np.zeros((6*9, 3), np.float32)
object_point[:, :2] = np.mgrid[0:9, 0:6].T.reshape(-1, 2)

# 3d point in real world space
object_points = []
# 2d points in image plane
image_points = []
h, w = 0, 0

# capture frames from the camera
count = 0
for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
    # grab the raw NumPy array representing the image, then initialize the timestamp
    # and occupied/unoccupied text
    image = frame.array

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    cv2.imshow("raw", gray)
    h, w = gray.shape[:2]

    # find chess board corners
    ret, corners = cv2.findChessboardCorners(gray, (9, 6), None)

    # add object points, image points
    if ret:
        object_points.append(object_point)
        cv2.cornerSubPix(gray, corners, (11, 11), (-1, -1), criteria)
        image_points.append(corners)

        # draw and display the corners
        cv2.imwrite(str(count) + ".png", image)
        cv2.drawChessboardCorners(image, (9, 6), corners, ret)
        cv2.imshow('image', image)
        key = cv2.waitKey(1) & 0xFF
        if key == ord("q"):
            break

    # clear the stream in preparation for the next frame
    rawCapture.truncate(0)
    count = count + 1
    print(count)