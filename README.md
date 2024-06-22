# マルチスレッドアプリケーション

このプロジェクトは、初期データをScalarDBデータベースにロードするマルチスレッドTCP/IPクライアントサーバーアプリケーションを示しています。

## プロジェクト構成

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
│ │ │ └── UserLoadInitialDataCommand.java
│ │ └── resources/
│ │ └── logback.xml
└── settings.gradle

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

4. メッセージを入力してEnterキーを押します。サーバーがメッセージをエコーするはずです。例:

    ```plaintext
    Enter text: hello
    Echo: hello
    ```

5. 終了するには、`bye` と入力してEnterキーを押します。

    ```plaintext
    Enter text: bye
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

### build.gradle

Gradleビルド設定ファイルで、サーバーとクライアントを実行するタスクが含まれています。