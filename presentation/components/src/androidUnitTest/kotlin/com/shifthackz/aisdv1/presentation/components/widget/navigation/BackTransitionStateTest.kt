package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises real composition-owned animations and navigation dispatch at their frame boundaries.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BackTransitionStateTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var state: BackTransitionState
    private var backCount = 0

    @Before
    fun setUp() {
        compose.setContent { state = rememberBackTransitionState() }
        compose.mainClock.autoAdvance = false
    }

    @Test
    fun `given back transition, completion starts, expected navigation only after settling`() {
        // Given
        compose.runOnIdle { state.updateProgress(0.3f) }
        // When
        compose.runOnIdle { state.complete({ true }) { backCount++ } }
        compose.mainClock.advanceTimeBy(64)
        // Then
        compose.runOnIdle { assertEquals(0, backCount); assertTrue(state.isActive) }
        compose.mainClock.advanceTimeBy(240)
        compose.runOnIdle {
            assertEquals(
                1,
                backCount
            ); assertTrue(state.hasCommittedGesture); assertFalse(state.isActive)
        }
    }

    @Test
    fun `given back transition, gesture is cancelled, expected restored screen without navigation`() {
        // Given
        compose.runOnIdle { state.updateProgress(0.6f) }
        // When
        compose.runOnIdle { state.cancel() }
        compose.mainClock.advanceTimeBy(320)
        // Then
        compose.runOnIdle {
            assertEquals(
                0f,
                state.progress
            ); assertFalse(state.isActive); assertEquals(0, backCount)
        }
    }

    @Test
    fun `given back transition, permission is denied initially, expected no navigation`() {
        // Given
        compose.runOnIdle { state.updateProgress(0.7f) }
        // When
        compose.runOnIdle { state.complete({ false }) { backCount++ } }
        compose.mainClock.advanceTimeBy(400)
        // Then
        compose.runOnIdle {
            assertEquals(0, backCount); assertEquals(
            0f,
            state.progress
        ); assertFalse(state.hasCommittedGesture)
        }
    }

    @Test
    fun `given back transition, permission is revoked during settling, expected cancellation instead of stale navigation`() {
        // Given
        var canCommit = true
        compose.runOnIdle { state.complete({ canCommit }) { backCount++ } }
        compose.mainClock.advanceTimeBy(64)
        // When
        compose.runOnIdle { canCommit = false }
        compose.mainClock.advanceTimeBy(480)
        // Then
        compose.runOnIdle {
            assertEquals(0, backCount); assertEquals(
            0f,
            state.progress
        ); assertFalse(state.isActive)
        }
    }

    @Test
    fun `given back transition, completion is requested twice while settling, expected one navigation`() {
        // Given
        compose.runOnIdle { state.complete({ true }) { backCount++ } }
        // When
        compose.runOnIdle { state.complete({ true }) { backCount++ } }
        compose.mainClock.advanceTimeBy(400)
        // Then
        compose.runOnIdle { assertEquals(1, backCount) }
    }

    @Test
    fun `given committed back transition, completion is requested before owner reset, expected no second navigation`() {
        // Given
        compose.runOnIdle { state.complete({ true }) { backCount++ } }
        compose.mainClock.advanceTimeBy(400)
        // When
        compose.runOnIdle { state.complete({ true }) { backCount++ } }
        compose.mainClock.advanceTimeBy(400)
        // Then
        compose.runOnIdle { assertEquals(1, backCount) }
    }

    @Test
    fun `given back transition, owner resets while settling, expected pending navigation cancelled`() {
        // Given
        compose.runOnIdle { state.complete({ true }) { backCount++ } }
        compose.mainClock.advanceTimeBy(64)
        // When
        compose.runOnIdle { state.reset() }
        compose.mainClock.advanceTimeBy(400)
        // Then
        compose.runOnIdle {
            assertEquals(0, backCount); assertEquals(
            0f,
            state.progress
        ); assertFalse(state.isActive)
        }
    }

    @Test
    fun `given committed back transition, owner resets and starts another, expected independent second navigation`() {
        // Given
        compose.runOnIdle { state.complete({ true }) { backCount++ } }
        compose.mainClock.advanceTimeBy(400)
        // When
        compose.runOnIdle { state.reset(); state.complete({ true }) { backCount++ } }
        compose.mainClock.advanceTimeBy(400)
        // Then
        compose.runOnIdle { assertEquals(2, backCount) }
    }

    @Test
    fun `given back gesture, platform progress exceeds bounds, expected bounded visual state`() {
        // Given
        compose.runOnIdle { state.updateProgress(-2f) }
        // When
        compose.runOnIdle { assertEquals(0f, state.progress); state.updateProgress(3f) }
        // Then
        compose.runOnIdle { assertEquals(1f, state.progress); assertTrue(state.isActive) }
    }

    @Test
    fun `given cancelling gesture, another drag starts, expected old settle cannot overwrite new progress`() {
        // Given
        compose.runOnIdle { state.updateProgress(0.8f); state.cancel() }
        compose.mainClock.advanceTimeBy(64)
        // When
        compose.runOnIdle { state.updateProgress(0.4f) }
        compose.mainClock.advanceTimeBy(400)
        // Then
        compose.runOnIdle { assertEquals(0.4f, state.progress); assertTrue(state.isActive) }
    }

    @Test
    fun `given committed gesture, stale pointer progress arrives, expected completed state preserved`() {
        // Given
        compose.runOnIdle { state.complete({ true }) { backCount++ } }
        compose.mainClock.advanceTimeBy(400)
        // When
        compose.runOnIdle { state.updateProgress(0.1f) }
        // Then
        compose.runOnIdle {
            assertEquals(
                1f,
                state.progress
            ); assertTrue(state.hasCommittedGesture); assertEquals(1, backCount)
        }
    }
}
