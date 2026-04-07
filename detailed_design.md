# 詳細設計書 (Detailed Design)

本ドキュメントは、「コールドウォレットにおけるアドレス改ざん防止機能」の実装に向けた詳細設計を定義する。
設計は「ドメイン駆動設計 (DDD)」および「クリーンアーキテクチャ」の原則に基づき、プラットフォーム（Androidアプリ、スマートコントラクトなど）に依存しないビジネスルールを中心に構築する。

---

## 1. 決定された実装仕様（前提条件）

実装にあたり、以下の技術的な決定事項を前提としてシステムの構築を行う。

1. **暗号アルゴリズムと署名のペイロード**:
   - **web3jの署名ロジック**を使用する。これにより、Ethereum標準の暗号アルゴリズム（ECDSA `secp256k1`）およびweb3jのメッセージ署名フォーマットに準拠して実装する。
2. **オンラインデバイス -> コールドウォレット間のデータ転送フォーマット (QRコード)**:
   - オフライン転送におけるデータのシリアライズフォーマットは、**JSON**形式をそのまま採用して進める。
3. **署名サーバでの「ドメイン所有権」確認ロジック**:
   - 署名サーバ自体が認証局として身元を確認し署名を行う設計とするため、今回のシステム（アプリやコントラクト）側でのシステム的な所有権検証ロジックは**実装しない**。
4. **スマートコントラクトの制約に関する仕様**:
   - ドメインの重複登録制御や上書きルールなどの高度な制約は、今回は**実装しない**（シンプルなレジストリとして扱う）。

---

## 2. アーキテクチャの基本方針（Clean Architecture）

システム（特にオンラインデバイス側のアプリ）は、以下の4層で構成される。

1. **Domain 層 (Entities, Value Objects)**: ビジネスルール。他の一切の層に依存しない。
2. **Application 層 (UseCases)**: アプリケーション固有のユースケース（機能）。Domain 層のみに依存。
3. **Interface Adapters 層 (Controllers, Presenters, Gateways)**: UIやDB、外部APIなどとの変換を担う。
4. **Infrastructure 層 (Frameworks & Drivers)**: Android SDK、Retrofit、Web3jなどのライブラリ、スマートコントラクトの実装。

---

## 3. ドメイン層 (Domain Layer)

### 3.1. 値オブジェクト (Value Objects)

- **`WalletAddress`**:
  - ウォレットのアドレスを表現。
  - バリデーションルール: 空でないこと。
- **`DomainName`**:
  - ドメイン名を表現。
  - バリデーションルール: 空でないこと。
- **`ServerSignature`**:
  - 署名サーバから発行されたデジタル署名を表現。
  - バリデーションルール: 空でないこと。
- **`TransactionAmount`**: (将来的な実装予定)
  - 送金額を表現する。

### 3.2. エンティティ/データモデル (Entities & Models)

- **`DomainRegistrationData`**:
  - ドメイン名とアドレスの紐づき、およびその正当性（署名）をカプセル化したデータ。
  - プロパティ: `address: WalletAddress`, `domain: DomainName`, `signature: ServerSignature`

### 3.3. ドメインサービス (Domain Services)

- **`SignatureVerifier`**:
  - 目的: 署名サーバの公開鍵を利用して、取得した署名の正当性を検証する。

### 3.4. リポジトリインターフェース (Repository Interfaces)

- **`SmartContractRepository`**:
  - スマートコントラクトおよびブロックチェーンネットワークへのアクセスを抽象化。
  - `suspend fun resolveDomain(domain: String): Result<String>` (※本来的にはドメイン、アドレス、署名のセットを返すべきだが、現状はアドレス文字列を返す)
  - `suspend fun submitTransaction(transactionData: DomainRegistrationData): Result<String>`
  - `suspend fun sendFunds(toAddress: String, amountInWei: BigInteger): Result<String>`
- **`DomainRegistrationRepository`**:
  - 署名サーバへのアクセスを抽象化。
  - `suspend fun fetchSignature(address: WalletAddress, domain: DomainName): ServerSignature`

---

## 4. アプリケーション層 (Application Layer / UseCases)

ユーザの操作（シナリオ）をクラスとして定義する。

- **`RegisterDomainUseCase`**:
  - 目的: 署名サーバからの署名取得と検証を一貫して行う。
  - 処理: `DomainRegistrationRepository.fetchSignature()` に要求 -> 取得した `ServerSignature` を `SignatureVerifier` で検証 -> 成功時に `DomainRegistrationData` を返す。
- **`SubmitTransactionUseCase`**:
  - 目的: スマートコントラクトへのドメイン登録トランザクションを送信する。
  - 処理: `SmartContractRepository.submitTransaction()` を呼び出す。
- **`ResolveDomainUseCase`**:
  - 目的: オンラインデバイス（送金元）がドメイン名から宛先情報を取得する。
  - 処理: `SmartContractRepository.resolveDomain()` を呼び出し、結果を返す。
- **`SendFundsUseCase`**:
  - 目的: 指定したアドレスに対する送金トランザクションを実行する。
  - 処理: 入力された額を Wei に変換し、`SmartContractRepository.sendFunds()` を呼び出す。
- **`CreateColdWalletTransferDataUseCase` (追加予定)**:
  - 目的: コールドウォレットでの署名に必要なデータをまとめ、QRコード用の情報を生成する。
  - 入力: 宛先アドレス、送金額、ドメイン、およびその署名等
  - 出力: QRデータ向けのシリアライズされたDTO（JSON形式）

---

## 5. インターフェースアダプタ層 (Interface Adapters Layer)

- **ViewModels (UI層との接続)**:
  - `RegistrationViewModel`: `RegisterDomainUseCase` と `SubmitTransactionUseCase` を使用し、ドメイン登録画面（UI State）の状態を管理。
  - `SendViewModel`: `ResolveDomainUseCase` や `SendFundsUseCase` などを呼び出し、送金画面の状態（Loading, Success, Error）を管理。
- **Gateways (Repository の実装)**:
  - `SmartContractRepositoryImpl`: `SmartContractRepository` を実装。内部で Web3j などのライブラリを使い、スマートコントラクトのデータを読み書きし、送金処理も担う。
  - `DomainRegistrationRepositoryImpl`: `DomainRegistrationRepository` を実装。Retrofit 等を利用して API (`DomainRegistrationApi`) を呼び出し、署名を取得する。

---

## 6. インフラストラクチャ層 (Infrastructure Layer)

- **Smart Contract (Solidity)**:
  - `SimpleENS` などの名前で、内部状態としてドメインとアドレスの紐付けを持つコントラクト。
- **Networking**:
  - 署名サーバAPIへの HTTP Client (`RetrofitClient`).
- **QR Code Encoder (追加予定)**:
  - 送金データや署名検証データをオフラインデバイスへ渡すため、実際に画面上にQRコードを描画するためのライブラリ（Zxing 等を想定）。

---

## 7. 「コールドウォレット側」のアーキテクチャ概略 (QRコード仕様等)

本設計ではAndroidアプリ（オンラインデバイス）側を主体としているが、コールドウォレット側の責務および連携の仕様は以下の通りとなる。

1. **データ連携 (QRコード)**:
   - オンラインデバイスにて、送金先アドレス、送金額(Value)、対象ドメイン(`DomainName`)、および署名データ(`ServerSignature`)を JSON 形式でシリアライズし、QRコードとして画面に表示する。
2. **読取とデータデコード**:
   - コールドウォレットデバイス側でカメラ等を用いて上記 QR コードを読み取り、JSON をデコードする。
3. **検証とデータ変換 (Verification & Conversion)**:
   - コールドウォレット側に予め保持している「署名サーバの公開鍵」を用いて、連携された `ServerSignature` の正当性を検証する（「アドレス」と「ドメイン」の紐付けが改ざんされていないかの確認）。
   - **アドレスからドメインへの表示変換**: 署名検証に成功した場合、トランザクションの宛先である無機質な「ウォレットアドレス（例: 0x123...）」を、紐づいている人間が読める「ドメイン名（例: alice.eth）」に変換（置き換え）して内部で扱う。
4. **表示 (Display)**:
   - 変換された `Domain` 名と送金 `Amount` を画面に提示し、ユーザに対して送金先の最終確認（承認）を求める。
5. **署名 (Transaction Signing)**:
   - ユーザの承認後、コールドウォレットの秘密鍵を用いて、対象の `Address` （ドメイン名ではなく実際のアドレス）を含めた Ethereum の標準トランザクションに署名する。その後、署名済みトランザクションを再度QRコードとして出力し、オンラインデバイスへ戻す。
