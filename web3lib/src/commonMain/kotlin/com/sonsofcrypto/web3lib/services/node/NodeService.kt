package com.sonsofcrypto.web3lib.services.node

import com.sonsofcrypto.web3lib.types.Network

/** Manages lifecycle for of geth LES nodes for networks */
interface NodeService {
    /** Starts node configured for `Network` */
    fun startNode(network: Network): Node
    /** Stops node configured for `Network` */
    fun stopNode(network: Network)
    /** Returns running node for `Network` if present */
    fun node(network: Network): Node?
    /** Returns all running nodes */
    fun runningNodes(): List<Node>
    /** List of supported networks */
    fun supportedNetworks(): List<Network>
}

class DefaultNodeService: NodeService {
    private var nodes: MutableMap<ULong, Node> = mutableMapOf()
//    private var timer: Job? = null
//    private val scope = CoroutineScope(SupervisorJob() + bgDispatcher)
//
//    init {
//        timer = timerFlow(10.seconds)
//            .onEach { poll() }
//            .launchIn(scope)
//    }
//
//    private fun poll() {
//        nodes.values.forEach {
//            println(it.getNodeInfo())
//            println(it.getPeersInfo())
//        }
//    }

    override fun startNode(network: Network): Node {
        println("=== NODE starting")
        nodes[network.chainId]?.let {
            return it
        }
        val node = Node()
        node.start()
        nodes[network.chainId] = node
        return node
    }

    override fun stopNode(network: Network) {
        println("=== NODE stopping")
        nodes[network.chainId]?.close()
        nodes.remove(network.chainId)
    }

    override fun node(network: Network): Node? = nodes[network.chainId]

    override fun runningNodes(): List<Node> = nodes.values.toList()

    override fun supportedNetworks(): List<Network> = listOf(
        Network.ethereum()
    )
}
