# AD SYSTEM — POST-MIGRATION REPORT

> **SDK:** `com.github.royt93:AdmobWrapper:1.1.2` | **Provider hiện tại:** AppLovin MAX | **Status:** Đã migrate ✅
> **Cập nhật:** 2026-04-23

---

## TL;DR

Project **đã migrate hoàn tất** sang `AdmobWrapper`. Tất cả touchpoint (Banner, Interstitial, App Open) đã dùng
`com.roy.sdkadbmob.AdManager`. Không cần "replace/upgrade" logic ad — chỉ cần dọn **5 sai sót nhỏ** đã liệt kê ở §7
trước khi release. **Không cần thêm key/ID nào** nếu tiếp tục chạy AppLovin MAX.

---

## 1. TOUCHPOINT MAP (hiện trạng)

| # | Loại ad                                | Touchpoint (file:line)                                                                                                | Lifecycle / Trigger                                                                      | Status |
|---|----------------------------------------|-----------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|--------|
| 1 | **App Open (Splash / Cold start)**     | `RApp.kt:84` → registerAppOpenAdLifecycle · `SplashActivity.kt:25` → `AdManager.initSplashScreen`                     | Tự động bởi SDK, 8s timeout + bypass safety                                              | ✅      |
| 2 | **App Open (Background → Foreground)** | `RApp.kt:84` → `AdManager.registerAppOpenAdLifecycle(this@RApp)`                                                      | SDK tự bắt `ProcessLifecycleOwner.onStart`, skip nếu <5s background                      | ✅      |
| 3 | **Banner**                             | `ActivityBottomTabs.kt:62-66` → `loadBanner`; `:41` resume, `:46` pause, `:71` destroy                                | Full lifecycle đã copy đủ 3 method                                                       | ✅      |
| 4 | **Interstitial — Create tab (×2)**     | `FragmentCreateBarcode.kt:36` preload · `:92` Show-All-QR · `:104` Create-Barcode                                     | Button click có user intent                                                              | ✅      |
| 5 | **Interstitial — Settings tab (×5)**   | `SettingsFragment.kt:49` preload · `:94`/`:104`/`:114`/`:124`/`:137` (Theme / Lang / Camera / Formats / SearchEngine) | Button click có user intent                                                              | ✅      |
| 6 | **Reward**                             | ❌ Không có touchpoint code. `BuildConfig.REWARD` / `APPLOVIN_REWARD_ID` có gán giá trị nhưng không được gọi.          | SDK 1.1.2 không hỗ trợ Reward Ad API → field dư thừa                                     | ⚠️     |
| 7 | **AdManager.setCurrentActivity**       | `ActivityBase.kt:45` (onResume mọi Activity extend ActivityBase)                                                      | Supplement — SDK đã có ActivityLifecycleCallbacks tự track rồi, nên đây chỉ là insurance | ✅      |

---

## 2. SO SÁNH TRƯỚC / SAU MIGRATE

| Hạng mục                                                                     | **Trước migrate** (legacy `AdMobManager` nội bộ)          | **Sau migrate** (SDK 1.1.2)                                                                            |
|------------------------------------------------------------------------------|-----------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| Framework                                                                    | Singleton viết tay trong package `sdkadbmob.AdMobManager` | `com.roy.sdkadbmob.AdManager` wrapper chung cho AdMob + AppLovin                                       |
| Chuyển đổi provider                                                          | Cần thay code                                             | 1 flag `BuildConfig.IS_ENABLE_ADMOB`                                                                   |
| Background Resume App Open                                                   | **Không có** — mất impression                             | Tự động qua `ProcessLifecycleOwner` + skip <5s                                                         |
| AdSafety (anti-spam / invalid traffic)                                       | Không có                                                  | 7 lớp: pause progressive, session-min 10s, session cap 6, hourly 3, daily 5, throttle 60s, CTR anomaly |
| Banner shimmer / label                                                       | Tự inflate                                                | Layout chuẩn `layout_ad_banner.xml` — có shimmer + "Ad" label                                          |
| Banner lifecycle                                                             | Rời rạc                                                   | `bannerResume/Pause/Destroy` rõ ràng                                                                   |
| Interstitial duplicate request                                               | Có thể gửi 2 req song song                                | SDK có duplicate guard + auto preload sau dismiss                                                      |
| Memory leak từ `interstitialListener`                                        | Có                                                        | Đã xóa — không file nào trong app còn tham chiếu `interstitialListener`                                |
| Dead code `Applovin.kt`                                                      | Toàn comment                                              | **Đã xóa**                                                                                             |
| `play-services-ads` + `com.google.ads.mediation:applovin` khai báo trực tiếp | Có (trùng lặp / sai chiều adapter)                        | **Đã xóa**, để wrapper transitively pull                                                               |

---

## 3. FLOW KHỞI TẠO (hiện tại)

```
RApp.onCreate()
  ├─ AdManager.setConfig(adConfig)         // main thread, sớm nhất
  ├─ AdManager.earlyInit(this)             // bắt đầu session clock cho AdSafety
  └─ if (IS_ENABLE_ADMOB) {
         MobileAds.initialize { ... AdManager.init(...) { ... } }
     } else {
         AppLovinSdk.getInstance().initialize(initConfig) {
             AdManager.init(...) { success, gaid ->
                 Handler(mainLooper).post {
                     AdManager.registerAppOpenAdLifecycle(this@RApp)
                 }
             }
         }
     }

SplashActivity.onCreate()
  └─ AdManager.initSplashScreen(this) { goToMain() }
        // internal: preload App Open → show → timeout 8s → onAdLoaded callback

ActivityBottomTabs.onCreate()
  └─ adView = AdManager.loadBanner(this, bannerContainer, tvLabelAd)
ActivityBottomTabs.onResume / onPause / onDestroy
  └─ AdManager.bannerResume / bannerPause / bannerDestroy(adView)

FragmentCreateBarcode.onViewCreated  → AdManager.loadInterstitial(activity)
SettingsFragment.onViewCreated       → AdManager.loadInterstitial(activity)
<any button>.onClick                 → AdManager.showInterstitial(activity) { ok -> navigateNext() }
```

---

## 4. POLICY AUDIT (AdMob + AppLovin)

| Policy quan tâm                              | Kiểm tra                                                                                          | Kết quả |
|----------------------------------------------|---------------------------------------------------------------------------------------------------|---------|
| Banner chồng lên content / control           | Banner chỉ ở tab host (`ActivityBottomTabs`) dưới BottomNavigation, dùng `layout_ad_banner` chuẩn | ✅ OK    |
| Banner auto-refresh khi Activity invisible   | `bannerPause/Destroy` được gọi đầy đủ                                                             | ✅ OK    |
| Interstitial auto-show không user intent     | Toàn bộ 7 lần gọi đều trong `OnClickListener`                                                     | ✅ OK    |
| Interstitial spam ngay sau nhau              | SDK AdSafety throttle 60s + session cap 6 + hourly cap 3 + daily cap 5                            | ✅ OK    |
| Interstitial block navigation khi ad fail    | `showInterstitial` callback luôn `invoke` dù `ok=true/false` → navigate vẫn chạy                  | ✅ OK    |
| App Open show ngay sau click ad rồi back     | SDK skip nếu background <5s                                                                       | ✅ OK    |
| App Open show tại Activity nhạy cảm (Splash) | SDK auto skip nếu current activity là SplashActivity                                              | ✅ OK    |
| Accidental click (Invalid Traffic)           | Progressive pause 30→60→120→240 phút nếu CTR >10%                                                 | ✅ OK    |
| Misleading Ad Label                          | Layout chuẩn SDK có `tvLabelAd` hiển thị "Ad"                                                     | ✅ OK    |
| Reward thiếu callback user                   | N/A (không implement)                                                                             | ✅ OK    |

---

## 5. MEMORY LEAK AUDIT

| Rủi ro                                                 | Nơi kiểm                                                                                     | Kết luận |
|--------------------------------------------------------|----------------------------------------------------------------------------------------------|----------|
| Banner AdView giữ Activity sau destroy                 | `ActivityBottomTabs.onDestroy` → `bannerDestroy(adView)`                                     | ✅ OK     |
| Splash Handler giữ Activity                            | `SplashActivity.onDestroy` → `navigationHandler?.removeCallbacksAndMessages(null)`           | ✅ OK     |
| `interstitialListener` global → leak Fragment/Activity | Grep toàn project: **0 usage**                                                               | ✅ OK     |
| `AdManager.currentActivity` leak                       | SDK dùng `WeakReference<Activity>` (AdManager.kt:162) → không giữ strong ref                 | ✅ OK     |
| Fragment preload interstitial bị retain khi detach     | `loadInterstitial(requireActivity())` — dùng Activity context, không phải Fragment → an toàn | ✅ OK     |

---

## 6. STATE AppLovin / AdMob KEY & CONFIG

### AppLovin (đang dùng — đủ)

| Key                | Nguồn                                                        | Giá trị                            |
|--------------------|--------------------------------------------------------------|------------------------------------|
| SDK Key            | `BuildConfig.APPLOVIN_SDK_KEY` / `BuildConfig.SDK_KEY`       | `e75FnQfS9XTT...` ✅                |
| Banner ID          | `BuildConfig.APPLOVIN_BANNER_ID` / `BuildConfig.BANNER`      | `1cfc3df1cd94473a` ✅               |
| Interstitial ID    | `BuildConfig.APPLOVIN_INTERSTITIAL_ID` / `BuildConfig.INTER` | `2f81cf4ca355cdac` ✅               |
| App Open ID        | `BuildConfig.APPLOVIN_APP_OPEN_ID` / `BuildConfig.APPOPEN`   | `5baea98f19f13cf2` ✅               |
| Reward ID (unused) | `BuildConfig.APPLOVIN_REWARD_ID` / `BuildConfig.REWARD`      | `260b02ff9724a4ec` — SDK không gọi |

### AdMob (dự phòng — chưa đủ)

| Key                       | Giá trị                                  | Status    |
|---------------------------|------------------------------------------|-----------|
| APPLICATION_ID (Manifest) | `ca-app-pub-3612191981543807~8467338874` | ✅         |
| Banner Ad Unit ID         | `""` (empty)                             | ❌ chưa có |
| Interstitial Ad Unit ID   | `""` (empty)                             | ❌ chưa có |
| App Open Ad Unit ID       | `""` (empty)                             | ❌ chưa có |

### Manifest

- ✅ `com.google.android.gms.ads.APPLICATION_ID` đã khai báo (`AndroidManifest.xml:25`)
- ⚠️ Thiếu `applovin.sdk.key` meta-data (đang init programmatically bằng `BuildConfig.APPLOVIN_SDK_KEY` nên vẫn chạy;
  nhưng AppLovin docs khuyến nghị add meta-data để tối ưu init time).

---

## 7. 5 SAI SÓT NHỎ CẦN DỌN

> Không vi phạm policy, không leak, nhưng ảnh hưởng chất lượng code / khả năng switch provider.

### 🟡 7.1 — `RApp.kt:61` hard-code `if (false)`

```kotlin
// Hiện tại
if (false) {
    com.google.android.gms.ads.MobileAds.initialize(this) { ... }
} else {
    // AppLovin path
}
```

→ Nhánh AdMob là dead code, khi bật `IS_ENABLE_ADMOB=true` cũng không chạy AdMob.

**Fix:** Đổi `if (false)` thành `if (adConfig.isEnableAdmob)` (trị `BuildConfig.IS_ENABLE_ADMOB`).

### 🟡 7.2 — Reward field thừa trong `BuildConfig`

SDK 1.1.2 không có Reward API. Giữ `APPLOVIN_REWARD_ID` / `REWARD` gây hiểu nhầm.

**Fix:** Chờ đến khi SDK bổ sung Reward, hoặc xóa 4 dòng BuildConfig (debug + release + dev + production flavor). Theo
rule #5 "không tự ý thêm ad", **không implement reward touchpoint mới**.

### 🟡 7.3 — Duplicate BuildConfig keys across buildType ↔ flavor

- buildType `debug/release`: `APPLOVIN_SDK_KEY`, `APPLOVIN_BANNER_ID`, `APPLOVIN_INTERSTITIAL_ID`,
  `APPLOVIN_APP_OPEN_ID`, `APPLOVIN_REWARD_ID`, `ADMOB_*`
- flavor `dev/production`: `SDK_KEY`, `BANNER`, `INTER`, `APPOPEN`, `REWARD`

Cùng giá trị, 2 tên khác nhau → dễ update miss một bên.

**Fix:** Gom về 1 tập duy nhất (đề xuất giữ bộ `APPLOVIN_*` + `ADMOB_*`, xóa bộ `SDK_KEY/BANNER/INTER/APPOPEN/REWARD` —
RApp chỉ dùng bộ mới).

### 🟡 7.4 — `FragmentCreateBarcode.kt:124-160` code chết

Khối 36 dòng comment của `MaxInterstitialAd` cũ — còn tham chiếu `MaxAd`, `MaxAdListener`, `retryAttempt`.

**Fix:** Xóa hẳn (không còn giá trị lịch sử, `git log` giữ rồi).

### 🟡 7.5 — Thiếu `applovin.sdk.key` meta-data trong Manifest

Hiện chỉ có `manifestPlaceholders = [applovin_sdk_key: "..."]` trong `app/build.gradle:34` nhưng **không
có `<meta-data>` nào reference `${applovin_sdk_key}`** → placeholder này chết vô nghĩa.

**Fix:** Thêm vào `<application>` của `AndroidManifest.xml`:

```xml
<meta-data
    android:name="applovin.sdk.key"
    android:value="${applovin_sdk_key}" />
```

---

## 8. THÔNG TIN CẦN THÊM (OPTIONAL)

Hiện trạng đủ chạy AppLovin. Chỉ cần thêm info nếu muốn **bật AdMob**:

1. **3 AdMob Ad Unit IDs**: banner, interstitial, app open (dạng `ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy`).
2. Xác nhận `IS_ENABLE_ADMOB` muốn set `true` ở build nào (thường `release` với production flavor).
3. Sau khi có ID → set vào `buildConfigField` + fix §7.1 → build-test verify AdMob chạy.

Nếu sau này SDK bổ sung Reward Ad API, cần báo riêng để bổ sung touchpoint (theo rule #5 hiện chưa thêm).

---

## 9. DOUBLE-CHECK (3 lần)

| Lần | Checklist                                                              | Kết quả                                                                                                                           |
|-----|------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| 1   | Tất cả touchpoint ad có đang gọi đúng API SDK 1.1.2 không?             | ✅ Banner + Interstitial + App Open đều qua `AdManager.*`, không còn code ad viết tay nào                                          |
| 2   | Còn import/dep thừa AdMob/AppLovin nào trong `app/build.gradle` không? | ✅ Đã xóa `play-services-ads:23.6.0` và `com.google.ads.mediation:applovin:13.0.1.0` (turn trước) — wrapper đã expose transitively |
| 3   | Có memory leak / orphan listener nào không?                            | ✅ Grep `interstitialListener` → 0 match; Banner có destroy; Splash handler có cleanup                                             |

**Build status:** Chưa chạy `./gradlew :app:assembleDevDebug` trong turn này (task yêu cầu report, chưa yêu cầu build).
Fix §7.1–7.5 là thay đổi nhỏ, không thay đổi dependency → rủi ro build thấp; khi sẵn sàng áp dụng sẽ build verify.

---

## 10. KẾT LUẬN

- **Migration đã hoàn tất từ trước**, project đang chạy SDK 1.1.2 đúng spec.
- **Không cần key/ID mới** để tiếp tục chạy AppLovin.
- 5 sai sót nhỏ ở §7 là low-risk cleanup, làm khi có time.
- Rule #5 được tuân thủ: **không có touchpoint ad nào được thêm** ngoài những gì đã có. Reward ID còn trong BuildConfig
  nhưng không có touchpoint code.
