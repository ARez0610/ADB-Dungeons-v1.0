import java.io.IOException;
import javax.swing.*;

/**
 * Classe principal do jogo
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
 */
public class Start extends JFrame {
    /** Music player usado para todas as telas do jogo */
    private MusicPlayer musica;

    /**
     * Método main
     * 
     * @param args Permite input do usuário
     * @throws IOException Se ocorrer um erro de I/O durante a leitura do arquivo
     */
    public static void main(String[] args) throws IOException {
        new Start();
    }
    
    /** 
     * Cria a tela inicial quando o jogo é aberto
     * @throws IOException Se ocorrer um erro de I/O durante a leitura do arquivo
    */
    Start() throws IOException{
        musica = new MusicPlayer();
        add(new TelaInicio(musica));
        setTitle("ADB Dungeons");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
}