#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// BLE 서비스 및 특성 UUID
#define SERVICE_UUID        "37C4E592-77F4-2C36-8BE2-6E5456E6E2CA"
#define CHARACTERISTIC_UUID "00001111-0000-1000-8000-00805f9b34fb"

// 부저 핀 번호
#define BUZZER_PIN 25

// 부저 상태 관리 변수
bool isBuzzerOn = false;  // 부저 상태

// BLE 서버와 특성 선언
BLECharacteristic *pCharacteristic;

// Callback class for the BLE characteristic
class MyCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    // This callback is triggered when the characteristic value is updated
    String value = pCharacteristic->getValue().c_str();
    String message = String(value.c_str()); // 메시지를 String으로 처리
    Serial.print("Received Value: ");
    Serial.println(message);  // 수신된 메시지 출력

    if (message == "DISTANCE_EXCEEDED1") {
      // DISTANCE_EXCEEDED1 메시지 수신 시 부저 기본 주파수(200Hz)로 울리기
      Serial.println("DISTANCE_EXCEEDED1 received - Buzzer should sound with 200Hz.");
      digitalWrite(BUZZER_PIN, HIGH);  // 부저 ON
      delay(2000);  // 2초 동안 울림
      digitalWrite(BUZZER_PIN, LOW);   // 부저 OFF
    }

    if (message == "DISTANCE_EXCEEDED") {
      // DISTANCE_EXCEEDED 메시지 수신 시 부저 주파수를 1400Hz로 설정
      Serial.println("DISTANCE_EXCEEDED received - Buzzer should sound with 1400Hz.");
      tone(BUZZER_PIN, 1400);  // 1400Hz로 부저 울리기
      delay(2000);  // 2초 동안 울림
      noTone(BUZZER_PIN);    // 부저 끄기
    }

    if (message == "OK1") {
      // OK 메시지 수신 시 부저 주파수를 1400Hz로 설정
      Serial.println("OK received - Buzzer should sound with 1400Hz.");
      tone(BUZZER_PIN, 1400);  // 1400Hz로 부저 울리기
      delay(2000);  // 2초 동안 울림
      noTone(BUZZER_PIN);    // 부저 끄기
    }

    if (message == "OK") {
      // OK1 메시지 수신 시 부저 기본 주파수(200Hz)로 울리기
      Serial.println("OK1 received - Buzzer should sound with 200Hz.");
      digitalWrite(BUZZER_PIN, HIGH);  // 부저 ON
      delay(2000);  // 2초 동안 울림
      digitalWrite(BUZZER_PIN, LOW);   // 부저 OFF
    }

    if (message == "Call1") {
      // Call 메시지 수신 시 부저 주파수를 1400Hz로 설정
      Serial.println("Call received - Buzzer should sound with 1400Hz.");
      tone(BUZZER_PIN, 1400);  // 1400Hz로 부저 울리기
      delay(5000);  // 5초 동안 울림
      noTone(BUZZER_PIN);    // 부저 끄기
    }

    if (message == "Call") {
      // Call1 메시지 수신 시 부저 기본 주파수(200Hz)로 울리기
      Serial.println("Call1 received - Buzzer should sound with 200Hz.");
      digitalWrite(BUZZER_PIN, HIGH);  // 부저 ON
      delay(5000);  // 5초 동안 울림
      digitalWrite(BUZZER_PIN, LOW);   // 부저 OFF
    }

    // 다른 메시지 처리
    else {
      Serial.println("Message received from BLE: " + message);
    }
  }
};

void setup() {
  Serial.begin(115200);

  pinMode(BUZZER_PIN, OUTPUT); // 부저 핀을 출력 모드로 설정

  // Initialize BLE
  BLEDevice::init("SmartUmbrella"); // Set the device name to match the Android app
  BLEServer *pServer = BLEDevice::createServer();

  // Set up the BLE service and characteristic
  BLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                          CHARACTERISTIC_UUID,
                          BLECharacteristic::PROPERTY_READ |
                          BLECharacteristic::PROPERTY_WRITE |
                          BLECharacteristic::PROPERTY_NOTIFY
                        );

  // Attach callback to handle incoming data
  pCharacteristic->setCallbacks(new MyCharacteristicCallbacks());

  // Start the service
  pService->start();

  // Start advertising so the device can be discovered by the Android app
  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  pAdvertising->start();

  Serial.println("BLE Server started");
}

void loop() {
  // BLE 서버가 계속 동작하도록 하며 특별히 처리할 내용은 없음
}
