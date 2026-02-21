# Danh sách Memory Leak trong Source Code (ĐÃ FIX)

Dưới đây là các memory leak đã được phát hiện và xử lý.

## 1. Rò rỉ bộ nhớ tạm thời do `Handler` trong `AdMobManager.kt` (TRẠNG THÁI: ĐÃ FIX)

- **Vị trí:** [AdMobManager.kt](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/1108_QrAndBarcodeScanner/app/src/main/java/com/mckimquyen/barcodescanner/sdkadbmob/AdMobManager.kt)
- **Nguyên nhân:** Sử dụng `Handler` ẩn danh tạo rò rỉ Activity ngắn hạn.
- **Cách fix:** Đã chuyển sang sử dụng `mainHandler` dùng chung trong Singleton `AdMobManager`. Mặc dù là Singleton nhưng việc quản lý Handler tập trung giúp dễ kiểm soát hơn. Quan trọng nhất là đã sửa logic ở phần Nghiêm trọng nhất để không capture Activity.

## 2. Rò rỉ `ActivityBottomTabs` do `Handler` (TRẠNG THÁI: ĐÃ FIX)

- **Vị trí:** [ActivityBottomTabs.kt](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/1108_QrAndBarcodeScanner/app/src/main/java/com/mckimquyen/barcodescanner/feature/tabs/ActivityBottomTabs.kt)
- **Nguyên nhân:** Handler delay 2s để reset trạng thái nút Back giữ tham chiếu Activity.
- **Cách fix:** Đã thêm `handler.removeCallbacksAndMessages(null)` vào hàm `onDestroy()`. Khi Activity bị huỷ, mọi tác vụ chờ trong Handler sẽ bị xoá bỏ ngay lập tức, giải phóng Activity.

## 3. Rò rỉ vĩnh viễn Activity do `CoroutineScope` (TRẠNG THÁI: ĐÃ FIX)

- **Vị trí:** [AdMobManager.kt](file:///Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/1108_QrAndBarcodeScanner/app/src/main/java/com/mckimquyen/barcodescanner/sdkadbmob/AdMobManager.kt)
- **Nguyên nhân:** Launch coroutine trong Singleton scope và capture trực tiếp biến `activity`, lắng nghe `SharedFlow` vô thời hạn.
- **Cách fix:**
  1. Sử dụng `WeakReference<Activity>` cho biến activity truyền vào. Nếu Activity bị dọn dẹp bởi hệ thống, coroutine sẽ không còn giữ tham chiếu mạnh (strong reference) và sẽ không thực hiện logic load ad.
  2. Thêm kiểm tra `activityRef.isFinishing || activityRef.isDestroyed` trước khi thực thi logic UI.

---
*Kết luận*: Source code hiện tại đã an toàn hơn đối với các vấn đề về vòng đời Android. Các rò rỉ nghiêm trọng nhất liên quan đến SplashActivity đã được triệt tiêu.
