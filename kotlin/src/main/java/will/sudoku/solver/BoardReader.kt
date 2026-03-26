package will.sudoku.solver

import will.sudoku.solver.BoardSettings.size
import will.sudoku.solver.BoardSettings.regionSize
import will.sudoku.solver.BoardSettings.symbols
import java.io.InputStream
import java.nio.file.Path

class BoardReader {

    companion object {
        @JvmStatic
        fun readBoard(path: Path): Board {
            try {
                return readBoard(path.toFile().inputStream())
            } catch (ex: Exception) {
                throw RuntimeException("Failed to read $path as a board", ex)
            }
        }

        @JvmStatic
        fun readBoard(inputStream: InputStream): Board {
            return readBoard(inputStream.bufferedReader().readText())
        }

        @JvmStatic
        fun readBoard(string: String): Board {
            // Clean input: remove whitespace and normalize line endings
            val cleanedString = string.trim().replace("\r\n", "\n").replace("\r", "\n")

            // Validate board format and extract values
            val values = validateAndParseBoard(cleanedString)

            // Validate no duplicate values in input
            validateNoDuplicates(values)

            return Board(values)
        }

        /**
         * Validates board format and converts characters to values array.
         *
         * @param string The board string to validate and parse
         * @return IntArray of values (0-8 for cells)
         * @throws ValidationException if format is invalid
         */
        private fun validateAndParseBoard(string: String): IntArray {
            // Remove separators and count valid cells
            val validValues = sequence {
                string.forEach { c ->
                    // Check if character is valid (symbol or separator)
                    if (c in symbols || c == '!' || c == '-' || c.isWhitespace()) {
                        if (c in symbols) {
                            yield(symbols.indexOf(c))
                        } else {
                            // Skip separators and whitespace
                        }
                    } else {
                        // Unknown character found
                        throw ValidationException(
                            "Invalid character '$c' found. Allowed characters: 1-9, '.', '!', '-'",
                            input = string
                        )
                    }
                }
            }.toList()

            // Validate board size
            val expectedSize = size * size
            if (validValues.size != expectedSize) {
                throw ValidationException(
                    "Expected $expectedSize cells (${size}x${size} grid), but found ${validValues.size} valid cells",
                    input = string
                )
            }

            return validValues.toIntArray()
        }

        /**
         * Validates that there are no duplicate confirmed values in rows, columns, or regions.
         *
         * @param values The values array to validate
         * @throws ValidationException if duplicates are found
         */
        private fun validateNoDuplicates(values: IntArray) {
            // Check each row for duplicates
            for (row in 0 until size) {
                val rowValues = mutableSetOf<Int>()
                for (col in 0 until size) {
                    val value = values[row * size + col]
                    if (value > 0) {
                        if (value in rowValues) {
                            throw ValidationException(
                                "Duplicate value $value found in row ${row + 1}",
                                input = null
                            )
                        }
                        rowValues.add(value)
                    }
                }
            }

            // Check each column for duplicates
            for (col in 0 until size) {
                val colValues = mutableSetOf<Int>()
                for (row in 0 until size) {
                    val value = values[row * size + col]
                    if (value > 0) {
                        if (value in colValues) {
                            throw ValidationException(
                                "Duplicate value $value found in column ${col + 1}",
                                input = null
                            )
                        }
                        colValues.add(value)
                    }
                }
            }

            // Check each region for duplicates
            for (regionRow in 0 until regionSize) {
                for (regionCol in 0 until regionSize) {
                    val regionValues = mutableSetOf<Int>()
                    for (cellRow in 0 until regionSize) {
                        for (cellCol in 0 until regionSize) {
                            val row = regionRow * regionSize + cellRow
                            val col = regionCol * regionSize + cellCol
                            val value = values[row * size + col]
                            if (value > 0) {
                                if (value in regionValues) {
                                    throw ValidationException(
                                        "Duplicate value $value found in region (${regionRow + 1}, ${regionCol + 1})",
                                        input = null
                                    )
                                }
                                regionValues.add(value)
                            }
                        }
                    }
                }
            }
        }

    }
}