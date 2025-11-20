package com.example.assignment1.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "socially_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROFILE_IMAGE = "profile_image"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REQUIRES_PROFILE_SETUP = "requires_profile_setup"
    }
    
    fun saveAuthData(token: String, userId: Int, username: String, email: String, requiresProfileSetup: Boolean = false) {
        sharedPreferences.edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_REQUIRES_PROFILE_SETUP, requiresProfileSetup)
            apply()
        }
    }
    
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }
    
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }
    
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }
    
    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }
    
    fun getProfileImage(): String? {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE, null)
    }
    
    fun setProfileImage(url: String?) {
        sharedPreferences.edit().putString(KEY_PROFILE_IMAGE, url).apply()
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun requiresProfileSetup(): Boolean {
        return sharedPreferences.getBoolean(KEY_REQUIRES_PROFILE_SETUP, false)
    }
    
    fun setProfileSetupComplete() {
        sharedPreferences.edit().putBoolean(KEY_REQUIRES_PROFILE_SETUP, false).apply()
    }
    
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
