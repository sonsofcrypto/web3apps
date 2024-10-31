package com.sonsofcrypto.web3walletcore.modules.mnemonicConfirmation

import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.extensions.stripLeadingWhiteSpace
import com.sonsofcrypto.web3walletcore.common.helpers.MnemonicInputViewModel
import com.sonsofcrypto.web3walletcore.common.helpers.MnemonicPresenterHelper
import com.sonsofcrypto.web3walletcore.common.viewModels.BarButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Header.Title
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia.SysName
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.mnemonicConfirmation.MnemonicConfirmationPresenterEvent.Confirm
import com.sonsofcrypto.web3walletcore.modules.mnemonicConfirmation.MnemonicConfirmationPresenterEvent.Dismiss
import com.sonsofcrypto.web3walletcore.modules.mnemonicConfirmation.MnemonicConfirmationPresenterEvent.MnemonicChanged
import com.sonsofcrypto.web3walletcore.modules.mnemonicConfirmation.MnemonicConfirmationPresenterEvent.SaltChanged
import com.sonsofcrypto.web3walletcore.services.mnemonic.MnemonicServiceError

sealed class MnemonicConfirmationPresenterEvent {
    object Dismiss: MnemonicConfirmationPresenterEvent()
    object Confirm: MnemonicConfirmationPresenterEvent()
    data class SaltChanged(val to: String): MnemonicConfirmationPresenterEvent()
    data class MnemonicChanged(
        val to: String,
        val cursorLocation: Int
    ): MnemonicConfirmationPresenterEvent()
}

interface MnemonicConfirmationPresenter {
    fun present()
    fun handle(event: MnemonicConfirmationPresenterEvent)
}

class DefaultMnemonicConfirmationPresenter(
    private val view: WeakRef<MnemonicConfirmationView>,
    private val wireframe: MnemonicConfirmationWireframe,
    private val interactor: MnemonicConfirmationInteractor
): MnemonicConfirmationPresenter {
    private var ctaTapped = false
    private val common = MnemonicPresenterHelper()

    override fun present() { updateView() }

    override fun handle(event: MnemonicConfirmationPresenterEvent) {
        when (event) {
            is Dismiss ->
                wireframe.navigate(
                    MnemonicConfirmationWireframeDestination.Dismiss
                )
            is MnemonicChanged -> {
                ctaTapped = false
                interactor.mnemonicStr = event.to.stripLeadingWhiteSpace()
                interactor.cursorLoc = event.cursorLocation -
                    (event.to.count() - interactor.mnemonicStr.count())
                updateView(event.to != interactor.mnemonicStr)
            }
            is SaltChanged -> {
                interactor.salt = event.to
                updateView()
            }
            is Confirm -> {
                if (!ctaTapped) {
                    ctaTapped = true
                    updateView()
                    return
                }
                if (!interactor.isMnemonicValid()) {
                    updateView()
                    return
                }
                interactor.markConfirmActionComplete()
                wireframe.navigate(
                    MnemonicConfirmationWireframeDestination.Dismiss
                )
            }
        }
    }

    private fun updateView(updateMnemonic: Boolean = false) {
        view.get()?.update(viewModel(), mnemonicItem(updateMnemonic))
    }

    private fun viewModel(): CollectionViewModel.Screen {
        val error = interactor.mnemonicError()
        var sections = mutableListOf(mnemonicSection(error))
        if (error == null && interactor.showSalt())
            sections.add(saltSection())
        return CollectionViewModel.Screen(
            Localized("mnemonicConfirmation.title"),
            sections,
            listOf(BarButtonViewModel(null, SysName("xmark"), false)),
            ctaItems = listOf(ButtonViewModel(ctaString())),
        )
    }

    private fun ctaString(): String = if (ctaTapped) {
        val isValid = interactor.isMnemonicValid()
        if (isValid) Localized("mnemonicConfirmation.cta.congratulations")
        else Localized("mnemonicConfirmation.cta.invalid")
    } else Localized("mnemonicConfirmation.cta")

    private fun saltSection(): CollectionViewModel.Section = CollectionViewModel.Section(
        Title(Localized("mnemonicConfirmation.salt")),
        listOf(
            CellViewModel.TextInput(
                "",
                interactor.salt,
                Localized("mnemonicConfirmation.salt.placeholder"),
            ),
        ),
        null,
    )

    private fun mnemonicSection(err: MnemonicServiceError?): CollectionViewModel.Section =
        CollectionViewModel.Section(
            Title(Localized("mnemonicConfirmation.confirm.wallet")),
            listOf(CellViewModel.Text(interactor.mnemonicStr)),
            mnemonicFooter(err),
        )

    private fun mnemonicItem(updateMnemonic: Boolean): MnemonicInputViewModel =
        MnemonicInputViewModel(
            interactor.potentialMnemonicWords(),
            interactor.mnemonicWordsInfo().map {
                MnemonicInputViewModel.Word(it.word, it.isInvalid)
            },
            if (ctaTapped) interactor.isMnemonicValid() else null,
            if (updateMnemonic) interactor.mnemonicStr else null,
        )

    private fun mnemonicFooter(error: MnemonicServiceError? = null): CollectionViewModel.Footer {
        if (error == null)
            return CollectionViewModel.Footer.HighlightWords(
                Localized("mnemonic.footer"),
                listOf(
                    Localized("mnemonic.footerHighlightWord0"),
                    Localized("mnemonic.footerHighlightWord1"),
                )
            )
        return when (error) {
            MnemonicServiceError.INVALID_WORD_COUNT -> CollectionViewModel.Footer.HighlightWords(
                Localized("mnemonic.error.invalid.wordCount"),
                listOf(Localized("mnemonic.error.invalid.wordCount.highlight"))
            )
            MnemonicServiceError.OTHER -> CollectionViewModel.Footer.HighlightWords(
                Localized("mnemonic.error.invalid"),
                listOf(Localized("mnemonic.error.invalid"))
            )
        }
    }
}
