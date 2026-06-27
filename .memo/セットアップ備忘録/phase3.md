# Gradleを使ったビルド (ビルド自動化の標準ツール)

## 現段階のフォルダ構造
```
JavaNumberGuess/ d Detach
├── .gitignore
├── compose.yaml
├── Dockerfile
├── generate_tree_ver2.py
├── Makefile
├── gradle/
└── src/
    └── main/
        ├── java/
        │   └── MessageProvider.java
        └── kotlin/
            └── HelloWorld.kt
```


## 最終的なフォルダ構造
```
python3 ./generate_tree_ver2.py . 100 .git build .gradle

JavaNumberGuess/
├── .dockerignore
├── .gitattributes
├── .gitignore
├── compose.yaml
├── Dockerfile
├── gradle.properties
├── gradlew
├── gradlew.bat
├── Makefile
├── settings.gradle.kts
├── .kotlin/
│   └── sessions/
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    └── src/
        └── main/
            ├── java/
            │   └── MessageProvider.java
            └── kotlin/
                └── App.kt
```

## Gradleについて
Gradleは初期設定として、`gradle init`とかを実行して`gradlew`や`build.gradle.kts`ファイルを生成しなければいけない。
そのため、DockerをMultiStageBuildにして、開発の初回で実行する`init`と、実行を司る`runtime`の2つに分けた。


## DockerfileをGradle用にする
```Dockerfile
# Dockerfile

# ========================================
# base image
# ========================================
FROM gradle:8.14.3-jdk21 AS base

WORKDIR /workspace

COPY . .

# wrapperがなければ init 用に使う
RUN chmod +x gradlew 2>/dev/null || true

# ========================================
# dev target（init用）
# ========================================
FROM base AS init

# initだけ実行する環境
CMD ["gradle", "init"]


# ========================================
# build target（ビルド＋実行準備）
# ========================================
FROM base AS build

RUN ./gradlew build

# デバッグ（重要）
RUN ls -R /workspace/app/build/libs

# ========================================
# runtime target（最終実行）
# ========================================
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /workspace

# ビルド成果物を移動する
COPY --from=build /workspace/app/build/libs/app.jar app.jar

# docker run時に実行する内容
# アプリを実行する
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## compose.yamlを編集する
```yaml
# compose.yaml
name: JavaNumberGuess

services:
  # Gradleの初回設定用
  gradle-init:
    container_name: java_numver_guess_init

    build:
      context: .
      dockerfile: Dockerfile
      target: init

    working_dir: /workspace
    volumes:
      - .:/workspace

    stdin_open: true
    tty: true

  # ビルド/実行をする用
  app:
    container_name: java_numver_guess_build

    build:
      context: .
      dockerfile: Dockerfile
      target: runtime

    working_dir: /workspace
```

## 実行する
```bash
docker compose run --rm gradle-init
```
このコマンドを実行して、各種設定ファイルを生成する。
色々質問される。
もう忘れたが、Applicationとかkotlinを選んでいく。

## ディレクトリを移動させる
初回はテストフォルダなどが用意されている。
先述の最終的なディレクトリ構造に合わせ、移動させる。

## 設定の編集
まず、App.ktにパッケージ名を追加する
```kotlin
// app/src/main/kotlin/App.kt
package main

fun main() {
    println("hello from Kotlin")

    // Javaのクラスとメソッドを呼び出す
    val message = MessageProvider.getMessage()
    println("Kotlin says: $message")

}
```
次に、`build.gradle.kts`を編集する。
ここで、main()があるクラスを選ばなければならない。
`main.AppKt`に編集していく。
```kts
plugins {
    kotlin("jvm") version "2.2.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)
}

application {
    mainClass.set("main.AppKt")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "main.AppKt"
        )
    }
}
```

## .dockerignoreの編集
混ざったりしてコンフリクト起こす？ので、次のように編集する
```
.gradle
build
app/build
**/build
```

## 実行してみる
これで、
```bash
docker compose run --rm app
```
を実行して、Hello World! が出れば成功