import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.random.RandomDataGenerator;

public class Randomizer {
    public static RandomDataGenerator generator = new RandomDataGenerator();

    public static double newScalar() {
        return Randomizer.generator.nextUniform(-1, 1);
    }

    public static RealVector newVector(int size) {
        RealVector vec = new ArrayRealVector(size);
        for (int i = 0; i < size; ++i)
            vec.setEntry(i, Randomizer.generator.nextUniform(-1, 1));
        return vec;
    }

    public static RealMatrix newMatrix(int width, int height) {
        RealMatrix matrix = new Array2DRowRealMatrix(width, height);
        for (int row = 0; row < width; ++row)
            for (int col = 0; col < height; ++col)
                matrix.setEntry(row, col, Randomizer.generator.nextUniform(-0.1, 0.1));
        return matrix;
    }
}