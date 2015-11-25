/**
  * Created by sander on 25/11/15.
  */
class MLPSetup(val nFeatures: Int, val hiddenSize: Int) {
  def getW() = Vector(hiddenSize)
  def getB() = Scalar()
  def getV() = Matrix(nFeatures, hiddenSize)
  def getA() = Vector(hiddenSize)
}
