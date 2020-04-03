package org.github.jantolis.seriouscallersonly.bot

import org.github.jantolis.seriouscallersonly.dsl.Protocol

data class Bot(val protocols: Collection<Protocol>) {
    fun greet() = protocols.map { it.greeting ?: "Hello!" }
            .joinToString(", ")
}