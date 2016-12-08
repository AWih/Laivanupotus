package kayttoliittyma
import peli._
import scala.io.StdIn._

object textUI extends App{
  
  //testausarvot
  private val size = Vector[Int](10, 12)
  private val fleet = Vector[Int](3,3,2,4,3)
  private val powerUps = Vector(0)
  private val aiLevel = 0
  private val testSettings: Opts = new Opts(size, fleet, powerUps, aiLevel)

  
  println("Laivanupotuspelin testi-UI")

  var mainMenu: Boolean = true
  while (mainMenu) {
    val ret: String = readLine("Aloita peli(a) / lopeta peli(l) ")
    
    if (ret == "a") {
      val game = new Game(testSettings)
      this.run(game)
    }
    if (ret == "l") mainMenu = false
    
  }
  
  def run(game: Game) = {
    var gameOver = false
    while(!gameOver) {
      val command = readLine("syötä komento: ")
      println(game.playTurns(command))
    }

  }
  
}