#include <CmdMessenger.h>
#include <Servo.h>
#include <SerialCommand.h>

char DRIVE_PIN1 = 12;
char DRIVE_PIN2 = 11;

Servo myservo;

SerialCommand sCmd;

void forward() {
  digitalWrite(DRIVE_PIN1, 1);
  digitalWrite(DRIVE_PIN2, 0);
  Serial.println("forward");
}

void backward() {
  digitalWrite(DRIVE_PIN1, 0);
  digitalWrite(DRIVE_PIN2, 1);
  Serial.println("backward");
}

void pause() {
  digitalWrite(DRIVE_PIN1, 0);
  digitalWrite(DRIVE_PIN2, 0);
  Serial.println("pause");

}

void left() {
  myservo.write(0);
  Serial.println("left");
}

void right() {
  myservo.write(180);
  Serial.println("right");
}

void straight() {
  myservo.write(90);
  Serial.println("straight");
}

void unrecognized(const char *command) {
  Serial.println("What?");
}

void setup() {
  Serial.begin(115200);
  pinMode(DRIVE_PIN1, OUTPUT);
  pinMode(DRIVE_PIN2, OUTPUT);

  myservo.attach(9);
  myservo.write(90);

  Serial.println("Ready");

  sCmd.addCommand("F", forward);
  sCmd.addCommand("B", backward);
  sCmd.addCommand("L", left);
  sCmd.addCommand("R", right);
  sCmd.addCommand("P", pause);
  sCmd.addCommand("S", straight);
  sCmd.setDefaultHandler(unrecognized);
}

void loop() {
  sCmd.readSerial();
}