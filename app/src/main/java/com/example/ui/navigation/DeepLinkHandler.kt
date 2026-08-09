package com.example.ui.navigation

import android.net.Uri

sealed class DeepLinkResult {
    data class NavigateTab(
        val tabRoute: String,
        val level: Int? = null,
        val semester: Int? = null,
        val faculty: String? = null,
        val searchQuery: String? = null,
        val difficulty: String? = null,
        val tag: String? = null,
        val openChat: Boolean = false
    ) : DeepLinkResult()

    object Invalid : DeepLinkResult()
}

object DeepLinkParser {
    fun parse(uri: Uri): DeepLinkResult {
        val scheme = uri.scheme ?: ""
        val host = uri.host ?: ""
        val path = uri.path?.removePrefix("/") ?: ""

        if (scheme != "jamaaty" && scheme != "https") {
            return DeepLinkResult.Invalid
        }

        // Target screen route determination
        val target = when {
            scheme == "jamaaty" && host.isNotEmpty() -> host
            scheme == "https" && host == "jamaaty.app" -> path.ifEmpty { "home" }
            else -> path.ifEmpty { host.ifEmpty { "home" } }
        }

        val level = uri.getQueryParameter("level")?.toIntOrNull()
            ?: uri.getQueryParameter("lvl")?.toIntOrNull()
        val semester = uri.getQueryParameter("semester")?.toIntOrNull()
            ?: uri.getQueryParameter("sem")?.toIntOrNull()
        val faculty = uri.getQueryParameter("faculty") ?: uri.getQueryParameter("fac")
        val searchQuery = uri.getQueryParameter("q") ?: uri.getQueryParameter("query")
        val difficulty = uri.getQueryParameter("difficulty") ?: uri.getQueryParameter("diff")
        val tag = uri.getQueryParameter("tag")

        return when (target.lowercase()) {
            "home", "" -> DeepLinkResult.NavigateTab("home", level = level)
            "library", "books", "projects" -> DeepLinkResult.NavigateTab(
                tabRoute = "library",
                level = level,
                semester = semester,
                faculty = faculty,
                searchQuery = searchQuery
            )
            "exams", "quiz", "questions" -> DeepLinkResult.NavigateTab(
                tabRoute = "exams",
                searchQuery = searchQuery,
                difficulty = difficulty,
                tag = tag
            )
            "wallet", "payments", "balance" -> DeepLinkResult.NavigateTab("wallet")
            "translate", "more", "favorites" -> DeepLinkResult.NavigateTab("translate_more")
            "chat", "ai", "assistant" -> DeepLinkResult.NavigateTab("home", openChat = true)
            else -> DeepLinkResult.NavigateTab("home", level = level)
        }
    }

    /**
     * Helper function to build a sharable jamaaty:// deep link string
     * based on current screen route and state filters.
     */
    fun buildLink(
        route: String,
        level: Int? = null,
        semester: Int? = null,
        faculty: String? = null,
        searchQuery: String? = null,
        difficulty: String? = null,
        tag: String? = null
    ): String {
        val builder = Uri.Builder()
            .scheme("jamaaty")
            .authority(route)

        level?.let { builder.appendQueryParameter("level", it.toString()) }
        semester?.let { builder.appendQueryParameter("semester", it.toString()) }
        faculty?.let { if (it != "الكل") builder.appendQueryParameter("faculty", it) }
        searchQuery?.let { if (it.isNotBlank()) builder.appendQueryParameter("q", it) }
        difficulty?.let { if (it != "الكل") builder.appendQueryParameter("difficulty", it) }
        tag?.let { if (it != "الكل") builder.appendQueryParameter("tag", it) }

        return builder.build().toString()
    }
}
