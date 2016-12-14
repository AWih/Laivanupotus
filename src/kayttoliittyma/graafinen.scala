package kayttoliittyma

import scala.swing._
import Sound._
import peli._
import event._
import javax.swing.ImageIcon

object graafinen extends SimpleSwingApplication {

  
/**  
  //testausarvot
  private val size = Vector[Int](10, 12)
  private val fleet = Vector[Int](3, 3, 2, 4, 3)
  private val powerUps = Vector(0)
  private val aiLevel = 0
  private val testSettings: Opts = new Opts(size, fleet, powerUps, aiLevel)
  val peli = new Game(testSettings)
*/
  
  val ikkuna = new MainFrame
  
  //TODO korvattava kahdella ruudukkonäkymällä
  val waterPic = new ImageIcon("img/ver3.png")

  // Components: 

  val commentary = new Label
  val result = new Label
  val pictureLabel = new Label

  
  //  this.updateView() tulee sisältämään seuraavat pelin mukaan päivitettyinä
  this.commentary.text = "Kirjoita jotain:"
  this.pictureLabel.icon = this.waterPic
  
  val input = new TextField

  this.listenTo(input.keys)
 
  
  // Events: 
  this.reactions += {
    case keyEvent: KeyPressed =>
      if (keyEvent.source == this.input && keyEvent.key == Key.Enter) {      
        val command = this.input.text.trim
        if (command.nonEmpty) {            
          this.input.text = ""
          this.playTurn(command)
        }
      }
      
  }

  
  val verticalPanel = new BoxPanel(Orientation.Vertical)
  verticalPanel.contents += commentary
  verticalPanel.contents += input
  verticalPanel.contents += result
  verticalPanel.contents += pictureLabel
  

  ikkuna.title = "Battleship"
  ikkuna.resizable = true
  ikkuna.contents = verticalPanel
  
  private def playTurn(command: String) = {
    
    Sound.hutiAani.play()
    this.result.text = "Kirjoitit tekstin: " + command
    
  }
  
  def top = this.ikkuna
}