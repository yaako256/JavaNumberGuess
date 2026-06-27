#  Java + Kotlin混合ビルド (言語間の連携を理解)

ビルドがむずいのでPhase3にスキップ


ただ、Dockerを編集まではしてもいいかもしれない。


## 最終的なフォルダ構成
```text
JavaNumberGuess/
├── compose.yaml
├── Dockerfile
├── HelloWorld.kt
├── Makefile
└── src/
    ├── HelloWorld.kt
    └── MessageProvider.java
```


## MessageProvider.javaについて
```java
// src/MessageProvider.java

public class MessageProvider {
    public static String getMessage() {
        return "Hello from Java!";
    }
}
```


## Dockerfileを編集
変更点

- srcを使ったbuild

```dockerfile
# Dockerfile

# ========================================
# Builder Stage
# ========================================
FROM eclipse-temurin:21-jdk AS build

# ビルドに必要なパッケージをインストール
RUN apt-get update && apt-get install -y wget unzip curl jq

# 最新バージョンのKotlinをインストール
RUN LATEST_KOTLIN_VERSION=$(curl -s https://api.github.com/repos/JetBrains/kotlin/releases/latest | jq -r .tag_name | sed 's/^v//') && \
    wget https://github.com/JetBrains/kotlin/releases/download/v${LATEST_KOTLIN_VERSION}/kotlin-compiler-${LATEST_KOTLIN_VERSION}.zip && \
    unzip kotlin-compiler-${LATEST_KOTLIN_VERSION}.zip -d /opt && \
    ln -s /opt/kotlinc/bin/kotlinc /usr/local/bin/kotlinc

# WORKDIRの設定
WORKDIR /app

# カレントディレクトリ直下にあるsrcを使う
COPY src/ ./src

# src内のすべてのKotlinとJavaファイルを一括でコンパイル
# -------------------------------------
# このコマンドについて
# - /usr/local/bin/kotlinc : Kotlinコンパイラ本体
# - src/*.kt src/*.java : 対象ファイルの選択
# -include-runtime : Kotlinを動かすために必要なコアライブラリ（部品）」を、作成するJARファイルの中に全部詰め込むという命令
# -d aaaaaaa.jar : 出力ファイル名の指定
# --------------------------------------
RUN /usr/local/bin/kotlinc src/*.kt src/*.java -include-runtime -d HelloWorld.jar

# ========================================
# Execute Stage
# ========================================
# 実行環境の準備
FROM eclipse-temurin:21-jre

# WORKDIRの設定
WORKDIR /app

# ビルド成果物をコピー
COPY --from=build /app/HelloWorld.jar .

# コンテナ起動時に実行するコマンド
ENTRYPOINT ["java", "-jar", "HelloWorld.jar"]

```