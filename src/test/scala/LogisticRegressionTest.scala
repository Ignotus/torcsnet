import org.junit.Test
import java.util.Scanner
import java.io.File
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector}

class LogisticRegressionTest {
  @Test
  def testTraining {
    val classLoader = getClass().getClassLoader();
    val scanner = new Scanner(new File(classLoader.getResource("test1.csv").getFile()))
    scanner.nextLine()

    // Collecting only data from trackEdgeSensors
    val X = new Array2DRowRealMatrix(4, 19)
    val tAccelerate = new ArrayRealVector(4)
    val tSteering = new ArrayRealVector(4)

    for (row <- 0 until 4) {
      val line = scanner.nextLine()
      val values = line.split(", ").map(_.toDouble)
      X.setRow(row, values.slice(4, 23))
      tAccelerate.setEntry(row, values(24))
      tSteering.setEntry(row, values(26))
    }

    val nn = new LogisticRegression()

    nn.train(X, tSteering, tAccelerate, 1)
  }
}

