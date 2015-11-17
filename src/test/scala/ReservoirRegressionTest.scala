import org.junit.Test
import java.util.Scanner
import java.io.File
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector}

class ReservoirRegressionTest {
  @Test
  def testTrainingAndPredict {
    val classLoader = getClass().getClassLoader()
    val scanner = new Scanner(new File(classLoader.getResource("test1.csv").getFile()))
    scanner.nextLine()

    // Collecting only data from trackEdgeSensors
    val data = new Array2DRowRealMatrix(4, 19)
    val tAccelerate = new ArrayRealVector(4)

    for (row <- 0 until 4) {
      val line = scanner.nextLine()
      val values = line.split(", ").map(_.toDouble)
      data.setRow(row, values.slice(4, 23))
      tAccelerate.setEntry(row, values(24))
    }

    val setup = new ReservoirRegressionSetup(data.getColumnDimension)
    val nn = new ReservoirRegression(setup)

    nn.train(data, tAccelerate, 1)
    val accelerate = nn.predict(new ArrayRealVector(data.getRow(0)))
  }
}
