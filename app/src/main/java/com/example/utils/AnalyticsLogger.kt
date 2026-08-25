package com.example.utils

import android.util.Log

/**
 * P3-4: Observability stub — wraps Firebase Crashlytics/Analytics/Performance.
 * Currently logs to Logcat and is ready to plug Crashlytics when dep added:
 *   implementation("com.google.firebase:firebase-crashlytics")
 *   implementation("com.google.firebase:firebase-analytics")
 * Never logs referenceNo or password (P3-5 security).
 */
object AnalyticsLogger {

    private const val TAG = "JamaatyAnalytics"

    fun logEvent(name: String, params: Map<String, Any?> = emptyMap()) {
        // Filter sensitive keys
        val safe = params.filterKeys { it != "referenceNo" && it != "password" && it != "token" }
        Log.d(TAG, "event=$name params=$safe")
        // FirebaseAnalytics.getInstance(context).logEvent(name, bundle) — when configured
    }

    fun logPaymentSuccess(amount: Double, method: String) {
        logEvent("payment_success", mapOf("amount" to amount, "method" to method))
    }

    fun logGpaCalculated(gpa: Double, credits: Int) {
        logEvent("gpa_calculated", mapOf("gpa" to gpa, "credits" to credits))
    }

    fun logError(throwable: Throwable, context: String) {
        Log.e(TAG, "error context=$context", throwable)
        // FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}
