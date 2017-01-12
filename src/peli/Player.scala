package peli
import scala.collection.mutable.Buffer
import scala.util.Random

trait Player {
  val r = new Random()
  var score: Int
  val options: Opts
  val resources: Array[Int] = this.options.powerUps.toArray
  val fleet: Buffer[Ship] 
  /*
   * squaresBombed sisältää kaiken tiedon, mitä pelaajalla on vastapelaajan ruudukosta
   * 0: tuntematon ruutu
   * 1: osuma
   * 2: huti
   * 3: paljastettu ruutu
   */
  val squaresBombed: Array[Array[Int]]
  def isDefeated: Boolean = this.fleet.forall(_.isSunk)
  var enemy: Player = this // TODO: laita privaatiksi
  
  def setEnemy(newEnemy: Player) = this.enemy = newEnemy
  
  def placeFleet: Boolean = {
    
    var a=false
    var attemptNo:Int=0
    var shipsPlaced:Int=0
    
    for (sh<-options.fleetComposition.indices) {
      
      do { if (r.nextBoolean()) {
        attemptNo += 1
        val x = r.nextInt(options.gridSize(0)-(options.fleetComposition(sh)-1))
        val x2 = x+(options.fleetComposition(sh)-1)
        val y = r.nextInt(options.gridSize(1))
        val y2=y
        a = placeShip(x,y,x2,y2)
        if (a) {
          shipsPlaced += 1
          attemptNo=0
        }
      }
      else {
        attemptNo += 1
        val y = r.nextInt(options.gridSize(1)-(options.fleetComposition(sh)-1))
        val y2 = y+(options.fleetComposition(sh)-1)
        val x = r.nextInt(options.gridSize(0))
        val x2=x
        a = placeShip(x,y,x2,y2)
        if (a) {
          shipsPlaced += 1
          attemptNo=0
        }
      } } while (!a && (attemptNo<150))
    }
    (shipsPlaced==options.fleetComposition.size)
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
                //println("Fail:Square occupied")
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
  
  def shoot(x: Int, y: Int): Boolean = {
    if(this.enemy.checkHit(x, y)) {
      this.squaresBombed(x)(y) = 1
      true
    }
    else{
      this.squaresBombed(x)(y) = 2
      false
    }
  }
  
  def hitToString(hit: Boolean): String = {
    hit match {
      case true => "Osuma!\n"
      case false => "Huti!\n"
    }
  }
  
  def bomb(x: Int, y: Int): String = {
    val radius = 1  //voisi tulla esim asetuksista. Toisaalta erilaiset koot vaatisivat muutoksia komentojen parsimiseen!
    var ret: String = ""
    val bombsLeft = this.resources(0)
    if (bombsLeft > 0) {
      for (i <- (y - radius) to (y + radius)) {
        for (j <- (x - radius) to (x + radius)) {
          if (i >= 0 && j >= 0 && i < this.options.gridSize(1) && j < this.options.gridSize(0)) {
            ret += this.shoot(j, i)
          }
        }
      }
      this.resources(0) = bombsLeft - 1
      ret + s"${this.resources(0)} pommi(a) jäljellä\n"
    }
    else "Pommit loppu!\n"
  }
  
  
  def checkHit(x:Int,y:Int):Boolean = this.fleet.exists(_.checkHit(x, y))
}

class HumanPlayer(val options: Opts) extends Player {
  var score = 0
  val fleet: Buffer[Ship] = Buffer[Ship]()
  val squaresBombed = Array.fill(options.gridSize(0), options.gridSize(1))(0)
  this.placeFleet
  
  def performTurn(command: String): String = {
    /*
     * Komento annetaan muodossa [toiminto] [x] [y], esim "shoot 3 5" tai "bomb 1 2"
     */
    val parts: Array[String] = command.split(' ')
    val action: String = parts(0)
    val x: Int = parts(1).toInt
    val y: Int = parts(2).toInt
    var outcome = ""
    if (action == "shoot") {
      outcome = hitToString(this.shoot(x, y))
    }
    else if (action == "bomb") {
      outcome = this.bomb(x, y)
    }
    else outcome = "väärä komento"
    outcome
  }
}


class ComputerPlayer(val options: Opts) extends Player {
  var score = 0
  val fleet: Buffer[Ship] = Buffer[Ship]()
  val squaresBombed = Array.fill(options.gridSize(0), options.gridSize(1))(0)
  this.placeFleet
  private var previousHit: Option[(Int, Int)] = None
 
  /** Koittaa valita mitkä tahansa olemassa olevat koordinaatit, joihin ei vielä ole ammuttu */
  def randomFreeSquare() = {
    val freeSquares: Seq[(Int, Int)] = for {
      i <- 0 until this.squaresBombed.size
      j <- 0 until this.squaresBombed(i).size
      if (this.squaresBombed(i)(j) == 0)
    } yield (i, j)
    
    if (!freeSquares.isEmpty) {
      freeSquares(r.nextInt(freeSquares.size))
    } else {
      // Virhe tapaus, koko kenttä oli jo täynnä!!!
      (0, 0)
    }
  }
   
  /** Tekee listan olemassa olevista naapurikoordinaateista */
  def neighborCoords(x: Int, y: Int): List[(Int, Int)] = {
    val w = options.gridSize(0)
    val h = options.gridSize(1)
    def inBounds(x: Int, y: Int): Boolean = x >= 0 && x < w && y >= 0 && y < h
    List( (-1, 0), (1, 0), (0, -1), (0, 1) )
      .map(t => (x + t._1, y + t._2) ).filter(t => inBounds(t._1, t._2))
  }
  
  /** Koittaa valita edellisen ampumisen vierestä koordinaatit, joihin ei ole vielä ammuttu */
  def shootClose(priviousHit: (Int, Int)): (Int, Int) = {
    val list = neighborCoords(priviousHit._1, priviousHit._2)
    val freeSquares = list.filter(squareIsFree)
    if (freeSquares.isEmpty) {
      randomFreeSquare()
    } else {
      freeSquares(r.nextInt(freeSquares.size))
    }
  }
  
  /** Palauttaa true, jos koordinaatteihin ei ole vielä ammuttu, false jos on */
  def squareIsFree(t: (Int, Int)) = (this.squaresBombed(t._1)(t._2) == 0) 
  
  def performTurn() = {    
    /*	
    * Jos edellinen ampuminen osui, pyrkii ampumaan viereen, muuten ihan minne vaan. 
    * Tarkistaa kummassakin tapauksessa, että ei ammu uudestaan sellaisilla koordinaateilla, 
    * joilla on jo ampunut.
    */  
    val whereToShoot: (Int, Int) = previousHit match {
      case Some(prevHit) =>
        shootClose(prevHit)
      case None =>
        randomFreeSquare()       
    }    
    
    val hit = shoot(whereToShoot._1, whereToShoot._2)
    val outcome = hitToString(hit)    
    // Alla päivitetään "previousHit"
    if (hit) {
      previousHit = Some(whereToShoot)
    } else {
      previousHit = None
    }
    
    s"Tietokone ampui. ${outcome}\n"
  }
  
}
