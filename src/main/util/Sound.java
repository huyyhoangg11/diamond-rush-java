package main.util;
//SoundManager.java
import javax.sound.sampled.*;
import java.net.URL;

public class Sound {

 // Các clip âm thanh
 private Clip bgMusic;
 private Clip diamondSound;
 private Clip hitSound;
 private Clip winSound;

 public void loadSounds() {
     bgMusic      = loadClip("/sounds/bgm.wav");
     diamondSound = loadClip("/sounds/diamond.wav");
     hitSound     = loadClip("/sounds/hit.wav");
     winSound     = loadClip("/sounds/win.wav");
 }

 private Clip loadClip(String path) {
     try {
         URL url = getClass().getResource(path);
         AudioInputStream ais = AudioSystem.getAudioInputStream(url);
         Clip clip = AudioSystem.getClip();
         clip.open(ais);
         return clip;
     } catch (Exception e) {
         System.out.println("Không tải được âm thanh: " + path);
         return null;
     }
 }

 public void playBGM() {
     if (bgMusic == null) return;
     bgMusic.setFramePosition(0);
     bgMusic.loop(Clip.LOOP_CONTINUOUSLY); // nhạc nền lặp mãi
 }

 public void stopBGM() {
     if (bgMusic != null) bgMusic.stop();
 }

 public void playSFX(String name) {
     Clip clip = switch (name) {
         case "diamond" -> diamondSound;
         case "hit"     -> hitSound;
         case "win"     -> winSound;
         default        -> null;
     };
     if (clip != null) {
         clip.setFramePosition(0);
         clip.start();
     }
 }
}

