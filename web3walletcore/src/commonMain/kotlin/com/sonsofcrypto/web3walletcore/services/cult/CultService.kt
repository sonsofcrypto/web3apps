package com.sonsofcrypto.web3walletcore.services.cult

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.extensions.jsonDecode
import com.sonsofcrypto.web3lib.extensions.jsonEncode
import com.sonsofcrypto.web3lib.extensions.stdJson
import com.sonsofcrypto.web3lib.legacy.BlockNumberToTimestampHelper
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal.GuardianInfo
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal.ProjectDocuments
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal.ProjectDocuments.Document.Link
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal.ProjectDocuments.Document.Note
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal.Status.CLOSED
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal.Status.PENDING
import com.sonsofcrypto.web3walletcore.services.cult.CultProposalJSON.Description
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.path
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.Charsets
import kotlinx.serialization.Serializable

data class CultServiceResponse(
    val pending: List<CultProposal>,
    val closed: List<CultProposal>,
)

interface CultService {
    @Throws(Throwable::class)
    suspend fun fetch(): CultServiceResponse
}

class DefaultCultService(
    private val store: KeyValueStore,
): CultService{

    private val client: HttpClient by lazy {
        HttpClient {
            Logging { level = LogLevel.NONE; logger = Logger.SIMPLE }
            install(ContentNegotiation) { json(
                stdJson, ContentType.Application.Json.withCharset(
                    Charsets.UTF_8)) }
        }
    }

    @Throws(Throwable::class)
    override suspend fun fetch(): CultServiceResponse {
        var pending: List<CultProposal>
        var closed: List<CultProposal>
        var err: Throwable? = null
        try {
            pending = fetch("pending").toCultProposals(PENDING)
            storePending(pending)
        } catch (e: Throwable) {
            pending = pending()
            err = e
        }
        try {
            closed = fetch("closed").toCultProposals(CLOSED)
            storeClosed(closed)
        } catch (e: Throwable) {
            closed = closed()
            err = e
        }
        if (pending.isEmpty() && closed.isEmpty() && err != null) { throw err }
        return CultServiceResponse(pending(), closed())
    }

    private fun storePending(proposals: List<CultProposal>) {
        store[pendingKey] = jsonEncode(proposals)
    }
    private fun pending(): List<CultProposal> =
        jsonDecode(store[pendingKey] ?: "[]") ?: emptyList()
    private val pendingKey = "pending"

    private fun storeClosed(proposals: List<CultProposal>) {
        store[closedKey] = jsonEncode(proposals)
    }
    private fun closed(): List<CultProposal> =
        jsonDecode(store[closedKey] ?: "[]") ?: emptyList()
    private val closedKey = "closed"

    @Throws(Throwable::class)
    private suspend fun fetch(proposals: String): List<CultProposalJSON> {
        val body = client.get {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append("X-API-KEY", "")
            }
            url {
                protocol = URLProtocol.HTTPS
                host = "api.cultdao.io"
                path("proposals/$proposals")
                parameters.append("format", "json")
            }
        }.bodyAsText().replace("\"description\":\"\"", "\"description\":{}")
        return jsonDecode<CultProposalServiceJSON>(body)?.data ?: emptyList()
    }
}

private fun List<CultProposalJSON>.toCultProposals(
    status: CultProposal.Status
): List<CultProposal> =
    map { it.toCultProposal(status) }

private fun CultProposalJSON.toCultProposal(status: CultProposal.Status): CultProposal {
    return CultProposal(
        id,
        "#" + id + " " + description.projectName,
        approvedVotes(),
        rejectedVotes(),
        endDate(),
        description.guardianInfo(),
        description.shortDescription ?: "",
        projectDocuments(),
        description.cultReward(),
        description.rewardDistribution(),
        description.wallet ?: "",
        status,
        stateName
    )
}

private fun CultProposalJSON.approvedVotes(): Double =
    forVotes.toDouble() * 0.000000000000000001

private fun CultProposalJSON.rejectedVotes(): Double =
    againstVotes.toDouble() * 0.000000000000000001

private fun CultProposalJSON.endDate(): Double =
    BlockNumberToTimestampHelper.timestamp(endBlock, Network.ethereum())?.toDouble() ?: 0.toDouble()

private fun Description.guardianInfo(): GuardianInfo? {
    val proposal = guardianProposal ?: return null
    val discord = guardianDiscord ?: return null
    val address = guardianAddress ?: return null
    return GuardianInfo(proposal, discord, address)
}

private fun CultProposalJSON.projectDocuments(): List<ProjectDocuments> {
    val documents: MutableList<ProjectDocuments> = mutableListOf()
    documents.add(
        ProjectDocuments(
            Localized("cult.proposals.result.liteWhitepaper"),
            listOf(Link(description.file ?: "", description.file ?: ""))
        )
    )
    val socialDocs = description.socialChannel?.replace("\n", " ") ?: ""
    val socialDocsUrls = socialDocs.split(" ").map { it }
    if (socialDocsUrls.isNotEmpty()) {
        documents.add(
            ProjectDocuments(
                Localized("cult.proposals.result.socialDocs"),
                socialDocsUrls.map { Link(it, it) }
            )
        )
    }
    val url = Url.parse(description.links ?: "")
    if (url != null) {
        documents.add(
            ProjectDocuments(
                Localized("cult.proposals.result.audits"),
                listOf(Link(url.toString(), url.toString()))
            )
        )
    } else {
        documents.add(
            ProjectDocuments(
                Localized("cult.proposals.result.audits"),
                listOf(Note(description.links ?: ""))
            )
        )
    }
    return documents
}

private fun Url.Companion.parse(str: String): Url? {
    return try {
        Url(str)
    } catch (e: Throwable) {
        null
    }
}

private fun Description.cultReward(): String =
    Localized("cult.proposal.parsing.cultRewardAllocation", range)

private fun Description.rewardDistribution(): String {
    val perString = Localized("cult.proposal.parsing.rewardDistribution.per")
    val rate = rate?.replace("%", "") ?: ""
    return "$rate% $perString $time"
}
@Serializable
private data class CultProposalServiceJSON(
    val code: Int,
    val data: List<CultProposalJSON>,
)

@Serializable
private data class CultProposalJSON(
    val id: String,
    val proposer: String,
    val eta: String,
    val startBlock: String,
    val endBlock: String,
    val forVotes: String,
    val againstVotes: String,
    val abstainVotes: String,
    val canceled: Boolean,
    val executed: Boolean,
    val description: Description,
    val state: String, // 1-Active, 2-Cancelled, 3-Defeated, 7-Executed
    val stateName: String,
) {
    @Serializable
    data class Description(
        val projectName: String?,
        val shortDescription: String?,
        val file: String?,
        val socialChannel: String?,
        val links: String?,
        val range: String?,
        val rate: String?,
        val time: String?,
        val wallet: String?,
        val guardianProposal: String?,
        val guardianDiscord: String?,
        val guardianAddress: String?,
    )
}
//
//@Serializer(forClass = BigInt::class)
//private object RangeSerializer : KSerializer<Any> {
//
//    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
//        "String", PrimitiveKind.STRING
//    )
//
//    override fun serialize(encoder: Encoder, value: String) {
//        encoder.encodeString("${value.toString()}")
//    }
//
//    override fun deserialize(decoder: Decoder): BigInt {
//        return BigInt.from(decoder.decodeString())
//    }
//}