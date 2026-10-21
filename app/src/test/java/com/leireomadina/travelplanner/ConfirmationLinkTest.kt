package com.leireomadina.travelplanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leireomadina.travelplanner.data.auth.AuthRepository
import com.leireomadina.travelplanner.data.auth.FakeAuthRepository
import com.leireomadina.travelplanner.di.AuthModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

// Checks that the confirmation email's link reaches the repository, whether it starts the
// app or arrives while the app is open, and only once.
@HiltAndroidTest
@UninstallModules(AuthModule::class)
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class ConfirmationLinkTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val authRepository: AuthRepository = FakeAuthRepository()

    private val handledLinks get() = (authRepository as FakeAuthRepository).confirmationLinks

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `the manifest sends confirmation links to MainActivity`() {
        val activity = context.packageManager.resolveActivity(link("abc"), 0)?.activityInfo

        assertEquals(MainActivity::class.java.name, activity?.name)
    }

    @Test
    fun `a link that starts the app is handled`() {
        Robolectric.buildActivity(MainActivity::class.java, link("abc")).setup()

        assertEquals(listOf(LINK_BASE + "abc"), handledLinks.map { it.dataString })
    }

    @Test
    fun `a link that arrives while the app is open is handled`() {
        val app = Robolectric.buildActivity(MainActivity::class.java).setup()

        app.newIntent(link("abc"))

        assertEquals(LINK_BASE + "abc", handledLinks.last().dataString)
    }

    @Test
    fun `rotating the screen doesn't handle the same link again`() {
        val app = Robolectric.buildActivity(MainActivity::class.java, link("abc")).setup()

        app.recreate()

        assertEquals(1, handledLinks.count { it.dataString == LINK_BASE + "abc" })
    }

    private fun link(code: String) = Intent(Intent.ACTION_VIEW, Uri.parse(LINK_BASE + code))
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setPackage(context.packageName)

    private companion object {
        const val LINK_BASE = "travelplanner://confirm?code="
    }
}
