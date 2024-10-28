# SmartUmbrella

<ESP32 Arduino 세팅>
보드 관리자 URL : https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
등록 후 보드 매니저에서 esp32 설치 후 보드 LOLIN D32 설정
아래의 소스로 보드에 업로드
  
#include <BLEDevice.h>
#include <BLEServer.h>

void setup() {
  Serial.begin(115200);

  // BLE 장치 초기화 및 서버 시작
  BLEDevice::init("ESP32_BLE_Test");
  BLEServer *pServer = BLEDevice::createServer();

  // BLE 광고 시작
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->start();

  Serial.println("BLE Advertising Started. Scan for 'ESP32_BLE_Test'.");
}

void loop() {
  // 메인 루프에서 별도 작업 필요 없음
}
