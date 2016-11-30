package peli

class Block(val x: Int, val y: Int) {
   var isIntact = true
   
   def checkHit(xShot: Int, yShot: Int): Boolean = {
     if ((this.x == xShot) && (this.y == yShot)) {
       this.isIntact = false
       true
     }
     else false
   }
  
  
 // kello on 2 yöllä
//kello on 2:11 yöllä
// kello on 2:44
}
