package kayttoliittyma
import peli._
import processing.core._
import javax.swing.JFrame
//import scala.swing._
import javax.swing._
import java.awt.BorderLayout
import java.awt.event._
import scala.collection.mutable.Buffer

object window extends PApplet with ActionListener{

  val sqrSize = 96
  var cGame: Option[Game] = None
  
//kuvien lataus
  
  //pystysuorat laivat
  val hor1 = loadImage("img/96/hor1.png")
  val hor2 = loadImage("img/96/hor2.png")
  val hor3 = loadImage("img/96/hor3.png")
  val hor4 = loadImage("img/96/hor4.png")
  //vaakasuorat laivat
  val ver1 = loadImage("img/96/ver1.png")
  val ver2 = loadImage("img/96/ver2.png")
  val ver3 = loadImage("img/96/ver3.png")
  val ver4 = loadImage("img/96/ver4.png")
  //muut
  val ripple = loadImage("img/96/ripple96overlay.png")
  val smoke = loadImage("img/96/smoke96overlay.png")
  val water = loadImage("img/96/water96grid.png")
  val fog = loadImage("img/96/fog96grid.png")
  //valikko
  val menuImg = loadImage("img/welcome.png")
  val menuImgB = loadImage("img/welcome_b.png")
  
  //val tuple = (1, true)
  
  val imageTable: Map[(Int, Boolean), PImage] = Map(
      (1,true) -> ver1, (1, false) -> hor1,
      (2, true) -> ver2, (2, false) -> hor2,
      (3, true) -> ver3, (3, false) -> hor3,
      (4, true) -> ver4, (4, false) -> hor4)

      
      
  //testausarvot
  private val gSize = Vector[Int](6, 8)
  private val fleet = Vector[Int](3, 3, 2, 4, 1)
  private val powerUps = Vector(3) //toistaiseksi 1. arvo on 1-säteisten pommien määrä
  private val aiLevel = 0
  private val testSettings: Opts = new Opts(gSize, fleet, powerUps, aiLevel)  

  
//  val gridWidth = this.cGame.options.gridSize(0)
//  val gridHeight = this.cGame.options.gridSize(1)
  val gridWidth = this.gSize(0)
  val gridHeight = this.gSize(1)
  
  override def setup(): Unit = {
    size(this.gridWidth*this.sqrSize, this.gridHeight*this.sqrSize) //testi-taustakuvan koko
  }
  
  def drawShip(ship: Ship) = {
    val picture: PImage = this.imageTable(ship.size, ship.vertical)
    image(picture, ship.ekaX * this.sqrSize, ship.ekaY * this.sqrSize)
  }
  
  override def draw(): Unit = {

    //veden piirtäminnen
//    val bg = loadImage("img/testwater.png")
//    image(bg, 0, 0)
    for (i <- 0 until gridHeight) {
      for (j <- 0 until gridWidth) {
        image(water, j * sqrSize, i * sqrSize)
      }
    }
    
    //(ruudukon piirtäminen)
    for (i <- 0 until gridHeight) {
      line(0, i*sqrSize, gridWidth*sqrSize, i*sqrSize)
    }
    for (j <- 0 until gridWidth) {
      line(j*sqrSize, 0, j*sqrSize, gridHeight*sqrSize)
    }
    
    //omien laivojen piirtäminen
    val ownFleet: Option[Buffer[Ship]] = this.cGame.map { game => game.human.fleet }
    for (fleet <- ownFleet) {
      fleet.foreach(this.drawShip(_))
    }
    
    //vastustajan laivojen piirtäminen
    //TODO: toteutus
  }
  
  
  override def mouseClicked(): Unit = {
    println(s"klikkaus pisteessä ${this.mouseX}, ${this.mouseY}")
    var powerUpValittu = false
    if (!powerUpValittu) {
      
    }
  }
  
  def actionPerformed(e: ActionEvent) = {
    println("nappulaa painettiin")
    if (e.getActionCommand == "start") {
      println("uusi peli")
      this.cGame = Some(new Game(this.testSettings))
    }
    else if (e.getActionCommand == "end") {
      println("lopetettiin peli")
      this.cGame = None
    }
  }
  
  def main(args: Array[String]) {

    val frame = new javax.swing.JFrame("Laivanupotus")

    
    val gamePanel = new JPanel
    val buttonPanel = new JPanel
//    panel.setLayout(new FlowLayout())
    
    gamePanel.add(this)
    val b1 = new JButton("Uusi peli")
    b1.setActionCommand("start")
    val b2 = new JButton("Lopeta peli")
    b2.setActionCommand("end")
    buttonPanel.add(b1)
    buttonPanel.add(b2)
    
    b1.addActionListener(this)
    b2.addActionListener(this)
    
    frame.add(buttonPanel, BorderLayout.PAGE_START)
    frame.add(gamePanel, BorderLayout.CENTER)


//    frame.getContentPane().add(gamePanel)
//    frame.getContentPane().add(buttonPanel)
    init
    frame.setSize(this.getSize())
    frame.pack
    frame.setVisible(true)
    frame.setLocationRelativeTo(null)
    frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
  }
  
}
