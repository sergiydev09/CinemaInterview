package com.cinema.core.ui.lazylist

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableIntState

@SuppressLint("AutoboxingStateValueProperty")
internal class IntStateInterceptor(
    private val state: MutableIntState,
    private val keyRemover: () -> Unit
) : MutableIntState by state {
    override var intValue: Int
        get() {
            keyRemover()
            return state.intValue
        }
        set(value) {
            state.intValue = value
        }

    override var value: Int
        get() {
            keyRemover()
            return state.value
        }
        set(value) {
            state.value = value
        }

    val intValueDirect: Int get() = state.intValue
}
