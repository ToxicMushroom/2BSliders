#include <SoftwareSerial.h>
const SoftwareSerial serial(10,11);

const int gate_a = 3; //3 - Left motor attached
const int gate_b = 9; //9 - Right motor attached
const char delim[] = "|";


void setup() {
  pinMode(gate_a, OUTPUT);
  pinMode(gate_b, OUTPUT);
  pinMode(gate_a - 1, OUTPUT);
  pinMode(gate_b - 1, OUTPUT);
  serial.begin(9600);
  //Serial.begin(9600);
}


void loop() {
  if (serial.available() < 1) {
    return; //returns when there is not serial input
  }
  delay(10);

  char val[32] = ""; //Creates c string to save the input
  while (serial.available() > 0) {
     append(val, serial.read());
  }
  //Serial.println(val);

  int val_length = strlen(val);
  if (val_length == 0) {
    return; //Returns if length is 0
  }

  char *ptr = strtok(val, delim);
  while(ptr != NULL) {
    applyToMotors(ptr);
    ptr = strtok(NULL, delim);
  }
}


void applyToMotors(char input[]) {
  //Serial.println(input);
  if (input[0] != '<' || input[strlen(input) - 1] != '>') return;
  else {
    //Removes first and last character
    int i = 1;
    char temp[32] = "";
    while(input[i+1] != '\0') {
      append(temp, input[i++]);
    }
    input = temp;
  }
  //Serial.println(input);

  char *endptr;
  int value = strtol(input, &endptr, 10);
  if (input == endptr) {
    //Serial.println("Input not number");
    return; //Returns if failed to parse
  }

  int dir = HIGH; //HIGH - forward | LOW - backwards
  int motor; // - left motor | 1 - right motor;
  if (value <= 510) {
    motor = gate_a;
    value = value - 255;
    if (value < 0) {
      dir = LOW;
      value = value * -1;
    }
  } else if (value <= 1021) {
    motor = gate_b;
    value = value - 766;
    if (value < 0) {
      dir = LOW;
      value = value * -1;
    }
  } else {
    return;
  }
  digitalWrite(motor - 1, dir);
  analogWrite(motor, value);
}


void append(char* s, char c) {
  int len = strlen(s);
  s[len] = c;
  s[len+1] = '\0';
}