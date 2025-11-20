package com.example.assignment1.data.network

import android.content.Context
import com.example.assignment1.data.prefs.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    private const val BASE_URL = "http://10.0.2.2/socially-api/public/" // Android emulator localhost
    // For physical device use: "http://YOUR_LOCAL_IP/socially-api/public/"
    
    private var retrofit: Retrofit? = null
    private lateinit var sessionManager: SessionManager
    
    fun initialize(context: Context) {
        sessionManager = SessionManager(context)
    }
    
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val authInterceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                
                // Add Authorization header if token exists
                sessionManager.getToken()?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                
                requestBuilder.addHeader("Accept", "application/json")
                chain.proceed(requestBuilder.build())
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
    
    fun getApiService(): ApiService {
        return getRetrofit().create(ApiService::class.java)
    }
    
    fun clearRetrofit() {
        retrofit = null
    }
}
