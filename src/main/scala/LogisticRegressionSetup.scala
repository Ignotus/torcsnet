class LogisticRegressionSetup(val nFeatures: Int) {
    def getWeights() = Vector(nFeatures)
    def getBias() = Scalar()
}