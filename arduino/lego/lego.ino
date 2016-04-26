#include <CmdMessenger.h>
#include <Servo.h>
#include <SerialCommand.h>

char DRIVE_PIN1 = 12;
char DRIVE_PIN2 = 11;
char DRIVE_ENABLE_PIN = 3;

Servo myservo;

SerialCommand sCmd;

int readArgument(int defaultValue = 255){
  int argument = 0;
  char *arg;

  arg = sCmd.next();  
  if (arg != NULL) {
    return atoi(arg);
  }
  else{
    return defaultValue;
  }
}

void forward() {
  int speed = readArgument(255);  
  digitalWrite(DRIVE_PIN1, 1);
  digitalWrite(DRIVE_PIN2, 0);
  analogWrite(DRIVE_ENABLE_PIN, speed);
  Serial.println("forward");
  Serial.println(speed);
}

void backward() {
  int speed = readArgument(255);
  digitalWrite(DRIVE_PIN1, 0);
  digitalWrite(DRIVE_PIN2, 1);
  analogWrite(DRIVE_ENABLE_PIN, speed);
  Serial.println("backward");
  Serial.println(speed);
}

void pause() {
  digitalWrite(DRIVE_PIN1, 0);
  digitalWrite(DRIVE_PIN2, 0);
  Serial.println("pause");

}

void steer() {
  int angle = readArgument(90);
  myservo.write(angle);
  Serial.println("steer");
}

void unrecognized(const char *command) {
  Serial.println("What?");
}

void setup() {
  Serial.begin(115200);
  pinMode(DRIVE_PIN1, OUTPUT);
  pinMode(DRIVE_PIN2, OUTPUT);
  pinMode(DRIVE_ENABLE_PIN, OUTPUT);

  myservo.attach(9);
  myservo.write(90);

  Serial.println("Ready");

  sCmd.addCommand("F", forward);
  sCmd.addCommand("B", backward);
  sCmd.addCommand("S", steer);
  sCmd.addCommand("P", pause);
  sCmd.setDefaultHandler(unrecognized);
}

void loop() {
  sCmd.readSerial();
}