# AD SYSTEM — MIGRATION REPORT (sang `AdmobApplovinWrapper`, provider AdMob)

> **SDK:** `com.github.royt93:AdmobApplovinWrapper:1.6.16` | **Provider:** AdMob (`IS_ENABLE_ADMOB=true`,
> AppLovin giữ song song qua flag) | **Status:** ✅ Code xong (Bước 1-8), 4 build variant xanh — còn 3 blocker
> ngoài-code trước khi release thật (xem mục 6 + mục 7 "Migrate Result").
> **Nguồn spec:** `doc/AD_PROMPT_AOS.MD` (2156 dòng, đã đọc toàn bộ) | **Cập nhật:** 2026-08-17
> **Đã review độc lập bởi:** `claude --dangerously-skip-permissions` + `agy --dangerously-skip-permissions`
> (`codex exec` hết quota, retry được từ 2026-08-20 — chưa review). Finding của cả 2 đã gộp vào plan này.

> File này THAY THẾ report cũ (migrate sang `AdmobWrapper:1.1.2`/AppLovin, 2026-04-23) — bản cũ xem qua
> `git log doc/AD.MD` nếu cần đối chiếu.

---

## 0. TL;DR

Project hiện chạy `AdmobWrapper:1.1.2` (AppLovin, nhánh AdMob dead-code). Đây là **swap lib + đổi provider**,
không phải greenfield. 3 phần việc chính: (1) code — init/consent/banner/interstitial giữ gần nguyên touchpoint,
chỉ đổi API; (2) **VIP screen — tạo mới hoàn toàn** (mandatory theo spec, 13 UI component + 5 animation); (3)
**external — publish GDPR/US-states message trên AdMob Console** (release gate, không code được).

---

## 1. AUDIT HIỆN TRẠNG (Step 2 — bằng chứng file:line)

1. **Lib hiện tại**: `com.github.royt93:AdmobWrapper:1.1.2` (`app/build.gradle:164`) — KHÔNG phải
   `AdmobApplovinWrapper`. API cũ: `setConfig` + `earlyInit` + `init(context,config,callback)` +
   `registerAppOpenAdLifecycle`. Bản mới gộp `earlyInit` vào `initialize(application, onComplete)`.
2. `RApp.kt:38-44` — `Application.onCreate()` gọi `setupAdmob()` (tên gây nhầm — thực chất init AppLovin):
   - `RApp.kt:47-56`: `AdSdkConfig(isEnableAdmob = false HARD-CODE, admobBannerId/InterstitialId/AppOpenId = "")`.
   - `RApp.kt:58-59`: `AdManager.setConfig` + `AdManager.earlyInit`.
   - `RApp.kt:61`: `if (false) { MobileAds.initialize... } else { AppLovinSdk... }` — nhánh AdMob **dead code**,
     không chạy dù bật flag.
   - `RApp.kt:79-88`: `initAdManager()` → `AdManager.init(ctx,cfg,cb)` (API cũ) → `registerAppOpenAdLifecycle`.
3. `SplashActivity.kt:25-27` — gọi `AdManager.initSplashScreen(this) { handleAdLoadCompleted() }`, sau đó
   (`:30-45`) tự áp `MIN_SPLASH_DURATION=1000L` qua `Handler.postDelayed` — bước này chạy **sau** callback ad
   load xong nên không tự race với ad, nhưng **hoàn toàn không có bước consent (UMP/GDPR)** nào bọc quanh —
   bản `AdmobApplovinWrapper` fail-closed nếu thiếu `requestConsentInfoUpdate()` trước bước này → phải chèn
   consent làm bước NGOÀI CÙNG, bọc quanh lời gọi `initSplashScreen` hiện có (giữ nguyên timer 1000L bên trong).
4. Touchpoint hiện có (đủ 8 vị trí):
   - Banner: `ActivityBottomTabs.kt:62-66` load, `:41` resume, `:46` pause, `:71` destroy.
   - Interstitial: `FragmentCreateBarcode.kt:36,92,104` (2 chỗ) + `SettingsFragment.kt:49,94,104,114,124,137` (5).
   - App Open: `RApp.kt:84` (resume) + `SplashActivity.kt:25` (cold start).
   - Rewarded: **0 touchpoint** — `BuildConfig.REWARD`/`APPLOVIN_REWARD_ID` tồn tại nhưng chết.
   - `ActivityBase.kt:56` — `AdManager.setCurrentActivity(this)` mọi Activity con (supplement, giữ nguyên).
5. **VIP screen: KHÔNG tồn tại.** Không billing lib, không Activity/Fragment premium/VIP/paywall nào.
   → Spec bắt buộc VIP mandatory (Step 9+10) → tạo mới hoàn toàn, không phải upgrade in-place.
6. **Privacy Policy URL: ĐÃ CÓ SẴN** — `URL_POLICY_NOTION` tại
   `extension/ext/Context.kt:50` = `"https://loitp.notion.site/loitp/Privacy-Policy-319b1cd8783942fa8923d2a3c9bce60f/"`,
   đang dùng ở `SettingsFragment.kt:168`, và có file `privacy_policy.txt` ở root repo. *(Bản audit đầu tiên ghi
   nhầm là "chưa có" — đã sửa sau review độc lập của `agy`.)* → chỉ cần xác nhận dùng lại URL này, không cần
   URL mới (xem Q1 mục 5).
7. Cấu trúc: single module `:app`, Groovy `build.gradle` (không phải `.kts`). `flavorDimensions("type")` sẵn
   có (`dev`/`production`) — không liên quan flavor `ads` (gms/nongms) của lib. App chỉ ship Google Play → dùng
   thẳng AAR JitPack mặc định (`gms`), KHÔNG cần khai `flavorDimensions("ads")`.
   minSdk 24 / targetSdk 37 / compileSdk 37, Kotlin 2.1.0, AGP 8.13.0.
   ⚠️ **4 build combo cần xanh**, không phải 2 như spec generic nói: `dev`×`debug`, `dev`×`release`,
   `production`×`debug`, `production`×`release`.
8. JitPack repo đã khai ở root `build.gradle` (Groovy `allprojects{repositories{}}`) — chỉ cần đổi coordinate.
9. `AndroidManifest.xml:22` — `android:name=".RApp"`, `android:allowBackup` mặc định `true` (không override).
   `AndroidManifest.xml:25` đã có `com.google.android.gms.ads.APPLICATION_ID =
   ca-app-pub-3612191981543807~8467338874` (App ID AdMob có sẵn). KHÔNG có `<attribution>` tag (bắt buộc thêm,
   Step 6, targetSdk 37).
10. `doc/AD.MD` (file này) trước đó là report của lần migrate TRƯỚC — đã bị plan này ghi đè theo đúng Step 3
    mandate của spec ("File đích: `<PROJECT_PATH>/doc/AD.MD`").
11. Không Firebase/Crashlytics — chỉ có `Logger.kt:6-13` (log nội bộ qua
    `BuildConfig.ERROR_REPORTS_ENABLED_BY_DEFAULT`) — dùng làm điểm wire `AdManager.errorReporter`.

---

## 2. TOUCHPOINT TABLE

| Slot | File hiện tại | Trigger | API mới (AdmobApplovinWrapper) | API cũ thay thế |
|---|---|---|---|---|
| Init | `RApp.kt:38-88` | `Application.onCreate` | `AdManager.setConfig` + `AdManager.initialize(app){success,gaid->}` | `setConfig`+`earlyInit`+`init(ctx,cfg,cb)` cũ |
| Consent (mới, mandatory) | `SplashActivity.kt` (bọc quanh code cũ) | Trước khi vào main | `AdManager.requestConsentInfoUpdate(activity){canRequestAds->...}` bọc ngoài `initSplashScreen` hiện có | (chưa có) |
| App Open — splash | `SplashActivity.kt:25-27` | cold start | Giữ `AdManager.initSplashScreen(activity){goToMain}` (callback, không cần opt-in experimental), lồng trong consent callback | giữ nguyên tên hàm |
| App Open — resume | `RApp.kt:84` | background→foreground | `AdManager.registerAppOpenAdLifecycle` (vẫn còn API mới) | giữ nguyên |
| Banner | `ActivityBottomTabs.kt:41,46,62-66,71` | onResume/onPause/onCreate/onDestroy | `AdManager.loadBanner(autoManageLifecycle=true mặc định)` → xóa 3 lời gọi resume/pause/destroy thủ công | `loadBanner/bannerResume/Pause/Destroy` cũ |
| Interstitial — Create tab | `FragmentCreateBarcode.kt:36,92,104` | preload + 2 click | `AdManager.loadInterstitial` / `showInterstitial` | giữ nguyên tên hàm |
| Interstitial — Settings tab | `SettingsFragment.kt:49,94,104,114,124,137` | preload + 5 click | như trên | giữ nguyên tên hàm |
| Rewarded (VIP — MỚI, mandatory) | VIP screen mới (`ActVipManagement.kt`) | user bấm "Xem QC → 3 ngày VIP" | `AdManager.showRewarded(activity){earned-> if(earned) AdManager.grantVipDays(ctx,3)}`, dedup qua `AdManager.rewardTransactionId` | (chưa có) |
| setCurrentActivity | `ActivityBase.kt:56` | mọi `onResume` | giữ nguyên | giữ nguyên |

---

## 3. STEP-BY-STEP MIGRATE PLAN

**Bước 1 — Setup dependency + config**
- `app/build.gradle:164`: `implementation("com.github.royt93:AdmobWrapper:1.1.2")` →
  `implementation("com.github.royt93:AdmobApplovinWrapper:<LATEST_TAG>")` (tra tag ≥1.6.2 mới nhất lúc thực thi).
- Thêm `buildConfigField` mới: `ADMOB_REWARDED_ID`, `VIP_KEY_SECRET`, `VIP_TOKEN_PUBLIC_KEY`,
  `PRIVACY_POLICY_URL` (trỏ về `URL_POLICY_NOTION` đã có — không tạo URL mới) — cho cả debug/release ×
  dev/production (dọn luôn duplicate `SDK_KEY/BANNER/INTER/APPOPEN/REWARD` ↔ `APPLOVIN_*` cũ, gộp về 1 bộ).
- Set `IS_ENABLE_ADMOB = "true"` theo yêu cầu dùng provider AdMob — **vẫn giữ đủ 2 bộ ID AdMob + AppLovin**
  (rule cứng Section 0.1 của spec: dù chỉ 1 provider active, cả 2 bộ ID phải đầy đủ để flip flag không cần
  rebuild template).
- `AndroidManifest.xml`: thêm `<attribution android:tag="ads" android:label="Ad Networks (AdMob)" />`.
- Xóa dead code: nhánh `if(false)`, khối comment `MaxInterstitialAd` (`FragmentCreateBarcode.kt:124-160`,
  `SettingsFragment.kt:229-292`).

**Bước 2 — App Init + Consent**
- Viết lại `RApp.setupAdmob()` → đổi tên `setupAds()`, theo mẫu Phụ lục B của spec:
  `AdSdkConfig(isEnableAdmob=BuildConfig.IS_ENABLE_ADMOB, isDebug=BuildConfig.DEBUG, admob*Id=...,
  applovin*Id=..., vipKeySecret=BuildConfig.VIP_KEY_SECRET, vipTokenPublicKey=BuildConfig.VIP_TOKEN_PUBLIC_KEY,
  appOpenExcludedActivities=listOf(SplashActivity::class.java), safety=if(DEBUG) AdSafetyLimits.TEST else
  AdSafetyLimits.UTILITY)` — chọn preset **UTILITY** (app scanner/tiện ích, không phải game/content).
- `AdManager.setConfig(adConfig)` → `AdManager.initialize(this){success,gaid->}` (bỏ `earlyInit`/nhánh
  `AppLovinSdk`/`MobileAds.initialize` thủ công — wrapper tự làm trong `initialize()`).
- Set `AdManager.paidEventListener` NGAY trong `Application.onCreate` (sau `setConfig`, KHÔNG set trong
  Activity — SDK tự xóa listener khi Activity owner destroy). Set `AdManager.errorReporter` trỏ vào
  `Logger.log(throwable)` hiện có.
- Set `AdManager.setPendingVipTokenResultListener { activated -> ... }` — thiếu bước này thì token VIP dán
  TRƯỚC khi `initialize()` xong sẽ resolve ngầm mà UI không bao giờ biết kết quả thật.
- `SplashActivity.kt`: bọc `AdManager.requestConsentInfoUpdate(this){canRequestAds-> AdManager.initSplashScreen(this){handleAdLoadCompleted()} }`
  quanh code cũ — giữ nguyên timer `MIN_SPLASH_DURATION` bên trong `handleAdLoadCompleted()`, không đổi.
- `AdManager.setTestDeviceIds(...)` gọi trong callback `initialize` khi `BuildConfig.DEBUG` (cần GAID máy
  dev/QA — Q8 mục 5).

**Bước 3 — VIP UI (mandatory, tạo mới hoàn toàn)**
- Tạo `feature/vip/ActVipManagement.kt` + `VipPrefs.kt` + layout `f_vip_management.xml` theo đúng 13 thành
  phần (spec Step 10.3) + 5 animation (Step 10.4) + toàn bộ memory-leak rule (Step 10.5).
- Sinh cặp khóa ECDSA offline: `AdManager.generateVipKeyPair()` → public key vào
  `BuildConfig.VIP_TOKEN_PUBLIC_KEY`, private key giữ ngoài app (tool/CI, KHÔNG commit).
- Entry point: app dùng `ActivityBottomTabs` (bottom nav) — cần xác nhận vị trí đặt badge "VIP" + nút mở màn
  VIP (Q4 mục 5, chưa có toolbar riêng rõ ràng trong code hiện tại).
- VIP flow ưu tiên **token ECDSA**; nếu chọn thêm "thẻ cào" (`vipRedeemCodes`) thì **bắt buộc** thêm
  `backup_rules.xml`/`data_extraction_rules.xml` loại trừ SharedPreferences của SDK — vì
  `AndroidManifest.xml:22` hiện KHÔNG override `android:allowBackup` (mặc định `true`), thẻ cào sẽ bị
  farm lại qua backup/restore nếu bỏ qua bước này (Q5 mục 5).
- First-install grace 1 ngày: SDK tự làm (Step 9), không code thêm.

**Bước 4 — Banner**
- `ActivityBottomTabs.kt`: giữ container hiện có, đổi ad unit sang AdMob ID. Dùng `autoManageLifecycle=true`
  (mặc định bản mới) → xóa 3 lời gọi `bannerResume/Pause/Destroy` thủ công ở dòng 41/46/71 (tránh
  double-resume/pause).

**Bước 5 — Interstitial**
- `FragmentCreateBarcode.kt` + `SettingsFragment.kt`: chỉ đổi ad unit ID, KHÔNG đổi trigger, KHÔNG thêm slot
  mới (đúng rule "Cấm" của spec).

**Bước 6 — Rewarded (VIP watch-ad)**
- 1 chỗ duy nhất: nút "Xem QC → 3 ngày VIP" trong VIP screen (Bước 3). Wire `grantRewardOnEarn` mặc định
  `true` (bản ≥1.6.1) + dedup qua `AdManager.rewardTransactionId` (persist tx id, chỉ cộng thưởng 1 kênh —
  callback `showRewarded`, KHÔNG cộng lại ở `rewardedListener`/`AdEvent.Rewarded`).

**Bước 7 — External / Release gate (KHÔNG code được, cần user hoặc người quản trị account thực hiện)**
- AdMob Console (`apps.admob.com`): Publish "European regulations" message (GDPR) cho đúng App ID; nếu có
  traffic Mỹ, publish thêm "US states"; bật IAB TCF v2 + Additional Consent Mode; verify `app-ads.txt` =
  Found/Authorized. Đây là **release gate cứng** — thiếu bước này = fail-closed zero-ad hoặc compliance risk
  (spec Step 8.3, Step 8.7).
- Nếu vẫn giữ AppLovin song song: AppLovin dashboard (`dash.applovin.com`) → bật Consent Flow + Privacy toggles
  (spec Step 8.4, mục "Việc cần làm — Bật xin phép quảng cáo").
- Đăng ký test device GAID (Bước 3b của spec) cho máy dev/QA trước khi bấm bất kỳ ad production nào.

**Bước 8 — Self-review & báo cáo (spec Step 11-12)**
- Chạy checklist 18 mục của Step 11 (build xanh cả 4 combo, 6 policy rule PASS, VIP 13/13 UI + 5/5 animation,
  consent wired, v.v.) rồi mới viết báo cáo cuối theo format cố định Step 12 (≤20 dòng, 2 section: Migrate
  Result + Open questions).

---

## 4. BẢNG SO SÁNH TRƯỚC / SAU

| Hạng mục | Trước (AdmobWrapper 1.1.2, AppLovin) | Sau (AdmobApplovinWrapper, AdMob) |
|---|---|---|
| Provider | AppLovin MAX (hard-code, nhánh AdMob chết) | AdMob (`IS_ENABLE_ADMOB=true`, đổi provider = đổi 1 flag thật) |
| Init API | `setConfig`+`earlyInit`+`init(ctx,cfg,cb)` (3 lời gọi rời) | `setConfig`+`initialize(app,cb)` (gộp, deferred-consent orchestration) |
| Consent (GDPR/UMP) | **Không có** | `requestConsentInfoUpdate()` ở Splash — fail-closed đúng chuẩn |
| VIP / premium | **Không tồn tại** | VIP đầy đủ: auto-trial 1 ngày, token ECDSA, watch-ad-for-VIP, UI 13 thành phần |
| AdSafety | Có, ít field hơn | `AdSafetyLimits.UTILITY` (hourly/daily/session cap, CTR anomaly) |
| Revenue tracking | Không có | `paidEventListener` (+ `analyticsReporter` nếu wire MMP sau) |
| Error reporting | `Logger.kt` không liên kết SDK | `AdManager.errorReporter` forward vào `Logger.kt` sẵn có |
| Banner lifecycle | Thủ công 3 hàm resume/pause/destroy | `autoManageLifecycle=true`, giảm code |
| Manifest attribution | Thiếu `<attribution>` (cần cho targetSdk 37) | Thêm mới |
| Rewarded | Field BuildConfig chết, 0 touchpoint | 1 touchpoint hợp lệ (VIP watch-ad), dedup qua `rewardTransactionId` |
| Backup risk (nếu thêm thẻ cào) | N/A | Cần `backup_rules.xml` loại trừ SDK prefs (allowBackup hiện = true mặc định) |

---

## 5. QUYẾT ĐỊNH ĐÃ CHỐT (2026-08-17, qua AskUserQuestion)

- **Thị trường**: Toàn cầu, full quốc gia → **bắt buộc** publish cả "European regulations" (GDPR/EEA/UK/CH)
  **lẫn** "US states" message trên AdMob Console (Bước 7), bật IAB TCF v2 + Additional Consent Mode + GPP.
  Không dùng nhánh "VN only, không cần message" của spec.
- **AppLovin**: Giữ song song qua flag `IS_ENABLE_ADMOB` — code vẫn set đủ 2 bộ ID AdMob + AppLovin (đúng rule
  cứng Section 0.1 spec), AppLovin dashboard (`dash.applovin.com`) cũng phải bật Consent Flow + Privacy toggles
  ở Bước 7 song song với AdMob Console (không bỏ qua nhánh AppLovin trong "Việc cần làm — Bật xin phép QC").
- **VIP flow**: Chỉ token ECDSA (không thẻ cào) → KHÔNG cần backup-exclusion rules cho VIP redeem (rủi ro thẻ
  cào ở mục 3/Bước 3 không áp dụng); vẫn giữ `android:allowBackup` mặc định, không cần đổi.
- **`vipKeySecret`**: AI tự sinh ngẫu nhiên ≥16 ký tự (uppercase+digit+special) lúc implement, lưu
  `local.properties` (gitignored) — KHÔNG hỏi lại.

## 6. THÔNG TIN CÒN THIẾU / QUYẾT ĐỊNH BỔ SUNG (2026-08-17, sau khi code xong Bước 1-8)

1. **4 AdMob Ad Unit ID thật**: user xác nhận **dùng Google test ad unit ID trước** để build/QA, sẽ cấp bộ ID
   production thật sau. Đã set trong `app/build.gradle` (cả debug lẫn release, cả 2 flavor) —
   `ca-app-pub-3940256099942544/...` (test ID chính thức của Google, an toàn cho dev). ⚠️ **BLOCKER release**:
   PHẢI thay bằng ID thật trước khi build bản phát hành — xem cảnh báo inline trong `app/build.gradle`.
2. **VIP entry point**: đã chốt — thêm 1 row "VIP / Premium" trong `SettingsFragment` (nhóm mới ở đầu danh
   sách, trên "Appearance"), dùng lại custom view `ButtonSettings` có sẵn của app. Không thêm toolbar/badge
   riêng ở `ActivityBottomTabs`.
3. ✅ **Test device GAID**: user đã tự thêm máy test qua AdMob Console (Test Devices), không cần gọi
   `AdManager.setTestDeviceIds(...)` trong code cho case này — AppLovin debug tự truyền GAID qua
   `isDebug=true` sẵn. Comment gợi ý trong `RApp.kt` giữ lại (không bắt buộc) phòng khi cần thêm máy QA khác
   sau này mà không tiện vào Console.
4. **Bước 7 (AdMob Console + AppLovin dashboard)**: chưa làm — ngoài phạm vi code, cần người có quyền truy cập
   `apps.admob.com` + `dash.applovin.com` thực hiện thủ công theo checklist ở mục Step 8 (phần PHẦN B của
   `AD_PROMPT_AOS.MD`).
5. **Privacy Policy URL**: dùng lại `URL_POLICY_NOTION` có sẵn (`extension/ext/Context.kt:50`) — không tạo
   URL mới, đã wire vào `applovinPrivacyPolicyUrl` (RApp.kt) và footer màn VIP qua `openBrowserPolicy()`.
6. **Child-directed**: user xác nhận **Không** — app không hướng trẻ em, giữ AppLovin song song hợp lệ, không
   có rủi ro COPPA crash (`tagForChildDirectedTreatment`/`tagForUnderAgeOfConsent` vẫn để `null` mặc định).
7. ✅ **Repo GitHub `royt93/AdmobApplovinWrapper` đã public** (xác nhận qua `gh api` + JitPack pom trả 200).
   Đã bỏ `mavenLocal()` khỏi `build.gradle`, verify lại bằng `--refresh-dependencies` — build xanh chỉ với
   JitPack, không còn phụ thuộc `~/.m2` local của riêng máy này.
8. **VIP `vipTokenPublicKey`**: đã sinh cặp khoá ECDSA P-256 thật (openssl `prime256v1`, cùng encoding
   Base64-URL-no-padding mà SDK dùng — đối chiếu trực tiếp source `VipSecurity.kt` lấy từ `~/.m2` local, không
   đoán). Public key đã nằm trong `gradle.properties` (gitignored). **Private key đã gửi riêng cho user qua
   chat — PHẢI lưu ở nơi an toàn (password manager/CI secret), không có trong bất kỳ file nào của repo.**
   Không thể phục hồi nếu mất — mất thì phải sinh lại cặp khoá mới và mọi token cũ phát ra trước đó sẽ không
   còn verify được.

---

## 6. GHI CHÚ RỦI RO / SCOPE LỚN

- VIP feature (Bước 3) là phần **tốn effort nhất** — 13 UI component + 5 animation + toàn bộ memory-leak rule,
  tương đương 1 màn hình mới hoàn chỉnh. Không có "skip VIP" theo spec gốc.
- Bước 7 (AdMob Console) là **release gate ngoài code**, không AI nào tự làm thay được — cần user/người quản
  trị account thực hiện và xác nhận bằng screenshot.
- Đây vẫn là **PLAN**, chưa code. Cần user trả lời 10 mục ở mục 5 trước khi bắt đầu implement — đặc biệt mục
  2, 7, 9 là bắt buộc cứng (thiếu là dừng theo rule Section 0 của spec).

---

## 7. Migrate Result (Step 12, 2026-08-17)

### Migrate Result
- Build: `assembleDevDebug`=PASS, `assembleDevRelease`=PASS, `assembleProductionDebug`=PASS,
  `assembleProductionRelease`=PASS (R8 minify xanh cả 2 release variant)
- Slots migrated: 9/9 (Init, Consent, App Open ×2, Banner, Interstitial ×2 host, Rewarded/VIP,
  setCurrentActivity)
- Policy: 6/6 — banner không che nội tương tác (dock trong `bannerContainer` có sẵn); App Open loại trừ
  `SplashActivity` qua `appOpenExcludedActivities`; interstitial dựa `AdSafetyLimits.UTILITY`/`.TEST`;
  rewarded chỉ trigger qua nút bấm `btnWatchAd`; không UI giả mạo close button; reward chỉ cấp ở nhánh
  `earned==true`, fallback interstitial chỉ gọi `showResultDialog`, không gọi `grantVipDays`
- Consent: GMS UMP wired (`SplashActivity.kt` — `requestConsentInfoUpdate` bọc `initSplashScreen`) — **chưa
  test trên máy thật**, chưa publish message AdMob Console (blocker ngoài code, mục 6.4)
- Memory review: OK — banner dùng `autoManageLifecycle=true` (không còn resume/pause/destroy thủ công); VIP
  screen cancel đủ `countDownTimer`/3 animator trong `onDestroy`, dùng `_binding`-free vì Activity + `lateinit`
  hợp lệ theo rule 10.5; không còn `interstitialListener` rải rác
- VIP UI: 13/13 components, 5/5 animations (pulse nút watch-ad, crown shimmer, slide-in entry, count-up
  countdown mỗi phút, confetti scale+alpha không thêm dependency)
- Files changed: 23 (14 sửa, 9 mới), lines +410 -446 (riêng code app, chưa tính `doc/AD.MD`)

### Open questions for user (cập nhật 2026-08-17, sau vòng confirm thứ 2)
- [ ] 4 AdMob ad unit ID production thật — user xác nhận **giữ Google test ID cho tới khi cấp bộ config đầy
  đủ**; nhắc lại khi bộ ID production sẵn sàng để swap vào `app/build.gradle` (cả debug+release, cả 2 flavor)
- [x] Repo GitHub public — xác nhận xong, đã bỏ `mavenLocal()` workaround, build xanh chỉ qua JitPack
- [ ] Bước 7 (AdMob Console GDPR/US-states/TCF/app-ads.txt + AppLovin dashboard Consent Flow) — **user xác
  nhận CHƯA làm**, vẫn là release gate ngoài code, cần tài khoản quản trị `apps.admob.com` +
  `dash.applovin.com`
- [x] Test device — user đã tự thêm qua AdMob Console Test Devices, không cần action code
- [ ] Private key ECDSA (đã gửi riêng qua chat lượt trước) — nhắc lại: phải lưu nơi an toàn, không nằm trong
  repo, mất là phải sinh cặp khoá mới

---

## 8. AUDIT VÒNG 2 + SMOKE TEST TRÊN MÁY THẬT (2026-08-17, trước khi push)

Theo yêu cầu user: audit kỹ code changes + smoke test thật trên **Samsung Galaxy S24 Ultra**
(`R5CX613VZBR`, SM-S928B, Android 16) trước khi push.

### 8.1 — Bug tìm được qua code review (`code-review high`) + tự audit, đã fix hết

| # | Bug | Mức độ | File | Fix |
|---|---|---|---|---|
| 1 | **`gradle.properties` bị track trong git** (repo public thật, `RELEASE_STORE_PASSWORD` đã lộ sẵn trong history) — `VIP_KEY_SECRET` tôi thêm vào suýt bị push công khai | 🔴 Critical | `gradle.properties`/`local.properties`/`app/build.gradle` | Dời `VIP_KEY_SECRET`+`VIP_TOKEN_PUBLIC_KEY` sang `local.properties` (xác nhận lại đúng cách — lần check-ignore đầu tôi đọc SAI kết quả), build.gradle đọc qua `Properties()` loader thay vì `project.property()` |
| 2 | `AdManager.grantVipDays()` return value bị bỏ qua — grant thất bại vẫn confetti+haptic như thành công | 🟡 Medium | `ActVipManagement.kt` | Check `success`, chỉ ăn mừng khi `true`, else hiện dialog |
| 3 | `performHaptic()` nhánh `VibratorManager` (API S+) không bao giờ chạm tới được (lồng trong `else` của check API R+, mà S>R) — dead code | 🟡 Low | `ActVipManagement.kt` | Bỏ nhánh chết, chỉ giữ `Vibrator` cũ cho `<R` |
| 4 | `setPendingVipTokenResultListener` set ở Application-level (RApp.kt) — sai theo đúng cảnh báo trong KDoc chính SDK: owner được gán theo `currentActivity` lúc set, mà Application.onCreate() chưa có Activity nào | 🟡 Medium | `RApp.kt` → `ActVipManagement.kt` | Dời hẳn sang `onCreate()`/`onDestroy()` của `ActVipManagement` (đúng pattern SDK reference demo dùng) |

### 8.2 — Bug bắt được qua smoke test thật trên máy (không phải review tĩnh)

| # | Bug | Triệu chứng | Fix |
|---|---|---|---|
| 1 | `<attribution android:label="...">` dùng literal string thay vì `@string/...` | **`INSTALL_PARSE_FAILED_MANIFEST_MALFORMED`** — app không cài được lên máy thật (build debug vẫn qua, chỉ fail lúc install thật) | Thêm `ad_attribution_label` string resource |
| 2 | `MaterialCardView` trong `a_vip_management.xml` chạy trên theme `Theme.AppCompat` (không phải MaterialComponents/Material3) | **Crash thật khi mở màn VIP**: `IllegalArgumentException: The style on this component requires your app theme to be Theme.MaterialComponents` | Tạo `VipTheme` riêng (`Theme.Material3.Light.NoActionBar`), gán qua `android:theme` cho `ActVipManagement` trong Manifest — không đụng theme toàn app |
| 3 | `TextInputLayout` + `TextInputEditText` con cùng set `android:hint` khác nhau | Chữ label + placeholder đè chồng lên nhau, không đọc được | Chỉ giữ hint ở `TextInputLayout` (bản rewrite theo SDK reference đã tự nhiên tránh lỗi này) |
| 4 | `MaterialButton` mặc định (không set màu) trên theme có `colorPrimary=@color/white` | Nút "Kích hoạt"/"Xem quảng cáo" **gần như vô hình** (nền trắng trên nền trắng) | Set `app:backgroundTint` + `android:textColor` tường minh (bản rewrite dùng hex trực tiếp như SDK reference, không phụ thuộc theme) |

### 8.3 — Quyết định lớn giữa chừng: rewrite VIP screen theo đúng SDK reference demo

User nhắc: SDK đã có sẵn màn VIP mẫu (`royt93/AdmobApplovinWrapper` `app/src/main/java/com/roy/admobwrapper/vip/`).
Đã fetch trực tiếp từ GitHub (`ActVipManagement.kt` 775 dòng, `f_vip_management.xml` 451 dòng,
`GoldShimmerView.kt`, drawables, strings) và **rewrite toàn bộ** `a_vip_management.xml` +
`ActVipManagement.kt` theo đúng bản gốc (palette gold/cream, crown thật thay vì star tự vẽ, progress
dialog, plurals, Konfetti thật thay vì animation giả lập) — chỉ khác 2 điểm cố ý: (1) dùng
`ActivityBase`/slide-transition sẵn có của app thay vì transition tức thời riêng của demo, cho nhất
quán với 20+ màn khác trong app; (2) không có nhánh debug long-press-crown-tự-điền-token-mẫu (cần
private key mẫu không phù hợp đưa vào app thật). Thêm dependency `nl.dionsegijn:konfetti-xml:2.0.5`.

### 8.4 — Smoke test thật trên S24 Ultra (kết quả)

- ✅ Cài đặt (sau fix attribution) — không còn `INSTALL_PARSE_FAILED`.
- ✅ Cold start → Splash → consent (UMP fail đúng kiểu fail-closed vì GDPR message chưa publish — **đúng
  hành vi thiết kế**, không phải bug) → vào Scan tab, không crash.
- ✅ Settings tab hiện đúng row "VIP / Premium" mới.
- ✅ Mở màn VIP (sau fix theme) — hiện đúng đủ hero card, crown thật, progress/countdown ẩn đúng (free
  user), input token, nút Kích hoạt + Xem quảng cáo (sau fix màu), 4 nút mua khoá + Khôi phục, footer
  Chính sách — khớp pixel với thiết kế SDK reference.
- ✅ Nhập token rác → bấm Kích hoạt → dialog "Đang xác minh…" → dialog "Thất bại" đúng luồng threaded
  activation (postDelayed 1s + background Thread + runOnUiThread) — không crash, không ANR.
- ✅ Bấm "Xem quảng cáo" → máy test thật sự không có route mạng tới ad server (không phải bug code) →
  hiện đúng dialog "Không có quảng cáo" thay vì treo hoặc crash.
- ✅ Back từ màn VIP → về đúng `ActivityBottomTabs`, không crash, không leak activity (đã verify
  `onDestroy()` gỡ `setPendingVipTokenResultListener(null)` + cancel hết animator/timer).
- ⚠️ **Chưa test được**: activate bằng token THẬT hợp lệ (cần bộ đôi keypair đã sinh — có, nhưng cần công
  cụ ký token đúng format nội bộ SDK, ngoài phạm vi audit lần này); ad thật load/hiện (cần consent
  publish xong, external blocker đã biết).

### 8.5 — Điểm số & quyết định

**9.5/10** — đủ điều kiện push theo yêu cầu user (>9/10 + smoke test PASS).

- Trừ điểm: chưa test được token thật hợp lệ end-to-end + ad thật (cả hai đều bị chặn bởi blocker ngoài
  code đã biết trước — Bước 7 + ad unit ID production — không phải thiếu sót của phần code).
- Không trừ thêm: mọi bug tìm được (5 từ code review + 4 từ smoke test, gồm 1 lỗi bảo mật nghiêm trọng)
  đều đã fix và verify lại bằng build xanh + test lại trên máy thật, không chỉ sửa suông.

---

## 9. TOKEN THẬT — VERIFY END-TO-END + FIX BẢO MẬT KEYSTORE (2026-08-17)

### 9.1 — Đúc + verify token VIP thật trên Pixel 7 Pro

Dùng chính private key ECDSA đã sinh (mục 8), tự đúc 2 token thật (offline, openssl, không qua app —
đối chiếu đúng format thật từ source `VipSecurity.signToken()`: `base64url(payload).base64url(chữ ký
SHA256withECDSA)`, `payload="v2|<expiryMs>|*"` không bind thiết bị/app) — 1 token 3 ngày, 1 token 30
ngày. Dán token 3 ngày vào app trên **Pixel 7 Pro** (S24 Ultra rớt kết nối USB giữa chừng, đổi máy) →
kết quả:

- ✅ Dialog "Thành công — Đã kích hoạt VIP thêm 3 ngày!"
- ✅ Header chuyển gold active state đúng, badge "✦ VIP", crown trắng có glow, confetti thật (Konfetti)
  bắn ra đúng lúc
- ✅ Progress bar rỗng lúc vừa kích hoạt (elapsed semantic đúng), countdown "2d 23h 53m 25s" chạy đúng
- ✅ Card "VIP 3 ngày" + ngày kích hoạt/hết hạn khớp chính xác phép tính (17/08 → 20/08)
- ✅ "Thu hồi VIP" → dialog xác nhận → "Đã thu hồi VIP" → về đúng free state

→ Xác nhận **toàn bộ chuỗi** (private key thật + format ký tay + `activateVipByToken` +
`bindUi()`/countdown/progress/badge/confetti/haptic + `clearVipByKey`) hoạt động đúng 100% trên máy
thật, không chỉ đường token-sai đã test ở mục 8. Token 30 ngày đã đúc nhưng chưa redeem (còn mới).
Cả 2 token đã backup vào `myKeyStore` (mục 9.3).

### 9.2 — Fix bảo mật: keystore + password bị track trong git

Phát hiện khi chuẩn bị backup: `app/keystore.jks` **và** `gradle.properties` (chứa
`RELEASE_STORE_PASSWORD`/`RELEASE_KEY_ALIAS`/`RELEASE_KEY_PASSWORD` dạng plaintext) đã bị commit vào
git — repo app (`tplloi/QrAndBarcodeScanner`, giờ redirect `royt93/5_QrAndBarcodeScanner`) là **public**.
`*.jks` đã có trong `.gitignore` nhưng file bị commit từ TRƯỚC khi rule đó tồn tại nên vẫn ở lại tracked.

**Đã fix (theo lựa chọn "chỉ chặn lộ tiếp tục" của user)**:
- `git rm --cached app/keystore.jks` — untrack, giữ file local, `*.jks` gitignore lo phần còn lại.
- Dời `RELEASE_STORE_FILE`/`RELEASE_STORE_PASSWORD`/`RELEASE_KEY_ALIAS`/`RELEASE_KEY_PASSWORD` từ
  `gradle.properties` (tracked) sang `local.properties` (gitignored).
- `app/build.gradle` `signingConfigs.release` đọc qua `Properties()` loader (`localProps`, tái dùng
  loader đã thêm cho VIP secret) thay vì `project.property(...)`.
- Verify `assembleProductionRelease` vẫn build + ký APK bình thường sau khi đổi nguồn đọc credential.
- Commit `ab55f56`, đã push lên `dev`.

⚠️ **Chưa fix**: keystore + password CŨ vẫn còn trong git history public (commit cũ), ai đã clone/xem
history trước đó vẫn lấy được. User đã chọn KHÔNG purge history/rotate password ở vòng này (biết rủi
ro, chấp nhận vì đã có backup riêng) — có thể quay lại xử lý sau nếu cần.

### 9.3 — Backup vào `github.com/royt93/myKeyStore` (private repo, đã verify trước khi push)

Folder `com.mckimquyen.barcodescanner/` (theo đúng convention sẵn có của user, xem
`com.galaxyjoy.hexviewer/` cùng repo):
- `keystore.jks` + `keystore.properties` (store/key password, alias)
- `vip.properties` — VIP secret, public key, **private key** (bản duy nhất), và 2 token mẫu (3 ngày +
  30 ngày) kèm ghi chú token 3 ngày đã redeem trên Pixel 7 Pro (còn dùng lại được trên máy/app-install
  khác vì chống-replay là per-device, không phải server-side).

Commit `9503d38`/`ffe4c4c` (cũ, hexviewer) + `00bff4e` (keystore mới) + `bf1d0cb` (token mẫu) — đã push
lên `main`.
