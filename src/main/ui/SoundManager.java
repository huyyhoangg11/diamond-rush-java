package main.ui;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

    public static final String SFX_DIE = "die";
    public static final String SFX_EAT_DIAMOND = "eat_diamond";
    public static final String SFX_LEVEL_CLEAR = "level_clear";
    public static final String SFX_LOSE_LIFE = "mat_1_mang";
    public static final String SFX_MENU_MOVE = "menu_move";
    public static final String SFX_MENU_SELECT = "menu_select";
    public static final String SFX_PUSH_ROCK = "push_rock";
    public static final String SFX_WALK_DIRT = "walk_dirt";

    private enum BgmTrack {
        NONE,
        MENU,
        STAGE
    }

    private Clip menuBgmClip;
    private Clip stageBgmClip;
    private Clip dieClip;
    private Clip eatDiamondClip;
    private Clip levelClearClip;
    private Clip loseLifeClip;
    private Clip menuMoveClip;
    private Clip menuSelectClip;
    private Clip pushRockClip;
    private Clip walkDirtClip;
    private BgmTrack activeBgm = BgmTrack.NONE;
    private boolean muted = false;

    public void loadSounds() {
        menuBgmClip = load("/sounds/bgm/menu_bgm.wav");
        stageBgmClip = load("/sounds/bgm/stage_bgm.wav");
        dieClip = load("/sounds/sfx/die.wav");
        eatDiamondClip = load("/sounds/sfx/eat_diamond.wav");
        levelClearClip = load("/sounds/sfx/level_clear.wav");
        loseLifeClip = load("/sounds/sfx/mat_1_mang.wav");
        menuMoveClip = load("/sounds/sfx/menu_move.wav");
        menuSelectClip = load("/sounds/sfx/menu_select.wav");
        pushRockClip = load("/sounds/sfx/push_rock.wav");
        walkDirtClip = load("/sounds/sfx/walk_dirt.wav");
    }

    private Clip load(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.out.println("[Sound] Missing: " + path);
                return null;
            }
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(url));
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("[Sound] Error: " + path + " - " + e.getMessage());
            return null;
        }
    }

    public void playMenuBGM() {
        playBGM(menuBgmClip, BgmTrack.MENU);
    }

    public void playStageBGM() {
        playBGM(stageBgmClip, BgmTrack.STAGE);
    }

    private void playBGM(Clip clip, BgmTrack track) {
        if (clip == null || muted) {
            return;
        }
        if (activeBgm == track && clip.isRunning()) {
            return;
        }
        stopBGM();
        activeBgm = track;
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopBGM() {
        stopClip(menuBgmClip);
        stopClip(stageBgmClip);
        activeBgm = BgmTrack.NONE;
    }

    public void playSFX(String name) {
        if (muted) {
            return;
        }
        Clip clip = switch (name) {
            case SFX_DIE -> dieClip;
            case SFX_EAT_DIAMOND -> eatDiamondClip;
            case SFX_LEVEL_CLEAR -> levelClearClip;
            case SFX_LOSE_LIFE -> loseLifeClip;
            case SFX_MENU_MOVE -> menuMoveClip;
            case SFX_MENU_SELECT -> menuSelectClip;
            case SFX_PUSH_ROCK -> pushRockClip;
            case SFX_WALK_DIRT -> walkDirtClip;
            default -> null;
        };
        if (clip == null) {
            return;
        }
        stopClip(clip);
        clip.setFramePosition(0);
        clip.start();
    }

    private void stopClip(Clip clip) {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            stopBGM();
        }
    }
}
