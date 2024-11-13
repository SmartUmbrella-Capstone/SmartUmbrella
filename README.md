아두이노 코드
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// BLE service and characteristic UUIDs
#define SERVICE_UUID        "37C4E592-77F4-2C36-8BE2-6E5456E6E2CA"
#define CHARACTERISTIC_UUID "00001111-0000-1000-8000-00805f9b34fb"

// BLE server and characteristic declaration
BLECharacteristic *pCharacteristic;

// Callback class for the BLE characteristic
class MyCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) override {
    // This callback is triggered when the characteristic value is updated
    String value = pCharacteristic->getValue().c_str(); // Get the value as a std::string
    String message = String(value.c_str()); // Convert std::string to Arduino String for easier handling
    Serial.print("Received Value: ");
    Serial.println(message);  // Display the received message

    // Check if the received message contains a specific command
    if (message == "OK") {
      Serial.println("Received OK message!");
      // 시리얼 모니터에 부저 울리는 동작 대신 메시지 출력
      Serial.println("Buzzer would be triggered now (simulated).");
    } else {
      // Process other messages
      Serial.println("Message received from BLE: " + message);
    }
  }
};

void setup() {
  Serial.begin(115200);

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
  // Add any additional BLE communication or handling code here
}
