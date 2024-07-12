# My Multi-threaded Server-Client Application with ScalarDB

## 概要

このプロジェクトは、マルチスレッドのサーバーとクライアントアプリケーションであり、ScalarDBを使用してデータベース操作を行います。サーバーはクライアントからのリクエストを処理し、ログインとゲームの操作を提供します。
ゲームは丁半博打とじゃんけんです。

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
│ │ │ ├── User.java
│ │ │ └── command/
│ │ │ ├── GameRequestHandler.java
│ │ │ ├── MultiGameRequestHandler.java
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

3. GUIが表示されます。最初に、GUIをクリックしてゲームモードを選択します。左が1人用モード、右が2人用モードです。2人用モードを遊ぶには、、2つのターミナルでクライアントを起動する必要があります。サーバーと合わせて、3つのターミナルが必要になります。

4. ユーザIDには、"1"と"2"のみが登録されています。

## コード概要

### Main.java

アプリケーションのエントリーポイントです。引数に応じてサーバーまたはクライアントを起動します。

### TCPIPClient.java

サーバーに接続し、メッセージを送信し、サーバーの応答を表示するクライアントアプリケーションです。

### TCPIPServer.java

クライアントからの接続を待ち受け、受信したメッセージをエコーするサーバーアプリケーションです。

### User.java

サーバーが管理する、対戦ゲームのルームにいるユーザーの情報です。

### UserLoadInitialDataCommand.java

初期データをScalarDBデータベースにロードするコマンドです。

### GameRequestHandler.java

受信したゲーム開始リクエストを処理し、結果をデータベースに反映するクラスです。

### MultiGameRequestHandler.java

対戦ゲームの結果から、データベースを更新するクラスです。名前が悪いかもしれません。

### LoginRequestHandler.java

受信したログインリクエストを処理し、ユーザー情報をデータベースから取得するクラスです。

### build.gradle

Gradleビルド設定ファイルで、サーバーとクライアントを実行するタスクが含まれています。

## 通信形式

### クライアント→サーバに送信：
形式: `COMMAND USER_ID その他のパラメータ`

- **ログインリクエスト**
`LOGIN USER_ID`

- **ゲーム開始リクエスト**
`GAME USER_ID COIN CHOICE`

- **対戦ゲーム マッチング開始リクエスト**
`MULTIGAME_MATCH USER_ID`

- **対戦ゲーム 手選択リクエスト**
`MULTIGAME_HAND USER_ID HAND`
ただし、HANDは0, 2, または5とする。それぞれグー、チョキ、パーに対応する(指の本数)。

### サーバ→クライアントに送信：
形式: `COMMAND STATE その他のパラメータ`

- **ログインレスポンス**
`LOGIN SUCCESS COIN`
`LOGIN FAIL`

- **ゲーム開始レスポンス**
`GAME WIN/LOSE DICE1 DICE2 UPDATED_COIN`

- **対戦ゲーム マッチング開始レスポンス**
`M`

- **対戦ゲーム マッチング完了・手要求レスポンス**
`H`

- **対戦ゲーム ゲーム完了レスポンス**
`W`, `L`, または`D`
それぞれ、win, lose, drawに対応

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
`LOGIN USER_ID`に反応して呼び出される。

- データベースにアクセスして、適切なユーザと現在のコイン数を取得
- 取得した情報をクライアントに送信

#### ゲームクラス
`GAME USER_ID COIN CHOICE`に反応して呼び出される。

- 受信情報をもとに勝敗を判定
- データベースにアクセスし、勝敗に基づいてコイン数を変動
- 勝敗とコイン数をクライアントに送信
