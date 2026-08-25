package com.example

import android.net.Uri
import com.example.ui.navigation.DeepLinkParser
import com.example.ui.navigation.DeepLinkResult
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DeepLinkParserTest {

    @Test
    fun `parse home link`() {
        val uri = Uri.parse("jamaaty://home")
        val result = DeepLinkParser.parse(uri) as DeepLinkResult.NavigateTab
        assertEquals("home", result.tabRoute)
    }

    @Test
    fun `parse library with query`() {
        val uri = Uri.parse("jamaaty://library?level=2&faculty=كلية الحاسوب وتكنولوجيا المعلومات&q=خوارزميات")
        val result = DeepLinkParser.parse(uri) as DeepLinkResult.NavigateTab
        assertEquals("library", result.tabRoute)
        assertEquals(2, result.level)
        assertEquals("كلية الحاسوب وتكنولوجيا المعلومات", result.faculty)
        assertEquals("خوارزميات", result.searchQuery)
    }

    @Test
    fun `parse translate opens overlay`() {
        val uri = Uri.parse("jamaaty://translate")
        val result = DeepLinkParser.parse(uri) as DeepLinkResult.NavigateTab
        assertEquals("more", result.tabRoute)
        assertTrue(result.openTranslate)
    }

    @Test
    fun `parse favorites`() {
        val uri = Uri.parse("jamaaty://favorites")
        val result = DeepLinkParser.parse(uri) as DeepLinkResult.NavigateTab
        assertEquals("favorites", result.tabRoute)
        assertTrue(result.openFavorites)
    }

    @Test
    fun `parse chat opens AI`() {
        val uri = Uri.parse("jamaaty://chat")
        val result = DeepLinkParser.parse(uri) as DeepLinkResult.NavigateTab
        assertTrue(result.openChat)
    }

    @Test
    fun `parse schedule`() {
        val uri = Uri.parse("jamaaty://schedule")
        val result = DeepLinkParser.parse(uri) as DeepLinkResult.NavigateTab
        assertEquals("schedule", result.tabRoute)
    }

    @Test
    fun `parse https jamaaty app`() {
        val uri = Uri.parse("https://jamaaty.app/library?level=3")
        val result = DeepLinkParser.parse(uri) as DeepLinkResult.NavigateTab
        assertEquals("library", result.tabRoute)
        assertEquals(3, result.level)
    }

    @Test
    fun `parse invalid scheme returns Invalid`() {
        val uri = Uri.parse("ftp://jamaaty.app/home")
        val result = DeepLinkParser.parse(uri)
        assertTrue(result is DeepLinkResult.Invalid)
    }

    @Test
    fun `buildLink roundtrip`() {
        val link = DeepLinkParser.buildLink(route = "library", level = 2, faculty = "كلية الهندسة والبترول", searchQuery = "شبكات")
        val uri = Uri.parse(link)
        val result = DeepLinkParser.parse(uri) as DeepLinkResult.NavigateTab
        assertEquals("library", result.tabRoute)
        assertEquals(2, result.level)
        assertEquals("كلية الهندسة والبترول", result.faculty)
        assertEquals("شبكات", result.searchQuery)
    }

    @Test
    fun `exams routes to library after removal`() {
        val uri = Uri.parse("jamaaty://exams?query=شبكات")
        val result = DeepLinkParser.parse(uri) as DeepLinkResult.NavigateTab
        assertEquals("library", result.tabRoute) // P0-6: exams now maps to library
    }
}
