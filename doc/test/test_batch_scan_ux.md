# Test Plan — Batch Scan UX Enhancements

> **Cập nhật:** 2026-06-22 | **Feature:** `enhance_batch_scan_ux` | **Complexity:** Low-Medium

---

## Unit Tests (`app/src/test/`)

### `BatchScanViewModelTest.kt` (hoặc presenter/state class tương đương)

| #  | Test                                       | Input                                | Expected                              |
|----|--------------------------------------------|--------------------------------------|---------------------------------------|
| U1 | addItem tăng count                         | Thêm "ABC123" vào batch list         | `batchCount == 1`, list chứa "ABC123" |
| U2 | addItem không duplicate trong window 500ms | Quét "ABC123" 2 lần liên tiếp <500ms | Count vẫn = 1 (dedup)                 |
| U3 | addItem duplicate sau 500ms OK             | Quét "ABC123", delay 600ms, quét lại | Count = 2                             |
| U4 | undoLastScan giảm count                    | addItem 3 lần → undoLast             | Count = 2, item cuối bị xóa           |
| U5 | undoLastScan khi list rỗng                 | Gọi undo trên list rỗng              | Không crash, count = 0                |
| U6 | canExport khi < 1 item                     | List rỗng                            | `canExport = false`                   |
| U7 | canExport khi >= 1 item                    | List có 1 item                       | `canExport = true`                    |
| U8 | clearAll xóa hết                           | List có 5 item → clearAll            | Count = 0, list empty                 |

### `BatchCounterBadgeTest.kt`

| #   | Test                       | Input        | Expected        |
|-----|----------------------------|--------------|-----------------|
| U9  | Badge text format đúng     | count = 0    | Ẩn badge (GONE) |
| U10 | Badge text format          | count = 1    | "1 mã"          |
| U11 | Badge text format          | count = 99   | "99 mã"         |
| U12 | Badge text format overflow | count = 100+ | "99+ mã"        |

---

## Widget Tests / Instrumented Tests (`app/src/androidTest/`)

### `BatchScanCameraFragmentTest.kt`

| #   | Test                            | Scenario                                  | Expected                                       | Priority |
|-----|---------------------------------|-------------------------------------------|------------------------------------------------|----------|
| W1  | Badge ẩn khi batch mode off     | Default camera mode                       | Badge GONE                                     | High     |
| W2  | Badge hiện khi batch mode on    | Toggle batch mode ON                      | Badge VISIBLE, text "0 mã"                     | High     |
| W3  | Badge animate khi quét          | Mock scan result "12345" trong batch mode | Badge pulse animation, count +1                | High     |
| W4  | Snackbar "Đã quét" xuất hiện    | Mock scan result trong batch mode         | Snackbar với "Đã quét: 12345" + nút "Hoàn tác" | High     |
| W5  | Snackbar dismiss sau 3s         | Không tap gì                              | Snackbar tự biến mất sau 3 giây                | Medium   |
| W6  | Undo hoạt động                  | Tap "Hoàn tác" trên Snackbar              | Count giảm 1, item cuối bị xóa                 | High     |
| W7  | Screen flash khi quét           | Mock scan result batch mode               | White overlay flash 150ms                      | Medium   |
| W8  | Vibration pattern double pulse  | Mock scan result batch mode               | Vibration khác với single scan                 | Low      |
| W9  | Export warning khi 1 item       | Export với 1 item                         | Dialog cảnh báo "Chỉ có 1 mã" xuất hiện        | Medium   |
| W10 | Export bình thường khi 2+ items | Export với 2 items                        | Không có dialog cảnh báo, export trực tiếp     | High     |

### `BatchScanAccessibilityTest.kt`

| #   | Test                           | Scenario              | Expected                                            |
|-----|--------------------------------|-----------------------|-----------------------------------------------------|
| W11 | Badge content description đúng | Badge hiển thị "5 mã" | contentDescription = "Đã quét 5 mã" (screen reader) |
| W12 | Snackbar accessible            | Snackbar Hoàn tác     | Nút accessible qua TalkBack                         |

---

## Integration Tests

| #  | Test                               | Flow                                          | Expected                                  |
|----|------------------------------------|-----------------------------------------------|-------------------------------------------|
| I1 | Batch → Export → Result đúng count | Quét 3 mã → Export                            | ActivityBatchExportResult hiển thị "3 mã" |
| I2 | Undo → Export count giảm           | Quét 3 mã → undo 1 → export                   | Export 2 mã, CSV có 2 rows                |
| I3 | Batch mode persist qua background  | Batch mode ON → background app → foreground   | Batch mode state giữ nguyên               |
| I4 | Batch + Ad interstitial            | Export → interstitial show → dismiss → result | Count đúng trên result screen             |

---

## Manual Checklist

```
[ ] Badge visible trên cả màn hình sáng và tối
[ ] Flash không quá chói (không gây khó chịu)
[ ] Vibration double pulse khác biệt rõ với single scan vibration
[ ] Snackbar không che camera preview quá nhiều
[ ] Test với scan nhanh liên tiếp (mỗi 0.5s)
[ ] Test undo sau khi close snackbar (undo không được nữa)
```
