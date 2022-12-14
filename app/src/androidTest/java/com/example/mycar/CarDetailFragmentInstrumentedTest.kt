package com.example.mycar

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarDetailFragmentInstrumentedTest {
    @get:Rule()
    val activity = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var uiDevice: UiDevice

    @Before
    fun setup(){
        uiDevice = UiDevice.getInstance(getInstrumentation())
    }

    @Test//IT WORKS
    fun clickDetail() {
        clickItem(2)
        clickId(R.id.detail_car)
    }

    @Test//IT WORKS WITH NOTIFICATION
    fun clickFabEditWithNotification() {
        clickDetail()
        clickId(R.id.edit_car)
        scrollTo(R.id.layout_add_new_car_with_connection)
        clearTextInput(R.id.km_car_input)
        clickTextInputWriteString(R.id.km_car_input, "150000")
        hideKeyboard()
        scrollTo(R.id.layout_add_new_car_with_connection)
        scrollTo(R.id.linearLayout2)
        scrollTo(R.id.linearLayout)
        clickId(R.id.save_btn)
        uiDevice.pressHome()
        uiDevice.openNotification()
        uiDevice.wait(androidx.test.uiautomator.Until.hasObject(By.textContains("Panda")), 5)
    }

    @Test//IT WORKS
    fun clickFabShare() {
        clickDetail()
        clickId(R.id.share_car)
    }

    @Test//IT WORKS
    fun clickFabDelete() {
        clickDetail()
        clickId(R.id.delete_car)
        onView(withText(R.string.yes)).perform(click())
    }

    @Test//IT WORKS
    fun clickFabNoDelete() {
        clickDetail()
        clickId(R.id.delete_car)
        onView(withText(R.string.no)).perform(click())
    }
}