package peli

/*
 * Game luokka käsittelee pelattavaa peliä itsessään ja sen on tarkoitus vuorovaikuttaa
 * jonkin käyttöliittymän kanssa. 
 * Uusi game-olio luodaan, kun aloitetaan uusi peli. Luokka ottaa parametriksi kyseisessä pelissä 
 * käytettävät asetukset (erikseen preferences (?) ).
 * 
 */
class Game(val options: Opts) { //parametri tulee UI:lta
    
  val human: HumanPlayer = new HumanPlayer(options)
  
  val ai: ComputerPlayer = new ComputerPlayer(options)
  
  human.placeFleet()
  ai.placeFleet()
  
  
  //
  def playTurns(input: String) = {
    
    
  }

}