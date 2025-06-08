import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/** 
 * Classe da Tela de Vitória do jogo
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
*/
public class TelaVitoria extends TelaBase{
    /** Botão para voltar para a tela inicial */
    private JButton returnButton = new JButton("Voltar ao menu");
    /** Imagem de fundo */
    private Image backgroundImage;

    /**
     * Construtor da tela secreta
     * 
     * @param musica Player de música compartilhado entre telas
     */
    TelaVitoria(MusicPlayer musica){
        super(musica);
        saveData();
        musica.playSong("assets/urban.wav", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        returnButton();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        
        add(returnButton, gbc);
        start();
    }

    /**
     * Configura o botão de retorno. Ao ser apertado, carrega a tela inicial
     */
    public void returnButton(){
        returnButton.setPreferredSize(new Dimension(250, 80));
        returnButton.setFont(new Font(NOME_FONTE, Font.BOLD, 30));
        returnButton.setBackground(Color.BLACK);
        returnButton.setForeground(Color.WHITE);
        returnButton.setFocusPainted(false);
        returnButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        
        returnButton.addActionListener(e -> {
            musica.stopSong();
            cleanUp();

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.getContentPane().removeAll();
            TelaInicio telaInicio;

            telaInicio = new TelaInicio(musica);
            frame.add(telaInicio);
            frame.pack();
            frame.revalidate();
            frame.repaint();
            telaInicio.requestFocusInWindow();
        });
    }

    /**
     * Carrega a imagem de fundo.
     */
    public void carregarImagens(){
        try {
            backgroundImage = ImageIO.read(new File("assets/backgroundVictory.png"));
        } catch (IOException e) {
            System.out.println("Erro ao carregar imagens");
            e.printStackTrace();
        }
    }

    /**
     * Renderiza os elementos visuais da tela de vitória.
     * 
     * @param g Contexto gráfico para renderização
     */
    @Override
    public void desenharTela(Graphics g) {
        if(estado != EstadoJogo.PARADO){
            if(backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, LARGURA_TELA, ALTURA_TELA, this);
            } else {
                g.setColor(Color.GREEN);
                g.fillRect(0, 0, LARGURA_TELA, ALTURA_TELA);
            }

            g.setColor(Color.BLACK);
            g.setFont(new Font(NOME_FONTE, Font.BOLD, 50));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Parabéns!! Você concluiu todas as Dungeons!",
                (LARGURA_TELA - metrics.stringWidth("Parabéns!! Você concluiu todas as Dungeons!")) / 2, g.getFont().getSize());

            g.setFont(new Font(NOME_FONTE, Font.BOLD, 25));
            FontMetrics metrics2 = getFontMetrics(g.getFont());
            g.drawString("Jogo feito por: Arthur dos Santos Rezende",
            (LARGURA_TELA - metrics2.stringWidth("Jogo feito por: Arthur dos Santos Rezende")), ALTURA_TELA - g.getFont().getSize());
        }
    }

    /**
     * Realiza limpeza de recursos antes da tela ser descartada.
     */
    public void cleanUp() {
        // Parar o timer
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        
        // Remover botões e seus listeners
        for (ActionListener al : returnButton.getActionListeners()) returnButton.removeActionListener(al);
        
        // Liberar imagens
        backgroundImage = null;
        
        // Coleta de lixo explícita
        System.gc();
    }
}
