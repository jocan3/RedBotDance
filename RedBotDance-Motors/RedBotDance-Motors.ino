// @autor Andres Mena
// @autor Oscar Rodríguez
// Code to drive redbot with commands serial-bluetooth using slow movements and low delays to achieve real-time dancing effect

#include <RedBot.h>

RedBotMotors motors;
boolean s1 = true;
boolean s2 = true;
boolean s3 = true;

// H-Bridge motor driver pins
#define    L_CTRL1   2
#define    L_CTRL2   4
#define    L_PWM     5

#define    R_CTRL1   7
#define    R_CTRL2   8
#define    R_PWM     6

// XBee SW_Serial pins
#define    SW_SER_TX A0
#define    SW_SER_RX A1



void setup()
{
   Serial.begin(9600);//connect to serial port
    motors.leftDrive(255);
    motors.rightDrive(-255);
    delay(100);
    motors.brake();
    delay(1000);
    motors.leftDrive(-255);
    motors.rightDrive(255);
    delay(100);
    motors.brake();
}

void loop()
{
  char inChar;
  //wait for serial commad
  if (Serial.available()) {
    inChar = (char)Serial.read();
    if (inChar == 'u') {
      motors.drive(-127);
      delay(50);
      motors.brake();
    }
    if (inChar == 'd') {
      motors.drive(127);
      delay(50);
      motors.brake();
    }
    if (inChar == 'l') {
      motors.leftDrive(-255);
      delay(50);
      motors.brake();
    }
    if (inChar == 'r') {
      motors.rightDrive(-255);
      delay(50);
      motors.brake();
    }
  }

}


