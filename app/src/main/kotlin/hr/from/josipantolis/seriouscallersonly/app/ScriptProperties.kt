package hr.from.josipantolis.seriouscallersonly.app

import org.springframework.core.env.AbstractEnvironment
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.core.env.get
import java.util.*

class ScriptProperties(env: AbstractEnvironment) : Properties() {
    init {
        env.propertySources
            .filterIsInstance<EnumerablePropertySource<*>>()
            .map { it.propertyNames }
            .flatMap { it.asList() }
            .filter { it.startsWith("sco.") }
            .map { it to env[it] }
            .forEach { (key, value) -> this[key] = value }
    }
}
