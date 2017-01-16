package peli



class Block(val x: Int, val y: Int) {
   var isIntact = true
   
   def checkHit(xShot: Int, yShot: Int, damage:Boolean): Boolean = {
     if ((this.x == xShot) && (this.y == yShot)) {
       if (damage) this.isIntact = false
       true
     }
     else false
   }
  

}
