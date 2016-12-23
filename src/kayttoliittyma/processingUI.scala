package kayttoliittyma
import processing.core._
import javax.swing.JFrame


object window extends PApplet {

  val sqrSize = 192
  val gridWidth = 7
  val gridHeight = 5
  
  override def setup(): Unit = {
    size(1280, 853) //testi-taustakuvan koko
  }
  
  override def draw(): Unit = {

    val bg = loadImage("img/testwater.png")
    image(bg, 0, 0)
//    val vesi = loadImage("img/water192.png")
//    for (i <- 0 until gridHeight) {
//      for (j <- 0 until gridWidth) {
//        image(vesi, j * sqrSize, i * sqrSize)
//      }
//    }
    
    for (i <- 0 until gridHeight) {
      line(0, i*sqrSize, gridWidth*sqrSize, i*sqrSize)
    }
    for (j <- 0 until gridWidth) {
      line(j*sqrSize, 0, j*sqrSize, gridHeight*sqrSize)
    }
    val ship = loadImage("img/hor1.png")
    image(ship, 0, 0)
    val ship2 = loadImage("img/hor2.png")
    image(ship2, 3*sqrSize, 4*sqrSize)
    image(loadImage("img/ver4.png"), 5*sqrSize, 0)
  }
  
  def main(args: Array[String]) {

    val frame = new javax.swing.JFrame("Laivanupotus")

    frame.getContentPane().add(this)
    init
    frame.setSize(this.getSize())
    frame.pack
    frame.setVisible(true)
    frame.setLocationRelativeTo(null)
    frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
  }
  
}
