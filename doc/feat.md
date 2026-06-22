# Đề Xuất Tính Năng Mới — QR & Barcode Scanner

> **Cập nhật:** 2026-06-22  
> **Legend:** ✅ Implemented · 🟡 In progress · 📋 Picked · ⏸️ Deferred · ❌ Skipped · 💭 Ideas

---

## ✅ 1. Batch Scanning & CSV Export

**Trạng thái:** Implemented

**Mô tả:** Quét liên tục nhiều mã mà không cần back về màn hình kết quả. Sau khi gom đủ số lượng, xuất ra file CSV và
chia sẻ.

**Đã implement:**

- `FragmentScanBarcodeFromCamera.kt` — batch mode toggle + auto-append to list sau mỗi lần quét
- `DialogFragmentScanHelper.kt` — onboarding BottomSheet M3 giải thích batch mode
- `ActivityBatchExportResult.kt` — màn hình kết quả sau export (có animation scale_pop + slide_up_fade)
- Layout: `dialog_batch_scan_helper.xml`, `a_batch_export_result.xml`

---

## 📋 2. Smart Security Shield — Chống Phishing QR (Quishing)

**Trạng thái:** Picked (chưa implement)

**Mô tả:** Phân tích URL từ QR trước khi mở trình duyệt. Hiển thị cảnh báo nếu domain đáng ngờ.

**Kế hoạch:**

- Chặn `Intent.ACTION_VIEW` auto-open bằng một bước confirm BottomSheet
- Phân tích domain: whitelist tài chính/banking quen thuộc → OK, còn lại → show cảnh báo đỏ
- Tận dụng `ez-vcard` + `kotlin-onetimepassword` cho Smart Action Parser:
    - VCard/MeCard → nút **Call** / **Save Contact** to tướng
    - WiFi QR → nút **Kết nối mạng ngay**
    - OTP URI → điều hướng thẳng đến `ActivityOtp`

---

## 💭 3. QR Code Customization — QR có màu & logo

**Trạng thái:** Ideas

**Mô tả:** Thêm màu sắc và logo vào QR khi tạo. Hiện tại QR chỉ là đen trắng cơ bản.

**Hướng implement:** Custom renderer bằng ZXing + Canvas overlay logo, color fill cho data modules.

---

## 💭 4. Predictive Back Gesture (Android 14+)

**Trạng thái:** Ideas

**Mô tả:** Hỗ trợ predictive back animation tiêu chuẩn Android 14+. Hiện tại không có `OnBackPressedCallback` pattern ở
hầu hết Activities.

---

## 💭 5. Live QR Preview while typing

**Trạng thái:** Ideas

**Mô tả:** Sinh QR realtime khi user gõ nội dung trong form tạo QR, thay vì phải nhấn "Create".
