package Sound
import javax.sound.sampled._

class Sound(path: String) {
  
  val tiedosto = new java.io.File(path)
  val audioIn = AudioSystem.getAudioInputStream(tiedosto)
  val clip = AudioSystem.getClip(null)
  
  
  clip.addLineListener(new LineListener() {
    def update(event: LineEvent) = {
      if (event.getType() == LineEvent.Type.STOP) {
        clip.close()
        audioIn.close()
      }
    }
  })

  def stop() = this.clip.stop()
  
  def play(): Unit = {
    clip.open(audioIn)
    clip.start()
  }
  
  def play(volume: Float): Unit = {
    clip.open(audioIn)
    setVolume(volume)
    clip.start()
  }
  
  def loop() {
    clip.open(audioIn)
    clip.loop(Clip.LOOP_CONTINUOUSLY)
    clip.start
  }
  
  private def setVolume(volume: Float) = {
    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN)
    val floatControl = gainControl match {
      case f: FloatControl => f
      case _ => throw new ClassCastException
    }
    floatControl.setValue(volume)
  }
  
  def loop(volume: Float): Unit = {
    clip.open(audioIn)
    setVolume(volume)
    clip.start
  }
}

object Sound {
  
  def miss = new Sound("Sound/lava.wav")
  def hit = new Sound("Sound/explosion5.wav")
  def radar = new Sound("Sound/radar1.wav")
  def bomb = new Sound("Sound/explosion1.wav")
  def backroundMusic = new Sound("Sound/wind1.wav")

}
/** Demo äänien käytöstä */
object main extends App{
  
  Sound.miss.play
  // Alla olevaa ei tarvitse pelissä
  Thread.sleep(1000)
  
}