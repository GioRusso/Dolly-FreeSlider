#include <SoftwareSerial.h>

SoftwareSerial BT(10,11); // RX, TX

void setup(){
  pinMode(8,OUTPUT);     // VCC
  digitalWrite(8,HIGH);
  Serial.begin(57600);    // Inicia Serial del Arduino
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  BT.begin(57600); // Inicia Serial Bluetooth
}

void loop()   {  
  if (BT.available()){
    Serial.write(BT.read());
  }
  if (Serial.available()){
    BT.write(Serial.read());
  }
}
