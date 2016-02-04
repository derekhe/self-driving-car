#include <SerialCommand.h>

char DRIVE_ENABLE_PIN = 10;
char DRIVE_PIN1 = 11;
char DRIVE_PIN2 = 12;

char STEER_PIN1 = 7;
char STEER_PIN2 = 8;
char STEER_ENABLE_PIN = 9;
char IRpin = 6;
char forcePause = false;
char hitCount = 0;
char maxHitCount = 2;
bool isForward = false;

SerialCommand SCmd;

void forward() {
  pause();
  char* arg = SCmd.next();
  byte speed = 100;
  if (arg != NULL) {
    speed = atoi(arg);
  }

  if (forcePause) {
    Serial.println("Force pause");
    return;
  }

  isForward = true;
  digitalWrite(DRIVE_PIN1, 1);
  digitalWrite(DRIVE_PIN2, 0);
  analogWrite(DRIVE_ENABLE_PIN, speed);
  Serial.println("forward");
}

void backward() {
  char* arg = SCmd.next();
  byte speed = 100;
  if (arg != NULL) {
    speed = atoi(arg);
  }

  backward(speed);
}

void backward(byte speed) {
  pause();
  isForward = false;
  digitalWrite(DRIVE_PIN1, 0);
  digitalWrite(DRIVE_PIN2, 1);
  analogWrite(DRIVE_ENABLE_PIN, speed);
  Serial.println("back");
}

void pause() {
  digitalWrite(DRIVE_PIN1, 0);
  digitalWrite(DRIVE_PIN2, 0);
  analogWrite(DRIVE_ENABLE_PIN, 0);
  Serial.println("pause");
}

void left() {
  digitalWrite(STEER_PIN1, 0);
  digitalWrite(STEER_PIN2, 1);
  digitalWrite(STEER_ENABLE_PIN, 1);
  Serial.println("left");
}

void right() {
  digitalWrite(STEER_PIN1, 1);
  digitalWrite(STEER_PIN2, 0);
  digitalWrite(STEER_ENABLE_PIN, 1);
  Serial.println("right");
}

void straight() {
  digitalWrite(STEER_PIN1, 0);
  digitalWrite(STEER_PIN2, 0);
  digitalWrite(STEER_ENABLE_PIN, 0);
  Serial.println("straight");
}

void unrecognized(const char *command) {
  Serial.println("What?");
  Serial.println(*command);
}

float distance() {
  float volts = analogRead(IRpin) * (5 / 1024.0); // value from sensor * (5/1024) - if running 3.3.volts then change 5 to 3.3
  float distance = 65 * pow(volts, -1.10);        // worked out from graph 65 = theretical distance / (1/Volts)S
  return distance;
}

void setup() {
  pinMode(DRIVE_ENABLE_PIN, OUTPUT);
  pinMode(DRIVE_PIN1, OUTPUT);
  pinMode(DRIVE_PIN2, OUTPUT);

  pinMode(STEER_ENABLE_PIN, OUTPUT);
  pinMode(STEER_PIN1, OUTPUT);
  pinMode(STEER_PIN2, OUTPUT);

  Serial.begin(115200);

  SCmd.addCommand("L", left);
  SCmd.addCommand("R", right);
  SCmd.addCommand("S", straight);
  SCmd.addCommand("F", forward);
  SCmd.addCommand("B", backward);
  SCmd.addCommand("P", pause);
  SCmd.setDefaultHandler(unrecognized);

  Serial.println("Ready");
}

void loop() {
  SCmd.readSerial();
  float dist = distance();
  delay(1);
  if (dist < 70) {
    Serial.println(dist);
    hitCount++;
    if (hitCount > maxHitCount) {
      hitCount = maxHitCount;
      if (!forcePause && isForward) {
        pause();
        backward(255);
        delay(500);
        pause();
      }

      Serial.println("Force Pause");
      forcePause = true;
    }
  } else {
    hitCount = 0;
    forcePause = false;
  }
}
