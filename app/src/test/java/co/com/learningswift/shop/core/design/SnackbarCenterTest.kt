package co.com.learningswift.shop.core.design

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SnackbarCenterTest {
    @Test fun showPublishesTheMessage() = runTest {
        val snackbar = SnackbarCenter(this)
        snackbar.show("A")
        assertEquals("A", snackbar.message.value)
        snackbar.clear()
    }

    @Test fun aNewMessageReplacesTheCurrentOne() = runTest {
        val snackbar = SnackbarCenter(this)
        snackbar.show("A")
        snackbar.show("B")
        assertEquals("B", snackbar.message.value)
        snackbar.clear()
    }

    @Test fun theMessageDisappearsAfterThreeSeconds() = runTest {
        val snackbar = SnackbarCenter(this)
        snackbar.show("A")
        advanceTimeBy(2_999)
        assertEquals("A", snackbar.message.value)
        advanceTimeBy(2)
        assertNull(snackbar.message.value)
    }

    @Test fun theOldTimerDoesNotClearTheNewMessage() = runTest {
        val snackbar = SnackbarCenter(this)
        snackbar.show("A")
        advanceTimeBy(2_000)
        snackbar.show("B")
        advanceTimeBy(1_500)   // t = 3,5 s: "A" ya habría vencido
        assertEquals("B", snackbar.message.value)
        advanceTimeBy(1_600)   // t = 5,1 s: "B" cumplió sus 3 s
        assertNull(snackbar.message.value)
    }
}
