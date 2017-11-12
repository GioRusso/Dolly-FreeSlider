#include <SoftwareSerial.h>
#include <stdlib.h>

SoftwareSerial BT(10,11); // RX, TX

char message[8];
char modo = 'N';
int i = 0;
int d = 0;

double dis = 0;
double rot = 0;
double rpm = 0;
double fot = 0;

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
    char data = BT.read();    
    if (data=='F'){
      modo='F'; 
    }
    if (modo=='F'){
      if (data=='-'){
        if (d==4){fot = atof(message);}
        if (d==3){rpm = atof(message);}
        if (d==2){rot = atof(message);}
        if (d==1){dis = atof(message);}
        char message[8];
        d++;
        i = 0; 
      }else{
        message[i] = data;
        i++;
        message[i] = '\0';
      }
    }
    if (d==5){
      Serial.println(dis);
      Serial.println(rot);
      Serial.println(rpm);
      Serial.println(fot);
      d = 0;
    }
    if (data=='D'){      
      Serial.println("DETENER");
      modo='N';
    }    
  }
}
