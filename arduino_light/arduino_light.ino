#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <BH1750.h>
#include "DHT.h"

// === PIN SETUP ===
#define DHTPIN 15
#define DHTTYPE DHT22
#define SOIL_PIN 34
#define PUMP_PIN 27
#define LIGHT_PIN 26

// === OBJEK SENSOR ===
DHT dht(DHTPIN, DHTTYPE);
BH1750 lightMeter;
LiquidCrystal_I2C lcd(0x27, 16, 2);

// === VARIABEL ===
float temperature;
float humidity;
int soilValue;
float lux;

// === PENGATURAN AMBANG ===
int soilMin = 20;     // persen minimum kelembapan tanah
int soilMax = 60;     // persen maksimum kelembapan tanah
float tempPump = 35;  // suhu trigger pompa (°C)
float tempLightOff = 28; // suhu maksimum agar lampu boleh nyala
float luxMin = 20000; // batas bawah lux (gelap)
float luxMax = 30000; // batas atas lux (terang)
unsigned long lightDuration = 3600000; // 1 jam = 3600000 ms
unsigned long lightInterval = 10800000; // 3 jam = 10800000 ms

// === WAKTU INTERNAL UNTUK LAMPU ===
unsigned long lastLightOn = 0;
bool lightActive = false;

void setup() {
  Serial.begin(115200);
  Wire.begin(21, 22); // SDA, SCL

  dht.begin();
  lightMeter.begin();
  lcd.begin();
  lcd.backlight();

  pinMode(PUMP_PIN, OUTPUT);
  pinMode(LIGHT_PIN, OUTPUT);
  digitalWrite(PUMP_PIN, LOW);
  digitalWrite(LIGHT_PIN, LOW);

  lcd.setCursor(0, 0);
  lcd.print("Smart Plant v2");
  lcd.setCursor(0, 1);
  lcd.print("Init sensors...");
  delay(2000);
  lcd.clear();

  Serial.println("=== Smart Plant System v2 Started ===");
}

void loop() {
  // === BACA SENSOR ===
  humidity = dht.readHumidity();
  temperature = dht.readTemperature();
  soilValue = analogRead(SOIL_PIN);
  lux = lightMeter.readLightLevel();

  // === KONVERSI SOIL KE % ===
  int soilPercent = map(soilValue, 4095, 0, 0, 100);
  soilPercent = constrain(soilPercent, 0, 100);

  // === TAMPILKAN DI SERIAL ===
  Serial.println("----- DATA SENSOR -----");
  Serial.printf("Suhu: %.1f C\n", temperature);
  Serial.printf("Kelembapan Udara: %.1f %%\n", humidity);
  Serial.printf("Tanah: %d %%\n", soilPercent);
  Serial.printf("Cahaya: %.0f lux\n", lux);
  Serial.println("-----------------------");

  // === TAMPILKAN DI LCD ===
  lcd.setCursor(0, 0);
  lcd.print("T:");
  lcd.print(temperature, 1);
  lcd.print(" H:");
  lcd.print(humidity, 0);
  lcd.print("  ");

  lcd.setCursor(0, 1);
  lcd.print("S:");
  lcd.print(soilPercent);
  lcd.print("% L:");
  lcd.print((int)lux);
  lcd.print("   ");

  // === LOGIKA POMPA ===
  if (((soilPercent > soilMin) || (temperature > tempPump)) && (soilPercent < soilMax)) {
    digitalWrite(PUMP_PIN, HIGH);
    Serial.println("💧 Pompa: ON");
  } else {
    digitalWrite(PUMP_PIN, LOW);
    Serial.println("💧 Pompa: OFF");
  }

  // === LOGIKA LAMPU ===
  unsigned long now = millis();

  // Apakah saatnya nyalakan lampu?
  if ((now - lastLightOn >= lightInterval) && !lightActive) {
    if ((lux >= luxMin && lux <= luxMax) && (temperature <= tempLightOff)) {
      digitalWrite(LIGHT_PIN, HIGH);
      lightActive = true;
      lastLightOn = now;
      Serial.println("💡 Growlight: ON (Start 1 jam)");
    }
  }

  // Matikan lampu setelah 1 jam
  if (lightActive && (now - lastLightOn >= lightDuration)) {
    digitalWrite(LIGHT_PIN, LOW);
    lightActive = false;
    lastLightOn = now; // Reset waktu untuk interval berikutnya
    Serial.println("💡 Growlight: OFF (1 jam selesai)");
  }

  // Jika suhu > 28°C atau terlalu terang → matikan lampu
  if (temperature > tempLightOff || lux > luxMax) {
    digitalWrite(LIGHT_PIN, LOW);
    lightActive = false;
    Serial.println("💡 Growlight: OFF (karena panas/terang)");
  }

  delay(2000);
}
