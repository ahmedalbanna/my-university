package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.StudentProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jamaaty_prefs")

/**
 * P1.3 — DataStore wrapper (replaces 8-row PreferenceEntity for new code).
 * Existing PreferenceEntity kept for Room-seeded data & offline fallback,
 * but new profile writes go through both stores transactionally.
 * Gradually migrate callers to this Flow-based API.
 */
class UserPreferencesManager(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING = booleanPreferencesKey("onboarding_completed")
        private val KEY_NAME = stringPreferencesKey("profile_name")
        private val KEY_ID = stringPreferencesKey("profile_id")
        private val KEY_UNIVERSITY = stringPreferencesKey("profile_university")
        private val KEY_FACULTY = stringPreferencesKey("profile_faculty")
        private val KEY_MAJOR = stringPreferencesKey("profile_major")
        private val KEY_LEVEL = intPreferencesKey("profile_level")
        private val KEY_SEMESTER = intPreferencesKey("profile_semester")
        private val KEY_GPA = doublePreferencesKey("profile_gpa")
        private val KEY_BALANCE = doublePreferencesKey("profile_balance")
        private val KEY_AUTH_EMAIL = stringPreferencesKey("auth_email")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_AUTH_LOGGED_IN = booleanPreferencesKey("auth_logged_in")
    }

    val profileFlow: Flow<StudentProfile> = context.dataStore.data.map { prefs ->
        StudentProfile(
            name = prefs[KEY_NAME] ?: StudentProfile.Default.name,
            studentId = prefs[KEY_ID] ?: StudentProfile.Default.studentId,
            university = prefs[KEY_UNIVERSITY] ?: StudentProfile.Default.university,
            faculty = prefs[KEY_FACULTY] ?: StudentProfile.Default.faculty,
            major = prefs[KEY_MAJOR] ?: StudentProfile.Default.major,
            level = prefs[KEY_LEVEL] ?: StudentProfile.Default.level,
            semester = prefs[KEY_SEMESTER] ?: StudentProfile.Default.semester,
            gpa = prefs[KEY_GPA] ?: StudentProfile.Default.gpa,
            balance = prefs[KEY_BALANCE] ?: StudentProfile.Default.balance
        )
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING] ?: false }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTH_LOGGED_IN] ?: false }

    suspend fun saveProfile(profile: StudentProfile) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME] = profile.name
            prefs[KEY_ID] = profile.studentId
            prefs[KEY_UNIVERSITY] = profile.university
            prefs[KEY_FACULTY] = profile.faculty
            prefs[KEY_MAJOR] = profile.major
            prefs[KEY_LEVEL] = profile.level
            prefs[KEY_SEMESTER] = profile.semester
            prefs[KEY_GPA] = profile.gpa
            prefs[KEY_BALANCE] = profile.balance
        }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING] = done }
    }

    suspend fun setAuthSession(email: String, token: String, loggedIn: Boolean) {
        context.dataStore.edit {
            it[KEY_AUTH_EMAIL] = email
            it[KEY_AUTH_TOKEN] = token
            it[KEY_AUTH_LOGGED_IN] = loggedIn
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit {
            it.remove(KEY_AUTH_EMAIL)
            it.remove(KEY_AUTH_TOKEN)
            it[KEY_AUTH_LOGGED_IN] = false
        }
    }
}
