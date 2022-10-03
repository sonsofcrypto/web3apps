// Created by web3d4v on 27/05/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import web3walletcore
import Foundation

enum OpenSeaNFTsServiceError: Error {
    
    case unableToConstructURL
    case failedToDownload
    case notFound
}

final class OpenSeaNFTsService {
    
    private let web3Service: Web3ServiceLegacy
    private let networksService: NetworksService
    private let defaults: UserDefaults
    
    private let API_KEY = ""
    
    private var nfts = [NFTItem]()
    private var collections = [NFTCollection]()
    
    private var listeners: [NFTsServiceListener] = []
    
    init(
        web3Service: Web3ServiceLegacy,
        networksService: NetworksService,
        defaults: UserDefaults
    ) {
        self.web3Service = web3Service
        self.networksService = networksService
        self.defaults = defaults
    }
}

extension OpenSeaNFTsService: NFTsService {    
    
    func nft(with identifier: String, onCompletion: (Result<NFTItem, Error>) -> Void) {
        guard let nft = nfts.first(where: { $0.identifier == identifier }) else {
            onCompletion(.failure(OpenSeaNFTsServiceError.notFound))
            return
        }
        onCompletion(.success(nft))
    }
    
    func collection(
        with identifier: String,
        onCompletion: (Result<NFTCollection, Error>) -> Void
    ) {
        guard let collection = collections.first(where: { $0.identifier == identifier }) else {
            onCompletion(.failure(OpenSeaNFTsServiceError.notFound))
            return
        }
        onCompletion(.success(collection))
    }
    
    func yourNFTs(forCollection collection: String, onCompletion: (Result<[NFTItem], Error>) -> Void) {
        let nfts = nfts.filter { $0.collectionIdentifier == collection }
        onCompletion(.success(nfts))
    }
    
    func yourNftCollections(onCompletion: (Result<[NFTCollection], Error>) -> Void) {
        onCompletion(.success(collections))
    }
    
    func yourNFTs(forNetwork network: Network) -> [NFTItem] {
        // TODO: @Annon discuss how we will implement when we support more than 1 network
        nfts
    }
    
    func yourNFTs() -> [NFTItem] {
        nfts
    }
    
    func fetchNFTs(
        onCompletion: @escaping (Result<[NFTItem], Error>) -> Void
    ) {

        guard let address = try? networksService.wallet()?.address()
            .toHexStringAddress()
            .hexString
        else {
            onCompletion(.success([]))
            return
        }
        
        guard networksService.network == Network.Companion().ethereum() else {
            onCompletion(.success([]))
            return
        }
        
        guard let urlRequest = makeURLRequest(for: .assets(owner: address)) else {
            onCompletion(.failure(OpenSeaNFTsServiceError.unableToConstructURL))
            return
        }
        
        URLSession.shared.dataTask(with: urlRequest) { [weak self] data, _, error in
            guard let self = self else { return }
            guard let data = data else {
                onCompletion(.failure(error ?? OpenSeaNFTsServiceError.failedToDownload))
                return
            }

            do {
                let decoder = JSONDecoder()
                decoder.keyDecodingStrategy = .convertFromSnakeCase
                let assets = try decoder.decode(AssetList.self, from: data).assets
                self.nfts = self.filteredNFTs(from: self.makeNFTItems(from: assets))
                self.collections = self.makeNFTCollections(
                    from: assets.excluding(self.nftIdsPendingSent)
                )
                self.updateWeb3WalletNFTListeners()
                DispatchQueue.main.async {
                    onCompletion(.success(self.nfts))
                }
            } catch {
                DispatchQueue.main.async {
                    onCompletion(.failure(error))
                }
            }
        }.resume()
    }
    
    func nftSent(identifier: String) {
        var nfts = nftIdsPendingSent
        nfts.append(identifier)
        storeNFTIdsPendingSent(with: nfts)
        broadcastNFTsChanged()
    }
    
    func addListener(_ listener: NFTsServiceListener) {
        guard !listeners.contains(where: { $0 === listener}) else { return }
        listeners.append(listener)
    }
    
    func removeListener(_ listener: NFTsServiceListener) {
        listeners.removeAll { $0 === listener }
    }
}

private extension OpenSeaNFTsService {
    
    func filteredNFTs(from nfts: [NFTItem]) -> [NFTItem] {
        
        var toRemove = [String]()
        var toExclude = [String]()
        nftIdsPendingSent.forEach { nftId in
            if nfts.contains(where: { $0.identifier == nftId }) {
                toExclude.append(nftId)
            } else {
                toRemove.append(nftId)
            }
        }
        storeNFTIdsPendingSent(with: toExclude)
        return nfts.filter { nftItem in
            let id = toExclude.first { nftItem.identifier == $0 }
            return nftItem.identifier != id
        }
    }
    
    func broadcastNFTsChanged() {
        
        listeners.forEach { $0.nftsChanged() }
    }
    
    var nftIdsPendingSent: [String] {
        
        guard let data = defaults.data(forKey: nftsPendingSendKey) else { return [] }
        return (try? JSONDecoder().decode([String].self, from: data)) ?? []
    }
    
    func storeNFTIdsPendingSent(with pendingSent: [String]) {
        
        guard let data = try? JSONEncoder().encode(pendingSent) else { return }
        defaults.set(data, forKey: nftsPendingSendKey)
        defaults.synchronize()
    }
    
    var nftsPendingSendKey: String {
        let address = try? networksService.wallet()?.address()
            .toHexStringAddress()
            .hexString
        
        return "\(address ?? "-").nfts.pending.sent"
    }
    
    var host: String { "api.opensea.io" }
    
    enum Details {
        
        case assets(owner: String)
        case collections(owner: String)
        
        var path: String {
            
            switch self {
            case .assets:
                return "/api/v1/assets"
            case .collections:
                return "/api/v1/collections"
            }
        }
    }
    
    func makeURLRequest(
        for details: Details
    ) -> URLRequest? {
        
        var urlComponents = URLComponents()
        urlComponents.scheme = "https"
        urlComponents.host = host
        
        switch details {
            
        case let .assets(owner):
            urlComponents.path = details.path
            urlComponents.queryItems = [
                .init(name: "owner", value: owner)
            ]
        case let .collections(owner):
            urlComponents.path = details.path
            urlComponents.queryItems = [
                .init(name: "asset_owner", value: owner)
            ]
        }
        
        guard let url = urlComponents.url else { return nil }
        
        var urlRequest = URLRequest(url: url)
        urlRequest.allHTTPHeaderFields = [
            "X-API-KEY": API_KEY,
            "Content-Type": "application/json",
            "Accept": "application/json"
        ]
        
        return urlRequest
    }
}

private extension OpenSeaNFTsService {
    
    func makeNFTItems(from assets: [Asset]) -> [NFTItem] {
        
        assets.compactMap {
            .init(
                identifier: $0.id.stringValue,
                collectionIdentifier: $0.collection.slug,
                name: $0.name ?? "",
                properties: $0.traits.compactMap {
                    .init(name: $0.traitType, value: $0.value, info: "")
                },
                image: $0.imageUrl ?? "",
                address: $0.assetContract.address,
                schemaName: $0.assetContract.schemaName,
                tokenId: $0.tokenId
            )
        }
    }
    
    func makeNFTCollections(from assets: [Asset]) -> [NFTCollection] {
        
        var collections = [NFTCollection]()
        
        for asset in assets {
            
            guard collections.first(where: { $0.identifier == asset.collection.slug }) == nil else {
                
                continue
            }
            
            collections.append(
                .init(
                    identifier: asset.collection.slug,
                    coverImage: asset.collection.imageUrl ?? "",
                    title: asset.collection.name ?? "",
                    author: asset.collection.slug,
                    isVerifiedAccount: false,
                    authorImage: "",
                    description: asset.collection.description
                    ?? Localized("nft.detail.section.title.description.empty")
                )
            )
        }
        
        return collections
    }
}

private extension OpenSeaNFTsService {
    
    func updateWeb3WalletNFTListeners() {
        
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            self.web3Service.nftsChanged()
        }
    }
}

private extension OpenSeaNFTsService {
    
    struct AssetList: Codable {
        
        enum CodingKeys: String, CodingKey {
            case assets
        }
        
        let assets: [Asset]
        
        init(from decoder: Decoder) throws {
            
            let container = try decoder.container(keyedBy: CodingKeys.self)
            let failableAssets = try container.decode([OptionalDecodable<Asset>].self, forKey: .assets)
            self.assets = failableAssets.compactMap { $0.item }
        }
    }

    struct Asset: Codable {
        
        let id: Int
        let name: String?
        let description: String?
        let imageUrl: String?
        let assetContract: AssetContract
        let collection: Collection
        let traits: [Trait]
        let tokenId: String
    }
    
    struct AssetContract: Codable {
        
        let name: String?
        let address: String
        let assetContractType: String
        let schemaName: String
    }
    
    struct Collection: Codable {
        
        let slug: String // id
        let name: String?
        let imageUrl: String?
        let description: String?
    }
    
    struct Trait: Codable {
        
        enum CodingKeys: String, CodingKey {
            case traitType
            case value
        }
        
        let traitType: String
        let value: String
        
        init(from decoder: Decoder) throws {
            
            let container = try decoder.container(keyedBy: CodingKeys.self)
            
            self.traitType = try container.decode(String.self, forKey: .traitType)
            if let value = try? container.decode(Int.self, forKey: .value) {
                self.value = "\(value)"
            } else {
                let value = try container.decode(String.self, forKey: .value)
                self.value = value
            }
        }
    }
}

private extension Array where Element == OpenSeaNFTsService.Asset {
    
    func excluding(_ nftIds: [String]) -> [OpenSeaNFTsService.Asset] {
        
        filter { asset in !nftIds.contains { asset.id.stringValue == $0 } }
    }
}

private struct OptionalDecodable<Item : Decodable> : Decodable {
    let item: Item?
    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        self.item = try? container.decode(Item.self)
    }
}
