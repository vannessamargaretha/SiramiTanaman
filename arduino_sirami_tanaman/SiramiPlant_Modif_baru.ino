#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <BH1750.h>
#include "DHT.h"

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h> 



#define DHTPIN 15
#define DHTTYPE DHT22
#define SOIL_PIN 34
#define PUMP_PIN 27
#define LIGHT_PIN 26


DHT dht(DHTPIN, DHTTYPE);
BH1750 lightMeter;
LiquidCrystal_I2C lcd(0x27, 16, 2);


float temperature;
float humidity;
int soilValue;
float lux;


const char* ssid = "SiramiIOT"; 
const char* password = "melon123"; 
const char* baseUrl = "http://202.10.47.148:8080/api";
const int deviceId = 1;



int soilMinDefault = 20;
int soilMaxDefault = 60;
int tempPumpDefault = 35;
int tempLightOffDefault = 28;
int luxMinDefault = 200;
int luxMaxDefault = 300;
int lightDurationDefault = 2000;  
int lightIntervalDefault = 3000; 


int soilMin = soilMinDefault;
int soilMax = soilMaxDefault;
int tempPump = tempPumpDefault;
int tempLightOff = tempLightOffDefault;
int luxMin = luxMinDefault;
int luxMax = luxMaxDefault;
int lightDuration = lightDurationDefault;
int lightInterval = lightIntervalDefault;




bool pumpOverrideActive = false;
unsigned long pumpOverrideEnd = 0;

bool lightOverrideActive = false;
unsigned long lightOverrideEnd = 0;



unsigned long lastLightOn = 0;
bool lightActive = false;




bool manualMode = true; 


void fetchCustomSettings() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
 
  String url = String(baseUrl) +"/device/" + deviceId + "/settings";
  http.begin(url);

  int httpCode = http.GET();
  if (httpCode == 200) {
    String payload = http.getString();

    StaticJsonDocument<512> doc;
    DeserializationError error = deserializeJson(doc, payload);
    if (!error) {
     
      manualMode = doc["manualMode"] | manualMode;

      
      if (manualMode) {
          Serial.println("Manual Mode: Using custom settings");

          soilMin = doc["soilMin"] | soilMinDefault;
          soilMax = doc["soilMin"] | soilMaxDefault;
          tempPump = doc["tempPump"] | tempPumpDefault;
          tempLightOff = doc["tempLightOff"] | tempLightOffDefault;
          luxMin = doc["luxMin"] | luxMinDefault;
          luxMax = doc["luxMax"] | luxMaxDefault;
          lightDuration = doc["lightDuration"] | lightDurationDefault;
          lightInterval = doc["lightInterval"] | lightIntervalDefault;

      } else {
          Serial.println("Automatic Mode: Using default thresholds");

          soilMin = soilMinDefault;
          soilMax = soilMaxDefault;
          tempPump = tempPumpDefault;
          tempLightOff = tempLightOffDefault;
          luxMin = luxMinDefault;
          luxMax = luxMaxDefault;
          lightDuration = lightDurationDefault;
          lightInterval = lightIntervalDefault;
      }
    }
  } else {
    Serial.printf("Failed to fetch settings. HTTP code: %d\n", httpCode);
  }
  http.end();
}

void setup() {
  Serial.begin(115200);
  Wire.begin(21, 22); 

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

  Serial.println("Connecting to WiFi...");
  Serial.println("Attempting to connect to WiFi...");
  Serial.println(ssid);

  WiFi.begin(ssid, password);
  int retries = 0;
while (WiFi.status() != WL_CONNECTED && retries < 20) {
  delay(1000);
  Serial.print(".");
  retries++;
}
if (WiFi.status() == WL_CONNECTED) {
  Serial.println("\nConnected to WiFi!");
  Serial.println(WiFi.localIP());
} else {
  Serial.println("\nFailed to connect to WiFi.");
}

  Serial.println("=== Smart Plant System v2 Started ===");
}

void loop() {

  fetchCustomSettings();

  humidity = dht.readHumidity();
  temperature = dht.readTemperature();
  soilValue = analogRead(SOIL_PIN);
  lux = lightMeter.readLightLevel();

  int soilPercent = map(soilValue, 4095, 0, 0, 100);
  soilPercent = constrain(soilPercent, 0, 100);

  Serial.println("----- DATA SENSOR -----");
  Serial.printf("Suhu: %.1f C\n", temperature);
  Serial.printf("Kelembapan Udara: %.1f %%\n", humidity);
  Serial.printf("Tanah: %d %%\n", soilPercent);
  Serial.printf("Cahaya: %.0f lux\n", lux);
  Serial.println("-----------------------");

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

  handleOverrideExpiry();

  if (!pumpOverrideActive) {
    if (((soilPercent > soilMin) || (temperature > tempPump)) && (soilPercent < soilMax)) {
      digitalWrite(PUMP_PIN, HIGH);
      Serial.println("💧 Pompa: ON");
    } else if (((soilPercent == 0) || (temperature > tempPump)) && (soilPercent < soilMax)) {
      digitalWrite(PUMP_PIN, HIGH);
      Serial.println("💧 Pompa: ON");
    } else {
      digitalWrite(PUMP_PIN, LOW);
      Serial.println("💧 Pompa: OFF");
    }
  }

  unsigned long now = millis();

  if (!lightOverrideActive) {

    if (temperature > tempLightOff || lux > luxMax) {
      digitalWrite(LIGHT_PIN, HIGH);
      Serial.println("💡 Light: OFF (too hot/bright)");
    } else {
      digitalWrite(LIGHT_PIN, LOW);
      Serial.println("💡 Light: ON (smart)");
    }

  }



  if (!lightOverrideActive && lightActive && (now - lastLightOn >= lightDuration)) {
      digitalWrite(LIGHT_PIN, HIGH); 
      lightActive = false;
      lastLightOn = now;
      Serial.println("💡 Growlight: OFF (1 jam selesai)");
  }

  
  if (!lightOverrideActive && (temperature > tempLightOff || lux > luxMax)) {
      digitalWrite(LIGHT_PIN, HIGH); 
      lightActive = false;
      Serial.println("💡 Growlight: OFF (karena panas/terang)");
  }


  sendSensorData(soilPercent, humidity, lux);
  fetchCommandsAndApply();

  delay(2000);

}

void sendSensorData(float soil, float airHum, float light) {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    String url = String(baseUrl) + "/sensor/update";
    http.begin(url);
    http.addHeader("Content-Type", "application/json");

    String payload = "{\"deviceId\":" + String(deviceId) +
                     ",\"soilMoisture\":" + String(soil, 2) +
                     ",\"airHumidity\":" + String(airHum, 2) +
                     ",\"lightIntensity\":" + String(light, 2) + "}";

    int httpResponseCode = http.POST(payload);
    Serial.printf("POST /sensor/update -> %d\n", httpResponseCode);
    http.end();
  } else {
    Serial.println("WiFi disconnected, can't send data!");
  }
}


void fetchCommandsAndApply() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  String url = String(baseUrl) + "/device/" + String(deviceId) + "/commands/latest";
  http.begin(url);

  int code = http.GET();
  if (code != 200) {
    Serial.printf("GET commands failed: %d\n", code);
    http.end();
    return;
  }

  String response = http.getString();
  Serial.println("Received command JSON:");
  Serial.println(response);

  DynamicJsonDocument doc(1024);
  if (deserializeJson(doc, response)) {
    Serial.println("JSON error");
    return;
  }

  bool waterOverride = doc["watering"]["override"];
  float waterValue = doc["watering"]["value"];         
  int duration = doc["watering"]["durationSeconds"];  

  static long lastWateringId = -1;
  long currentWateringId = doc["watering"]["id"] | 0; 

  if (waterOverride && currentWateringId != lastWateringId) {
      lastWateringId = currentWateringId;
      pumpOverrideActive = true;
      pumpOverrideEnd = millis() + (duration * 1000);

      if (waterValue > 0) {
          digitalWrite(PUMP_PIN, HIGH);
          Serial.println("💧 Pump: ON (override)");
      } else {
          digitalWrite(PUMP_PIN, LOW);
          Serial.println("💧 Pump: OFF (override)");
      }
  }

  bool lightOverride = doc["lighting"]["override"];
  float lightValue = doc["lighting"]["value"];
  int lightDuration = doc["lighting"]["durationSeconds"];
  static long lastLightingId = -1;
  long currentLightingId = doc["lighting"]["id"] | 0; 

  if (lightOverride && currentLightingId != lastLightingId) {
      lastLightingId = currentLightingId;
      lightOverrideActive = true;
      lightOverrideEnd = millis() + (lightDuration * 1000);

      if (lightValue > 0) {
          digitalWrite(LIGHT_PIN, LOW);
          Serial.println("💡 Light: ON (override)");
      } else {
          digitalWrite(LIGHT_PIN, HIGH);
          Serial.println("💡 Light: OFF (override)");
      }
  }

  http.end();
}


void handleOverrideExpiry() {
    unsigned long now = millis();

    if (pumpOverrideActive && now >= pumpOverrideEnd) {
        pumpOverrideActive = false;
        digitalWrite(PUMP_PIN, LOW);
        Serial.println("💧 Pump override ended → Returning to normal logic");

        if (WiFi.status() == WL_CONNECTED) {
            HTTPClient http;
            String url = String(baseUrl) + "/device/" + String(deviceId) + "/command/clear?type=watering";
            http.begin(url);
            http.POST("");
            http.end();
        }
    }

    if (lightOverrideActive && now >= lightOverrideEnd) {
        lightOverrideActive = false;
        digitalWrite(LIGHT_PIN, LOW);
        Serial.println("💡 Light override ended → Returning to normal logic");

        if (WiFi.status() == WL_CONNECTED) {
            HTTPClient http;
            String url = String(baseUrl) + "/device/" + String(deviceId) + "/command/clear?type=lighting";
            http.begin(url);
            http.POST("");
            http.end();
        }
    }
}


