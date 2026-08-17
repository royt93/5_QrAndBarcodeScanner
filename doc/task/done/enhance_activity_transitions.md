# [TODO] Enhance — Activity Transition Animations

> **Ưu tiên:** High | **Loại:** Enhancement | **Độ phức tạp:** Low

## Vấn đề

Chỉ có transition Splash→Main (`fade_in/fade_out`). Tất cả Activity launch khác dùng default Android (slide từ phải).
Thiếu nhất quán với iOS-style animation đã có ở dialogs.

## Touchpoints cần thêm transition

| Activity                           | Enter          | Exit (Back)     |
|------------------------------------|----------------|-----------------|
| ActivityBarcode (scan result)      | slide-in-up    | slide-out-down  |
| ActivityCreateBarcode              | slide-in-right | slide-out-right |
| ActivityCreateQrCodeAll            | slide-in-right | slide-out-right |
| ActivityBatchExportResult          | slide-in-up    | slide-out-down  |
| ChooseTheme/Language/Camera/Format | slide-in-right | slide-out-right |
| ActivityExportHistory              | slide-in-right | slide-out-right |
| ActivityBarcodeImage               | slide-in-right | slide-out-right |

## Cách implement

**Option A (đơn giản):** Thêm `overridePendingTransition()` trong `ActivityBase.kt:startActivity()` extension

```kotlin
fun Activity.startActivityWithTransition(intent: Intent) {
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}
```

Override `onBackPressed()` trong `ActivityBase` để thêm exit transition.

**Option B (Android 14+):** `ActivityOptions.makeSceneTransitionAnimation()` với shared element nếu cần.

## Anim files cần tạo

- `res/anim/slide_in_right.xml`
- `res/anim/slide_out_left.xml`
- `res/anim/slide_in_left.xml`
- `res/anim/slide_out_right.xml`
- `res/anim/slide_in_up.xml`
- `res/anim/slide_out_down.xml`

## Files cần sửa

- `feature/ActivityBase.kt` — override startActivity helpers
- Hoặc toàn bộ `startActivity(intent)` calls trong các file navigation

## Test

- Animation gaps: `doc/test/animation_test_cases.md` — Section 5
- Full test plan: `doc/test/test_activity_transitions.md`
