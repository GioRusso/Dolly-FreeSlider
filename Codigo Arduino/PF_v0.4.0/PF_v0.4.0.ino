/*  Codigo fuente para el Proyecto Final Dolly Free-Slider
    Autores: Giorgio Russo y Daniela Rada
    http://www.github.com/...
*/

#include <Stepper.h>                  // Libreria de arduino para el control de motores de paso.
#include <math.h>

#define motorSteps 200                // El numero de pasos para una revolucion completa del eje.
#define M1C1 4
#define M1C2 5
#define M2C1 6
#define M2C2 7
Stepper mDes(motorSteps,M1C1,M1C2);   // M1 es el motor de desplazamiento = mDes
Stepper mRot(motorSteps,M2C1,M2C2);   // M2 es el motor de rotacion de la camara = mRot

unsigned long tPrevioDes = 0;         // Es el tiempo del ultimo paso de desplazamiento.
unsigned long tPrevioRot = 0;         // Es el tiempo del ultimo paso de rotacion.

//  Se incializan las variables de control para los motores.
//  Estas podran ser leidas y/o calculadas segun los parametros del usuario.
double rpm = 20;
double dis = 50;
double rot = 180;

double pActual_mDes = 0;              // Paso actual del motor de desplazamiento.
double pActual_mRot = 0;              // Paso actual del motor de rotacion.        
int debug = 0;                        // Permite visualizar estado de las variable en salida serial.

void setup() {
  if (debug){
    Serial.begin(115200);    
    Serial.println("Modo Debug...");
    Serial.write("RPM = ");
    Serial.println(rpm);
    Serial.write("Distancia = ");
    Serial.println(dis);
  }  
}

void loop() {
  unsigned long tActual = millis();   // Es el tiempo actual en millisegundos al comienzo de cada loop.
  
  double tDes = 300/rpm;
  double pTotal_mDes = round(dis/0.102);
  double pTotal_mRot = round(rot/1.8); 
  double tRot = tDes*pTotal_mDes/pTotal_mRot;
  
  if (debug && pActual_mDes == 0){
    Serial.write("Pasos Totales = ");
    Serial.println(pTotal_mDes);
    Serial.write("Intervalo entre pasos = ");
    Serial.println(tDes);
  }   
  if (tActual-tPrevioDes >= tDes && pTotal_mDes > pActual_mDes){
    tPrevioDes = tActual;
    mDes.step(1);
    pActual_mDes++;
    if (debug){
      Serial.write("[DES] Paso y tiempo actual = ");
      Serial.print(pActual_mDes);
      Serial.write(" :: ");
      Serial.println(tActual);
    }        
  }
  if (tActual-tPrevioRot >= tRot && pTotal_mRot > pActual_mRot){
    tPrevioRot = tActual;
    mRot.step(1);
    pActual_mRot++;
    if (debug){
      Serial.write("[ROT] Paso y tiempo actual = ");
      Serial.print(pActual_mRot);
      Serial.write(" :: ");
      Serial.println(tActual);
    }
  }
}

