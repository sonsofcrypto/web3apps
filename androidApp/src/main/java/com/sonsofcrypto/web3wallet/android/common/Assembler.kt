package com.sonsofcrypto.web3wallet.android.common

enum class AssemblerRegistryScope { SINGLETON, INSTANCE }

interface Assembler {
    fun configure(components: List<AssemblerComponent>)
    fun <T> resolve(name: String): T
}

interface AssemblerResolver {
    fun <T> resolve(name: String): T
}

interface AssemblerRegistry {
    fun register(
        name: String,
        scope: AssemblerRegistryScope,
        factory: (AssemblerResolver) -> Any,
    )
}

interface AssemblerComponent {
    fun register(to: AssemblerRegistry)
}

class DefaultAssembler(): Assembler {
    private val container = Container()

    override fun configure(components: List<AssemblerComponent>) {
        components.forEach { it.register(container) }
    }
    override fun <T> resolve(name: String): T = container.resolve(name)
}

private class Container: AssemblerRegistry, AssemblerResolver {
    private var factories = mutableMapOf<String, Pair<AssemblerRegistryScope, (AssemblerResolver) -> Any>>()
    private var sharedInstances = mutableMapOf<String, Any>()

    override fun register(
        name: String,
        scope: AssemblerRegistryScope,
        factory: (AssemblerResolver) -> Any,
    ) {
        factories[name] = Pair(scope, factory)
    }

    override fun <T> resolve(name: String): T {
        val pair = factories[name] ?: throw Error("[Assembler] Name $name not registered.")
        return when (pair.first) {
            AssemblerRegistryScope.SINGLETON -> {
                sharedInstances[name]?.let { return it as T }
                val instance = pair.second(this)
                sharedInstances[name] = instance
                return instance as T
            }
            AssemblerRegistryScope.INSTANCE -> {
                pair.second(this) as T
            }
        }
    }
}