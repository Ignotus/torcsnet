import org.apache.commons.math3.linear._

class ReservoirRegression(lr_w: RealVector, lr_b: Double,
                          var input_w: RealMatrix, var reservoir_w: RealMatrix)
  // Number of features for the logistic regression layer will be reservoirSize
  // instead of numOfFeatures
  extends LogisticRegression(lr_w, lr_b) {

  def this(setup: ReservoirRegressionSetup) {
    this(setup.getLRWeights, setup.getLRBias,
         setup.getInputWeights, setup.getReservoirWeights)
  }

  override def train(data: RealMatrix, t: RealVector,
                     iterations: Int = 10, lr: Double = 0.001) {
    val input = data.multiply(input_w).multiply(reservoir_w)
    super.train(input, t, iterations, lr)
  }

  override def predict(x: RealVector) = {
    super.predict(reservoir_w.preMultiply(input_w.preMultiply(x)))
  }

}
