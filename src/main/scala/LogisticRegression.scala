import java.security.InvalidParameterException

import org.apache.commons.math3.linear._
import org.apache.commons.math3.random.RandomDataGenerator

class LogisticRegression(var steeringWeights: RealVector, var steeringBias: Double,
                         var accelerateWeights: RealVector, var accelerateBias: Double,
                         val numOfFeaures: Int) {
  if (steeringWeights != null && accelerateWeights != null
    && steeringWeights.getDimension != accelerateWeights.getDimension) {
    throw new InvalidParameterException
  }

  val randomGen = new RandomDataGenerator()
  if (steeringWeights == null) {
    steeringWeights = new ArrayRealVector(numOfFeaures)
    for (i <- 0 until numOfFeaures) {
      steeringWeights.setEntry(i, randomGen.nextUniform(-1, 1))
    }
    steeringBias = randomGen.nextUniform(-1, 1)
  }

  if (accelerateWeights == null) {
    accelerateWeights = new ArrayRealVector(numOfFeaures)
    for (i <- 0 until numOfFeaures) {
      accelerateWeights.setEntry(i, randomGen.nextUniform(-1, 1))
    }
    accelerateBias = randomGen.nextUniform(-1, 1)
  }

  def this(numOfFeaures: Int) {
    this(null, 0, null, 0, numOfFeaures)
  }

  // Bishop (4.91)
  private def grad(x: RealVector, t: Double, w: RealVector, b: Double) = {
    val y = output(x, w, b)
    val db = (y - t)
    (x.mapMultiply(db), db)
  }

  protected def output(x: RealVector, w: RealVector, b: Double) = 1 / (1 + Math.exp(-w.dotProduct(x) - b))

  def train(X: RealMatrix, tSteering: RealVector, tAccelerate: RealVector,
            iterations: Int = 10, lr: Double = 0.001) {
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

  // Returns a tuple of steering and accelerate
  def predict(x: RealVector) = (output(x, steeringWeights, steeringBias),
                                output(x, accelerateWeights, accelerateBias))
}
