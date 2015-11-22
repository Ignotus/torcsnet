import org.junit.Test
import java.util.Scanner
import java.io.{File,FileOutputStream,ObjectOutputStream}
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector}

class MLPTrainingTest {
  // Uncomment it to run
  // @Test
  def testTraining {
    val classLoader = getClass().getClassLoader()
    val scanner = new Scanner(new File("/home/ignotus/Development/data/track.csv"))

    // Collecting only data from trackEdgeSensors
    val nsensors = DataRecorder.SENSOR_TRACK_EDGE_19 - DataRecorder.SENSOR_SPEED + 1
    val data = new Array2DRowRealMatrix(21076, nsensors)
    var tAccelerate = new ArrayRealVector(21076)

    for (row <- 0 until 21076) {
      val line = scanner.nextLine()
      val values = line.split(", ").map(_.toDouble)
      data.setRow(row, values.slice(DataRecorder.SENSOR_SPEED, DataRecorder.SENSOR_TRACK_EDGE_19 + 1))
      tAccelerate.setEntry(row, values(DataRecorder.ACTION_ACCELERATION))
    }

    for (column <- DataRecorder.SENSOR_SPEED to DataRecorder.SENSOR_TRACK_EDGE_19) {
      val col = data.getColumnVector(column)
      val min = col.getMinValue
      val diff = col.getMaxValue - col.getMinValue
      data.setColumnVector(column, col.mapSubtractToSelf(min).mapDivide(diff))
    }

    val min = tAccelerate.getMinValue
    val diff = tAccelerate.getMaxValue - tAccelerate.getMinValue
    var t = tAccelerate.mapSubtractToSelf(min).mapDivide(diff)

    val setup = new MLPSetup(data.getColumnDimension, nsensors * 2)
    val nn = new MLP(setup)

    println("MLP Evaluation")
    nn.train(data.getSubMatrix(0, 21075, 0, nsensors - 1), t.getSubVector(0, 21076), 500, 0.1)
    val fos = new FileOutputStream("weights.dump");
    val oos = new ObjectOutputStream(fos);
    MatrixUtils.serializeRealVector(nn.getW, oos)
    // Serialize Scalar B
    oos.writeDouble(nn.getB)
    MatrixUtils.serializeRealMatrix(nn.getV, oos)
    MatrixUtils.serializeRealVector(nn.getA, oos)
    //var error: Double = 0
    //for (row <- 5000 until 5185) {
    //  error += Math.pow(nn.predict(data.getRowVector(row)) - t.getEntry(row), 2)
      //println((nn.predict(data.getRowVector(row)), t.getEntry(row)))
    //}
    //error /= 185

    //println("Mean squared error: " + error)
  }
}

