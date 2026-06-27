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