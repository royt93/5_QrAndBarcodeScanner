# [TODO] QR Code Customization — QR có màu & logo

> **Ưu tiên:** Medium | **Loại:** New exclusive feature | **Độ phức tạp:** High

## Mô tả

Cho phép user tùy chỉnh QR code: chọn màu foreground/background, thêm logo/icon vào tâm QR. Hiện tại QR chỉ là đen trắng
cơ bản.

## Lý do

- Trending mạnh trong các app tạo QR cao cấp
- User dùng QR cho business card, social link muốn QR có brand riêng
- Tạo điểm khác biệt vs app cạnh tranh

## Scope

- [ ] Color picker cho foreground color (modules) và background
- [ ] Error correction level selector (L/M/Q/H) — H cho phép logo ~30% che
- [ ] Import logo từ Gallery hoặc chọn icon preset (10-15 icon cơ bản)
- [ ] Preview realtime khi chọn màu/logo
- [ ] Save QR với màu vào Gallery (PNG với alpha nếu bg transparent)

## Files cần tạo/sửa

- `feature/tabs/create/QrCustomizerFragment.kt` — mới
- `feature/barcode/QrRenderer.kt` — mới (ZXing BitMatrix → custom Canvas render)
- `layout/f_qr_customizer.xml` — mới

## Test

Xem `doc/test/test_qr_customization.md`

## Note

Cần giữ QR readable: test scan lại sau khi thêm màu/logo. Error correction H là bắt buộc khi có logo.
