# KẾ HOẠCH NÂNG CẤP AD SYSTEM (AppLovin MAX / AdMob)

## 1. Phân Tích Hệ Thống Ad Hiện Tại

Ứng dụng hiện tại đang sử dụng tích hợp nội bộ thủ công thông qua singleton `AdMobManager.kt` (
`com.mckimquyen.barcodescanner.sdkadbmob.AdMobManager`).

### Danh sách Touchpoints đang có sẵn:

1. **Chuyển đổi Ad SDK**: Quản lý tại `RApp.kt` dựa trên cờ `BuildConfig`.
2. **App Open Ad (Splash)**: Load ngầm và show sau khi delay ở `SplashActivity`.
3. **App Open Ad (Background Return)**: **Đang thiếu sót!** Mã nguồn hiện tại dường như chỉ gọi ở Splash và các logic
   AppOpen ở Background hoàn toàn không được bắt lifecycle của `Application`. (Điều này vi phạm UX quảng cáo vì lỡ nhiều
   lượt impression từ user mở lại app).
4. **Banner Ad**: Nằm ở `ActivityBottomTabs` (Giao diện chính).
5. **Interstitial Ad**:
    * `SettingsFragment`: Các nút -> `ChooseTheme`, `ChooseLanguage`, `ChooseCamera`, `SupportedFormats`,
      `ChooseSearchEngine`.
    * `FragmentCreateBarcode`: Các nút -> `Show All QR Code`, `Show All Barcode`.
6. **Rewarded Ad**: **Không tìm thấy** bất kỳ touchpoint nào áp dụng loại Ad này trong codebase hiện tại. Tuân thủ theo
   rule: *Chỉ thay thế những ad hiện có, không vẽ tự dậm thêm touchpoint mới*. Sẽ chỉ setup Key chứ không chèn logic
   show.

### Vi pham & Điểm yếu của hệ thống cũ:

1. **Vòng lập quảng cáo (Policy Violation)**: Cấu hình gọi `showInterstitial` trên mỗi click chuyển setting. Nếu user
   nhấn liên tục thì quảng cáo sẽ spam liên tục → **Rất dễ ăn gậy Invalid Traffic của Google/AppLovin**.
2. **Memory Leak**: Đã được tìm thấy từ các phase trước (`interstitialListener`).
3. **Trống (Orphan) App Open Ad Lifecycle**: Không bắt sự kiện resume từ màn hình nền nên bị mất sạch impression ở
   Foreground session tiếp theo.

---

## 2. Lợi Ích Sau Khi Migrate Sang `_260321AdmobWrapper`

* **AdSafetyConfig Tự Động**: Chống spam, chống nhấp chuột không hợp lệ (Session Cap, Hourly Cap, Daily Cap, tối thiểu
  60s giữa 2 lần hiện Interstitial). Đảm bảo 100% tuân thủ Policy.
* **Background Lifecycle Tự Động**: SDK mới tự gắn `ProcessLifecycleOwner` để gọi App Open Ad mỗi khi resume app > 5s.
* **Kiến trúc sạch, tập trung**: Loại bỏ được biến listener lỏng lẻo dễ leak.

---

## 3. Các Bước Triển Khai Step-by-Step

### Bước 1: Khai báo Phụ Thuộc (Dependency & AndroidManifest)

1. Trong `settings.gradle.kts`: Cập nhật `maven { url = uri("https://jitpack.io") }`
2. Trong root `app/build.gradle.kts`:
    * Xóa những dependency AdMob cũ tĩnh không cần thiết nếu có (wrapper lo việc này).
    * Bổ sung `implementation("com.github.royt93:AdmobWrapper:1.1.1")`.
    * Cấu hình các Constant (AppLovin key) vào `buildConfigField`.
3. Trong `AndroidManifest.xml`: Đẩy AppLovin meta-data `applovin.sdk.key` từ provider key đã cung cấp.

### Bước 2: Dọn Dẹp Code Cũ

* Xóa package nội bộ `com.mckimquyen.barcodescanner.sdkadbmob.AdMobManager`.
* Xóa các cờ logic lắt nhắt của ad ở trong `RApp.kt`.
* Dọn dẹp các implement của `AdMobManager.InterstitialAdListener` ở các Fragment.

### Bước 3: Khởi Tạo Mới Tại Application `RApp`

* Tạo cục `AdSdkConfig` với: `IS_ENABLE_ADMOB = false` (Bắt buộc theo AppLovin).
* Nạp provider info: SDK Key và ID cấp sẵn.
* Đẩy Lifecycle: `AdManager.setConfig() -> earlyInit() -> AppLovinSdk.getInstance().initializeSdk()`.

### Bước 4: Migrate App Open Ad (Splash Screen)

* Thay thế logic rườm rà ở `SplashActivity` thành vỏn vẹn một call wrapper duy nhất là:
  `AdManager.initSplashScreen(...)`, auto recovery cho app load.

### Bước 5: Migrate Banner Ad (`ActivityBottomTabs`)

* Layout cũ (chứa TextView ảo tuỳ chỉnh) sẽ map đúng ID view cho `AdManager.loadBanner`.
* **Quan trọng:** Phải gắn LifeCycle `onResume`, `onPause`, `onDestroy` vào Activity cho Banner để chống impression ma (
  Chữa thẳng vào vi phạm policy Ad gây Invalid Activity).

### Bước 6: Migrate Interstitial

* Ở `SettingsFragment` và `FragmentCreateBarcode`: Cắt bỏ đoạn gán listener chết người. Thay bằng logic check gọn: Gọi
  `AdManager.loadInterstitial` ở `onViewCreated` (để load mồi trước). Truyền inline callback
  `AdManager.showInterstitial(activity) { success -> performAction() }`.

---

## 4. Danh Sách Key Đã Xác Nhận Sẵn Sàng (AppLovin)

* **SDK Key:** `e75FnQfS9XTT...` (✓ Đã có)
* **Banner ID:** `1cfc3df1cd94473a` (✓ Đã có)
* **Interstitial ID:** `2f81cf4ca355cdac` (✓ Đã có)
* **App Open ID:** `5baea98f19f13cf2` (✓ Đã có)
* **Reward ID:** `260b02ff9724a4ec` (✓ Đã có, sẽ đặt chung vào BuildConfig dù app chưa xài tới).

## Trạng Thái Thông Tin Phản Hồi:

Hiện tại theo tài liệu của Wrapper cũng như logic của base code, **TẤT CẢ các ID yêu cầu đã có đủ**. Tôi **chưa cần thêm
config key nào khác**.

> Hãy phê duyệt plan tại `implementation_plan.md` để tôi bắt đầu tháo tung hệ thống Ad cũ của bạn và di dời một cách an
> toàn!
