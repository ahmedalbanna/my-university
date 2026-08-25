package com.example.data.remote

import com.example.data.repository.JamaatyRepository

/**
 * P4-3: Multi-university sync — RemoteConfig replaces static `getGovernorates()` `JamaatyRepository:153`.
 * Fetches from `JamaatyApiService.getGovernorates()` with fallback to local static list.
 * When offline, NetworkMonitor.isOnline = false → cached Room + DataStore; WorkManager syncs on reconnect.
 */
class RemoteConfig(
    private val api: JamaatyApiService,
    private val localRepo: JamaatyRepository
) {
    suspend fun getGovernorates(): List<String> = try {
        api.getGovernorates().takeIf { it.isNotEmpty() } ?: localRepo.getGovernorates()
    } catch (_: Exception) { localRepo.getGovernorates() }

    suspend fun getUniversities(governorate: String?): List<String> = try {
        api.getUniversities(governorate).takeIf { it.isNotEmpty() } ?: localRepo.getUniversitiesForGovernorate(governorate ?: "")
    } catch (_: Exception) {
        if (governorate != null) localRepo.getUniversitiesForGovernorate(governorate) else localRepo.getUniversities()
    }
}
