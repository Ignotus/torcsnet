import org.apache.commons.math3.linear._

object Matrix {
    def apply(width: Int, height: Int) = {
        val matrix = new Array2DRowRealMatrix(width, height)
        for (row <- 0 until width) {
            for (col <- 0 until height) {
                matrix.setEntry(row, col, Randomizer.generator.nextUniform(-0.1, 0.1))
            }
        }
        matrix
    }
}