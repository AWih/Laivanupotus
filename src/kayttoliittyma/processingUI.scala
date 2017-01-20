package kayttoliittyma
import peli._
import processing.core._
import javax.swing.JFrame
//import scala.swing._
import javax.swing._
import java.awt.BorderLayout
import java.awt.event._
import java.awt.Dimension
import scala.collection.mutable.Buffer
import scala.math.min
import scala.math.max
import scala.math.abs
import Sound._

//TEHTÄVÄLISTA
/*
 * Asetukset (musiikille ym., sekä pelin toiminnoille erikseen) (sis. mukautettu-asetukset) (?)
 */

object window extends PApplet with ActionListener{

  val sqrSize = 96
  val offset = 1
  var cGame: Option[Game] = None
  var currSettings: Opts = Opts.createWithDifficulty("medium")
  var newSettings: Opts = Opts.createWithDifficulty("medium")
  var cWeapon: String = "shoot"
  var startGameFlag: Boolean = false
  var endGameFlag: Boolean = false
  
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
  //nappulat luodaan jo tässä, jotta niitä voi muokata pelitilanteen mukaan
  val b1 = new JButton("Uusi peli")
  val b2 = new JButton("Lopeta peli")    
  val b3 = new JButton("Pommi")
  val b4 = new JButton("Peru")
  val b5 = new JButton("Tutka")
  
  val d1 = new JRadioButton("Helppo")
  val d2 = new JRadioButton("Keskitaso")
  val d3 = new JRadioButton("Vaikea")
  val d4 = new JRadioButton("Mukautettu")
  
  //varsinainen pääikkuna, joka luodaan jo tässä, jotta ikkunan kokoa voidaan myöhemmin muuttaa
  val bigFrame = new javax.swing.JFrame("Laivanupotus")
  val helpFrame = new javax.swing.JFrame("Ohjeet")

  val dRb = new ButtonGroup()
  dRb.add(d1)
  dRb.add(d2)
  dRb.add(d3)
  dRb.add(d4)
  
  /*
   * Pelissä käytettävät aseet/power-upit ovat:
   * shoot (oletus)
   * bomb
   * radar
   */
 
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
  val water = loadImage("img/96/water96.png")
  val ocean = loadImage("img/ocean2.png")
  var resizedOcean = ocean.get
  //val fog = loadImage("img/96/fog96.png")
  //valikko
  val menuImg = loadImage("img/welcome.png")
  val menuImgB = loadImage("img/welcome_b.png")
  var resizedMenuImg = menuImg.get
  var resizedMenuImgB = menuImgB.get
  
  var picFade = 0
  var textScroll = 0
  
  
  //imageTable yhdistää laivan kokoon ja suuntaan niitä vastaavan kuvan
  val imageTable: Map[(Int, Boolean), PImage] = Map(
      (1, true) -> ver1, (1, false) -> hor1,
      (2, true) -> ver2, (2, false) -> hor2,
      (3, true) -> ver3, (3, false) -> hor3,
      (4, true) -> ver4, (4, false) -> hor4)

      
  //testausarvot
//  private val gSize = Vector[Int](8, 8)
//  private val fleet = Vector[Int](3, 3, 2, 4, 1)
//  private val powerUps = Vector(3) //toistaiseksi 1. arvo on 1-säteisten pommien määrä
//  private val aiLevel = 0
//  private val testSettings: Opts = new Opts(gSize, fleet, powerUps, aiLevel)  


  def gridWidth = this.currSettings.gridSize(0)
  def gridHeight = this.currSettings.gridSize(1)
  
  override def setup(): Unit = {
    this.currSettings = this.newSettings
    size((2 * this.gridWidth + this.offset) * this.sqrSize + 1, this.gridHeight * this.sqrSize + 1) //testi-taustakuvan koko
    resizedMenuImg = menuImg.get
    resizedMenuImgB = menuImgB.get
    smartResize(resizedMenuImg)
    smartResize(resizedMenuImgB)
    if (this.width>resizedOcean.width || this.height>resizedOcean.height) smartResize(ocean)
    picFade = 0
    textScroll = 0
    //kokoon lisätään 1, että reunimmaiset ruudukon viivat näkyvät
  }
  
  def smartResize(img:PImage) = { //skaalaa kuvan kattamaan peliruudun rikkomatta kuvasuhdetta
    img.resize(
              (if (1.0*this.height/img.height < 1.0*this.width/img.width) (gridWidth*2+offset)*sqrSize
              else gridHeight*sqrSize*img.width/img.height),
              (if (1.0*this.height/img.height < 1.0*this.width/img.width) (gridWidth*2+offset)*sqrSize*img.height/img.width
              else gridHeight*sqrSize)
              )
  }
  
  def drawCentered(img:PImage) = { //piirtää kuvan keskelle peliruutua
    image(img,-(img.width-this.width)/2,-(img.height-this.height)/2)
  }
  
  def drawShip(ship: Ship) = {
    val picture: PImage = this.imageTable(ship.size, ship.vertical)
    image(picture, ship.ekaX * this.sqrSize, ship.ekaY * this.sqrSize)
  }
  
  def drawFoeShip(ship: Ship) = {
    if ((this.cGame.forall(_.options.aiLevel<=2) && ship.isSunk) || this.cGame.forall(_.isOver)) { //uponneet laivat näkyvät jos ailevel 2 tai vähemmän, kaikki aina pelin päätyttyä
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
  
  def drawTargeting() {
        //piirrä tähtäin
    val chR = 10 //crosshair radius
    //line(mouseX - chR, mouseY - chR, mouseX + chR, mouseY + chR)
    //line(mouseX + chR, mouseY - chR, mouseX - chR, mouseY + chR)
    stroke(0,255)
        //ruututähtäin
    if (this.cWeapon == "bomb" || this.cWeapon == "radar") {
      val chX = max(mouseX/sqrSize - 1, gridWidth+offset)
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
    
    //pommitähtäin (ellipsit)
    if (this.cWeapon == "bomb" && this.cGame.isDefined) {
      stroke(0,255)
      noFill()
      textSize(30)
      for (r <- 1 to 3) {                        //bomb-metodin radius on maaginen väliaikaismuuttuja, ei viitattavissa
      ellipse(mouseX, mouseY, chR*3*r, chR*3*r)  
      }
      fill(0,255)
      text(this.cGame.get.human.resources(0),mouseX-chR*4,mouseY)
    } 
    
    //tutkan tähtäin
    if (this.cWeapon == "radar" && this.cGame.isDefined) {
      stroke(0, 150, 0)
      noFill()
      textSize(30)
      for (r <- 1 to 8) {                        //radar-metodin radius on maaginen väliaikaismuuttuja, ei viitattavissa
      ellipse(mouseX, mouseY, chR*3*r, chR*3*r)  
      }
      fill(0,255)
      text(this.cGame.get.human.resources(1),mouseX-chR*4,mouseY)
    } 
  }
  
  def drawEndScreen() {
      picFade = min(230,picFade+1/*abs(picFade-200)/40*/)
      textScroll = min(textScroll+abs(textScroll-300)/30,this.height/2)
      tint(255, picFade)
      fill(255,255)
      drawCentered(resizedMenuImgB)
      tint(255, 255)
      textSize(30)
      text("Peli loppui!", min(300, textScroll), 200)

      text(if (this.cGame.get.winner.get == this.cGame.get.human) "Voitit pelin!" else "Hävisit pelin!", 350, min(250,textScroll))
  }
  
  def drawStartScreen() {
      drawCentered(resizedMenuImg)
    }
  
  def drawGameState() {
    stroke(255,128)
    //omien laivojen piirtäminen
    val ownFleet: Option[Buffer[Ship]] = this.cGame.map { game => game.human.fleet }
    for (fleet <- ownFleet) {
      fleet.foreach(this.drawShip(_))
    }
    
    
    //vastustajan uponneet laivat 
    val foeFleet: Option[Buffer[Ship]] = this.cGame.map{ game => game.ai.fleet }
    for (fleet <- foeFleet) {
      fleet.foreach(this.drawFoeShip(_))
    }
    
    //lisätään osumamerkit sekä sumu vastustajan ruudukon päälle
    val shotsFired: Option[Array[Array[Int]]] = this.cGame.map { game => game.human.squaresBombed }
    for (shots <- shotsFired) {
        /*
         * squaresBombed sisältää kaiken tiedon, mitä pelaajalla on vastapelaajan ruudukosta
         * 0: tuntematon ruutu
         * 1: osuma
         * 2: huti
         * 3: paljastettu ruutu (laiva)
         * 4: paljastettu ruutu (tyhjä)
         */
      for (j <- 0 until shots.length) {
        for (i <- 0 until shots(0).length) {
          shots(j)(i) match {
            case 0 => if (!this.cGame.get.isOver) {
              //tint(255, 128)
              noStroke()
              fill(255, 128)
              rect((j + gridWidth + offset) * sqrSize, i * sqrSize, sqrSize, sqrSize)
              stroke(255,255,255, 128)
              noFill()
            }
            case 1 => image(smoke, (j + gridWidth + offset) * sqrSize, i * sqrSize)
            case 2 => //image(ripple, (j + gridWidth + offset) * sqrSize, i * sqrSize)
            //tilanteissa 3 ja 4 piirretään sumu ja siihen päälle tutkan lukema
            case 3 => if (!this.cGame.get.isOver) {
              noStroke()
              fill(255, 128)
              rect((j + gridWidth + offset) * sqrSize, i * sqrSize, sqrSize, sqrSize)
              stroke(255,255,255, 128)
              fill(0,255,0)
              ellipse((j + gridWidth + offset) * sqrSize + sqrSize/2, i * sqrSize + sqrSize/2, 10, 10)
            }
            case 4 => if (!this.cGame.get.isOver) {
              noStroke()
              fill(255, 128)
              rect((j + gridWidth + offset) * sqrSize, i * sqrSize, sqrSize, sqrSize)
              stroke(255,255,255, 128)
              fill(50, 50, 50)
              ellipse((j + gridWidth + offset) * sqrSize + sqrSize/2, i * sqrSize + sqrSize/2, 10, 10)
            }
          }
        }
      }
    }
    
    //osumamerkit oman ruudukon päälle
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
  }
  
  private def updateButtons(): Unit = {
    if (this.cGame.isDefined) {
      b3.setText(s"Pommi (${this.cGame.get.human.resources(0)})")
      b5.setText(s"Tutka (${this.cGame.get.human.resources(1)})")
      
      if (this.cGame.get.human.resources(0) == 0)  {
        if (this.cWeapon == "bomb") this.cWeapon = "shoot"
        b3.setEnabled(false) //nappulaa ei voi painaa
      }
      else {
        b3.setEnabled(true)
      }
      if (this.cGame.get.human.resources(1) == 0) {
        if (this.cWeapon == "radar" ) this.cWeapon = "shoot"
        b5.setEnabled(false)
      }
      else {
        b5.setEnabled(true)
      }
      b4.setEnabled(true)
    }
    else {
      b3.setEnabled(false)
      b3.setText("Pommi")
      b4.setEnabled(false)
      b5.setEnabled(false)
      b5.setText("Tutka")
    }
  }
  
  //--VARSINAINEN PIIRTÄMINEN SEKÄ PELIN ALOITUS JA LOPETUS--
  override def draw(): Unit = {
    
    //--aloitetaan peli lipun perusteella--
    if (this.startGameFlag) {
      if (this.d3.isSelected()) this.newSettings = Opts.createWithDifficulty("hard")
      else if (this.d2.isSelected()) this.newSettings = Opts.createWithDifficulty("medium")
      else if (this.d1.isSelected()) this.newSettings = Opts.createWithDifficulty("easy")
      //TODO: mukautetut asetukset
      
      this.setup()
      this.bigFrame.setSize(this.getSize())
      this.bigFrame.pack()
      this.cGame = Some(new Game(this.newSettings))

      this.updateButtons()
      this.startGameFlag = false
    }
    
    background(50)
    //toiminnot joka päivityksen yhteydessä
    if (this.cGame.isDefined) {
      //this.drawWaterAt(0, 0)
      //this.drawWaterAt((this.gridWidth + this.offset) * this.sqrSize, 0)
      drawCentered(ocean)
      //peli päättynyt
      if (this.cGame.forall { game => game.isOver }) { 
        this.drawGameState()
        this.drawEndScreen()
      }
      else { // peli käynnissä mutta ei päättynyt
        stroke(255,128)
        this.drawGridAt(0, 0)
        this.drawGridAt((this.gridWidth + this.offset) * this.sqrSize, 0)
        fill(50,255)
        noStroke()
        rect(this.gridWidth*sqrSize+1,0,sqrSize-1,gridHeight*sqrSize)
        this.drawGameState()
        if (mouseX/sqrSize >= gridWidth+offset) drawTargeting()
      }
    }
    else { //peli ei alkanut
      drawStartScreen()
    }   
    
    
    //--pelin lopetus lipun perusteella--
    if (this.endGameFlag) {
      this.cGame = None
      this.updateButtons()
      this.endGameFlag = false
    }
  }
  
  
  override def mouseClicked(): Unit = {
    if (this.cGame.forall(!_.isOver)) {
      val enemyGridX = (this.gridWidth + this.offset) * this.sqrSize
      if (this.mouseX >= enemyGridX) {
        val x = (this.mouseX - enemyGridX) / this.sqrSize
        val y = this.mouseY / this.sqrSize
        //--- tässä pelataan varsinainen vuoro! ---
        this.cGame.foreach { game => game.playTurns(s"${this.cWeapon} $x $y") }
        this.updateButtons()
        
        
      }
    }
  }
  
  def actionPerformed(e: ActionEvent) = {
    
    val cmd: String = e.getActionCommand
    cmd match {
      case "start" => this.startGameFlag = true
      case "end" => this.endGameFlag = true
      case "bomb" => this.cWeapon = "bomb"
      case "cancel" => this.cWeapon = "shoot"
      case "radar" => this.cWeapon = "radar"
      case "help" => this.helpFrame.setVisible(true)
    }
    
  }
  
  def main(args: Array[String]) {

    //varsinainen pääikkuna luodaan jo olion alustuksessa
    
    val gamePanel = new JPanel
    val buttonPanel = new JPanel
    val controlPanel = new JPanel
    
    val helpB = new JButton("Ohjeet")
    val ohjeet: String = """Valitse pelin vaikeustaso painikkeilla “Helppo”, “Keskivaikea” ja “Vaikea” ja valitse sitten “Uusi peli”.

Vasemmalla näet oman laivaston tilan ja vihollisen pommitukset. Oikealla puolella ruutua on omat ampumiset ja vihollisen laivat.

Ammu tavallisesti: Klikkaa vihollislaivaston alueelta valitsemaasi ruutua. Laivaan osumisesesta saa uuden vuoron.

Käytä erikoisasetta: Valitse “Pommi” tai “Tutka”. “Peru” poistaa valinnan. Pommi ampuu yhdeksän ruutua ja tutka paljastaa yhdeksän ruutua. Käytä kuten tavallista asetta, klikkaa yhtä ruutua kerrallaan. 
Erikoisaseen käyttäminen kuluttaa yhden vuoron.

Upota kaikki vastustajan laivat!"""
    
    
    val tekstikentta = new JTextArea(ohjeet)
    tekstikentta.setEditable(false)
    tekstikentta.setLineWrap(true)
    tekstikentta.setWrapStyleWord(true)
    this.helpFrame.add(tekstikentta)
    this.helpFrame.setSize(new Dimension(768, 384))
    
    helpB.setActionCommand("help")
    b1.setActionCommand("start")
    b2.setActionCommand("end")
    b3.setActionCommand("bomb")
    b4.setActionCommand("cancel")
    b5.setActionCommand("radar")
    
    b3.setEnabled(false)
    b4.setEnabled(false)
    b5.setEnabled(false)
    
    b1.addActionListener(this)
    b2.addActionListener(this)
    b3.addActionListener(this)
    b4.addActionListener(this)
    b5.addActionListener(this)
    helpB.addActionListener(this)
    
    gamePanel.add(this)
    buttonPanel.add(b1)
    buttonPanel.add(b2)
    buttonPanel.add(d1)
    buttonPanel.add(d2)
    d2.setSelected(true) //keskivaikea vaikeustaso valitaan oletuksena
    buttonPanel.add(d3)
    buttonPanel.add(helpB)
    
    controlPanel.add(b3)
    controlPanel.add(b5)
    controlPanel.add(b4)
    
    bigFrame.add(buttonPanel, BorderLayout.PAGE_START)
    bigFrame.add(gamePanel, BorderLayout.CENTER)
    bigFrame.add(controlPanel, BorderLayout.PAGE_END)

    init
    bigFrame.setSize(this.getSize())
    bigFrame.pack
    bigFrame.setVisible(true)
    bigFrame.setLocationRelativeTo(null)
    bigFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
    
    Sound.backroundMusic.loop(5)
  }
  
}
