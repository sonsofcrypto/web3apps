package com.sonsofcrypto.web3walletcore.services.actions

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.utils.WeakRef

interface ActionsListener {
    /** This will be called each time the list of Actions changes. */
    fun actionsUpdated()
}

interface ActionsService {
    /** List of actions yet to be completed */
    fun actions(): List<Action>
    /** Mark action as completed */
    fun markComplete(action: Action)
    /** Dismiss action */
    fun dismiss(action: Action)
    /** Add a listener to the service */
    fun addListener(listener: ActionsListener)
    /** Remove a listener from the service */
    fun removeListener(listener: ActionsListener)
}

class DefaultActionsService(
    private val store: KeyValueStore,
    private val networksService: NetworksService,
): ActionsService {

    private var listeners: MutableList<WeakRef<ActionsListener>> = mutableListOf()

    override fun actions(): List<Action> {
        val actions = mutableListOf<Action>()
        if (!isCompleted(Action.ConfirmMnemonic)) {
            actions.add(Action.ConfirmMnemonic)
        }
        actions.add(Action.Themes)
        actions.add(Action.ImprovementProposals)
        return actions
    }

    override fun markComplete(action: Action) {
        store[actionKey(action)] = true
        listeners.forEach { it.value?.actionsUpdated() }
    }

    override fun dismiss(action: Action) {
        TODO("Not yet implemented")
    }

    private fun isCompleted(action: Action): Boolean =
        store.get<Boolean>(actionKey(action)) == true

    private fun actionKey(action: Action): String = when (action) {
        is Action.ConfirmMnemonic -> {
            (action::class.simpleName ?: "") +
                (networksService.wallet()?.id() ?: "")
        }
        else -> action::class.simpleName ?: "$action"
    }

    override fun addListener(listener: ActionsListener) {
        listeners.add(WeakRef(listener))
    }

    override fun removeListener(listener: ActionsListener) {
        listeners
            .first { it.get() == listener }
            .let { listeners.remove(it) }
    }
}