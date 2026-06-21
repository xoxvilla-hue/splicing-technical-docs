# 🍎 จับผลไม้ (Catch the Fruit)

เกมแอนดรอยด์ง่าย ๆ สนุก ๆ สำหรับเด็ก ๆ (เหมาะกับวัย 9 ขวบ)
สร้างด้วย **Kotlin + Jetpack Compose**

## วิธีเล่น
- **ลากนิ้วซ้าย-ขวา** เพื่อขยับตะกร้า 🧺
- รับ **ผลไม้** 🍎🍌🍇🍓 ให้ได้คะแนน
- หลบ **ระเบิด** 💣 ถ้ารับโดนจะเสียหัวใจ ❤️
- มี 3 หัวใจ หมดเมื่อไหร่ก็จบเกม แล้วเริ่มใหม่ได้
- ยิ่งคะแนนเยอะ ของจะตกเร็วขึ้น ท้าทายขึ้นเรื่อย ๆ

> ออกแบบให้เด็กเล่นสบายใจ: พลาดรับผลไม้ **ไม่เสียคะแนน** เสียหัวใจเฉพาะตอนรับระเบิดเท่านั้น

## โครงสร้างโปรเจกต์
```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/catchfruit/
│   ├── MainActivity.kt      # จุดเริ่มต้นแอป
│   └── GameScreen.kt        # ตรรกะเกมทั้งหมด (ลูปเกม, การชน, การวาด)
└── res/                     # ไอคอน, สี, ข้อความ
```

## วิธีบิลด์และรัน

### ทางที่ง่ายที่สุด — Android Studio
1. เปิด Android Studio (เวอร์ชันล่าสุด)
2. **Open** แล้วเลือกโฟลเดอร์โปรเจกต์นี้
3. รอให้ Gradle sync เสร็จ (จะโหลด Android Gradle Plugin อัตโนมัติ)
4. เสียบมือถือ Android (เปิด USB debugging) หรือสร้าง Emulator
5. กดปุ่ม **Run ▶**

### ทาง command line
ต้องมี Android SDK และตั้งค่า `local.properties` ให้ชี้ไปที่ SDK ก่อน:
```
sdk.dir=/path/to/Android/Sdk
```
จากนั้น:
```bash
./gradlew assembleDebug      # ได้ไฟล์ APK ที่ app/build/outputs/apk/debug/
./gradlew installDebug       # ติดตั้งลงเครื่องที่เชื่อมต่ออยู่
```

## ข้อมูลเทคนิค
- ภาษา: Kotlin 2.0.21
- UI: Jetpack Compose (Material 3)
- Android Gradle Plugin: 8.7.3
- `minSdk` 26 (Android 8.0), `targetSdk`/`compileSdk` 34
- ไม่ต้องใช้ไฟล์รูปภาพ — ใช้อิโมจิเป็นตัวละครทั้งหมด 🎨
