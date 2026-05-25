package main.ui;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * Đặt file .wav trong src/sounds/:
 *   bgm.wav, diamond.wav, hit.wav, win.wav, footstep.wav
 */
public class SoundManager {

    public static final String SFX_DIAMOND  = "diamond";
    public static final String SFX_HIT      = "hit";
    public static final String SFX_WIN      = "win";
    public static final String SFX_STEP     = "footstep";

    private Clip bgmClip;
    private Clip diamondClip;
    private Clip hitClip;
    private Clip winClip;
    private Clip stepClip;

    private boolean muted = false;

    public void loadSounds() {
        bgmClip     = load("/sounds/bgm.wav");
        diamondClip = load("/sounds/diamond.wav");
        hitClip     = load("/sounds/hit.wav");
        winClip     = load("/sounds/win.wav");
        stepClip    = load("/sounds/footstep.wav");
    }

    private Clip load(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.out.println("[Sound] Không tìm thấy: " + path);
                return null;
            }
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(url));
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("[Sound] Lỗi: " + path + " — " + e.getMessage());
            return null;
        }
    }

    public void playBGM() {
        if (bgmClip == null || muted) return;
        bgmClip.setFramePosition(0);
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) bgmClip.stop();
    }

    public void playSFX(String name) {
        if (muted) return;
        Clip clip = switch (name) {
            case SFX_DIAMOND -> diamondClip;
            case SFX_HIT     -> hitClip;
            case SFX_WIN     -> winClip;
            case SFX_STEP    -> stepClip;
            default          -> null;
        };
        if (clip == null) return;
        clip.setFramePosition(0);
        clip.start();
    }

    public void toggleMute() {
        muted = !muted;
        if (muted) stopBGM(); else playBGM();
    }
}