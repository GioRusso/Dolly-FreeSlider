#include <Stepper.h>

#define motorSteps 200
#define motorPin1 8
#define motorPin2 9
#define ledPin 13

// Initialize of the Stepper library:
Stepper myStepper(motorSteps, motorPin1,motorPin2); 

void setup() {
  // set the motor speed at 60 RPMS:
  myStepper.setSpeed(250);
  pinMode(ledPin, OUTPUT);
  blink(5);
}

void loop() {
  myStepper.step(2000);
  blink(1);
  delay(500);

  myStepper.step(-2000);
  blink(1);
  delay(500); 

}

// Blink the reset LED:
void blink(int howManyTimes) {
  int i;
  for (i=0; i< howManyTimes; i++) {
    digitalWrite(ledPin, HIGH);
    delay(200);
    digitalWrite(ledPin, LOW);
    delay(200);
  }
}
