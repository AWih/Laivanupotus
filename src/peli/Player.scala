package peli
import scala.collection.mutable.Buffer
import scala.util.Random

trait Player {
  var score: Int
  val options: Opts
  val resources: Array[Int] = this.options.powerUps.toArray
  val fleet: Buffer[Ship] 
  val squaresBombed: Array[Array[Int]]  
  def isDefeated: Boolean = this.fleet.forall(_.isSunk)
  
  def placeFleet: Boolean = {
    val r=new Random()
    var a=false
    var attemptNo:Int=0
    
    for (sh<-options.fleetComposition.indices) {
      
      do { if (r.nextBoolean()) {
        attemptNo += 1
        val x = r.nextInt(options.gridSize(0)-(options.fleetComposition(sh)-1))
        val x2 = x+(options.fleetComposition(sh)-1)
        val y = r.nextInt(options.gridSize(1))
        val y2=y
        a = placeShip(x,y,x2,y2)
      }
      else {
        attemptNo += 1
        val y = r.nextInt(options.gridSize(1)-(options.fleetComposition(sh)-1))
        val y2 = y+(options.fleetComposition(sh)-1)
        val x = r.nextInt(options.gridSize(0))
        val x2=x
        a = placeShip(x,y,x2,y2)
      } } while (!a && (attemptNo<50))
    }
    a
  }
  
  def placeShip(x:Int,y:Int,x2:Int,y2:Int): Boolean = {
    if (x<0 || x2<0 || y<0 || y2<0 || x>x2 || y>y2 || x>options.gridSize(0) || x2>options.gridSize(0) || y>options.gridSize(1) || y2>options.gridSize(1))
      false
    else {
      var a=true
      for (posx <- x to x2) {
        for (posy <- y to y2) {
          for (sh <- fleet.indices) {
            for (bl <- fleet(sh).blocks.indices) {
              if (posx==fleet(sh).blocks(bl).x && posy==fleet(sh).blocks(bl).y) {
                println("Fail:Square occupied")
                a=false
              }
            }
          }
        }
      }
    if (a) {
      fleet += new Ship(x,y,x2,y2)
    }
    a
    }
    
  }
  
  def shoot(x: Int, y: Int): Boolean = ???
  
  def performTurn(command: String): String
  
  def checkHit(x:Int,y:Int):Boolean = this.fleet.exists(_.checkHit(x, y))
}

class HumanPlayer(val options: Opts) extends Player {
  var score = 0
  val fleet: Buffer[Ship] = Buffer[Ship]()
  val squaresBombed = Array.fill(options.gridSize(0), options.gridSize(1))(0)
  this.placeFleet
  
  def performTurn(command: String): String = {
    s"pelaaja pelasi komennon $command"
  }
}

class ComputerPlayer(val options: Opts) extends Player {
  var score = 0
  val fleet: Buffer[Ship] = Buffer[Ship]()
  val squaresBombed = Array.fill(options.gridSize(0), options.gridSize(1))(0)
  this.placeFleet
  
  def performTurn(command: String) = {
    "tietokoneen vuoro"
  }
}
