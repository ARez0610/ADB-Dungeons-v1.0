import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private Clip clip;
    private List<LineListener> lineListeners = new ArrayList<>();
    private boolean isPlaying = false;
    
    public void playSong(String caminho, boolean loop) {
        try {
            File arquivoMusica = new File(caminho);
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(arquivoMusica);
            clip = AudioSystem.getClip();
            clip.open(audioInput);
            
            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            isPlaying = true;
        } catch (Exception e) {
            System.out.println("Erro ao reproduzir música: " + e.getMessage());
        }
    }
    
    public void stopSong() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
        isPlaying = false;
    }

    public void addLineListener(LineListener listener) {
        if (clip != null) {
            clip.addLineListener(listener);
            lineListeners.add(listener);
        }
    }

    public void removeLineListener(LineListener listener) {
        if (clip != null) {
            clip.removeLineListener(listener);
            lineListeners.remove(listener);
        }
    }

    public LineListener[] getLineListeners() {
        return lineListeners.toArray(new LineListener[0]);
    }
    
    public Clip getClip() {return clip; }
    public boolean isPlaying() { return isPlaying; }
}