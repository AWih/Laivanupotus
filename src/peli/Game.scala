package peli
import Sound._

class Game(val options: Opts) {
    
  val human: HumanPlayer = new HumanPlayer(options)
  
  val ai: ComputerPlayer = new ComputerPlayer(options)
  
  human.setEnemy(ai) 
  ai.setEnemy(human)
  
  //Sound.backroundMusic.loop(5)
  
  def winner: Option[Player] = {
    if (this.ai.isDefeated) Some(this.human) else if (this.human.isDefeated) Some(this.ai) else None
  }
  
  def isOver = this.winner.isDefined
  
  
  
  //missä muodossa UI antaa syötteen pääohjelmalle (ja miten HumanPlayer ottaa parametreja)?
  /*
   * Tämän avulla pelataan varsinainen vuoro pelissä (ensin ihmispelaaja ja sitten tekoäly).
   * Metodi voisi palauttaa jonkinlaisen kuvauksen tapahtuneesta vuorosta (String, Boolean ?).
   * Mitään ruudukkoa ei varmaan tarvi palauttaa, koska performTurn-metodit tekevät tarvittavat
   * tilanmuutokset ja UI voi vain esim tulostaa ruudukon uudestaan.
   */
  def playTurns(command: String): String = {
    var turnDesc:String=""
    
    val ret: String = this.human.performTurn(command)
    var ret2:String = ""
    turnDesc += ret
    if (!ret.contains("Osuma")) {
      do {
        ret2 = if (!this.isOver) this.ai.performTurn() else ("")
        turnDesc += ret2
      } while (ret2.contains("Osuma"))
    }
    //ret + ret2

    turnDesc
  }
}