package peli

/*
 * testikommentti
 */

class Ship(private val x1: Int, 
    private val y1: Int, 
    private val x2: Int, 
    private val y2: Int) {

  val blocks: Array[Block] = Array[Block]()
  
  def checkHit(x: Int, y: Int) = {
    this.blocks.exists(_.checkHit(x, y))
  }
  
  def isSunk: Boolean = !this.blocks.exists(_.isIntact)
  
  ???
}