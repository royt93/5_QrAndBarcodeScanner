# Test Plan — QR Code Customization (màu & logo)

> **Cập nhật:** 2026-06-22 | **Feature:** `feature_qr_customization` | **Complexity:** High

---

## Unit Tests (`app/src/test/`)

### `QrRendererTest.kt`

| #   | Test                                   | Input                                                              | Expected                                         |
|-----|----------------------------------------|--------------------------------------------------------------------|--------------------------------------------------|
| U1  | Render QR đen trắng mặc định           | content="Hello", size=512, fgColor=BLACK, bgColor=WHITE, logo=null | Bitmap 512x512, corner pixels = WHITE, scannable |
| U2  | Render QR với màu                      | fgColor=BLUE, bgColor=YELLOW                                       | Bitmap pixels ở data modules = BLUE              |
| U3  | Render QR không crash với content rỗng | content=""                                                         | Không crash, return null hoặc empty bitmap       |
| U4  | Render QR với logo                     | logo=small 64x64 png, errorCorrection=H                            | Logo tại center, QR vẫn scannable                |
| U5  | Logo quá lớn bị scale down             | logo=256x256 trên QR=512                                           | Logo bị scale xuống ≤30% diện tích QR            |
| U6  | Error correction H khi có logo         | logo != null                                                       | `ErrorCorrectionLevel.H` được dùng tự động       |
| U7  | Error correction L khi không có logo   | logo = null, user chọn L                                           | `ErrorCorrectionLevel.L`                         |
| U8  | Background transparent                 | bgColor=TRANSPARENT                                                | Bitmap với alpha channel                         |
| U9  | QR size tối thiểu                      | size=64                                                            | Render thành công, không crash                   |
| U10 | QR size quá lớn                        | size=4096                                                          | Không OOM, render hoặc clamp xuống max           |
| U11 | Scannable sau khi thêm màu             | Render với random color                                            | Kết quả decode đúng content ban đầu              |
| U12 | Scannable sau khi thêm logo 30%        | Logo che 30% QR, error H                                           | Decode thành công                                |

### `QrCustomizerStateTest.kt`

| #   | Test                                     | Input                        | Expected                                           |
|-----|------------------------------------------|------------------------------|----------------------------------------------------|
| U13 | State default                            | Tạo state mới                | fgColor=BLACK, bgColor=WHITE, logo=null, ecLevel=M |
| U14 | updateFgColor                            | Chọn màu RED                 | State.fgColor = RED, dirty = true                  |
| U15 | updateLogo gán đúng                      | Bitmap 64x64                 | State.logo = bitmap                                |
| U16 | clearLogo                                | logo đang có → clearLogo()   | State.logo = null, ecLevel reset về M              |
| U17 | ecLevel tự nâng H khi có logo            | Set logo → ecLevel           | ecLevel = H tự động                                |
| U18 | ecLevel không bị force khi không có logo | logo = null, set ecLevel = L | ecLevel = L (không bị override)                    |

---

## Widget Tests / Instrumented Tests (`app/src/androidTest/`)

### `QrCustomizerFragmentTest.kt`

| #   | Test                               | Scenario                        | Expected                                         | Priority |
|-----|------------------------------------|---------------------------------|--------------------------------------------------|----------|
| W1  | Preview cập nhật khi chọn màu      | Tap màu BLUE trong color picker | QR preview reload với foreground BLUE            | High     |
| W2  | Preview cập nhật khi chọn bg       | Chọn background YELLOW          | Preview reload với background YELLOW             | High     |
| W3  | Preview cập nhật realtime          | Drag color picker slider        | Preview re-render kèm 300ms debounce             | Medium   |
| W4  | Add logo từ Gallery                | Tap "Chọn logo" → pick image    | Logo xuất hiện ở center preview                  | High     |
| W5  | Remove logo                        | Tap X trên logo preview         | Logo bị xóa, ecLevel không còn force H           | High     |
| W6  | Error correction selector          | Tap L/M/Q/H                     | Selector cập nhật, preview re-render             | Medium   |
| W7  | ecLevel H auto-select khi add logo | Add logo                        | H được auto-chọn, UI selector reflect đúng       | High     |
| W8  | Cảnh báo khi logo + ecLevel < H    | Chọn logo → manually chọn L/M/Q | Toast/Snackbar: "Khuyến nghị dùng H khi có logo" | Medium   |
| W9  | Save QR PNG vào Gallery            | Tap "Lưu"                       | File PNG được lưu vào Pictures/QRScanner         | High     |
| W10 | Save QR share                      | Tap "Chia sẻ"                   | Share intent với PNG                             | Medium   |
| W11 | Reset về default                   | Tap "Reset"                     | fgColor=BLACK, bgColor=WHITE, logo=null          | Medium   |

### `QrCustomizerAccessibilityTest.kt`

| #   | Test                      | Expected                                    |
|-----|---------------------------|---------------------------------------------|
| W12 | Color picker accessible   | Content description thông báo màu đang chọn |
| W13 | Logo button accessible    | "Thêm logo" / "Xóa logo" rõ ràng            |
| W14 | Preview image description | "Mã QR xem trước với màu {x}"               |

---

## Integration Tests

| #  | Test                          | Flow                                 | Expected                                        |
|----|-------------------------------|--------------------------------------|-------------------------------------------------|
| I1 | Custom QR → Save → Gallery    | Tạo QR URL màu xanh + logo → Lưu     | File tồn tại trong Gallery, có alpha đúng       |
| I2 | Custom QR → Scan lại          | Tạo QR URL có màu → Scan bằng app    | App decode đúng URL gốc                         |
| I3 | Custom QR với logo → Scan lại | Tạo QR + logo 30% → Scan             | Decode đúng                                     |
| I4 | Custom QR → Share             | Tạo QR → Share → nhận trên app khác  | Image quality đủ để scan                        |
| I5 | Rotation mid-customization    | Xoay ngang trong khi đang chọn màu   | State không bị mất (ViewModel survive rotation) |
| I6 | Memory khi render QR lớn      | Render nhiều lần liên tiếp 1024x1024 | Không OOM, Bitmap được recycle                  |

---

## Performance Tests

| #  | Test                              | Scenario             | Expected                                  |
|----|-----------------------------------|----------------------|-------------------------------------------|
| P1 | Render time QR 512x512            | 1 lần render         | < 100ms trên mid-range device             |
| P2 | Render time QR 1024x1024 với logo | 1 lần render         | < 300ms                                   |
| P3 | Memory sau 10 lần render          | Render loop 10x      | Không tăng heap liên tục (no bitmap leak) |
| P4 | Debounce color picker             | Drag nhanh 20 events | Chỉ trigger render 1 lần sau 300ms stop   |

---

## Edge Cases

| #  | Case                            | Expected                                  |
|----|---------------------------------|-------------------------------------------|
| E1 | Logo không phải PNG/JPG         | GIF, WebP                                 | Convert sang Bitmap, không crash |
| E2 | Logo với transparency           | PNG transparent logo                      | Composite đúng trên QR background |
| E3 | fgColor == bgColor              | Cả hai cùng màu WHITE                     | Cảnh báo: "QR không thể scan được" |
| E4 | Content rất dài (URL 500 chars) | URL dài + logo + ecLevel H                | QR dense nhưng vẫn render, cảnh báo scan might fail |
| E5 | Gallery permission denied       | Tap "Chọn logo" khi permission bị từ chối | Yêu cầu permission đúng flow, không crash |

---

## Manual Checklist

```
[ ] QR màu đỏ scan được bằng iPhone camera
[ ] QR màu đỏ scan được bằng chính app này
[ ] QR + logo scan được từ khoảng cách 20cm
[ ] PNG lưu ra Gallery có transparency đúng (bg transparent)
[ ] Màu sắc trên màn hình AMOLED (pure black bg)
[ ] Test trên màn hình có độ tương phản thấp (cẩn thận fgColor nhạt)
[ ] OOM test: render 20 lần liên tiếp không crash
```
