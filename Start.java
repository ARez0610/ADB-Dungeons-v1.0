import java.io.IOException;

import javax.swing.*;

public class Start extends JFrame {
    private MusicPlayer musica;
    public static void main(String[] args) throws IOException {
        new Start();
    }
    Start() throws IOException{
        musica = new MusicPlayer();
        // Inicia com a tela inicial (sem música ou com música específica)
        add(new TelaInicio(musica));
        setTitle("Game Test");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
}