import org.apache.commons.math3.linear.{RealVector, RealMatrix, ArrayRealVector}
import org.apache.commons.math3.random.RandomDataGenerator

class LogisticRegression(var steeringWeights: RealVector, var steeringBias: Double,
                         var accelerateWeights: RealVector, var accelerateBias: Double) {
  def this() {
    this(null, 0, null, 0)
  }

  private def grad(x: RealVector, t: Double, w: RealVector, b: Double) = {
    val y = predict(x, w, b)
    val db = (y - t)
    (x.mapMultiply(db), db)
  }

  private def predict(x: RealVector, w: RealVector, b: Double) = 1 / (1 + Math.exp(-w.dotProduct(x) - b))

  def train(X: RealMatrix, tSteering: RealVector, tAccelerate: RealVector,
            iterations: Int = 10, lr: Double = 0.001) {
    val randomGen = new RandomDataGenerator()
    steeringWeights = new ArrayRealVector(X.getColumnDimension)
    accelerateWeights = new ArrayRealVector(X.getColumnDimension)
    for (i <- 0 until X.getColumnDimension) {
      steeringWeights.setEntry(i, randomGen.nextUniform(-1, 1))
      accelerateWeights.setEntry(i, randomGen.nextUniform(-1, 1))
    }
    steeringBias = randomGen.nextUniform(-1, 1)
    accelerateBias = randomGen.nextUniform(-1, 1)


    for (i <- 1 to iterations) {
      for (j <- 0 until X.getRowDimension) {
        val x = X.getRowVector(j)
        val (dws, dbs) = grad(x, tSteering.getEntry(j), steeringWeights, steeringBias)
        steeringWeights = steeringWeights.add(dws)
        steeringBias += dbs
        val (dwa, dba) = grad(x, tAccelerate.getEntry(j), accelerateWeights, accelerateBias)
        accelerateWeights = accelerateWeights.add(dwa)
        accelerateBias += dba
      }
    }
  }

  def predictSteering(x: RealVector) = predict(x, steeringWeights, steeringBias)
  def predictAccelerate(x: RealVector) = predict(x, accelerateWeights, accelerateBias)
}
