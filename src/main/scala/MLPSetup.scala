class MLPSetup(val nFeatures: Int, val hiddenSize: Int) {
    def getW() = Vector(hiddenSize)
    def getB() = Scalar()
    def getV() = Matrix(nFeatures, hiddenSize)
    def getA() = Vector(hiddenSize)
}