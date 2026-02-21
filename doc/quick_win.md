# Quick Wins Implementation Status

### 1. 🚀 Migrate từ Kotlin Synthetics sang ViewBinding

- **Trạng thái:** Đang xử lý.
- **Chi tiết:** Đã khảo sát và phát hiện **57 file Kotlin** đang sử dụng `kotlinx.android.synthetic`. Do số lượng file quá lớn, việc chuyển đổi thủ công sẽ tốn rất nhiều thời gian và rủi ro sai sót sót. Đang chờ xác nhận phương án tự động hoá (dùng Python script) hoặc chuyển đổi theo từng cụm tính năng.

### 2. 🧹 Đồng bộ kiến trúc bất đồng bộ (Coroutines/Flow)

- **Trạng thái:** Chưa bắt đầu.
- **Chi tiết:** Kế hoạch loại bỏ `RxJava` và đồng bộ hoàn toàn với `Coroutines/Flow`.

### 3. 🎯 Dọn dẹp `doc/init.md` và chuẩn hoá version

- **Trạng thái:** **Hoàn thành ✅**
- **Chi tiết:**
  - Đã gỡ bỏ các tiêu chuẩn Flutter không liên quan trong `doc/init.md` và thay bằng Guideline chuẩn cho dự án Android Native (ViewBinding, Coroutines, v.v.).
  - Đã hạ `compileSdkVersion` và `targetSdkVersion` trong `app/build.gradle` từ bản thử nghiệm 36 xuống bản ổn định 34 để tránh lọt lỗi experimental.
