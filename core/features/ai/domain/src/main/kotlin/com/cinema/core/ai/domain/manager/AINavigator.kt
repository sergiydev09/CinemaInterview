package com.cinema.core.ai.domain.manager

interface AINavigator {
    fun navigateTo(target: String, parameters: Map<String, String>)
}
