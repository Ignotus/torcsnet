import org.apache.commons.math3.analysis.function.Sqrt
import org.apache.commons.math3.linear._

class ReservoirRegressionSetup(val nFeatures: Int, reservoirSize: Int = 20) {
    def getLRWeights() = Vector(reservoirSize)
    def getLRBias() = Scalar()
    def getInputWeights() = Matrix(nFeatures, reservoirSize)
    def getReservoirWeights() = {
        val weights = Matrix(reservoirSize, reservoirSize)
        val dec = new EigenDecomposition(weights)
        val imagEigenvalues = new ArrayRealVector(dec.getImagEigenvalues)
        val realEigenvalues = new ArrayRealVector(dec.getRealEigenvalues)

        // Lecture 3 - Neural Networks: Reservoir Computing. Page 23
        val sqrt = new Sqrt
        val absoluteEigenvalues = imagEigenvalues.ebeMultiply(imagEigenvalues)
          .add(realEigenvalues.ebeMultiply(realEigenvalues)).map(sqrt)
        val rho = absoluteEigenvalues.getMaxValue

        val alpha = 0.9
        weights.scalarMultiply(alpha / rho)
    }
}