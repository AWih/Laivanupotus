package kayttoliittyma
import peli._
import processing.core._
import javax.swing.JFrame
//import scala.swing._
import javax.swing._
import java.awt.BorderLayout
import java.awt.event._
import scala.collection.mutable.Buffer
import scala.math.min
import scala.math.max

object window extends PApplet with ActionListener{

  val sqrSize = 96
  val offset = 1
  var cGame: Option[Game] = None
  var cWeapon: String = "shoot"
  
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
  
  
  //imageTable yhdistää laivan kokoon ja suuntaan niitä vastaavan kuvan
  val imageTable: Map[(Int, Boolean), PImage] = Map(
      (1, true) -> ver1, (1, false) -> hor1,
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
    size((2 * this.gridWidth + this.offset) * this.sqrSize + 1, this.gridHeight * this.sqrSize + 1) //testi-taustakuvan koko
    //kokoon lisätään 1, että reunimmaiset ruudukon viivat näkyvät
  }
  
  def drawShip(ship: Ship) = {
    val picture: PImage = this.imageTable(ship.size, ship.vertical)
    image(picture, ship.ekaX * this.sqrSize, ship.ekaY * this.sqrSize)
  }
  
  def drawFoeShip(ship: Ship) = {
    if (ship.isSunk) {
      val picture: PImage = this.imageTable(ship.size, ship.vertical)
      image(picture, (ship.ekaX + offset + gridWidth) * this.sqrSize, ship.ekaY * this.sqrSize)
    }
  }
  
  def drawWaterAt(topLeftX: Int, topLeftY: Int): Unit = {
    for (i <- topLeftY until (topLeftY + gridHeight * this.sqrSize) by this.sqrSize) {
      for (j <- topLeftX until (topLeftX + gridWidth * this.sqrSize) by this.sqrSize) {
        image(water, j, i)
      }
    }
  }
  
  def drawGridAt(topLeftX: Int, topLeftY: Int): Unit = {
    //horizontal lines
    for (i <- topLeftY to (topLeftY + this.gridHeight * this.sqrSize) by this.sqrSize) {
      line(topLeftX, i, topLeftX + this.gridWidth * this.sqrSize, i)
    }
    //vertical lines
    for (j <- topLeftX to (topLeftX + this.gridWidth * this.sqrSize) by this.sqrSize) {
      line(j, topLeftY, j, topLeftY + this.gridHeight * this.sqrSize)
    }
  }
  
  override def draw(): Unit = {

    background(50)
    //---OMA RUUDUKKO---
    this.drawWaterAt(0, 0)
    this.drawGridAt(0, 0)
    
    
    //omien laivojen piirtäminen
    val ownFleet: Option[Buffer[Ship]] = this.cGame.map { game => game.human.fleet }
    for (fleet <- ownFleet) {
      fleet.foreach(this.drawShip(_))
    }
    
    
    //---VASTUSTAJAN RUUDUKKO---
    this.drawWaterAt((this.gridWidth + this.offset) * this.sqrSize, 0)
    
    //vastustajan uponneet laivat piirretään? TODO: voisi riippua vaikeusasteesta?
    val foeFleet: Option[Buffer[Ship]] = this.cGame.map{ game => game.ai.fleet }
    for (fleet <- foeFleet) {
      fleet.foreach(this.drawFoeShip(_))
    }
    
    val shotsFired: Option[Array[Array[Int]]] = this.cGame.map { game => game.human.squaresBombed }
    for (shots <- shotsFired) {
        /*
         * squaresBombed sisältää kaiken tiedon, mitä pelaajalla on vastapelaajan ruudukosta
         * 0: tuntematon ruutu
         * 1: osuma
         * 2: huti
         * 3: paljastettu ruutu
         */
      for (j <- 0 until shots.length) {
        for (i <- 0 until shots(0).length) {
          shots(j)(i) match {
            case 0 => image(fog, (j + gridWidth + offset) * sqrSize, i * sqrSize)
            case 1 => image(smoke, (j + gridWidth + offset) * sqrSize, i * sqrSize)
            case 2 => image(ripple, (j + gridWidth + offset) * sqrSize, i * sqrSize)
            // case 3: miten paljastetaan ruutu?
          }
        }
      }
    }
    
    val shotsReceived: Option[Array[Array[Int]]] = this.cGame.map { game => game.ai.squaresBombed } //ei kovin eleganttia kopioida yltä, mutta halusin nopean tavan visualisoida testaukseen
    for (shots <- shotsReceived) {
      for (j <- 0 until shots.length) {
        for (i <- 0 until shots(0).length) {
          shots(j)(i) match {
            case 0 => //image(fog, (j) * sqrSize, i * sqrSize)
            case 1 => image(smoke, (j) * sqrSize, i * sqrSize)
            case 2 => image(ripple, (j) * sqrSize, i * sqrSize)
          }
        }
      }
    }
    
    this.drawGridAt((this.gridWidth + this.offset) * this.sqrSize, 0)
    
    if (!this.cGame.isDefined) {
      menuImg.resize((gridWidth*2+offset)*sqrSize,gridHeight*sqrSize)
      image(menuImg,0,0)
    }
    
    //piirrä tähtäin
    //TODO: tähtäimen sijaan hiiren vierellä voisi näkyä jokin kuvake, joka kertoo, onko valittu pommi tms.
    val chR = 10 //crosshair radius
    //line(mouseX - chR, mouseY - chR, mouseX + chR, mouseY + chR)
    //line(mouseX + chR, mouseY - chR, mouseX - chR, mouseY + chR)
    
    //ruututähtäin
    if (this.cWeapon == "bomb") {
      val chX = max(mouseX/sqrSize - 1, 0)
      val chY = max(mouseY/sqrSize - 1, 0)
      val chX2 = min(mouseX/sqrSize + 1, gridWidth*2+offset-1)
      val chY2 = min(mouseY/sqrSize + 1, gridHeight-1)
      noFill()
      rect(chX*sqrSize+8, chY*sqrSize+8,(chX2-chX+1)*sqrSize-16,(chY2-chY+1)*sqrSize-16)
    }
    else {
      val chX = mouseX/sqrSize
      val chY = mouseY/sqrSize
      val chX2 = mouseX/sqrSize
      val chY2 = mouseY/sqrSize
      noFill()
      rect(chX*sqrSize+8, chY*sqrSize+8,sqrSize-16,sqrSize-16)
    }
    
    //pommitähtäin
    if (this.cWeapon == "bomb" && this.cGame.isDefined) {
      noFill()
      textSize(30)
      for (r <- 1 to 3) {                        //bomb-metodin radius on maaginen väliaikaismuuttuja, ei viitattavissa
      ellipse(mouseX, mouseY, chR*3*r, chR*3*r)  
      }
      text(this.cGame.get.human.resources(0),mouseX-chR*4,mouseY)
    }
    
    if (this.cGame.isDefined && this.cGame.forall { game => game.isOver }) {
      tint(255, 200)
      menuImgB.resize((gridWidth*2+offset)*sqrSize,gridHeight*sqrSize)
      image(menuImgB,0,0)
      tint(255, 255)
      textSize(30)
      text("Peli loppui!", 300, 200)
      text(s"${this.cGame.get.winner.getOrElse("")} voitti pelin!", 350, 250) // "Lopeta peli" tuottaa joskus exceptionin: None.get
      //TODO: kerro kumpi voitti, lisää muotoilu, lopeta komentojen ottaminen?
    }
  }
  
  
  override def mouseClicked(): Unit = {
    val enemyGridX = (this.gridWidth + this.offset) * this.sqrSize
    if (this.mouseX >= enemyGridX) {
      val x = (this.mouseX - enemyGridX) / this.sqrSize
      val y = this.mouseY / this.sqrSize
      //--- tässä pelataan varsinainen vuoro! ---
      this.cGame.foreach { game => game.playTurns(s"${this.cWeapon} $x $y") }
      //TODO: ota palautusarvo talteen. Ilmoita esim. pommien nykyinen määrä
    }

  }
  
  def actionPerformed(e: ActionEvent) = {
    println("nappulaa painettiin")
    
    val cmd: String = e.getActionCommand
    cmd match {
      case "start" => this.cGame = Some(new Game(this.testSettings))
      case "end" => this.cGame = None
      case "bomb" => this.cWeapon = "bomb"
      case "cancel" => this.cWeapon = "shoot"
    }
    
  }
  
  def main(args: Array[String]) {

    val frame = new javax.swing.JFrame("Laivanupotus")
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    
    val gamePanel = new JPanel
    val buttonPanel = new JPanel
    val controlPanel = new JPanel
    
    val b1 = new JButton("Uusi peli")
    b1.setActionCommand("start")
    val b2 = new JButton("Lopeta peli")
    b2.setActionCommand("end")
    val b3 = new JButton("Pommi")
    b3.setActionCommand("bomb")
    val b4 = new JButton("Peru")
    b4.setActionCommand("cancel")
    
    b1.addActionListener(this)
    b2.addActionListener(this)
    b3.addActionListener(this)
    b4.addActionListener(this)
    
    gamePanel.add(this)
    buttonPanel.add(b1)
    buttonPanel.add(b2)
    controlPanel.add(b3)
    controlPanel.add(b4)
    //TODO: lisää pommien (hetkellinen) lukumäärä, + piste-/rahamäärä, muiden power-uppien määrä ?
    
    frame.add(buttonPanel, BorderLayout.PAGE_START)
    frame.add(gamePanel, BorderLayout.CENTER)
    frame.add(controlPanel, BorderLayout.PAGE_END)

    init
    frame.setSize(this.getSize())
    frame.pack
    frame.setVisible(true)
    frame.setLocationRelativeTo(null)
    frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
  }
  
}
