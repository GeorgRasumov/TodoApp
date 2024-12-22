package com.georg.todoapp.data

import java.time.LocalDate

/**
 * Represents a type of date and its associated value. All properties are immutable.
 *
 * @property type The type of the date (e.g., FIXED_DATE, NO_DATE, WAITING).
 * @property date An optional LocalDate associated with the type.
 */
class DateType private constructor(
    val type: Type,
    val date: LocalDate? = null,
) {
    enum class Type {
        FIXED_DATE,
        NO_DATE,
        WAITING,
    }

    companion object {
        private val cache = mutableMapOf<Pair<Type, LocalDate?>, DateType>()

        /**
         * Returns a unique instance of DateType for the given type and date.
         *
         * @param type The type of the date.
         * @param date An optional LocalDate associated with the type.
         * @return A unique instance of DateType.
         *
         * @throws IllegalArgumentException if type is FIXED_DATE but no date is provided.
         */
        fun getInstance(
            type: Type,
            date: LocalDate? = null,
        ): DateType {
            if (type == Type.FIXED_DATE && date == null) {
                throw IllegalArgumentException("A date must be provided for FIXED_DATE type.")
            }
            val key = Pair(type, date)
            return cache.getOrPut(key) { DateType(type, date) }
        }
    }
}
