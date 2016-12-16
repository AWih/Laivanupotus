package kayttoliittyma

import scala.swing._
import Sound._
import peli._
import event._
import javax.swing.ImageIcon

object graafinen extends SimpleSwingApplication {
  
 
  val picLab1 = new Label
  val picLab2 = new Label
  val picLab3 = new Label
  val picLab4 = new Label
  
  var pictures = Array(Array(picLab1, picLab2), Array(picLab3, picLab4))
  
  var world: Array[Array[Int]] = Array.fill(2, 2)(0)
  world(1)(0) = 1
  
  val waterPic = new ImageIcon("img/water192.png")
  val ship = new ImageIcon("img/hor1.png")
  
  
  for (x <- 0 until world.size) {
    for(y <- 0 until world.size) { 
      world(x)(y) match {          
        case 0 => {   
          pictures(x)(y).icon = waterPic           
        }
        case 1 => {                
          pictures(x)(y).icon = ship
        }
      }     
    }
  }
  
  val canvas = new GridPanel(2,2)
 
 
  pictures.flatten.foreach(canvas.contents += _)

  
  def top = new MainFrame {
    
    resizable = false
    title = "Battleship"
    contents = canvas
    
  }
  

}