package com.cinema.core.ui.lazylist

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember

/**
 * Enables smooth item animations in [LazyGridState] by preventing automatic scroll
 * position adjustments when grid items change.
 */
class AnimatedLazyGridState(
    private val gridState: LazyGridState,
    enabled: Boolean = true
) {
    private val scrollPositionField = gridState.javaClass.getDeclaredField("scrollPosition").apply {
        isAccessible = true
    }

    private val scrollPositionObj = scrollPositionField.get(gridState)

    private val lastKeyRemover: () -> Unit =
        scrollPositionField.type.getDeclaredField("lastKnownFirstItemKey").run {
            isAccessible = true
            fun() { set(scrollPositionObj, null) }
        }

    private val indexField = scrollPositionField.type.getDeclaredField("index\$delegate").apply {
        isAccessible = true
    }

    var enabled: Boolean = enabled
        set(value) {
            if (field == value) return
            field = value
            setProps(value)
        }

    init {
        setProps(enabled)
    }

    private fun setProps(enable: Boolean) {
        val oldValue = indexField.get(scrollPositionObj).run {
            if (this is IntStateInterceptor) {
                intValueDirect
            } else {
                gridState.firstVisibleItemIndex
            }
        }

        val mutableIntState: MutableIntState = if (enable) {
            IntStateInterceptor(
                state = mutableIntStateOf(oldValue),
                keyRemover = lastKeyRemover
            )
        } else {
            mutableIntStateOf(oldValue)
        }

        indexField.set(scrollPositionObj, mutableIntState)
    }
}

/**
 * Creates an [AnimatedLazyGridState] that enables smooth item animations.
 */
@Composable
fun rememberAnimatedLazyGridState(
    gridState: LazyGridState,
    enabled: Boolean = true
): AnimatedLazyGridState {
    return remember(gridState) {
        AnimatedLazyGridState(gridState, enabled)
    }.apply {
        this.enabled = enabled
    }
}
