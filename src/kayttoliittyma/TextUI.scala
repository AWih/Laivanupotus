package kayttoliittyma
import peli._
import scala.io.StdIn._

object textUI extends App {

  //testausarvot
  private val size = Vector[Int](6, 6)
  private val fleet = Vector[Int](3,3,4,4,3)
  private val powerUps = Vector(3) //toistaiseksi 1. arvo on 1-säteisten pommien määrä
  private val aiLevel = 0
  private val testSettings: Opts = new Opts(size, fleet, powerUps, aiLevel)
  
  private val aiShips = Array.fill(size(0), size(1))(0)
  private val humanShips = Array.fill(size(0), size(1))(0)
  
  println("Laivanupotuspelin testi-UI")

  var mainMenu: Boolean = true
  while (mainMenu) {
    val ret: String = readLine("Aloita peli(a) / lopeta peli(l) ")
    if (ret == "a") {
      val game = new Game(testSettings)
      game.ai.fleet.foreach(markShip(_, aiShips))
      game.human.fleet.foreach(markShip(_, humanShips))
      this.run(game)
    }
    if (ret == "l") mainMenu = false

  }

  def run(game: Game) = {
    var gameOver = false
    while (!gameOver) {
      println("Omat laivat:\n" + view(humanShips))   
      println("Omat pommitukset:\n" + view(game.human.squaresBombed))
      println("Vastapelaajan laivat:\n" + view(aiShips))
      println("Vastapelaajan pommitukset:\n" + view(game.ai.squaresBombed))
      val command = readLine("syötä komento: ")
      println(game.playTurns(command))
      gameOver = game.isOver
    }
    println(s"${game.winner.getOrElse("")} voitti pelin!")

  }

  def markShip(ship: Ship, table: Array[Array[Int]])= {
    
    val x1 = ship.ekaX
    val x2 = ship.tokaX
    val y1 = ship.ekaY
    val y2 = ship.tokaY
    
    if (x1 == x2 && y1 == y2) {            // Ykkösen kokoiset laivat    
      table(x1)(y1) = 1
    } else if (x1 == x2 && y1 != y2) {     // Vaakasuorassa olevat laivat
      (y1 to y2).foreach(table(x1)(_) = ship.size)
    } else {                                // Pystysuorassa olevat laivat
      (x1 to x2).foreach(table(_)(y1) = ship.size) 
    }
  }
  
  def view(table: Array[Array[Int]]): String  = {
    println()
    var rivi = ""
    (0 until size(1)).map(rivi += _.toString + " ")
    rivi + "\n" + table.map(_.mkString(" ")).map(_.replace('0', '-')).mkString("\n")
  }
  

}