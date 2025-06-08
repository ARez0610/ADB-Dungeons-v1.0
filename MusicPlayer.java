import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** 
 * Classe para a reprodução de músicas e efeitos sonoros no jogo
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
*/
public class MusicPlayer {
    /** Clipe a ser tocado */
    private Clip clip;
    /** Lista de line listeners do clipe */
    private List<LineListener> lineListeners = new ArrayList<>();
    /** Indica se o clipe está tocando */
    private boolean isPlaying = false;
    
    /**
     * Começa a tocar um clipe
     * 
     * @param caminho O caminho do áudio em questão
     * @param loop Indica se o áudio ficará em loop ou não
     */
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
    
    /**
     * Para a reprodução do clipe
     */
    public void stopSong() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
        isPlaying = false;
    }

    /**
     * Adiciona um novo line listener ao clipe
     * 
     * @param listener O listener a ser adicionado
     */
    public void addLineListener(LineListener listener) {
        if (clip != null) {
            clip.addLineListener(listener);
            lineListeners.add(listener);
        }
    }

    /**
     * Remove um clipe da lista de line listeners
     * 
     * @param listener O listener a ser removido
     */
    public void removeLineListener(LineListener listener) {
        if (clip != null) {
            clip.removeLineListener(listener);
            lineListeners.remove(listener);
        }
    }

    /**
     * @return A lista de line listeners
     */
    public LineListener[] getLineListeners() {
        return lineListeners.toArray(new LineListener[0]);
    }
    
    // Getters

    /**
     * @return O clipe
     */
    public Clip getClip() {return clip; }

    /**
     * @return Se o clipe está tocando
     */
    public boolean isPlaying() { return isPlaying; }
}