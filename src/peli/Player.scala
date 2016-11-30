package peli
import scala.collection.mutable.Buffer

trait Player {
  var score: Int
  val options: Opts
  val resources: Array[Int] = this.options.powerUps.toArray
  val fleet: Buffer[Ship] 
  val squaresBombed: Array[Array[Int]]
  
  def isDefeated: Boolean = this.fleet.isEmpty
  
  def placeFleet(): Boolean = {
    println("asetettiin laivat")
    false
  }
  
  def placeShip(): Boolean = {
        //TODO: toteutus
    ???
  }
  
  def shoot(x: Int, y: Int): Boolean = ???
  
  def performTurn(command: String): String  //voisi palauttaa esim string-arvon, joka välitetään käyttöliittymään
  
  
}

class HumanPlayer(val options: Opts) extends Player {
  var score = 0
  val fleet: Buffer[Ship] = Buffer[Ship]()
  val squaresBombed = Array.fill(options.gridSize(0), options.gridSize(1))(0)
  this.placeFleet()
  
  def performTurn(command: String) = {
    s"pelaaja pelasi komennon $command"
  }
}

class ComputerPlayer(val options: Opts) extends Player {
  var score = 0
  val fleet: Buffer[Ship] = Buffer[Ship]()
  val squaresBombed = Array.fill(options.gridSize(0), options.gridSize(1))(0)
  this.placeFleet()
  
  def performTurn(command: String) = {
    "tietokoneen vuoro"
  }
}