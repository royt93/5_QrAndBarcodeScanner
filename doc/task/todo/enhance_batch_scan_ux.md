# [TODO] Enhance — Batch Scan UX

> **Ưu tiên:** Medium | **Loại:** Enhancement | **Độ phức tạp:** Low-Medium

## Vấn đề

Batch Scan đã implement nhưng còn thiếu feedback UX quan trọng:

- Không có badge/counter hiển thị đã quét bao nhiêu item
- Không có visual feedback khi quét thành công từng item (chỉ có beep)
- Không có nút "Xóa item cuối" nếu quét nhầm
- Không có "Undo last scan"

## Enhancements

### 1. Scan Counter Badge (quan trọng nhất)

- Floating badge góc phải trên preview camera: "12 mã đã quét"
- Animate badge khi count tăng (pulse animation)

### 2. Scan Success Flash

- Flash nhẹ (white overlay 150ms) toàn màn hình sau mỗi lần quét thành công
- Kèm vibration pattern khác biệt (double pulse) để distinguish batch vs single

### 3. Undo Last Scan

- Snackbar "Đã quét: {value}" với nút "Hoàn tác" xuất hiện 3 giây
- Tap "Hoàn tác" → xóa item cuối khỏi batch list

### 4. Min Batch Warning

- Khi export với < 2 items → hiển thị dialog xác nhận "Chỉ có 1 mã, bạn chắc chắn muốn xuất?"

## Files cần sửa

- `feature/tabs/scan/FragmentScanBarcodeFromCamera.kt`
- `layout/f_scan_barcode_from_camera.xml` — thêm counter badge view
