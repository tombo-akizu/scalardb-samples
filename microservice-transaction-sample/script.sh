#!/bin/bash

# データベースを立ち上げ
docker-compose up -d mysql cassandra

# データベースにデータベーススキーマを読み込み
java -jar scalardb-schema-loader-3.12.2.jar --config database-mysql.properties --schema-file customer-service-schema.json
java -jar scalardb-schema-loader-3.12.2.jar --config database-cassandra.properties --schema-file order-service-schema.json --coordinator

# サンプルアプリケーションのdockerイメージをビルド
./gradlew docker

# マイクロサービスを立ち上げ
docker-compose up -d customer-service order-service