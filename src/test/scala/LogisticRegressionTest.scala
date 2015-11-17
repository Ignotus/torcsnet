import org.junit.Test
import java.util.Scanner
import java.io.File
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector}

class LogisticRegressionTest {
  @Test
  def testTraining {
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

    val setup = new LogisticRegressionSetup(data.getColumnDimension)
    val nn = new LogisticRegression(setup)

    nn.train(data, tAccelerate, 1)
  }
}

