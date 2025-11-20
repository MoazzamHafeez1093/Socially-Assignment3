package com.example.assignment1

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        // Initialize Espresso Intents for activity navigation testing
        Intents.init()
    }

    @After
    fun tearDown() {
        // Release Espresso Intents
        Intents.release()
    }

    @Test
    fun testLoginButtonIsDisplayed() {
        // Verify login screen loads with all required elements
        onView(withId(R.id.emailTextBox))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.passwordTextBox))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.btnLogin2))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testLoginWithEmptyEmail() {
        // Test validation: empty email should prevent login
        onView(withId(R.id.passwordTextBox))
            .perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin2))
            .perform(click())

        // Should remain on login screen
        onView(withId(R.id.emailTextBox))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testLoginWithEmptyPassword() {
        // Test validation: empty password should prevent login
        onView(withId(R.id.emailTextBox))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin2))
            .perform(click())

        // Should remain on login screen
        onView(withId(R.id.passwordTextBox))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testLoginWithEmptyFields() {
        // Test validation: both fields empty
        onView(withId(R.id.btnLogin2))
            .perform(click())

        // Verify we're still on login screen
        onView(withId(R.id.emailTextBox))
            .check(matches(isDisplayed()))
        onView(withId(R.id.passwordTextBox))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testNavigateToSignup() {
        // Test signup button navigation
        onView(withId(R.id.signupBtn))
            .check(matches(isDisplayed()))
            .perform(click())

        // Verify navigation to signup activity
        intended(hasComponent(signup::class.java.name))
    }

    @Test
    fun testForgotPasswordButtonExists() {
        // Verify forgot password button is present
        onView(withId(R.id.forgotPassword))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testEmailFieldAcceptsInput() {
        // Test email field accepts text input
        val testEmail = "test@example.com"
        onView(withId(R.id.emailTextBox))
            .perform(typeText(testEmail), closeSoftKeyboard())
        
        onView(withId(R.id.emailTextBox))
            .check(matches(withText(testEmail)))
    }

    @Test
    fun testPasswordFieldAcceptsInput() {
        // Test password field accepts text input and masks it
        val testPassword = "password123"
        onView(withId(R.id.passwordTextBox))
            .perform(typeText(testPassword), closeSoftKeyboard())
        
        // Verify text was entered (though it will be masked)
        onView(withId(R.id.passwordTextBox))
            .check(matches(withText(testPassword)))
    }
}