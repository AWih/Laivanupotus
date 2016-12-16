package peli
import scala.collection.mutable.Buffer

/*
 * testikommentti
 */

class Ship(private val x1: Int, 
    private val y1: Int, 
    private val x2: Int, 
    private val y2: Int) {

  println("x1:"+x1+" y1:"+y1+" x2:"+x2+" y2:"+y2)
  
  val blocks: Buffer[Block] = Buffer[Block]()
  
  for (xx<-x1 to x2) {
    for (yy<-y1 to y2) {
      blocks += new Block(xx,yy)
    }
  }
  
  /**
   * checkHit tarkistaa, osuiko ampuninen kyseiseen laivaan. Palauttaa true osumasta, muuten false.
   * Jos löytyy osuma, päivitetään tieto vastaavaan laivan osaan, eli osuman saanut osa "upotetaan".
   */
  def checkHit(x: Int, y: Int): Boolean = {
    this.blocks.exists(_.checkHit(x, y))
  }
  
  def isSunk: Boolean = !this.blocks.exists(_.isIntact)
  
  // Nämä on testiohjelman printtausta varten, näitä ei välttämättä tarvii myöhemmin. En vain 
  // viitsinyt omin päin muuttaa noita yksityisiä muuttujia julkisiksi.
  def ekaX = this.x1
  def tokaX = this.x2
  def ekaY = this.y1
  def tokaY = this.y2
 
}