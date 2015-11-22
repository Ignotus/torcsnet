import org.junit.Test
import java.util.Scanner
import java.io._
import org.apache.commons.math3.linear._

class Setup1 {
  var min_diff: RealMatrix = null

  var wa: RealVector = null
  var ba: Double = 0
  var va: RealMatrix = null
  var aa: RealVector = null

  var ws: RealVector = null
  var bs: Double = 0
  var vs: RealMatrix = null
  var as: RealVector = null

  var wb: RealVector = null
  var bb: Double = 0
  var vb: RealMatrix = null
  var ab: RealVector = null
}

class MLPTrainingTest {
  def writeWeights(nn: MLP, oos: ObjectOutputStream) {
    MatrixUtils.serializeRealVector(nn.getW, oos)
    oos.writeDouble(nn.getB)
    MatrixUtils.serializeRealMatrix(nn.getV, oos)
    MatrixUtils.serializeRealVector(nn.getA, oos)
  }
  // Uncomment it to run
  // @Test
  def testTraining {
    val classLoader = getClass().getClassLoader()
    val scanner = new Scanner(new File("/home/ignotus/Development/data/track.csv"))

    // Collecting only data from trackEdgeSensors
    val nsensors = DataRecorder.SENSOR_TRACK_EDGE_19 - DataRecorder.SENSOR_SPEED + 1
    val nItems = 21076
    val data = new Array2DRowRealMatrix(nItems, nsensors)
    var tAccelerate = new ArrayRealVector(nItems)
    var tSteering = new ArrayRealVector(nItems)
    var tBraking = new ArrayRealVector(nItems)

    val fos = new FileOutputStream("weights.dump")
    val oos = new ObjectOutputStream(fos)

    for (row <- 0 until nItems) {
      val line = scanner.nextLine()
      val values = line.split(", ").map(_.toDouble)
      data.setRow(row, values.slice(DataRecorder.SENSOR_SPEED, DataRecorder.SENSOR_TRACK_EDGE_19 + 1))
      tAccelerate.setEntry(row, values(DataRecorder.ACTION_ACCELERATION))
      tSteering.setEntry(row, values(DataRecorder.ACTION_STEERING))
      tBraking.setEntry(row, values(DataRecorder.ACTION_BRAKING))
    }

    val min_diff =  new Array2DRowRealMatrix(nsensors + 3, 2)

    for (column <- DataRecorder.SENSOR_SPEED to DataRecorder.SENSOR_TRACK_EDGE_19) {
      val col = data.getColumnVector(column)
      val min = col.getMinValue
      val diff = col.getMaxValue - col.getMinValue
      min_diff.setEntry(column - DataRecorder.SENSOR_SPEED, 0, min)
      min_diff.setEntry(column - DataRecorder.SENSOR_SPEED, 1, diff)
      data.setColumnVector(column, col.mapSubtractToSelf(min).mapDivide(diff))
    }

    // Normalize tAccelerate
    val min_a = tAccelerate.getMinValue
    val diff_a = tAccelerate.getMaxValue - tAccelerate.getMinValue
    min_diff.setEntry(nsensors, 0, min_a)
    min_diff.setEntry(nsensors, 1, diff_a)
    var ta = tAccelerate.mapSubtractToSelf(min_a).mapDivide(diff_a)

    // Normalize tSteering
    val min_s = tSteering.getMinValue
    val diff_s = tSteering.getMaxValue - tSteering.getMinValue
    min_diff.setEntry(nsensors + 1, 0, min_s)
    min_diff.setEntry(nsensors + 1, 1, diff_s)
    var ts = tSteering.mapSubtractToSelf(min_s).mapDivide(diff_s)

    // Normalize tBraking
    val min_b = tBraking.getMinValue
    val diff_b = tBraking.getMaxValue - tBraking.getMinValue
    min_diff.setEntry(nsensors + 2, 0, min_b)
    min_diff.setEntry(nsensors + 2, 1, diff_b)
    var tb = tBraking.mapSubtractToSelf(min_b).mapDivide(diff_b)

    MatrixUtils.serializeRealMatrix(min_diff, oos)

    // Training acceleration
    val setup1 = new MLPSetup(data.getColumnDimension, nsensors * 2)
    val nn1 = new MLP(setup1)

    println("MLP Evaluation")
    nn1.train(data.getSubMatrix(0, nItems - 1, 0, nsensors - 1),
              ta.getSubVector(0, nItems), 500, 0.1)
    
    writeWeights(nn1, oos)

    // Training steering
    val setup2 = new MLPSetup(data.getColumnDimension, nsensors * 2)
    val nn2 = new MLP(setup2)

    println("MLP Evaluation")
    nn2.train(data.getSubMatrix(0, nItems - 1, 0, nsensors - 1),
              ts.getSubVector(0, nItems), 500, 0.1)
    writeWeights(nn2, oos)

    // Training braking
    val setup3 = new MLPSetup(data.getColumnDimension, nsensors * 2)
    val nn3 = new MLP(setup3)

    println("MLP Evaluation")
    nn3.train(data.getSubMatrix(0, nItems - 1, 0, nsensors - 1),
              tb.getSubVector(0, nItems), 500, 0.1)
    writeWeights(nn3, oos)

    oos.close()
    //var error: Double = 0
    //for (row <- 5000 until 5185) {
    //  error += Math.pow(nn.predict(data.getRowVector(row)) - t.getEntry(row), 2)
      //println((nn.predict(data.getRowVector(row)), t.getEntry(row)))
    //}
    //error /= 185

    //println("Mean squared error: " + error)
  }

  // @Test
  def testImportWeights {
    val fis = new FileInputStream("weights.dump")
    val ois = new ObjectInputStream(fis)

    var setup = new Setup1()

    MatrixUtils.deserializeRealMatrix(setup, "min_diff", ois)

    MatrixUtils.deserializeRealVector(setup, "wa", ois)
    setup.ba = ois.readDouble()
    MatrixUtils.deserializeRealMatrix(setup, "va", ois)
    MatrixUtils.deserializeRealVector(setup, "aa", ois)

    MatrixUtils.deserializeRealVector(setup, "ws", ois)
    setup.bs = ois.readDouble()
    MatrixUtils.deserializeRealMatrix(setup, "vs", ois)
    MatrixUtils.deserializeRealVector(setup, "as", ois)

    MatrixUtils.deserializeRealVector(setup, "wb", ois)
    setup.bb = ois.readDouble()
    MatrixUtils.deserializeRealMatrix(setup, "vb", ois)
    MatrixUtils.deserializeRealVector(setup, "ab", ois)
  }
}

