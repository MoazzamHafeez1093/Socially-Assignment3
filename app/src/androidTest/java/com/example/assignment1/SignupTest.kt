package com.example.assignment1

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
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
class SignupTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(signup::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testSignupScreenLoadsCorrectly() {
        // Verify all required fields are present
        onView(withId(R.id.userName1))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.emailEditText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.passwordEditText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.createAccountBtn))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testUsernameFieldAcceptsInput() {
        // Test username input
        val testUsername = "testuser123"
        onView(withId(R.id.userName1))
            .perform(typeText(testUsername), closeSoftKeyboard())
        
        onView(withId(R.id.userName1))
            .check(matches(withText(testUsername)))
    }

    @Test
    fun testEmailFieldAcceptsInput() {
        // Test email input
        val testEmail = "test@example.com"
        onView(withId(R.id.emailEditText))
            .perform(typeText(testEmail), closeSoftKeyboard())
        
        onView(withId(R.id.emailEditText))
            .check(matches(withText(testEmail)))
    }

    @Test
    fun testPasswordFieldAcceptsInput() {
        // Test password input
        val testPassword = "securePassword123"
        onView(withId(R.id.passwordEditText))
            .perform(typeText(testPassword), closeSoftKeyboard())
        
        onView(withId(R.id.passwordEditText))
            .check(matches(withText(testPassword)))
    }

    @Test
    fun testSignupWithEmptyUsername() {
        // Test validation: empty username should prevent signup
        onView(withId(R.id.emailEditText))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.createAccountBtn))
            .perform(click())

        // Should remain on signup screen
        onView(withId(R.id.userName1))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSignupWithEmptyEmail() {
        // Test validation: empty email should prevent signup
        onView(withId(R.id.userName1))
            .perform(typeText("testuser"), closeSoftKeyboard())

        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.createAccountBtn))
            .perform(click())

        // Should remain on signup screen
        onView(withId(R.id.emailEditText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSignupWithEmptyPassword() {
        // Test validation: empty password should prevent signup
        onView(withId(R.id.userName1))
            .perform(typeText("testuser"), closeSoftKeyboard())

        onView(withId(R.id.emailEditText))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        onView(withId(R.id.createAccountBtn))
            .perform(click())

        // Should remain on signup screen
        onView(withId(R.id.passwordEditText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSignupWithAllFieldsEmpty() {
        // Test validation: all empty fields
        onView(withId(R.id.createAccountBtn))
            .perform(click())

        // Should remain on signup screen
        onView(withId(R.id.userName1))
            .check(matches(isDisplayed()))
        onView(withId(R.id.emailEditText))
            .check(matches(isDisplayed()))
        onView(withId(R.id.passwordEditText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testProfileImageButtonIsDisplayed() {
        // Verify profile image selection button exists
        onView(withId(R.id.cameraButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testProfileImageSelectionInteraction() {
        // Test profile image button interaction
        onView(withId(R.id.cameraButton))
            .perform(click())

        // Button should still be visible after click
        onView(withId(R.id.cameraButton))
            .check(matches(isDisplayed()))
    }
}