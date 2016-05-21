#include <Arduino.h>

#include <Servo.h>
#include <SerialCommand.h>
#include <SoftwareSerial.h>

char DRIVE_PIN1 = 12;
char DRIVE_PIN2 = 11;
char DRIVE_ENABLE_PIN = 3;
char DEFAULT_STEER = 90;
char STEER_SERVO = 9;
char VOLTAGE_SENSOR = A1;

Servo myservo;
SerialCommand sCmd;

bool pingReceived = false;

int readArgument(int defaultValue = 255){
  char *arg;

  arg = sCmd.next();
  if (arg != NULL) {
    return atoi(arg);
  }
  else{
    return defaultValue;
  }
}

void ping(){
  Serial.println("@");
}

void throttle() {
  int speed = readArgument(255);
  if(speed == 0){
    digitalWrite(DRIVE_PIN1, 0);
    digitalWrite(DRIVE_PIN2, 0);
  } else if (speed > 0){
    digitalWrite(DRIVE_PIN1, 1);
    digitalWrite(DRIVE_PIN2, 0);
  }
  else{
    digitalWrite(DRIVE_PIN1, 0);
    digitalWrite(DRIVE_PIN2, 1);
  }

  analogWrite(DRIVE_ENABLE_PIN, abs(speed));
  ping();
}

void reset() {
  digitalWrite(DRIVE_PIN1, 0);
  digitalWrite(DRIVE_PIN2, 0);
  myservo.write(DEFAULT_STEER);
  ping();
}

void steer() {
  int angle = readArgument(90);
  myservo.write(angle);
  ping();
}

void voltage(){
  float voltage = analogRead(VOLTAGE_SENSOR) * (5 / 1023.0) * (25 / 5);
  Serial.print("V");
  Serial.println(voltage);
}

void unrecognized() {
  Serial.println("Unknown");
}

void setup() {
  Serial.begin(115200);
  pinMode(DRIVE_PIN1, OUTPUT);
  pinMode(DRIVE_PIN2, OUTPUT);
  pinMode(DRIVE_ENABLE_PIN, OUTPUT);

  myservo.attach(STEER_SERVO);
  myservo.write(DEFAULT_STEER);

  Serial.println("Ready");

  sCmd.addCommand("T", throttle);
  sCmd.addCommand("S", steer);
  sCmd.addCommand("R", reset);
  sCmd.addCommand("@", ping);
  sCmd.addCommand("V", voltage);
  sCmd.addDefaultHandler(unrecognized);
}

void loop() {
  sCmd.readSerial();
}
