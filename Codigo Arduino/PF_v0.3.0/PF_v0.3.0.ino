#include <Stepper.h>
#include <SoftwareSerial.h>

#define motorSteps 200
#define M1C1 4
#define M1C2 5
#define M2C1 6
#define M2C2 7
#define ledPin LED_BUILTIN

SoftwareSerial Bluetooth(10,11);  // RX, TX
Stepper mDes(motorSteps,M1C1,M1C2);
Stepper mRot(motorSteps,M2C1,M2C2);  

void setup() {
  mDes.setSpeed(60);
  mRot.setSpeed(60);
  pinMode(ledPin, OUTPUT);
  blink(5);
  pinMode(8,OUTPUT); // VCC Bluetooth

  Serial.begin(9600); // Inicia Serial del Arduino
  digitalWrite(8,HIGH); // Asigna nivel para habilitar Bluetooth
  Serial.println("Esperando comandos AT:"); // Imprime por serial arduino
  Bluetooth.begin(38400); // Inicia Serial Bluetooth
}

void loop() {
  mDes.step(200);
  mRot.step(-200);
  blink(3);
  mDes.step(-200);
  mRot.step(200);
  blink(3);
  if (Bluetooth.available()){
    Serial.write(Bluetooth.read());
  } // Lo que recibe el serial bluetooth, lo imprime por serial arduino
  if (Serial.available()){  
    Bluetooth.write(Serial.read());
  } // Lo que recibe el serial arduino, lo imprime por serial bluetooth

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
