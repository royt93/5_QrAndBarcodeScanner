# Test Plan — Smart Security Shield (Anti-Phishing QR)

> **Cập nhật:** 2026-06-22 | **Feature:** `feature_smart_security_shield` | **Complexity:** Medium

---

## Unit Tests (`app/src/test/`)

### `UrlSafetyCheckerTest.kt`

| #   | Test                         | Input URL                                      | Expected Result                     |
|-----|------------------------------|------------------------------------------------|-------------------------------------|
| U1  | Domain whitelist — ngân hàng | `https://vietcombank.com.vn/...`               | `isSafe = true`                     |
| U2  | Domain whitelist — payment   | `https://momo.vn/...`                          | `isSafe = true`                     |
| U3  | Domain whitelist — google    | `https://google.com/search?q=test`             | `isSafe = true`                     |
| U4  | Domain not in whitelist      | `https://unknown-bank-vn.xyz/login`            | `isSafe = false`                    |
| U5  | HTTP (không HTTPS)           | `http://vietcombank.com.vn`                    | `isSafe = false` (HTTP luôn warn)   |
| U6  | Domain giả mạo (lookalike)   | `https://vietcombank.com.vn.malicious.xyz/...` | `isSafe = false`                    |
| U7  | Subdomain của whitelist      | `https://api.google.com/...`                   | `isSafe = true`                     |
| U8  | Subdomain của non-whitelist  | `https://safe.unknown.xyz`                     | `isSafe = false`                    |
| U9  | URL rỗng                     | `""`                                           | Không crash, không mở URL           |
| U10 | URL không hợp lệ             | `not-a-url`                                    | Không crash, return `isUrl = false` |
| U11 | IP address URL               | `http://192.168.1.1/login`                     | `isSafe = false`                    |
| U12 | Localhost                    | `http://localhost:8080`                        | `isSafe = false`                    |
| U13 | URL quá dài (>2048 chars)    | Long URL                                       | Không crash, truncate safely        |

### `SmartActionParserTest.kt`

| #   | Test              | Input                                           | Expected                                      |
|-----|-------------------|-------------------------------------------------|-----------------------------------------------|
| U14 | Parse VCard       | `BEGIN:VCARD\nFN:John\nTEL:+84...\nEND:VCARD`   | `ActionType.VCARD`, phone extracted           |
| U15 | Parse WiFi QR     | `WIFI:S:MyNetwork;T:WPA;P:password123;;`        | `ActionType.WIFI`, ssid + password extracted  |
| U16 | Parse OTP URI     | `otpauth://totp/user@example.com?secret=BASE32` | `ActionType.OTP`                              |
| U17 | Parse mailto      | `mailto:test@example.com?subject=Hello`         | `ActionType.EMAIL`, email extracted           |
| U18 | Parse phone       | `tel:+84912345678`                              | `ActionType.PHONE`                            |
| U19 | Parse plain URL   | `https://google.com`                            | `ActionType.URL`                              |
| U20 | Parse plain text  | `Hello World`                                   | `ActionType.TEXT`                             |
| U21 | VCard thiếu phone | `BEGIN:VCARD\nFN:John\nEND:VCARD`               | `ActionType.VCARD`, phone = null, không crash |

---

## Widget Tests / Instrumented Tests (`app/src/androidTest/`)

### `SafetyBottomSheetTest.kt`

| #   | Test                                | Scenario                                     | Expected                                          | Priority |
|-----|-------------------------------------|----------------------------------------------|---------------------------------------------------|----------|
| W1  | BottomSheet hiện với URL đáng ngờ   | Decode URL non-whitelist                     | SafetyBottomSheet xuất hiện, domain highlight đỏ  | High     |
| W2  | BottomSheet KHÔNG hiện với URL safe | Decode URL whitelist                         | Intent mở trực tiếp, không có BottomSheet         | High     |
| W3  | Nút "Tiếp tục mở" mở URL            | Tap "Tiếp tục"                               | `Intent.ACTION_VIEW` được gọi với đúng URL        | High     |
| W4  | Nút "Hủy" đóng sheet                | Tap "Hủy"                                    | Sheet dismiss, không mở URL                       | High     |
| W5  | Full URL hiển thị đúng              | URL dài `https://malicious.xyz/path?query=1` | Full URL hiển thị, không bị truncate giữa         | Medium   |
| W6  | Domain được highlight               | URL có domain                                | Domain (`malicious.xyz`) có màu đỏ/warning        | Medium   |
| W7  | VCard → nút Call + Save Contact     | Quét VCard QR                                | BottomSheet hiển thị 2 nút "Gọi" và "Lưu danh bạ" | High     |
| W8  | WiFi → nút Kết nối                  | Quét WiFi QR                                 | BottomSheet nút "Kết nối mạng này"                | High     |
| W9  | OTP URI → navigate ActivityOtp      | Quét OTP QR                                  | Điều hướng tới `ActivityOtp`, không cần confirm   | High     |
| W10 | mailto → nút Soạn email             | Quét mailto QR                               | Nút "Soạn email" + "Copy"                         | Medium   |
| W11 | Drag-to-dismiss an toàn             | Kéo sheet xuống                              | Sheet dismiss, URL không mở                       | High     |
| W12 | Tap scrim dismiss                   | Tap ngoài sheet                              | Sheet dismiss, URL không mở                       | High     |

### `SafetyBottomSheetAccessibilityTest.kt`

| #   | Test                       | Scenario             | Expected                                     |
|-----|----------------------------|----------------------|----------------------------------------------|
| W13 | Warning label accessible   | Sheet với warning đỏ | contentDescription rõ ràng cho screen reader |
| W14 | Buttons có đủ touch target | "Tiếp tục" và "Hủy"  | Tối thiểu 48dp touch target                  |

---

## Integration Tests

| #  | Test                                          | Flow                                               | Expected                                             |
|----|-----------------------------------------------|----------------------------------------------------|------------------------------------------------------|
| I1 | Scan URL → Safety check → mở browser          | Quét QR chứa `http://unknown.xyz` → tap "Tiếp tục" | Browser mở URL đúng                                  |
| I2 | Scan whitelist URL → no sheet                 | Quét QR `https://google.com/...`                   | Mở browser trực tiếp, không có sheet                 |
| I3 | Scan VCard → Save → danh bạ                   | Quét VCard → tap "Lưu"                             | Contact được tạo trong Contacts app                  |
| I4 | Scan WiFi → Connect                           | Quét WiFi QR (API 29+)                             | Prompt kết nối WiFi xuất hiện                        |
| I5 | HTTP URL luôn có cảnh báo                     | Quét `http://google.com`                           | Sheet hiện dù google.com whitelist (vì HTTP)         |
| I6 | History item detail → open URL → safety check | Tap URL trong history → tap "Mở"                   | Safety check cũng áp dụng từ history, không chỉ scan |

---

## Edge Cases Đặc Biệt

| #  | Case                           | Expected                                                    |
|----|--------------------------------|-------------------------------------------------------------|
| E1 | No internet khi tap "Tiếp tục" | Browser mở, handle no-internet ở browser                    |
| E2 | URL mở app khác (deep link)    | `ACTION_VIEW` vẫn chạy như bình thường                      |
| E3 | Very long domain               | Truncate display ≤80 chars, không overflow layout           |
| E4 | Unicode domain (IDN)           | Hiển thị punycode + Unicode, warning vì không whitelist     |
| E5 | URL chứa credentials           | `https://user:pass@domain.com` → warn, tách hiển thị domain |

---

## Manual Checklist

```
[ ] Quét QR giả mạo ngân hàng thực tế → cảnh báo đúng
[ ] Quét QR Google Wallet/Momo → mở thẳng không warn
[ ] Quét VCard trên danh thiếp vật lý → nút Call/Save
[ ] Quét WiFi QR → kết nối (test trên Android 10+ và Android 9-)
[ ] Test accessiblity với TalkBack bật
[ ] Sheet animation smooth (slide-up M3) giống DialogFragmentScanHelper
```

---

## Whitelist Test Data

```
SAFE (no sheet):
- https://vietcombank.com.vn
- https://techcombank.com
- https://momo.vn
- https://google.com

WARN (show sheet):
- https://vietcombank.com.vn.malicious.xyz  ← lookalike!
- http://momo.vn                            ← HTTP
- https://totally-fake-bank.vn
- https://bit.ly/suspicious               ← shortener
```
