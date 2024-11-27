#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// BLE service and characteristic UUIDs
#define SERVICE_UUID        "37C4E592-77F4-2C36-8BE2-6E5456E6E2CA"
#define CHARACTERISTIC_UUID "00001111-0000-1000-8000-00805f9b34fb"

// 부저 핀 번호
#define BUZZER_PIN 25// 부저 핀 번호



// BLE server and characteristic declaration
BLECharacteristic *pCharacteristic;

// Callback class for the BLE characteristic
class MyCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    // This callback is triggered when the characteristic value is updated
    String value = pCharacteristic->getValue().c_str(); //x Get the value as a std::string
    String message = String(value.c_str()); // Convert std::string to Arduino String for easier handling
    Serial.print("Received Value: ");
    Serial.println(message);  // Display the received message
    if (message == "DISTANCE_EXCEEDED") {
      // 임계값 초과 메시지를 수신한 경우 부저 울리기
      digitalWrite(BUZZER_PIN, HIGH);  // 부저 ON
      delay(2000);  // 2초 동안 울림
      digitalWrite(BUZZER_PIN, LOW);   // 부저 OFF
    }
    // Check if the received message contains a specific command
    if (message == "OK") {
      Serial.println("Received OK message!");
      // 시리얼 모니터에 부저 울리는 동작 대신 메시지 출력
      Serial.println("Buzzer would be triggered now (simulated).");
      digitalWrite(BUZZER_PIN,HIGH);
      delay(500);
      digitalWrite(BUZZER_PIN,LOW);
      delay(500);
    }
     if (message == "Call") {
      Serial.println("Received call alert!");
      // 부저 울리기 (전화 수신 알림)
      digitalWrite(BUZZER_PIN, HIGH);
      delay(1000);  // 1초 동안 울림
      digitalWrite(BUZZER_PIN, LOW);
    } else {
      // Process other messages
      Serial.println("Message received from BLE: " + message);
    }
  }
};

void setup() {
  Serial.begin(115200);

  pinMode(BUZZER_PIN, OUTPUT);
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
}

