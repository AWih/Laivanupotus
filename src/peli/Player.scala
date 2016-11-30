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
    //TODO: toteutus
    ???
  }
  
  def placeShip(): Boolean = {
        //TODO: toteutus
    ???
  }
  
  def shoot(x: Int, y: Int): Boolean = ???
  
  def performTurn(): String  //voisi palauttaa esim string-arvon, joka välitetään käyttöliittymään
  
  
}

class HumanPlayer(val options: Opts) extends Player {
}

class ComputerPlayer(val options: Opts) extends Player {
  
}