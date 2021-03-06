package peli
import scala.collection.mutable.Buffer
import scala.math.max


class Ship(private val x1: Int, 
    private val y1: Int, 
    private val x2: Int, 
    private val y2: Int) {
  
  val blocks: Buffer[Block] = Buffer[Block]()
  val size: Int = max(x2 - x1 + 1, y2 - y1 + 1)
  val vertical: Boolean = y2 - y1 > x2 - x1
  
  for (xx<-x1 to x2) {
    for (yy<-y1 to y2) {
      blocks += new Block(xx,yy)
    }
  }
  
  /**
   * checkHit tarkistaa, osuiko ampuninen kyseiseen laivaan. Palauttaa true osumasta, muuten false.
   * Jos damage on true, päivitetään tieto vastaavaan laivan osaan, eli osuman saanut osa "upotetaan".
   */
  def checkHit(x: Int, y: Int, damage: Boolean): Boolean = {
    this.blocks.exists(_.checkHit(x, y, damage))
  }
  
  def isSunk: Boolean = !this.blocks.exists(_.isIntact)
  
  //Nämä ovat testiohjelman printtausta varten
  def ekaX = this.x1
  def tokaX = this.x2
  def ekaY = this.y1
  def tokaY = this.y2
 
}