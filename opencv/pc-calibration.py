import cv2
import numpy as np
import glob
import os

# termination criteria
criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)

# 6x9 chess board, prepare object points, like (0,0,0), (1,0,0), (2,0,0) ....,(6,5,0)
object_point = np.zeros((6 * 9, 3), np.float32)
object_point[:, :2] = np.mgrid[0:9, 0:6].T.reshape(-1, 2)

# 3d point in real world space
object_points = []
# 2d points in image plane
image_points = []
h, w = 0, 0

images = glob.glob('calibration/*.png')

for file_name in images:
    image = cv2.imread(file_name)
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    h, w = gray.shape[:2]

    # find chess board corners
    ret, corners = cv2.findChessboardCorners(gray, (9, 6), None)

    # add object points, image points
    if ret:
        object_points.append(object_point)
        cv2.cornerSubPix(gray, corners, (11, 11), (-1, -1), criteria)
        image_points.append(corners)

        # draw and display the corners
        cv2.drawChessboardCorners(image, (9, 6), corners, ret)
        cv2.imshow('image', image)
        cv2.waitKey(1)

print("calculating")
# calibration
ret, mtx, dist, rvecs, tvecs = cv2.calibrateCamera(object_points, image_points, (w, h), None, None)

print("camera matrix:\n", mtx);

# pi camera intrinsic parameters
ay = mtx[1, 1]
u0 = mtx[0, 2]
v0 = mtx[1, 2]
print("Ay:", ay)
print("u0:", u0)
print("v0:", v0)

cv2.destroyAllWindows()

for file_name in images:
    image = cv2.imread(file_name)
    newcameramtx, roi = cv2.getOptimalNewCameraMatrix(mtx, dist, (w, h), 0, (w, h))
    dst = cv2.undistort(image, mtx, dist, None, newcameramtx)
    name = os.path.basename(file_name)
    cv2.imwrite('./out/calibrated_' + name, dst)
