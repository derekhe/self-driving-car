# import the necessary packages

from camera import *

w = 320
h = 240

cameraThread = CameraThread(w=320, h=240)
cameraThread.start()

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


last = cv2.getTickCount()

while True:
    image = cameraThread.read()

    if image is None:
        time.sleep(0.1)
        continue

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
