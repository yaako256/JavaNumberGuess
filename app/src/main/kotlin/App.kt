// app/src/main/kotlin/App.kt
package main

fun main() {
    println("hello from Kotlin")

    // Javaのクラスとメソッドを呼び出す
    val message = MessageProvider.getMessage()
    println("Kotlin says: $message")

}