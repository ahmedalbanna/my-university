package com.example.data.security

import android.content.Context
import android.content.SharedPreferences

/**
 * P3-5: Security — EncryptedSharedPreferences for balance/studentId (replaces plain PreferenceEntity for sensitive fields).
 * TODO: when adding `androidx.security:security-crypto` dep, switch to EncryptedSharedPreferences.create().
 * Currently uses plain private prefs as fallback — still private to app, never logs referenceNo (see AnalyticsLogger).
 */
class SecureStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("jamaaty_secure", Context.MODE_PRIVATE)

    fun putBalance(balance: Double) { prefs.edit().putString("secure_balance", balance.toString()).apply() }
    fun getBalance(default: Double = 250000.0): Double = prefs.getString("secure_balance", null)?.toDoubleOrNull() ?: default

    fun putStudentId(id: String) { prefs.edit().putString("secure_student_id", id).apply() }
    fun getStudentId(default: String = ""): String = prefs.getString("secure_student_id", default) ?: default

    fun clear() { prefs.edit().clear().apply() }
}
