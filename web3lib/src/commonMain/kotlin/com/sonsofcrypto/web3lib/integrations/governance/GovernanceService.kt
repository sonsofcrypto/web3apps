package com.sonsofcrypto.web3lib.integrations.governance

import com.sonsofcrypto.web3lib.abi.Contract
import com.sonsofcrypto.web3lib.provider.Provider
import com.sonsofcrypto.web3lib.legacy.LegacySigner

/** Governance supports `GovernorAlpha`, `GovernorBravo` & Compound
 * compatibility */
interface GovernanceService {
    /** Fetches list of proposals */
    suspend fun proposals(): List<com.sonsofcrypto.web3lib.integrations.governance.GovernanceProposal>
    /** Vote on proposals */
    suspend fun vote(proposal: com.sonsofcrypto.web3lib.integrations.governance.GovernanceProposal, value: Int)
    // TODO: Add all the other relevant methods like delegating votes etc
}

class DefaultGovernanceService(
    val contract: Contract,
    val provider: Provider,
    val legacySigner: LegacySigner
): com.sonsofcrypto.web3lib.integrations.governance.GovernanceService {

    override suspend fun proposals(): List<com.sonsofcrypto.web3lib.integrations.governance.GovernanceProposal> {
        TODO("Implement")
    }

    override suspend fun vote(proposal: com.sonsofcrypto.web3lib.integrations.governance.GovernanceProposal, value: Int) {
        TODO("Implement")
    }
}

