# phase1 KotlinをDockerを使ってbuildする

## 目標
- KotlinをDockerを使ってbuildすること。
- HelloWorldを出力する

## 内容
### 最終的なフォルダ構成
最終的なフォルダ構成はこのようになっている
```text
JavaNumberGuess/
├── compose.yaml
├── Dockerfile
├── HelloWorld.kt
└── Makefile
```

### Kotlinプログラム
まずはKotlinプログラムを作る。
このようになる。
```
// HelloWorld.kt
fun main() {
    print("hello world")
}
```

### Dockerの整備
Dockerfileとcompose.yamlを使ってビルドをする。
各ファイルを次の通りにする。

```dockerfile
# Dockerfile

# 1. 変更：openjdk から eclipse-temurin に変更
FROM eclipse-temurin:21-jdk AS build

# ビルドに必要なパッケージをインストール
RUN apt-get update && apt-get install -y wget unzip curl jq

# 最新バージョンのKotlinをインストール
RUN LATEST_KOTLIN_VERSION=$(curl -s https://api.github.com/repos/JetBrains/kotlin/releases/latest | jq -r .tag_name | sed 's/^v//') && \
    wget https://github.com/JetBrains/kotlin/releases/download/v${LATEST_KOTLIN_VERSION}/kotlin-compiler-${LATEST_KOTLIN_VERSION}.zip && \
    unzip kotlin-compiler-${LATEST_KOTLIN_VERSION}.zip -d /opt && \
    ln -s /opt/kotlinc/bin/kotlinc /usr/local/bin/kotlinc

WORKDIR /app
# カレントディレクトリ直下にあるHelloWorld.ktを使う
COPY HelloWorld.kt .
RUN /usr/local/bin/kotlinc HelloWorld.kt -include-runtime -d HelloWorld.jar

# 6. 実行環境の準備
# 15. 変更：こちらも合わせて eclipse-temurin のJRE（またはJDK）に変更
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/HelloWorld.jar .

# コンテナ起動時に実行するコマンド
ENTRYPOINT ["java", "-jar", "HelloWorld.jar"]
```



```yaml
# compose.yaml

name: JavaNumberGuess

services:
  android:
    container_name: android-build

    build:
      context: .
      dockerfile: Dockerfile
```
buildのみなので、composeは簡単なものになっている。


## 開発補助
Makefileを作る。
```makefile
# Makefile
# メモ => PHONY: ファイルではないという指定(ファイルは更新されていないと実行されない): 命令である

# ========================================
# 開発コマンド
# ========================================
.PHONY: build stop down ps

## Dockerを立ち上げ、プロジェクトのビルドをする
build:
	docker compose up --build

## Dockerを停止する
stop:
	docker compose stop


## DockerをDownさせる
down:
	docker compose down

## 立ち上がっているかを確認する
ps:
	docker compose ps

# ==================================
### その他 (Utilities)
# ==================================
.PHONY: chown tree help

## カレントディレクトリ内の全ファイルに権限の付与
chown:
	sudo chown -R $(shell whoami):$(shell whoami) .

## フォルダツリーを表示 (自作Pythonスクリプト実行)
tree:
	python3 ./generate_tree_ver2.py . 100 target .git .sqlx frontend

## このMakefileのヘルプメッセージを表示
# `#`が3つのものを検知し、グループ名を表示している
# `#`が2つのものを検知し、そのあとのkeyと組み合わせることでhelpを表示している
help:
	@awk '/^### / {print ""; printf "\033[1;35m%s\033[0m\n", substr($$0, 5); next} /^## / {desc=substr($$0, 4)} /^[a-zA-Z_-]+:/ {if (desc) {sub(/:.*/, "", $$1); printf "  \033[36m%-15s\033[0m %s\n", $$1, desc; desc=""}}' $(MAKEFILE_LIST)
```


## github
gitをする
```bash
git init
git branch -m main
```