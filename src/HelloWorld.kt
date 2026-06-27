// src/HelloWorld.kt

fun main() {
    print("hello from Kotlin")

    // Javaのクラスとメソッドを呼び出す
    val message = MessageProvider.getMessage()
    println("Kotlin says: $message")
}