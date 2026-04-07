# 詳細設計：ドメイン解決および送金機能

## 1. 構成概要
本機能は、既存の `SmartContractRepository` を拡張し、ドメイン名の解決（ENS相当）とEther送金を実現する。

## 2. リポジトリ層 (Repository Layer)

### 2.1. `SmartContractRepository` インターフェースの拡張
既存のインターフェースに以下のメソッドを追加する。

```kotlin
interface SmartContractRepository {
    // 既存
    suspend fun submitTransaction(transactionData: DomainRegistrationData): Result<String>
    
    // 追加
    suspend fun resolveDomain(domain: String): Result<String>
    suspend fun sendFunds(toAddress: String, amountInWei: BigInteger): Result<String>
}
```

### 2.2. `SmartContractRepositoryImpl` の実装詳細

#### `resolveDomain(domain: String)`
1. `SimpleENS.load(...)` を使用してコントラクトのラッパーをインスタンス化する。
2. `simpleENS.resolve(domain).send()` を呼び出す。
3. 戻り値の `Tuple2<String, String>` から1番目の要素（アドレス）を取得する。
4. **バリデーション**:
    - アドレスが `0x0000000000000000000000000000000000000000` (零アドレス) または空文字列の場合は `Result.failure(Exception("Domain not registered"))` を返す。
5. それ以外の場合は `Result.success(address)` を返す。

#### `sendFunds(toAddress: String, amountInWei: BigInteger)`
1. `web3j.ethGetTransactionCount(...)` を使用して `BuildConfig.WALLET_ADDRESS2` から `nonce` を取得する。
2. `web3j.ethGasPrice().send()` で `gasPrice` を取得する。
3. `RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, amountInWei)` を作成する。
    - `gasLimit` は標準的な 21,000 または余裕を持って設定する。
4. `TransactionEncoder.signMessage(rawTransaction, chainId, credentials)` で署名する。
5. `web3j.ethSendRawTransaction(hexValue).send()` で送信し、ハッシュを返す。

## 3. ドメイン層 (Domain Layer)

### 3.1. `ResolveDomainUseCase` (新規)
- `repository.resolveDomain(domain)` を呼び出すだけのシンプルなユースケース。

### 3.2. `SendFundsUseCase` (新規)
- `repository.sendFunds(address, amountInWei)` を呼び出す。
- UIから渡される Ether 単位の数値を `Convert.toWei` などで Wei に変換してリポジトリに渡す。

## 4. プレゼンテーション層 (Presentation Layer)

### 4.1. `SendUiState` (新規)
既存の `RegistrationUiState` に倣い、シールクラスで定義する。

```kotlin
sealed interface SendUiState {
    object Idle : SendUiState
    object Resolving : SendUiState
    data class Resolved(val address: String) : SendUiState
    object Sending : SendUiState
    data class Success(val txHash: String) : SendUiState
    data class Error(val message: String) : SendUiState
}
```

### 4.2. `SendViewModel` (新規)
- `domainName: String` (入力)
- `etherAmount: String` (入力)
- `resolve()`: ドメイン解決を実行。
- `send()`: 送金を実行。有効なアドレスが解決されている場合のみ実行可能。

### 4.3. `SendScreen` (新規 Compose)
- ドメイン入力フィールド。
- 「解決」ボタン。
- 解決済みアドレスの表示エリア。解決失敗時は赤文字でエラー表示。
- 金額入力フィールド。
- 「送金」ボタン（解決済みかつ金額入力時のみ有効）。
- 送金成功時にトランザクションハッシュを表示。

## 5. ナビゲーション (Navigation)

### 5.1. `SecureWalletScreen` の更新
- `Send` 項目を追加。

### 5.2. `HomeScreen` の更新
- 「送金画面へ」ボタンを追加し、クリック時に `SecureWalletScreen.Send` へ遷移するようにする。

### 5.3. `MainNavigationHost` の更新
- `SendScreen` へのルートを追加。
- `SendViewModel` を提供する。

## 6. リソース (Resources)
- `strings.xml` に以下の文字列を追加：
    - `send_title`: 「送金画面」
    - `amount_label`: 「送金金額 (Ether)」
    - `resolve_button`: 「解決」
    - `send_button`: 「送金」
    - `domain_not_found`: 「このドメインは登録されていません」
