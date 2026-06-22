# [TODO] Smart Security Shield — Chống Phishing QR (Quishing)

> **Ưu tiên:** High | **Loại:** New exclusive feature | **Độ phức tạp:** Medium

## Mô tả

Phân tích URL từ QR trước khi mở trình duyệt. Hiển thị BottomSheet cảnh báo nếu domain đáng ngờ. Đây là tính năng **độc
quyền** — hầu hết app quét QR free trên thị trường đều auto-open URL không cảnh báo.

## Lý do ưu tiên cao

- Nạn "Quishing" (QR phishing) đang bùng nổ tại VN (dán QR giả lên QR thanh toán nhà hàng)
- User review thường đề cập lo ngại bảo mật khi quét QR
- Tạo differentiation rõ ràng so với app cạnh tranh

## Scope

### Phase 1: URL Safety Check

- [ ] Chặn `Intent.ACTION_VIEW` auto-launch sau khi decode URL
- [ ] Hiển thị `SafetyBottomSheet` với full URL (text + domain highlight)
- [ ] Nút "Tiếp tục mở" (secondary) + "Hủy" (primary thoát)
- [ ] Nếu domain match whitelist → skip cảnh báo, mở thẳng
- [ ] Nếu domain suspect → hiển thị icon ⚠️ + text đỏ "Cảnh báo: Domain không quen thuộc"

### Phase 2: Smart Action Parser (tận dụng thư viện sẵn có)

- [ ] `ez-vcard` VCard/MeCard → show nút **Gọi** + **Lưu danh bạ**
- [ ] WiFi QR schema → nút **Kết nối mạng** 1 chạm
- [ ] OTP URI (`otpauth://`) → auto-navigate tới `ActivityOtp`
- [ ] Email (`mailto:`) → nút **Soạn email** + **Copy địa chỉ**

## Files cần tạo/sửa

- `feature/tabs/scan/SafetyBottomSheetFragment.kt` — mới
- `feature/tabs/scan/UrlSafetyChecker.kt` — mới (domain whitelist logic)
- `feature/tabs/scan/FragmentScanBarcodeFromCamera.kt` — thêm intercept URL
- `feature/barcode/ActivityBarcode.kt` — thêm intercept URL action
- `layout/bs_url_safety.xml` — mới

## Whitelist domain sẵn có (gợi ý)

```
vietcombank.com.vn, techcombank.com, mbbank.com.vn, vpbank.com.vn,
momo.vn, zalopay.vn, vnpay.vn, google.com, apple.com, facebook.com,
youtube.com, tiktok.com, shopee.vn, lazada.vn
```
