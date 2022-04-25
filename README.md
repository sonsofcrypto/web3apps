# web3wallet

### NOTE: Currently under active development. MVP within 4 weeks.

This repo contains iOS, Android and React browser extension apps. All apps use shared ~GoLang~ Kotlin `web3lib` for all of web3 needs. Creating and managing wallets, signing transactions, connecting to networks, interacting with smart contracts.

## Compilation instructions
- Clone and checkout submodules `web3lib`
```
git clone https://github.com/sonsofcrypto/web3wallet.git
git submodule update --init --recursive
```
- ~currently `web3lib` have to build manually instuctions [here](https://github.com/sonsofcrypto/web3lib). This will soon be automated.~


## TODO: MVP 1.0 4 weeks
	- [] w1 Key Store and associated UI (create, restore bip39, bip44, bip32)
	- [x] bip39
	- [] bip44
	- [] Add support for 24 word mnemonics (option to select & text to scale down when entering)
	- [] Import private key
	- [] Handle multiple accounts from from same mnemonic
	- [] Connect harware wallet
	- [] Create multisig

## UI tasks:
	- [ ] Fix keychain storage bug
	- [ ] Add bip39 validation 
	- [ ] Add inset for keybord and scroll to fiedl when keyboard comes up
 	- [ ] Implement custom layout for separators
 	- [ ] Fix add wallet dismiss animation when in cards mode
	- [ ] Interactive gesture for dismisg card flip
	- [ ] Validation UI for importing menmonic
	- [ ] Create password VC
	- [ ] Play haptics when swipe starts

	- [] w2 Connection to networks
		- [] RPC APIs
		- [] Run light client
		- [] Pocket network
		- [] Infura
		- [] Alchyme
	- [] w3 Homescreen
	- [] w4 Send / Recieve

## TODO(web3dgn):
	- [] Coin Gecko Currency metadata service
	- [] Add swipe to delete to wallets scree

## TODO(design4.crypto):
	- [] NFTs
	- [] Redesign enter PIN / Pass / Salt

## MVP 2.0 4 weeks
	- [] Additional networks support
	- [] As many integrations as posible
	- [] Stuff left out from MVP 1.0

For more info [sonsOfCrypto.com](https://sonsofcrypto.com/)
