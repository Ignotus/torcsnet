import org.apache.commons.math3.linear._
object Vector {
    def apply(size: Int) = {
        val vec = new ArrayRealVector(size)
        for (i <- 0 until size) {
            vec.setEntry(i, Randomizer.generator.nextUniform(-1, 1))
        }
        vec
    }
}