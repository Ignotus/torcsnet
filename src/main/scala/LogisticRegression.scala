import org.apache.commons.math3.linear._

// http://cs229.stanford.edu/notes/cs229-notes1.pdf
class LogisticRegression(var w: RealVector, var b: Double) {
  def this(setup: LogisticRegressionSetup) {
    this(setup.getWeights, setup.getBias)
  }


  // Bishop (4.91)
  private def grad(x: RealVector, t: Double) = {
    val y = sigmoid(x)
    val db = (y - t)
    (x.mapMultiply(db), db)
  }

  def train(data: RealMatrix, t: RealVector,
            iterations: Int = 10, lr: Double = 0.001) {
    for (i <- 1 to iterations) {
      for (j <- 0 until data.getRowDimension) {
        val x = data.getRowVector(j)
        val (dw, db) = grad(x, t.getEntry(j))
        w = w.subtract(dw)
        b -= db
      }
    }
  }

  private def sigmoid(x: RealVector) = 1 / (1 + Math.exp(-w.dotProduct(x) - b))
  def predict(x: RealVector) = sigmoid(x)
}
