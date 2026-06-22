# Animation Test Cases — QR & Barcode Scanner

> **Cập nhật:** 2026-06-22  
> **Phạm vi:** Toàn bộ animation hiện có trong project  
> **Quy ước:** ✅ Pass · ❌ Fail · ⚠️ Partial

---

## 1. Splash → Main Transition

**File:** `SplashActivity.kt:goToMain()` → `overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)`

| #   | Test Case                                  | Bước thực hiện                       | Expected                                           | Priority |
|-----|--------------------------------------------|--------------------------------------|----------------------------------------------------|----------|
| 1.1 | Cold start — transition xuất hiện          | Mở app lần đầu (cold start)          | Splash fade out, Main fade in mượt mà ~300ms       | High     |
| 1.2 | Cold start với Ad — transition sau ad      | App Open Ad hiện, đóng, rồi vào Main | Transition chỉ xảy ra 1 lần sau khi ad dismiss     | High     |
| 1.3 | Cold start — không giật, không flash trắng | Splash → Main transition             | Không có frame trắng/đen giữa hai màn hình         | High     |
| 1.4 | Warm start (background → foreground)       | Background app rồi mở lại            | Không chạy lại Splash transition                   | Medium   |
| 1.5 | Slow device — MIN_SPLASH_DURATION=1000ms   | Khởi chạy trên emulator chậm         | Splash tồn tại ít nhất 1 giây trước khi transition | Medium   |

---

## 2. Dialog Enter/Exit (iOS Spring Style)

**File:** `styles.xml:DialogAnimationiOS` → `AppAlertDialogTheme`, `DialogTheme`  
**Anim enter:** `anim_dialog_enter.xml` — scale 0.75→1.0 (420ms overshoot) + fade 0→1 (280ms)  
**Anim exit:** `anim_dialog_exit.xml` — scale 1.0→0.85 (220ms accelerate) + fade 1→0 (200ms)

| #   | Test Case                               | Bước thực hiện                            | Expected                                             | Priority |
|-----|-----------------------------------------|-------------------------------------------|------------------------------------------------------|----------|
| 2.1 | Dialog enter — scale xuất phát từ 75%   | Mở bất kỳ AlertDialog trong app           | Dialog phóng to từ 75% → 100% với elastic bounce nhẹ | High     |
| 2.2 | Dialog enter — fade đồng thời với scale | Mở AlertDialog                            | Background mờ dần trong khi dialog scale             | High     |
| 2.3 | Dialog exit — thu nhỏ về 85%            | Dismiss dialog (bấm nút hoặc tap outside) | Dialog thu về 85% + fade out trong ~220ms            | High     |
| 2.4 | Dialog exit — không bị lag sau dismiss  | Sau khi dialog đóng                       | UI bên dưới phản hồi ngay, không freeze              | High     |
| 2.5 | AlertDialog trong dark theme            | Bật dark mode → mở dialog                 | Animation vẫn đúng, không bị flicker màu background  | Medium   |
| 2.6 | Dialog enter trên API 24 (min SDK)      | Test trên Android 7 device/emulator       | Animation smooth, không crash                        | Medium   |
| 2.7 | Rapid open/close                        | Mở dialog → dismiss ngay → mở lại         | Không bị animation overlap hoặc stuck                | Low      |
| 2.8 | Rotation during dialog                  | Xoay ngang khi dialog đang open           | Dialog re-appear đúng, không mất animation state     | Low      |

**Touchpoints cần test:**

- History → Delete confirmation dialog
- Settings → Language selection dialog
- Barcode detail → Save/Delete confirmation
- Batch Scan → Export confirmation

---

## 3. BottomSheet — Material3 Slide-Up

**File:** `DialogFragmentScanHelper.kt` → `BottomSheetDialogFragment` (theme: `BottomSheetM3`)  
**Animation:** Native M3 slide-up từ bottom, 28dp top rounded corners

| #   | Test Case                 | Bước thực hiện                                    | Expected                                                                  | Priority |
|-----|---------------------------|---------------------------------------------------|---------------------------------------------------------------------------|----------|
| 3.1 | Slide-up enter            | Bước vào Scan tab lần đầu → BottomSheet helper mở | Sheet trượt lên mượt từ bottom, corners 28dp rõ ràng                      | High     |
| 3.2 | Expanded ngay khi mở      | BottomSheet mở                                    | Sheet mở đầy đủ, không dừng ở half-expanded                               | High     |
| 3.3 | Dismiss bằng drag down    | Kéo sheet xuống                                   | Sheet slide-down smooth, dismiss hoàn toàn                                | High     |
| 3.4 | Dismiss bằng nút X        | Tap nút ✕ góc phải                                | Sheet dismiss với animation slide-down                                    | High     |
| 3.5 | Dismiss bằng nút "Got It" | Tap "Got It"                                      | Sheet dismiss, callback `onDismissCallback` được gọi                      | High     |
| 3.6 | Tap scrim bên ngoài       | Tap vùng tối bên ngoài sheet                      | Sheet dismiss với slide-down animation                                    | Medium   |
| 3.7 | Corner radius visible     | Mở sheet                                          | 28dp top rounded corners hiển thị đúng, không bị cut-off                  | Medium   |
| 3.8 | Scrim dimming             | Mở sheet                                          | Background dim (scrim) xuất hiện đồng thời với slide-up                   | Medium   |
| 3.9 | Dark theme corners        | Dark mode → mở sheet                              | Background color của sheet = `dialog_background_color`, không bị override | Medium   |

---

## 4. BatchExportResult — Success Animations

**File:** `ActivityBatchExportResult.kt:76-79`  
**Anim 1:** `anim_scale_pop.xml` → `imageViewSuccess` (scale 0→1, 500ms overshoot)  
**Anim 2:** `anim_slide_up_fade.xml` → `layoutContent` (translate Y 60dp→0, start 200ms delay, 500ms)

| #   | Test Case                              | Bước thực hiện                                   | Expected                                                                      | Priority |
|-----|----------------------------------------|--------------------------------------------------|-------------------------------------------------------------------------------|----------|
| 4.1 | Success icon scale-pop                 | Export batch scan → mở ActivityBatchExportResult | Checkmark icon bắt đầu từ 0% → scale đến >100% rồi về 100% (overshoot bounce) | High     |
| 4.2 | Content slide-up delay                 | Xem kết quả export                               | Content box xuất hiện muộn hơn icon 200ms, trượt lên từ 60dp                  | High     |
| 4.3 | Cả 2 animations chạy đồng thời         | Vào màn hình result                              | Icon scale-pop chạy trước, content slide-up chạy sau 200ms                    | High     |
| 4.4 | Duration đủ dài để user nhận ra        | Xem animation                                    | Scale-pop 500ms — user thấy bounce; slide-up 500ms — không bị quá chậm        | Medium   |
| 4.5 | Back và vào lại                        | Back → mở lại export result khác                 | Animation re-run đúng, không bị skip                                          | Medium   |
| 4.6 | Animation không block button tương tác | Trong khi animation chạy                         | Có thể tap "Open File" ngay cả khi animation chưa xong                        | Low      |

---

## 5. Activity Navigation Transitions (Gap — Chưa có)

**Hiện trạng:** Hầu hết Activity launch **không có** `overridePendingTransition`, dùng default Android slide-from-right.

| #   | Transition                             | File                           | Trạng thái                      | Priority |
|-----|----------------------------------------|--------------------------------|---------------------------------|----------|
| 5.1 | BottomTabs → ActivityBarcode           | `ActivityBarcode.kt`           | ❌ Không có transition tùy chỉnh | High     |
| 5.2 | BottomTabs → ActivityBarcodeImage      | `ActivityBarcodeImage.kt`      | ❌ Không có                      | Medium   |
| 5.3 | BottomTabs → ActivityCreateBarcode     | `ActivityCreateBarcode.kt`     | ❌ Không có                      | High     |
| 5.4 | BottomTabs → ActivityCreateQrCodeAll   | `ActivityCreateQrCodeAll.kt`   | ❌ Không có                      | High     |
| 5.5 | Any → ActivityExportHistory            | `ActivityExportHistory.kt`     | ❌ Không có                      | Medium   |
| 5.6 | Settings → ChooseTheme/Language/Camera | `SettingsFragment.kt`          | ❌ Không có                      | Medium   |
| 5.7 | Scan → ActivityBatchExportResult       | `ActivityBatchExportResult.kt` | ❌ Không có                      | Medium   |

> **Gợi ý fix:** Thêm `overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)` cho enter và
`anim_slide_in_left / anim_slide_out_right` cho back. Hoặc dùng `ActivityOptions.makeSceneTransitionAnimation()` cho
> Android 5+.

---

## 6. Tab Switch Animation (BottomNavigation)

**File:** `ActivityBottomTabs.kt`

| #   | Test Case                       | Bước thực hiện                | Expected                                   | Priority |
|-----|---------------------------------|-------------------------------|--------------------------------------------|----------|
| 6.1 | Tab switch — Scan ↔ Create      | Tap tab icon                  | Fragment switch không bị flicker           | Medium   |
| 6.2 | Tab switch — Create ↔ History   | Tap tab icon                  | Fragment switch smooth                     | Medium   |
| 6.3 | Tab switch — History ↔ Settings | Tap tab icon                  | Không có animation gap                     | Low      |
| 6.4 | Rapid tab switching             | Tap nhiều tab liên tiếp nhanh | Không crash, không fragment stack overflow | High     |

---

## 7. OTP Screen — Countdown (Enhancement)

**File:** `ActivityOtp.kt` — hiện chưa có animation countdown ring

| #   | Test Case          | Bước thực hiện         | Expected                                                 | Priority |
|-----|--------------------|------------------------|----------------------------------------------------------|----------|
| 7.1 | OTP digit update   | Mở OTP screen, chờ 30s | Mỗi lần digit thay đổi, text có fade hoặc flip animation | Low      |
| 7.2 | Countdown progress | Xem circular progress  | Ring thu dần từ 100% → 0% trong 30s                      | Low      |

---

## Checklist Chạy Test

```
[ ] 1. Cold start trên physical device
[ ] 2. Emulator API 24 (min SDK)
[ ] 3. Emulator API 34 (target SDK)
[ ] 4. Dark mode
[ ] 5. Light mode
[ ] 6. Landscape rotation
[ ] 7. Slow animation scale 5x (Developer Options → Window animation scale = 5)
[ ] 8. Animation disabled (Developer Options → all scales = off)
```

> **Khi animation bị tắt (Accessibility):** App phải vẫn dùng được bình thường — tất cả navigation/dialog đều hoạt động,
> chỉ là không có animation.
