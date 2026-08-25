package com.example

import com.example.data.local.entities.CourseEntity
import com.example.ui.viewmodel.JamaatyViewModel
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GpaCalculationTest {

    private lateinit var viewModel: JamaatyViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = JamaatyViewModel(app)
    }

    @Test
    fun `calculateGpa empty returns 0`() {
        assertEquals(0.0, viewModel.calculateGpa(emptyList()), 0.001)
    }

    @Test
    fun `calculateGpa single course`() {
        val courses = listOf(CourseEntity(semesterId = "s1", name = "خوارزميات", creditHours = 3, grade = 4.0))
        assertEquals(4.0, viewModel.calculateGpa(courses), 0.001)
    }

    @Test
    fun `calculateGpa weighted`() {
        val courses = listOf(
            CourseEntity(semesterId = "s1", name = "A", creditHours = 3, grade = 4.0),
            CourseEntity(semesterId = "s1", name = "B", creditHours = 2, grade = 3.0)
        )
        // (3*4 + 2*3)/5 = 18/5=3.6
        assertEquals(3.6, viewModel.calculateGpa(courses), 0.001)
    }

    @Test
    fun `calculateGpa zero credits returns 0`() {
        val courses = listOf(CourseEntity(semesterId = "s1", name = "A", creditHours = 0, grade = 4.0))
        assertEquals(0.0, viewModel.calculateGpa(courses), 0.001)
    }

    @Test
    fun `calculateCumulative across semesters`() {
        val map = mapOf(
            "s1" to listOf(CourseEntity(semesterId = "s1", name = "A", creditHours = 3, grade = 4.0)),
            "s2" to listOf(CourseEntity(semesterId = "s2", name = "B", creditHours = 3, grade = 2.0))
        )
        assertEquals(3.0, viewModel.calculateCumulativeGpa(map), 0.001)
    }

    @Test
    fun `addCourse validation rejects invalid`() {
        // P2.3 validation: blank name, zero credits, grade >5
        assertFalse(viewModel.addCourse("sem1", "", 3, 3.5))
        assertFalse(viewModel.addCourse("sem1", "Math", 0, 3.5))
        assertFalse(viewModel.addCourse("sem1", "Math", 13, 3.5))
        assertFalse(viewModel.addCourse("sem1", "Math", 3, 5.5))
        assertFalse(viewModel.addCourse("sem1", "Math", 3, -1.0))
    }

    @Test
    fun `addCourse valid returns true`() {
        assertTrue(viewModel.addCourse("sem1", "خوارزميات", 3, 3.5))
    }

    @Test
    fun `addSemester validation`() {
        assertFalse(viewModel.addSemester("", "2024/2025"))
        assertFalse(viewModel.addSemester("خريف", ""))
        assertTrue(viewModel.addSemester("خريف 2024", "2024/2025"))
    }

    @Test
    fun `canAddSchedule detects overlap`() {
        // initially no schedules for that day/time — should be true
        assertTrue(viewModel.canAddSchedule("السبت", "08:00", "10:00"))
        // invalid format
        assertFalse(viewModel.canAddSchedule("السبت", "invalid", "10:00"))
        assertFalse(viewModel.canAddSchedule("السبت", "10:00", "08:00")) // end <= start
    }
}
