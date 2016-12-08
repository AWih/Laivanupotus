package peli

class Game(val options: Opts) {
    
  val human: HumanPlayer = new HumanPlayer(options)
  
  val ai: ComputerPlayer = new ComputerPlayer(options)
  
//  this.human.placeFleet()   laivojen sijoitus voidaan tehdä jo pelaajaobjektien alustuksessa Player-luokassa
//  this.ai.placeFleet()
  
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
    val ret: String = this.human.performTurn(command)
    val ret2: String = this.ai.performTurn(command)  //tekoäly ei varmaan vaadi parametreja
    ret + ret2
    
  }
}