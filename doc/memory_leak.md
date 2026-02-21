# Danh sách Memory Leak và Fix Logic Initialization (ĐÃ FIX TẤT CẢ)

Bản cập nhật này giải quyết rò rỉ bộ nhớ và lỗi logic khiến Quảng cáo App Open không hiển thị trên Splash Screen.

## 1. Rò rỉ vĩnh viễn Activity & Lỗi Race Condition (Mức độ: Rất Cao - ĐÃ FIX)

- **Vấn đề:**
    1. `EventBus` dùng `SharedFlow` không có replay khiến Splash screen dễ bị lỡ mất sự kiện "Init Success" nếu AdMob khởi tạo quá nhanh.
    2. Logic `collect` coroutine rò rỉ Activity vĩnh viễn (đã fix ở bước trước).
    3. Việc sử dụng `collectLatest` cũ hoặc `collect` mới không khéo léo có thể gây ra nhiều lần load ad hoặc treo máy.
- **Cách fix mới nhất:**
    1. Chuyển `EventBus` sang `MutableStateFlow(false)` để đảm bảo mọi subscriber luôn nhận được trạng thái mới nhất ngay cả khi join muộn.
    2. Sử dụng `EventBus.eventFlow.first { it == true }` trong `initSplashScreen`. Hàm này sẽ tạm dừng coroutine cho đến khi AdMob init xong thì thực thi logic load ad duy nhất 1 lần, sau đó tự huỷ.

## 2. Treo Splash Screen do logic `isAppOpenLoading` (ĐÃ FIX)

- **Vấn đề:** Trong `loadAppOpenAd`, nếu `isAppOpenLoading` đang là `true` và ở chế độ `DEBUG`, logic cũ không thực hiện `return` mà cũng không gọi callback, khiến Splash screen bị treo vô hạn.
- **Cách fix:** Đã chuẩn hoá lại logic kiểm tra trạng thái load. Nếu đang load hoặc ad còn hiệu lực thì sẽ callback ngay lập tức (`onAdLoaded(false)`) sau 1s để Splash screen có thể đi tiếp vào Main.

## 3. Rò rỉ bộ nhớ từ Handler (ĐÃ FIX)

- **Vị trí:** `AdMobManager.kt` và `ActivityBottomTabs.kt`.
- **Cách fix:** Đã dọn dẹp Handler trong `onDestroy` và sử dụng central Handler trong singleton để tránh capture context bừa bãi.

---
**Tổng kết:** Ứng dụng hiện tại đã giải quyết được cả về rò rỉ bộ nhớ lẫn tính ổn định của luồng khởi tạo quảng cáo. Luồng dữ liệu qua `StateFlow` đảm bảo Splash screen luôn nhận được tín hiệu để chuyển trang.
