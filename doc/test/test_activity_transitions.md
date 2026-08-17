# Test Plan — Activity Transition Animations

> **Cập nhật:** 2026-06-22 | **Feature:** `enhance_activity_transitions` | **Complexity:** Low

---

## Unit Tests (`app/src/test/`)

### `ActivityTransitionExtTest.kt`

| #  | Test                                                          | Input                      | Expected                                                                        |
|----|---------------------------------------------------------------|----------------------------|---------------------------------------------------------------------------------|
| U1 | `startActivityWithTransition` gọi `overridePendingTransition` | Mock Activity, mock Intent | `overridePendingTransition(slide_in_right, slide_out_left)` được gọi đúng 1 lần |
| U2 | `finishWithTransition` gọi đúng exit anim                     | Mock Activity              | `overridePendingTransition(slide_in_left, slide_out_right)` được gọi            |
| U3 | `startActivitySlideUp` dùng đúng anim                         | Mock Activity              | `overridePendingTransition(slide_in_up, android.R.anim.fade_out)`               |
| U4 | `startActivitySlideUp` với flag đúng                          | Intent mới                 | `FLAG_ACTIVITY_CLEAR_TOP` không bị thêm tự động                                 |

---

## Widget Tests / Instrumented Tests (`app/src/androidTest/`)

### `ActivityTransitionInstrumentedTest.kt`

| #  | Test                                      | Scenario             | Expected                              | Device |
|----|-------------------------------------------|----------------------|---------------------------------------|--------|
| W1 | Splash → Main fade                        | Launch app cold      | `fade_in` visible ~300ms, không flash | All    |
| W2 | Main → ActivityBarcode slide-up           | Tap scan result item | Barcode screen slide từ bottom lên    | All    |
| W3 | ActivityBarcode → back slide-down         | Press back           | Screen slide xuống dismiss            | All    |
| W4 | Main → ActivityCreateBarcode slide-right  | Tap Create tab item  | Screen slide từ phải vào              | All    |
| W5 | ActivityCreateBarcode → back slide-left   | Press back           | Screen slide ra trái                  | All    |
| W6 | Settings → ChooseTheme slide-right        | Tap Theme option     | Screen slide phải                     | All    |
| W7 | ChooseTheme → back                        | Press back           | Screen slide trái về Settings         | All    |
| W8 | Main → ActivityBatchExportResult slide-up | Export batch         | Screen slide lên từ bottom            | All    |

### `AnimationDisabledTest.kt`

| #   | Test                                         | Scenario                             | Expected                                              |
|-----|----------------------------------------------|--------------------------------------|-------------------------------------------------------|
| W9  | Navigation works khi animation tắt           | Developer Options: animation scale 0 | Tất cả Activity navigate đúng, chỉ không có animation |
| W10 | Không crash khi transition với null animator | Mock context trả về null             | Fallback gracefully, không NullPointerException       |

---

## Integration Tests

| #  | Test                           | Flow                                                | Expected                                        |
|----|--------------------------------|-----------------------------------------------------|-------------------------------------------------|
| I1 | Back stack đúng sau transition | Main → Create → Barcode → back → back               | Back stack: Main                                | Đúng activity khi back |
| I2 | Không double transition        | `startActivity` với `FLAG_SINGLE_TOP`               | Chỉ 1 transition                                | Không bị double animate |
| I3 | Transition + Ad interstitial   | Tap Create → interstitial show → dismiss → navigate | Transition chạy sau khi ad dismiss, không trước | High risk area |

---

## Manual Checklist (Device)

```
[ ] Physical device API 24 (Android 7)
[ ] Physical device API 34 (Android 14)
[ ] Emulator API 36 (target)
[ ] Slow animation 5x (Developer Options)
[ ] Animation disabled (Developer Options: scale = off)
[ ] Dark mode
[ ] Landscape rotation mid-transition
```

---

## Anim Files Cần Verify Tồn Tại

```
res/anim/slide_in_right.xml   — 300ms, translateX 100%→0
res/anim/slide_out_left.xml   — 300ms, translateX 0→-30%
res/anim/slide_in_left.xml    — 300ms, translateX -100%→0
res/anim/slide_out_right.xml  — 300ms, translateX 0→100%
res/anim/slide_in_up.xml      — 350ms, translateY 100%→0
res/anim/slide_out_down.xml   — 300ms, translateY 0→100%
```
