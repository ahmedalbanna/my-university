package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.PaymentEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `insert and get favorite`() = runTest {
        val fav = FavoriteEntity(id = "f1", type = "BOOK", title = "كتاب", subtitle = "مؤلف", faculty = "حاسوب")
        db.appDao().insertFavorite(fav)
        val list = db.appDao().getAllFavorites().first()
        assertEquals(1, list.size)
        assertEquals("f1", list[0].id)
    }

    @Test
    fun `toggle favorite via delete`() = runTest {
        val dao = db.appDao()
        dao.insertFavorite(FavoriteEntity(id = "f2", type = "BOOK", title = "t", subtitle = "s", faculty = "f"))
        dao.deleteFavoriteById("f2")
        assertEquals(0, dao.getAllFavorites().first().size)
        assertEquals(0, dao.getPaymentsCount())
    }

    @Test
    fun `payments count and insert`() = runTest {
        val dao = db.appDao()
        assertEquals(0, dao.getPaymentsCount())
        dao.insertPayment(PaymentEntity(title = "رسوم", category = "TUITION", amount = 1000.0, status = "PAID", date = "2026-01-01", referenceNo = "TXN-1", paymentMethod = "كاش"))
        assertEquals(1, dao.getPaymentsCount())
        assertEquals(1, dao.getAllPayments().first().size)
        dao.deleteAllPayments()
        assertEquals(0, dao.getPaymentsCount())
    }

    @Test
    fun `schedules count`() = runTest {
        val dao = db.appDao()
        assertEquals(0, dao.getSchedulesCount())
        dao.insertSchedule(com.example.data.local.entities.ScheduleEntity(dayOfWeek = "الأحد", subject = "رياضيات", doctor = "د. أحمد", startTime = "08:00", endTime = "10:00", room = "101", color = 0xFF0000.toInt()))
        assertEquals(1, dao.getSchedulesCount())
        dao.deleteAllSchedules()
        assertEquals(0, dao.getSchedulesCount())
    }

    @Test
    fun `notifications count and unread`() = runTest {
        val dao = db.appDao()
        assertEquals(0, dao.getNotificationsCount())
        dao.insertNotification(com.example.data.local.entities.NotificationEntity(id = "n1", title = "t", message = "m", timestamp = System.currentTimeMillis(), type = "GENERAL"))
        assertEquals(1, dao.getNotificationsCount())
        assertEquals(1, dao.getUnreadNotificationsCount().first())
        dao.markAsRead("n1")
        assertEquals(0, dao.getUnreadNotificationsCount().first())
    }

    @Test
    fun `preferences roundtrip`() = runTest {
        val dao = db.appDao()
        dao.insertPreference(com.example.data.local.entities.PreferenceEntity("k1", "v1"))
        assertEquals("v1", dao.getPreference("k1"))
        dao.insertPreference(com.example.data.local.entities.PreferenceEntity("k1", "v2"))
        assertEquals("v2", dao.getPreference("k1"))
    }
}
