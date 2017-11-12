/*  Codigo fuente para el Proyecto Final Dolly Free-Slider
    Autores: Giorgio Russo y Daniela Rada
    http://www.github.com/...
*/

#include <SoftwareSerial.h>                 // Libreria para conexion serial con el modulo Bluetooth.
#include <Stepper.h>                        // Libreria de arduino para el control de motores de paso.
#include <math.h>
#include <stdlib.h>

#define motorSteps 200                      // El numero de pasos para una revolucion completa del eje.
#define M1C1 4
#define M1C2 5
#define M2C1 6
#define M2C2 7
Stepper mDes(motorSteps,M1C1,M1C2);         // M1 es el motor de desplazamiento = mDes
Stepper mRot(motorSteps,M2C1,M2C2);         // M2 es el motor de rotacion de la camara = mRot
SoftwareSerial BT(10,11);                   // Pines para puerto serial Bluetooth (RX, TX)

unsigned long tPrevioDes = 0;               // Es el tiempo del ultimo paso de desplazamiento.
unsigned long tPrevioRot = 0;               // Es el tiempo del ultimo paso de rotacion.
unsigned long tPrevioFot = 0;               // Es el tiempo de la ultima foto tomada.

//  Se incializan las variables de control para los motores.
//  Estas podran ser leidas y/o calculadas segun los parametros del usuario.
char message[8];
char modo = 'N';
int i = 0;
int d = 0;
double dis = 0;
double rot = 0;
double rpm = 0;
double fot = 0;

double pActual_mDes = 0;                    // Paso actual del motor de desplazamiento.
double pActual_mRot = 0;                    // Paso actual del motor de rotacion.
double fActual = 0;                         // La foto actual tomada.        

void setup() {
  pinMode(2,OUTPUT);                        // Pin de control camara.
  pinMode(8,OUTPUT);                        // VCC Para modulo Bluetooth.
  digitalWrite(8,HIGH);                     // Se habilita el modulo.
  digitalWrite(2,HIGH);
  BT.begin(57600);                          // Inicia Serial Bluetooth
}

void loop() {
  unsigned long tActual = millis();         // Es el tiempo actual en millisegundos al comienzo de cada loop.

    if (BT.available()){
    char data = BT.read();                  // Guarda en "data" el bit recibido por Bluetooth.
    if (data=='F'){                   
      modo='F'; 
    }
    if (data=='V'){
      modo='V'; 
    }
    if (modo=='F'){                         // FOTO: Se guardan los parametros separados por "-".
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
    if (modo=='V'){                         // VIDEO: Se guardan los parametros separados por "-".
      if (data=='-'){
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
    if (data=='D'){                       // DETENER: Se resetean la posicion actual de los motores y cambia modo.
      modo='N';
      pActual_mDes = 0;
      pActual_mRot = 0;
      fActual = 0; 
      d = 0;
    }       
  }
  if (modo=='F' && d==5){                 // FOTO: Cuando recibio todos los parametros se calculan los pasos y tiempo de paso.
    double tDes = 300/rpm;
    double pTotal_mDes = round(dis/0.106814);
    double pTotal_mRot = round(rot/1.8);
    double tRot = tDes*pTotal_mDes/pTotal_mRot;
    double tFot = tDes*pTotal_mDes/fot;
      
    if (tActual-tPrevioDes >= tDes && pTotal_mDes > pActual_mDes){
      tPrevioDes = tActual;
      mDes.step(-1);
      pActual_mDes++;   
    }
    if (tActual-tPrevioRot >= tRot && pTotal_mRot > pActual_mRot){
      tPrevioRot = tActual;
      mRot.step(1);
      pActual_mRot++;
    }
    if (tActual-tPrevioFot >= tFot && fot > fActual){
      tPrevioFot = tActual;
      digitalWrite(2,LOW);   
      fActual++;
    }
    if (tActual-tPrevioFot >= 200){
      digitalWrite(2,HIGH);     
    }
  }
  if (modo=='V' && d==4){               // VIDEO: Cuando recibio todos los parametros se calculan los pasos y tiempo de paso.
    double tDes = 300/rpm;
    double pTotal_mDes = round(dis/0.106814);
    double pTotal_mRot = round(rot/1.8); 
    double tRot = tDes*pTotal_mDes/pTotal_mRot;
      
    if (tActual-tPrevioDes >= tDes && pTotal_mDes > pActual_mDes){
      tPrevioDes = tActual;
      mDes.step(-1);
      pActual_mDes++;   
    }
    if (tActual-tPrevioRot >= tRot && pTotal_mRot > pActual_mRot){
      tPrevioRot = tActual;
      mRot.step(1);
      pActual_mRot++;
    }
  }
}

