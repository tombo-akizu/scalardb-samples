# My Multi-threaded Server-Client Application with ScalarDB

## 概要

このプロジェクトは、マルチスレッドのサーバーとクライアントアプリケーションであり、ScalarDBを使用してデータベース操作を行います。サーバーはクライアントからのリクエストを処理し、ログインとゲームの操作を提供します。
ゲームは丁半博打です。

## プロジェクト構成

my-multi-threadディレクトリにあります。

```
my-multi-thread/
├── build.gradle
├── database.properties
├── schema.json
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── sample/
│ │ │ ├── Main.java
│ │ │ ├── TCPIPClient.java
│ │ │ ├── TCPIPServer.java
│ │ │ └── command/
│ │ │ ├── GameRequestHandler.java
│ │ │ ├── LoginRequestHandler.java
│ │ │ └── UserLoadInitialDataCommand.java
│ │ └── resources/
│ │ └── logback.xml
└── settings.gradle
```

## 前提条件

- Java Development Kit (JDK) 11以上
- Gradle 6.0以上
- Docker（Cassandraを実行するため）
- ScalarDB Schema Loader

## セットアップ

1. リポジトリをクローンします:

    ```sh
    git clone <リポジトリURL>
    cd my-multi-thread
    ```

2. Cassandraコンテナを起動します:

    ```sh
    docker-compose up -d
    ```

3. データベーススキーマをロードします:

    ```sh
    java -jar scalardb-schema-loader-3.12.2.jar --config database.properties --schema-file schema.json --coordinator
    ```

## ビルド

1. プロジェクトをビルドします:

    ```sh
    ./gradlew build
    ```

## アプリケーションの実行

### サーバー

1. ターミナルを開き、プロジェクトディレクトリに移動します。
2. サーバーを起動します:

    ```sh
    ./gradlew runServer
    ```

   次のような出力が表示されることを確認します:

    ```plaintext
    Server is listening on port 12345
    ```

### クライアント

1. 別のターミナルを開き、プロジェクトディレクトリに移動します。
2. クライアントを起動します:

    ```sh
    ./gradlew runClient
    ```

3. 次のプロンプトが表示されることを確認します:

    ```plaintext
    Enter text:
    ```

4. ログインリクエストを入力してEnterキーを押します。サーバーがログイン可能かを判定して結果を返します。
   リクエストにはユーザIDを含めます。
   レスポンスには判定結果とユーザの所持コイン数が含まれます。
   例:

    ```plaintext
    Enter text: LOGIN 1
    Sent to server: LOGIN 1
    Received from server: LOGIN SUCCESS 100
    ```

5. ゲーム開始リクエストを入力してEnterキーを押します。サーバーがゲームの勝敗を判定し、勝敗結果を返します。
　　リクエストにはユーザID, 賭けるコイン数, 選択肢(丁：0, 半：1)を含めます
　　レスポンスには勝敗、一つ目のサイコロの出目、二つ目のサイコロの出目、更新された所持コイン数が含まれます。例:

    ```plaintext
    Enter text: GAME 1 100 0
    Sent to server: GAME 1 50 0
    Received from server: GAME WIN 6 4 150
    ```

## コード概要

### Main.java

アプリケーションのエントリーポイントです。引数に応じてサーバーまたはクライアントを起動します。

### TCPIPClient.java

サーバーに接続し、メッセージを送信し、サーバーの応答を表示するクライアントアプリケーションです。

### TCPIPServer.java

クライアントからの接続を待ち受け、受信したメッセージをエコーするサーバーアプリケーションです。

### UserLoadInitialDataCommand.java

初期データをScalarDBデータベースにロードするコマンドです。

### GameRequestHandler.java

受信したゲーム開始リクエストを処理し、結果をデータベースに反映するクラスです。

### LoginRequestHandler.java

受信したログインリクエストを処理し、ユーザー情報をデータベースから取得するクラスです。

### build.gradle

Gradleビルド設定ファイルで、サーバーとクライアントを実行するタスクが含まれています。

## 通信形式

### クライアント→サーバに送信：
形式: `COMMAND USER_ID その他のパラメータ`

- **ログインリクエスト**
LOGIN USER_ID

- **ゲーム開始リクエスト**
GAME USER_ID COIN CHOICE


### サーバ→クライアントに送信：
形式: `COMMAND STATE その他のパラメータ`

- **ログインレスポンス**
LOGIN SUCCESS COIN
LOGIN FAIL

- **ゲーム開始レスポンス**
GAME WIN/LOSE DICE1 DICE2 UPDATED_COIN

## サーバ側の受信時の挙動

### メインクラス内

1. リクエストをパース
2. 各リクエストに対応するクラスを呼び出す
3. 各リクエストクラスでは、データベースにアクセスしてデータを取得/更新し、クライアントにデータを送信

### サーバ側で必要なクラス

#### メインクラス
役割: ルーティング

- ポートで待ち構えて、受信したクエリをパースし、適切なクエリクラスを呼び出す

#### ログインクラス
"LOGIN USER_ID"に反応して呼び出される。

- データベースにアクセスして、適切なユーザと現在のコイン数を取得
- 取得した情報をクライアントに送信

#### ゲームクラス
"GAME USER_ID COIN 丁か半か"に反応して呼び出される。

- 受信情報をもとに勝敗を判定
- データベースにアクセスし、勝敗に基づいてコイン数を変動
- 勝敗とコイン数をクライアントに送信