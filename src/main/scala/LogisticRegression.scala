import org.apache.commons.math3.linear._

// http://cs229.stanford.edu/notes/cs229-notes1.pdf
class LogisticRegression(var w: RealVector, var b: Double) {
  def this(setup: LogisticRegressionSetup) {
    this(setup.getWeights, setup.getBias)
  }


  // Bishop (4.91)
  // Evaluated on the paper
  protected def grad(x: RealVector, t: Double) = {
    val y = sigmoid(x)
    val db = (y - t)
    val dw = x.mapMultiply(db)
    (dw, db)
  }

  protected def updateWeights(dw: RealVector, db: Double, lr: Double) {
    w = w.subtract(dw.mapMultiply(lr))
    b -= db * lr
  }

  def train(data: RealMatrix, t: RealVector,
            iterations: Int = 10, lr: Double = 0.001) {
    for (i <- 1 to iterations) {
      for (j <- 0 until data.getRowDimension) {
        val x = data.getRowVector(j)
        val (dw, db) = grad(x, t.getEntry(j))
        updateWeights(dw, db, lr)
      }
    }
  }

  protected def sigmoid(x: RealVector) = 1 / (1 + Math.exp(-w.dotProduct(x) - b))
  def predict(x: RealVector) = sigmoid(x)
}