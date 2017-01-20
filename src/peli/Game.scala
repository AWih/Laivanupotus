package peli
import Sound._

class Game(val options: Opts) {
    
  val human: HumanPlayer = new HumanPlayer(options)
  val ai: ComputerPlayer = new ComputerPlayer(options)
  human.setEnemy(ai) 
  ai.setEnemy(human)
    
  def winner: Option[Player] = {
    if (this.ai.isDefeated) Some(this.human) else if (this.human.isDefeated) Some(this.ai) else None
  }
  
  def isOver = this.winner.isDefined
  
  /*
   * Tämän avulla pelataan varsinainen vuoro pelissä (ensin ihmispelaaja ja sitten tekoäly).
   * Kukin pelaaja saa jatkaa vuoroaan niin kauan kuin osuu laivoihin.
   * 
   * Komento annetaan muodossa [toiminto] [x] [y], esim "shoot 3 5" tai "bomb 1 2"
   */
  def playTurns(command: String): String = {
    var turnDesc:String = ""
    
    val ret: String = this.human.performTurn(command)
    var ret2:String = ""
    turnDesc += ret
    if (!ret.contains("Osuma")) {
      do {
        ret2 = if (!this.isOver) this.ai.performTurn() else ("")
        turnDesc += ret2
      } while (ret2.contains("Osuma"))
    }

    turnDesc
  }
}