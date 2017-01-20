package peli
import scala.collection.mutable.Buffer
import scala.util.Random
import Sound._

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
   * 3: paljastettu ruutu (laiva)
   * 4: paljastettu ruutu (tyhjä)
   */
  val squaresBombed: Array[Array[Int]]
  def isDefeated: Boolean = this.fleet.forall(_.isSunk)
  var enemy: Player = this // TODO: laita privaatiksi

  def setEnemy(newEnemy: Player) = this.enemy = newEnemy

  def placeFleet: Boolean = {

    var a = false
    var attemptNo: Int = 0
    var shipsPlaced: Int = 0

    for (sh <- options.fleetComposition.indices) {

      do {
        if (r.nextBoolean()) {
          attemptNo += 1
          val x = r.nextInt(options.gridSize(0) - (options.fleetComposition(sh) - 1))
          val x2 = x + (options.fleetComposition(sh) - 1)
          val y = r.nextInt(options.gridSize(1))
          val y2 = y
          a = placeShip(x, y, x2, y2)
          if (a) {
            shipsPlaced += 1
            attemptNo = 0
          }
        } else {
          attemptNo += 1
          val y = r.nextInt(options.gridSize(1) - (options.fleetComposition(sh) - 1))
          val y2 = y + (options.fleetComposition(sh) - 1)
          val x = r.nextInt(options.gridSize(0))
          val x2 = x
          a = placeShip(x, y, x2, y2)
          if (a) {
            shipsPlaced += 1
            attemptNo = 0
          }
        }
      } while (!a && (attemptNo < 150))
    }
    (shipsPlaced == options.fleetComposition.size)
  }

  def placeShip(x: Int, y: Int, x2: Int, y2: Int): Boolean = {
    if (x < 0 || x2 < 0 || y < 0 || y2 < 0 || x > x2 || y > y2 || x > options.gridSize(0) || x2 > options.gridSize(0) || y > options.gridSize(1) || y2 > options.gridSize(1))
      false
    else {
      var a = true
      for (posx <- x to x2) {
        for (posy <- y to y2) {
          for (sh <- fleet.indices) {
            for (bl <- fleet(sh).blocks.indices) {
              if (posx == fleet(sh).blocks(bl).x && posy == fleet(sh).blocks(bl).y) {
                //println("Fail:Square occupied")
                a = false
              }
            }
          }
        }
      }
      if (a) {
        fleet += new Ship(x, y, x2, y2)
      }
      a
    }

  }

  def shoot(x: Int, y: Int): Boolean = {
    if (this.enemy.checkHit(x, y, true)) {
      this.squaresBombed(x)(y) = 1
      true
    } else {
      this.squaresBombed(x)(y) = 2
      false
    }
  }

  def hitToString(hit: Boolean): String = {
    hit match {
      case true  => "Osuma!\n"
      case false => "Huti!\n"
    }
  }

  /*
   * Vakoilu paljastaa pelaajalle yhden vihollisen ruudun ja palauttaa true jos paljastui laivan osa, muuten false.
   * Jos ruutu on ennestään tunnettu, ei tehdä mitään ja palautetaan false
   */
  def revealEnemySqr(x: Int, y: Int): Boolean = {
    if (this.squaresBombed(x)(y) == 0) {
      if (this.enemy.checkHit(x, y, false)) {
        this.squaresBombed(x)(y) = 3 //3 vastaa paljastunutta laivan osaa
        true
      } else {
        this.squaresBombed(x)(y) = 4 //4 vastaa paljastunutta tyhjää ruutua
        false
      }
    } else false
  }

  def useRadar(x: Int, y: Int): String = {
    //tutka käyttää ruudun paljastusta samalla tavalla kuin pommitus ampumista
    val radius = 1 //voisi tulla esim asetuksista. Toisaalta erilaiset koot vaatisivat muutoksia komentojen parsimiseen!
    var ret: String = ""
    val radarUsesLeft = this.resources(1)
    if (radarUsesLeft > 0) {
      for (i <- (y - radius) to (y + radius)) {
        for (j <- (x - radius) to (x + radius)) {
          if (i >= 0 && j >= 0 && i < this.options.gridSize(1) && j < this.options.gridSize(0)) {
            ret += this.revealEnemySqr(j, i)
          }
        }
      }
      this.resources(1) = radarUsesLeft - 1
      ret + s"${this.resources(1)} tutkan käyttö(ä) jäljellä\n"
    } else "Tutkan käytöt loppu!\n"
  }

  def bomb(x: Int, y: Int): String = {
    val radius = 1 //voisi tulla esim asetuksista. Toisaalta erilaiset koot vaatisivat muutoksia komentojen parsimiseen!
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
    } else "Pommit loppu!\n"
  }

  def checkHit(x: Int, y: Int, damage: Boolean): Boolean = this.fleet.exists(_.checkHit(x, y, damage))

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
    } else if (action == "bomb") {
      outcome = this.bomb(x, y)
    } else if (action == "radar") {
      outcome = this.useRadar(x, y)
    } else outcome = "väärä komento"
    
    // Äänet
    if      (outcome.equals("Osuma!\n"))(Sound.hit.play(-15))
    else if (outcome.equals("Huti!\n")) (Sound.miss.play(-5))
    else if (action.equals("bomb"))     (Sound.bomb.play())
    else if (action.equals("radar"))    (Sound.radar.play(5))
    outcome
  }
}

class ComputerPlayer(val options: Opts) extends Player {
  var score = 0
  val fleet: Buffer[Ship] = Buffer[Ship]()
  val squaresBombed = Array.fill(options.gridSize(0), options.gridSize(1))(0)
  this.placeFleet
  private var previousHit: Option[(Int, Int)] = None

  /** Tekee listan kentän koordinaateista, joihin ei ole vielä ammuttu */
  def freeSquares(): Seq[(Int, Int)] = {
      val freeSquares: Seq[(Int, Int)] = for {
      i <- 0 until this.squaresBombed.size
      j <- 0 until this.squaresBombed(i).size
      if (this.squaresBombed(i)(j) == 0 || this.squaresBombed(i)(j) == 4) // 0 ja 4 ruutuja, joihin ei ole vielä ammuttu
    } yield (i, j)
    freeSquares
  }
  
  /** Koittaa valita mitkä tahansa olemassa olevat koordinaatit, joihin ei vielä ole ammuttu */
  def randomFreeSquare(): (Int, Int) = {
    val coords = freeSquares()
    if (!coords.isEmpty && this.resources(0) == 0) {     // Jos kentällä suinkin on vapaita ruutuja ja lisäksi ei ole resursseissa erikoispommeja 
      coords(r.nextInt(freeSquares.size))
    } else if (this.resources(0) > 0) {   // Jos on erikoispommeja, arvotaan niille suotuisten koordinaattien joukosta                
      coordsForBomb()
    } else {
      (0, 0)
    }
  }
  
  def coordsForBomb() = {
    val coordsList = List((1,1), (options.gridSize(0) - 2, options.gridSize(1) - 2), (options.gridSize(0) - 2, 1), (1, options.gridSize(1) - 2))
    var coords = coordsList(r.nextInt(coordsList.size))  
    while (!squareIsFree(coords)) {                    // Tämä toimii, kunhan pommeja ei ole enemmän kuin listassa alkioita. Jos pelivaihtoehtoihin lisätään pommeja,
      coords = coordsList(r.nextInt(coordsList.size))  // on suotuisia koordinaatteja lisättävä. Jatkokehittely: automaattinen tapa generoida sopivia koordinaatteja
    }
    coords
  }

  /** Tekee listan olemassa olevista naapurikoordinaateista */
  def neighborCoords(x: Int, y: Int): List[(Int, Int)] = {
    val w = options.gridSize(0)
    val h = options.gridSize(1)
    def inBounds(x: Int, y: Int): Boolean = x >= 0 && x < w && y >= 0 && y < h
    List((-1, 0), (1, 0), (0, -1), (0, 1))
      .map(t => (x + t._1, y + t._2)).filter(t => inBounds(t._1, t._2))
  }

  /** Koittaa valita edellisen ampumisen vierestä koordinaatit, joihin ei ole vielä ammuttu */
  def coordsClose(priviousHit: (Int, Int)): (Int, Int) = {
    val list = neighborCoords(priviousHit._1, priviousHit._2)
    val freeCoords = list.filter(squareIsFree)
    if (freeCoords.isEmpty) {
      randomFreeSquare()
    } else {
      freeCoords(r.nextInt(freeCoords.size))
    }
  }

  /** Palauttaa true, jos koordinaatteihin ei ole vielä ammuttu, false jos on */
  def squareIsFree(t: (Int, Int)) = (this.squaresBombed(t._1)(t._2) == 0)
 
  /** Tason "easy" tekoäly */
  def performEasy: String = {
      val whereToShoot = randomFreeSquare()
      val hit = shoot(whereToShoot._1, whereToShoot._2)
      s"Tietokone ampui. ${hitToString(hit)}\n"
  }

  /** Tasojen medium ja difficult tekoälyt */
  def performMedOrDif: String = {
    var whereToShoot: (Int, Int) = previousHit match {  
      case None =>          // Käytännössä tämä tapaus vastaa tilannetta, jossa ei olla kertaakaan osuttu tavallisella ammuksella.                            
        randomFreeSquare() 
      case Some(prevHit) =>                            
        coordsClose(prevHit)
    }
  
    var hit = false
    if (bomb(whereToShoot._1, whereToShoot._2).equals("Pommit loppu!\n")) {                                                                     
      hit = shoot(whereToShoot._1, whereToShoot._2)                           // Jos pommeja ei ollut jälhellä, ammutaan normaalisti.
      if (hit)(previousHit = Some(whereToShoot))                              // Osuman tapahtuessa päivitetään "previousHit", jotta seuraavaksi osataan ampua
    }                                                                         // sen viereen.    
   
    s"Tietokone ampui. ${hitToString(hit)}\n" // Isoa pommia käytettäessä väittää tekstiUi:ssä "Huti!" vaikka osuma tai osumia olisi tullutkin.
  }
  
  def performTurn(): String = {
     if (options.aiLevel == 1) (performEasy)
     else (performMedOrDif)
  }

  /*
     *     if (useRadar(whereToShoot._1, whereToShoot._2).equals("Tutkan käytöt loppu!\n")) {    // Ehtolausekkeessa käytetään tutkaa, jos tutkia on jäljellä.  
        val revealed = this.findRevealed
        if (!revealed.isEmpty) {                                                            // Jos tutka on paljastanut laivan osia, ammutaan sinne
          hit = shoot(revealed(0)._1, revealed(0)._2)
        } else {
          hit = shoot(whereToShoot._1, whereToShoot._2)    
        }  
      }
      tuli error: Exception in thread "Animation Thread" scala.MatchError: 4 (of class java.lang.Integer)
     * 
     *   /** Etsii tutkan paljastamat laivanpalaset ja palauttaa niiden sijaintikoordinaatit */
  def findRevealed() = {
    val revealedBlocks: Seq[(Int, Int)] = for {
      i <- 0 until this.squaresBombed.size
      j <- 0 until this.squaresBombed(i).size
      if (this.squaresBombed(i)(j) == 4)
    } yield (i, j)
    revealedBlocks
  }
     * 
     * 
     * 
     */

}
