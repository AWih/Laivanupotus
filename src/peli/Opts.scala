package peli

class Opts(val gridSize: Vector[Int], //leveys, korkeus (ruuduissa)
    //laivojen koot lueteltuna: lukuarvo kertoo laivan koon, jokaista lukua vastaa yksi laiva
    val fleetComposition: Vector[Int],
    //pommien määrä, tutkan käyttöjen määrä
    val powerUps: Vector[Int],
    //vaikeustaso asteikolla helposta (1) vaikeaan (3)
    val aiLevel: Int) {
 
}

object Opts {
  
  def createWithDifficulty(diffic: String): Opts = {
    if (diffic == "hard") {
      new Opts(Vector(9,9), Vector(4,3,3,2,2,2,2,1,1), Vector(1,1), 3)
    }
    else if (diffic == "medium") {
      new Opts(Vector(6,8), Vector(4,3,3,2,1), Vector(2,2), 2)
    }
    else {
      new Opts(Vector(5,5), Vector(3,2,1), Vector(0,1), 1)
    }
  }
  
}