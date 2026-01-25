package com.cinema.core.ui.lazylist

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember

/**
 * Enables smooth item animations in [LazyListState] by preventing automatic scroll
 * position adjustments when list items change.
 */
class AnimatedLazyListState(
    private val listState: LazyListState,
    enabled: Boolean = true
) {
    private val scrollPositionField = listState.javaClass.getDeclaredField("scrollPosition").apply {
        isAccessible = true
    }

    private val scrollPositionObj = scrollPositionField.get(listState)

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
                listState.firstVisibleItemIndex
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
 * Creates an [AnimatedLazyListState] that enables smooth item animations.
 */
@Composable
fun rememberAnimatedLazyListState(
    listState: LazyListState,
    enabled: Boolean = true
): AnimatedLazyListState {
    return remember(listState) {
        AnimatedLazyListState(listState, enabled)
    }.apply {
        this.enabled = enabled
    }
}
