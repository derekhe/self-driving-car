#include <SerialCommand.h>

char DRIVE_ENABLE_PIN = 10;
char DRIVE_PIN1 = 11;
char DRIVE_PIN2 = 12;

char STEER_PIN1 = 7;
char STEER_PIN2 = 8;
char STEER_ENABLE_PIN = 9;

SerialCommand SCmd;

void forward() {
  char* arg = SCmd.next();
  byte speed = 100;
  if (arg != NULL) {
    speed = atoi(arg);
  }
  
  digitalWrite(DRIVE_PIN1, 1);
  digitalWrite(DRIVE_PIN2, 0);
  analogWrite(DRIVE_ENABLE_PIN, speed);
}

void backward() {
  char* arg = SCmd.next();
  byte speed = 100;
  if (arg != NULL) {
    speed = atoi(arg);
  }
  
  digitalWrite(DRIVE_PIN1, 0);
  digitalWrite(DRIVE_PIN2, 1);
  analogWrite(DRIVE_ENABLE_PIN, speed);
}

void pause() {
  digitalWrite(DRIVE_PIN1, 0);
  digitalWrite(DRIVE_PIN2, 0);
  digitalWrite(DRIVE_ENABLE_PIN, 0);
}

void left() {
  digitalWrite(STEER_PIN1, 1);
  digitalWrite(STEER_PIN2, 0);
  digitalWrite(STEER_ENABLE_PIN, 1);
}

void right() {
  digitalWrite(STEER_PIN1, 0);
  digitalWrite(STEER_PIN2, 1);
  digitalWrite(STEER_ENABLE_PIN, 1);
}

void straight() {
  digitalWrite(STEER_PIN1, 0);
  digitalWrite(STEER_PIN2, 0);
  digitalWrite(STEER_ENABLE_PIN, 0);
}

void unrecognized(const char *command) {
  Serial.println("What?");
  Serial.println(*command);
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
}
