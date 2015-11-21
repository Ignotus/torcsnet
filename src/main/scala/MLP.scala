import org.apache.commons.math3.linear._
import org.apache.commons.math3.analysis.function._

class MLP(w: RealVector, b: Double, var v: RealMatrix, var a: RealVector)
    extends LogisticRegression(w, b) {

    def this(setup: MLPSetup) {
        this(setup.getW, setup.getB, setup.getV, setup.getA)
    }

    protected def mlpGrad(x: RealVector, t: Double) = {
        // h dimension [K * 1]
        val h = vecSigmoid(x)
        // dw dimension [K * 1], db - scalar
        val (dw, db) = grad(h, t)
        // da dimension [K * 1]
        val da = h.ebeMultiply(h.mapMultiply(-1).mapAdd(1)).mapMultiply(db)
        // dv dimension [N * K]
        val dv = x.outerProduct(da)
        (dw, db, dv, da)
    }

    override def train(data: RealMatrix, t: RealVector,
                       iterations: Int = 10, lr: Double = 0.001) {
        for (i <- 1 to iterations) {
          for (j <- 0 until data.getRowDimension) {
            val x = data.getRowVector(j)
            val (dw, db, dv, da) = mlpGrad(x, t.getEntry(j))
            updateWeights(dw, db, lr)
            v = v.subtract(dv.scalarMultiply(lr))
            a = a.subtract(da.mapMultiply(lr))
          }
        }
    }

    private def vecSigmoid(x: RealVector) = v.preMultiply(x).add(a).map(new Sigmoid)
    override def predict(x: RealVector) = sigmoid(vecSigmoid(x))
}