package com.javanumberguess

/**
 * 数当てゲームのロジック（Kotlin）
 */
class NumberGuessGame {

    private var answer: Int = generateAnswer()
    var attempts: Int = 0
        private set

    enum class Result {
        CORRECT, TOO_HIGH, TOO_LOW, INVALID
    }

    data class GuessResult(
        val result: Result,
        val attempts: Int
    )

    private fun generateAnswer(): Int = (0..100).random()

    fun guess(input: String): GuessResult {
        val number = input.trim().toIntOrNull()
        if (number == null || number < 0 || number > 100) {
            return GuessResult(Result.INVALID, attempts)
        }

        attempts++

        val result = when {
            number == answer -> Result.CORRECT
            number > answer  -> Result.TOO_HIGH
            else             -> Result.TOO_LOW
        }

        return GuessResult(result, attempts)
    }

    fun reset() {
        answer = generateAnswer()
        attempts = 0
    }
}