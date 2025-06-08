import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/** 
 * Classe da Tela de Game Over do jogo
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
*/
public class TelaGameOver extends TelaBase{
    /** Botão para voltar para o jogo */
    private JButton continueButton = new JButton("Continuar");
    /** Botão para voltar para a tela inicial */
    private JButton returnButton = new JButton("Voltar ao menu");

    /** Imagem de Game Over */
    private Image fritoImg;

    /** Valor gerado aleatoreamente ao carregar a tela. Caso seja 0, a dica de como entrar na sala secreta será carregada */
    private int showTip;

    /**
     * Construtor da tela de Game Over
     * 
     * @param musica Player de música compartilhado entre telas
     */
    TelaGameOver(MusicPlayer musica){
        super(musica);
        musica.playSong("assets/vcPerdeu.wav", false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        continueButton();
        returnButton();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        add(continueButton, gbc);
        
        gbc.gridy = 1;
        add(returnButton, gbc);
        
        // 10% de chance de mostrar a dica de como entrar na sala secreta.
        showTip = new Random().nextInt(10);

        start();
    }

    /**
     * Configura o botão de continuar. Ao ser apertado, carrega a tela de jogo
     */
    public void continueButton(){
        continueButton.setPreferredSize(new Dimension(250, 80));
        continueButton.setFont(new Font(NOME_FONTE, Font.BOLD, 30));
        continueButton.setBackground(Color.DARK_GRAY);
        continueButton.setForeground(Color.RED);
        continueButton.setFocusPainted(false);
        continueButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        
        continueButton.addActionListener(e -> {
            musica.stopSong();
            cleanUp();

            try {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.getContentPane().removeAll();
                TelaJogo telaJogo;
                telaJogo = new TelaJogo(musica);
                frame.add(telaJogo);
                frame.pack();
                frame.revalidate();
                frame.repaint();
                telaJogo.requestFocusInWindow();
            } catch (IOException ex) {
                // Erro ao carregar o save - informa o usuário e continua
                System.err.println("Erro ao inicializar o jogo: " + ex.getMessage());
                
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar dados do jogo.\nO jogo será iniciado com configurações padrão.", 
                    "Aviso", 
                    JOptionPane.WARNING_MESSAGE);
                
                // Tenta criar a tela mesmo assim
                try {
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    frame.getContentPane().removeAll();
                    TelaJogo telaJogo = new TelaJogo(musica);
                    frame.add(telaJogo);
                    frame.pack();
                    frame.revalidate();
                    frame.repaint();
                    telaJogo.requestFocusInWindow();
                    
                } catch (Exception ex2) {
                    // Se ainda assim falhar, mostra erro crítico
                    System.err.println("Erro crítico ao inicializar o jogo: " + ex2.getMessage());
                    JOptionPane.showMessageDialog(this, 
                        "Erro crítico. O jogo não pode ser iniciado.", 
                        "Erro Fatal", 
                        JOptionPane.ERROR_MESSAGE);
                }
                    
            } catch (Exception ex) {
                // Outros erros inesperados
                System.err.println("Erro inesperado: " + ex.getMessage());
                ex.printStackTrace();
                    
                JOptionPane.showMessageDialog(this, 
                    "Ocorreu um erro inesperado.", 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Configura o botão de retorno. Ao ser apertado, carrega a tela inicial
     */
    public void returnButton(){
        returnButton.setPreferredSize(new Dimension(250, 80));
        returnButton.setFont(new Font(NOME_FONTE, Font.BOLD, 30));
        returnButton.setBackground(Color.DARK_GRAY);
        returnButton.setForeground(Color.RED);
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
     * Carrega a imagem de Game Over.
     */
    public void carregarImagens(){
        try {
            fritoImg = ImageIO.read(new File("assets/Frito.png"));
        } catch (IOException e) {
            System.out.println("Erro ao carregar imagens");
            e.printStackTrace();
        }
    }

    /**
     * Renderiza os elementos visuais da tela de Game Over.
     * 
     * @param g Contexto gráfico para renderização
     */
    @Override
    public void desenharTela(Graphics g) {
        if(estado != EstadoJogo.PARADO){
            if(fritoImg != null) {
                g.drawImage(fritoImg, (LARGURA_TELA - 150)/2, 75, 150, 150, this);
            } else {
                g.setColor(Color.RED);
                g.fillRect((LARGURA_TELA - 150)/2, 75, 150, 150);
            }

            g.setColor(Color.RED);
            g.setFont(new Font(NOME_FONTE, Font.BOLD, 50));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Ih... Você está frito!", (LARGURA_TELA - metrics.stringWidth("Ih... Você está frito!")) / 2, g.getFont().getSize());
            
            if(showTip == 0){
                g.setFont(new Font(NOME_FONTE, Font.BOLD, 25));
                FontMetrics metrics2 = getFontMetrics(g.getFont());
                g.drawString("Cansado de perder? Relaxe e tome um cappuccino!",
                (LARGURA_TELA - metrics2.stringWidth("Cansado de perder? Relaxe e tome um cappuccino!")), ALTURA_TELA - g.getFont().getSize());
            }
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
        for (ActionListener al : continueButton.getActionListeners()) continueButton.removeActionListener(al);
        for (ActionListener al : returnButton.getActionListeners()) returnButton.removeActionListener(al);
        
        // Liberar imagens
        fritoImg = null;
        
        // Coleta de lixo explícita
        System.gc();
    }
}
