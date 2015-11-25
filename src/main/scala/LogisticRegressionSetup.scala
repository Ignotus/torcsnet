/**
  * Created by sander on 25/11/15.
  */
class LogisticRegressionSetup(val nFeatures: Int) {
  def getWeights() = Vector(nFeatures)
  def getBias() = Scalar()
}