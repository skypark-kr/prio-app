# 지금 뭐부터

할 일 이름만 적으면 AI가 시간·에너지·주기를 추정하고, 타이머 기록으로 학습해서 지금 할 일 하나를 추천해주는 안드로이드 앱.

## APK 받기
Actions 탭 → 최근 "Build APK" 실행 → Artifacts의 `prio-app-debug-apk` 다운로드 → 압축 해제 후 `app-debug.apk` 설치.

## 구조
- `app/src/main/assets/index.html` — 앱 본체(HTML/JS)
- `app/src/main/java/.../MainActivity.java` — WebView 래퍼
