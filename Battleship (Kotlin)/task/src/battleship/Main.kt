package battleship

const val BOARD_SIZE = 10
val LETTERS = ('A'..'J').toList()
val SHIPS = listOf(
    "Aircraft Carrier" to 5, "Battleship" to 4, "Submarine" to 3, "Cruiser" to 3, "Destroyer" to 2
)

fun createBoard(): Array<Array<Char>> {
    return Array(BOARD_SIZE) { Array(BOARD_SIZE) { '~' } }
}

fun convertCoords(coord: String): Pair<Int, Int> {
    val row = LETTERS.indexOf(coord[0].uppercaseChar())
    val col = coord.substring(1).toInt() - 1
    return Pair(row, col)
}

fun isValidPlacement(
    board: Array<Array<Char>>, start: Pair<Int, Int>, end: Pair<Int, Int>, size: Int
): Pair<Boolean, String> {
    val (startRow, startCol) = start
    val (endRow, endCol) = end
    if (startRow != endRow && startCol != endCol) {
        return Pair(false, "Wrong ship location!")
    }
    if (kotlin.math.abs(endRow - startRow) + 1 != size && kotlin.math.abs(endCol - startCol) + 1 != size) {
        return Pair(false, "Wrong length of the ship!")
    }
    for (i in (startRow - 1)..(endRow + 1)) {
        for (j in (startCol - 1)..(endCol + 1)) {
            if (i in 0 until BOARD_SIZE && j in 0 until BOARD_SIZE && board[i][j] == 'O') {
                return Pair(false, "You placed it too close to another one.")
            }
        }
    }
    return Pair(true, "")
}

fun placeShip(board: Array<Array<Char>>, start: Pair<Int, Int>, end: Pair<Int, Int>): List<Pair<Int, Int>> {
    val (startRow, startCol) = start
    val (endRow, endCol) = end
    val coordinates = mutableListOf<Pair<Int, Int>>()
    if (startRow == endRow) { // Horizontal placement
        for (col in startCol..endCol) {
            board[startRow][col] = 'O'
            coordinates.add(Pair(startRow, col))
        }
    } else { // Vertical placement
        for (row in startRow..endRow) {
            board[row][startCol] = 'O'
            coordinates.add(Pair(row, startCol))
        }
    }
    return coordinates
}

fun printBoard(board: Array<Array<Char>>) {
    print("  ")
    for (i in 1..BOARD_SIZE) {
        print("$i ")
    }
    println()
    for (i in 0 until BOARD_SIZE) {
        print("${LETTERS[i]} ")
        println(board[i].joinToString(" "))
    }
}

fun printBoardWithFog(board: Array<Array<Char>>) {
    print("  ")
    for (i in 1..BOARD_SIZE) {
        print("$i ")
    }
    println()
    for (i in 0 until BOARD_SIZE) {
        print("${LETTERS[i]} ")
        println(board[i].joinToString(" ") { if (it == 'O') "~" else it.toString() })
    }
}

fun takeShot(
    board: Array<Array<Char>>,
    hiddenBoard: Array<Array<Char>>,
    shipCells: MutableMap<String, MutableList<Pair<Int, Int>>>,
    playerName: String
) {
    while (true) {
        println("$playerName, it's your turn:")
        val input = readlnOrNull()
        if (input.isNullOrBlank() || input.length < 2) {
            println("Error! You entered the wrong coordinates! Try again:")
            continue
        }
        val shotCoords = convertCoords(input)
        val (row, col) = shotCoords
        if (row !in 0 until BOARD_SIZE || col !in 0 until BOARD_SIZE) {
            println("Error! You entered the wrong coordinates! Try again:")
            continue
        }
        when (board[row][col]) {
            'O' -> {
                board[row][col] = 'X'
                hiddenBoard[row][col] = 'X'
                printBoardWithFog(hiddenBoard)
                println("You hit a ship!")
                var shipSunk = false
                var shipToRemove: String? = null
                for ((shipName, coordinates) in shipCells) {
                    if (coordinates.contains(shotCoords)) {
                        coordinates.remove(shotCoords)
                        if (coordinates.isEmpty()) {
                            shipSunk = true
                            shipToRemove = shipName
                            break
                        }
                    }
                }
                if (shipSunk) {
                    if (shipCells.values.flatten().isEmpty()) {
                        println("You sank the last ship. You won. Congratulations!")
                        return
                    } else {
                        println("You sank a ship! Specify a new target:")
                        shipCells.remove(shipToRemove)
                    }
                }
            }

            '~' -> {
                board[row][col] = 'M'
                hiddenBoard[row][col] = 'M'
                printBoardWithFog(hiddenBoard)
                println("You missed!")
            }

            'X', 'M' -> {
                if (board[row][col] == 'X') {
                    println("You hit a ship!")
                } else {
                    println("You missed!")
                }
            }

            else -> {
                println("Error! Unexpected value in the board! Try again:")
                continue
            }
        }
        break
    }
    println("Press Enter and pass the move to another player")
    readln()
    println("\n".repeat(50))
}

fun setupShips(playerName: String): Pair<Array<Array<Char>>, MutableMap<String, MutableList<Pair<Int, Int>>>> {
    val board = createBoard()
    val shipCells = mutableMapOf<String, MutableList<Pair<Int, Int>>>()
    println("$playerName, place your ships on the game field")
    printBoard(board)
    for ((name, size) in SHIPS) {
        while (true) {
            println("Enter the coordinates of the $name ($size cells):")
            val input = readlnOrNull()?.split(" ") ?: continue
            if (input.size != 2) {
                println("Error! Invalid input. Try again:")
                continue
            }
            val start = convertCoords(input[0])
            val end = convertCoords(input[1])
            val (sortedStart, sortedEnd) = if (start.first > end.first || start.second > end.second) {
                end to start
            } else {
                start to end
            }
            val (valid, message) = isValidPlacement(board, sortedStart, sortedEnd, size)
            if (valid) {
                val shipCoordinates = placeShip(board, sortedStart, sortedEnd)
                printBoard(board)
                shipCells[name] = shipCoordinates.toMutableList()
                break
            } else {
                println("Error! $message Try again:")
            }
        }
    }
    println("Press Enter and pass the move to another player")
    readln()
    println("\n".repeat(50))
    return Pair(board, shipCells)
}

fun playBattleship() {
    val (player1Board, player1Ships) = setupShips("Player 1")
    val player1HiddenBoard = createBoard()
    val (player2Board, player2Ships) = setupShips("Player 2")
    val player2HiddenBoard = createBoard()
    println("The game starts!")
    while (true) {
        printBoardWithFog(player2HiddenBoard)
        println("---------------------")
        printBoard(player1Board)
        takeShot(player2Board, player2HiddenBoard, player2Ships, "Player 1")
        if (player2Ships.values.flatten().isEmpty()) break
        printBoardWithFog(player1HiddenBoard)
        println("---------------------")
        printBoard(player2Board)
        takeShot(player1Board, player1HiddenBoard, player1Ships, "Player 2")
        if (player1Ships.values.flatten().isEmpty()) break
    }
}

fun main() {
    playBattleship()
}
