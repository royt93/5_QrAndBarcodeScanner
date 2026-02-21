## 🔥 High Priority Features & Guidelines (Android Native)

- **UI & Views**: Sử dụng `ViewBinding`. Tuyệt đối **không** dùng `kotlin-android-extensions` (Kotlin Synthetics) vì đã bị deprecated.
- **Asynchronous**: Khuyến khích sử dụng `Kotlin Coroutines` và `Flow` thay thế dần cho `RxJava` để đồng bộ kiến trúc và giảm thiểu rò rỉ bộ nhớ.
- **Thông báo UI**: Sử dụng `Toast` hoặc `Snackbar` của Android Core/Material thay cho các thư viện ngoài không cần thiết.
- **Format**: Các ô text input liên quan đến nhập số lượng/tiền bạc hoặc data đặc biệt phải có format rõ ràng, tham khảo các screen khác để đồng bộ.
- **Animation**: Cần có animation đồng bộ giữa các màn hình.
- **Quality**: Phải đảm bảo code không gây memory leak, quản lý vòng đời (lifecycle) chặt chẽ, và hạn chế crash/bug tối đa. Thường xuyên check rò rỉ ở các async call.
