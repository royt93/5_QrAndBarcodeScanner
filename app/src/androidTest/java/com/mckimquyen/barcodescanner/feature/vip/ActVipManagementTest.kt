package com.mckimquyen.barcodescanner.feature.vip

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.barcodescanner.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test cho trạng thái tĩnh của ActVipManagement — KHÔNG test luồng activate/watch-ad thật
 * (async Thread + postDelayed 1s + gọi AdManager thật, dễ flaky trong CI). Luồng đó đã verify tay
 * trên máy thật (S24 Ultra + Pixel 7 Pro, token thật) — xem doc/AD.MD mục 9.1.
 */
@RunWith(AndroidJUnit4::class)
class ActVipManagementTest {

    @Before
    fun setUp() {
        // Đảm bảo bindUi() luôn khởi động ở trạng thái Free user, không phụ thuộc thứ tự chạy test
        // khác trong cùng suite (vd nếu 1 test trước đó activate VIP mà chưa revoke).
        com.roy.sdkadbmob.AdManager.clearVipByKey()
    }

    @Test
    fun freeUser_showsFreeStateAndDisabledActivateButton() {
        ActivityScenario.launch(ActVipManagement::class.java).use {
            onView(withText(R.string.vip_toolbar_title)).check(matches(isDisplayed()))
            onView(withText(R.string.vip_free_user)).check(matches(isDisplayed()))
            onView(withId(R.id.btnActivate)).check(matches(isNotEnabled()))
        }
    }

    @Test
    fun typingIntoTokenField_enablesActivateButton() {
        ActivityScenario.launch(ActVipManagement::class.java).use {
            onView(withId(R.id.etVipKey)).perform(typeText("not-a-real-token"))
            onView(withId(R.id.btnActivate)).check(matches(isEnabled()))
        }
    }

    @Test
    fun garbageToken_activateClick_doesNotCrash() {
        // activateVipByToken() là crypto verify local (không cần mạng) — an toàn chạy trong CI.
        // Chỉ assert KHÔNG crash trong lúc chờ luồng threaded (postDelayed 1s + background Thread +
        // runOnUiThread), không assert dialog cụ thể để tránh flaky theo timing máy CI.
        val scenario = ActivityScenario.launch(ActVipManagement::class.java)
        scenario.use {
            onView(withId(R.id.etVipKey)).perform(typeText("obviously-not-a-real-token"))
            onView(withId(R.id.btnActivate)).perform(click())

            Thread.sleep(3000)

            assert(scenario.state.isAtLeast(Lifecycle.State.CREATED)) {
                "Activity bị destroy/crash sau khi kích hoạt token rác"
            }
        }
    }
}
