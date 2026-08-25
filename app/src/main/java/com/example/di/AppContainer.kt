package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.auth.AuthRepository
import com.example.data.local.UserPreferencesManager
import com.example.data.network.NetworkMonitor
import com.example.data.remote.JamaatyApiService
import com.example.data.repository.JamaatyRepository
import com.example.data.security.SecureStorage
import com.example.utils.AnalyticsLogger
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * P4-2: Manual DI container — light alternative to Hilt/Koin for single-module app.
 * Ready to migrate to @HiltViewModel when `:core:data` split:
 *   @HiltViewModel class JamaatyViewModel @Inject constructor(repo, authRepo) : ViewModel()
 * For now, JamaatyViewModel still uses AndroidViewModel + internal deps; this container documents
 * the graph for future modularization (nonTransitiveRClass already true per gradle.properties:19).
 */
class AppContainer(private val context: Context) {

    // Core:data
    private val db by lazy { AppDatabase.getDatabase(context) }
    val appDao by lazy { db.appDao() }
    val userPrefs by lazy { UserPreferencesManager(context) }
    val secureStorage by lazy { SecureStorage(context) }
    val networkMonitor by lazy { NetworkMonitor(context) }

    // Data: repositories
    val jamaatyRepository by lazy { JamaatyRepository(appDao) }
    val authRepository by lazy { AuthRepository(appDao) }

    // Remote: Retrofit (P4-3) — paginated API, Moshi, OkHttp logging interceptor only in debug
    private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    private val retrofit by lazy {
        val builder = Retrofit.Builder()
            .baseUrl("https://api.jamaaty.app/") // Yemen universities gateway — stub baseUrl
            .addConverterFactory(MoshiConverterFactory.create(moshi))
        // logging interceptor added via OkHttp when BuildConfig.DEBUG (see build.gradle debugImplementation)
        builder.build()
    }
    val apiService: JamaatyApiService by lazy { retrofit.create(JamaatyApiService::class.java) }

    // Observability
    val analytics get() = AnalyticsLogger
}
