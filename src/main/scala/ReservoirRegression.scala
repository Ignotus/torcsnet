import org.apache.commons.math3.analysis.function.Sqrt
import org.apache.commons.math3.linear._

class ReservoirRegression(steeringWeights: RealVector, steeringBias: Double,
                          accelerateWeights: RealVector, accelerateBias: Double,
                          numOfFeatures: Int,
                          var inputWeights: RealMatrix, var reservoirWeights: RealMatrix,
                          reservoirSize: Int = 20)
  // Number of features for the logistic regression layer will be reservoirSize instead of numOfFeatures
  extends LogisticRegression(steeringWeights, steeringBias, accelerateWeights, accelerateBias, reservoirSize) {

  if (inputWeights == null) {
    inputWeights = new Array2DRowRealMatrix(numOfFeatures, reservoirSize)
    for (i <- 0 until reservoirSize) {
      for (j <- 0 until numOfFeatures) {
        inputWeights.setEntry(j, i, randomGen.nextUniform(-1, 1))
      }
    }
  }

  if (reservoirWeights == null) {
    reservoirWeights = new Array2DRowRealMatrix(reservoirSize, reservoirSize)
    for (i <- 0 until reservoirSize) {
      for (j <- 0 until reservoirSize) {
        // Generates connection with a probability of 0.25
        val hasConnection = if (randomGen.nextInt(0, 4) < 1) 1 else 0
        reservoirWeights.setEntry(i, j,  randomGen.nextUniform(-1, 1) * hasConnection)
      }
    }

    val dec = new EigenDecomposition(reservoirWeights)
    val imagEigenvalues = new ArrayRealVector(dec.getImagEigenvalues)
    val realEigenvalues = new ArrayRealVector(dec.getRealEigenvalues)

    // Lecture 3 - Neural Networks: Reservoir Computing. Page 23
    val sqrt = new Sqrt
    val absoluteEigenvalues = imagEigenvalues.ebeMultiply(imagEigenvalues)
      .add(realEigenvalues.ebeMultiply(realEigenvalues)).map(sqrt)
    val rho = absoluteEigenvalues.getMaxValue

    val alpha = 0.9
    reservoirWeights = reservoirWeights.scalarMultiply(alpha / rho)
  }

  def this(numOfFeatures: Int, reservoirSize: Int = 20) {
    this(null, 0, null, 0, numOfFeatures, null, null, reservoirSize)
  }

  override def train(X: RealMatrix, tSteering: RealVector, tAccelerate: RealVector,
                               iterations: Int = 10, lr: Double = 0.001): Unit = {
    //println("X shape: (" + X.getRowDimension + "," + X.getColumnDimension + ")")
    //println("inputWeights shape: (" + inputWeights.getRowDimension + "," + inputWeights.getColumnDimension +")")
    //println("reservoirWeights shape: (" + reservoirWeights.getRowDimension + "," + reservoirWeights.getColumnDimension + ")")
    val input = X.multiply(inputWeights).multiply(reservoirWeights)
    //println("Input shape: (" + input.getRowDimension + "," + input.getColumnDimension + ")")
    super.train(input, tSteering, tAccelerate, iterations, lr)
  }

  override def predict(x: RealVector) = {
    super.predict(reservoirWeights.preMultiply(inputWeights.preMultiply(x)))
  }

}
