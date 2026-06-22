# [TODO] Predictive Back Gesture — Android 14+

> **Ưu tiên:** Low | **Loại:** New feature / Platform compliance | **Độ phức tạp:** Low

## Vấn đề

Android 14 (API 34) thêm Predictive Back Gesture: kéo từ cạnh màn hình sẽ preview màn hình sau khi back trước khi
release. App hiện chưa opt-in vào tính năng này — dùng deprecated `onBackPressed()`.

## Enhancement

- Enable `android:enableOnBackInvokedCallback="true"` trong `AndroidManifest.xml`
- Migrate `onBackPressed()` → `OnBackPressedDispatcher` + `OnBackPressedCallback` trong `ActivityBase.kt`
- Các màn hình có custom back logic (OTP, Batch Scan đang active) → đăng ký callback riêng

## Files cần sửa

- `AndroidManifest.xml` — thêm attribute vào `<application>`
- `feature/ActivityBase.kt` — migrate onBackPressed pattern
- `feature/tabs/scan/FragmentScanBarcodeFromCamera.kt` — batch mode active guard
- `feature/barcode/otp/ActivityOtp.kt` — nếu có countdown running

## Note

Chỉ impact Android 14+, backward compatible hoàn toàn. Thêm `targetSdkVersion 34+` là điều kiện tiên quyết (hiện tại
đang target 36 ✅).
