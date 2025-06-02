import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class TelaBase extends JPanel implements ActionListener {
    protected static final int LARGURA_TELA = 1300;
    protected static final int ALTURA_TELA = 750;
    protected static final int TAMANHO_BLOCO = 50;
    protected static final int INTERVALO = 10;
    protected static final String NOME_FONTE = "Papyrus";
    protected enum EstadoJogo {
        RODANDO, PAUSADO, PARADO
    }
    protected EstadoJogo estado = EstadoJogo.PARADO;
    protected MusicPlayer musica;
    protected MusicPlayer efeito = new MusicPlayer();
    protected int save = 1;
    Timer timer;

    TelaBase(MusicPlayer musica){
        this.musica = musica;

        setPreferredSize(new Dimension(LARGURA_TELA, ALTURA_TELA));
        setBackground(Color.BLACK);
        setFocusable(true);

        carregarImagens();
    }

    public void saveData() {
        try {
            File saveFile = new File("save_data/save.dat");
            File saveDir = saveFile.getParentFile();
            
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(saveFile))) {
                writer.println(this.save);
                System.out.println("Dados salvos com sucesso!");
            }
            
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Erro ao salvar o jogo.", 
                "Erro de Save", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void start(){
        estado = EstadoJogo.RODANDO;
        timer = new Timer(INTERVALO, this);
        timer.start();
    }

    public abstract void carregarImagens();

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        desenharTela(g);
    }

    public abstract void desenharTela(Graphics g);

    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public abstract void cleanUp();
}
