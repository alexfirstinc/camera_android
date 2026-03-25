package com.example.gua

object GuaCalculator {
    fun calculate(gender: String, year: Int): Int {
        val reduced = reduceToSingleDigit(year)

        val raw = if (year < 2000) {
            if (gender == "Мужской") 10 - reduced else 5 + reduced
        } else {
            if (gender == "Мужской") 9 - reduced else 6 + reduced
        }

        var gua = reduceToSingleDigit(raw)
        if (gua == 5) {
            gua = if (gender == "Мужской") 2 else 8
        }
        if (gua == 0) {
            gua = if (gender == "Мужской") 9 else 1
        }
        return gua
    }

    private fun reduceToSingleDigit(value: Int): Int {
        var n = kotlin.math.abs(value)
        while (n > 9) {
            var sum = 0
            while (n > 0) {
                sum += n % 10
                n /= 10
            }
            n = sum
        }
        return n
    }
}
