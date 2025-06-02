import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class TelaSecreta extends TelaBase{
    private JButton returnButton = new JButton("Voltar ao menu");
    private JButton[] worldButtons = new JButton[5];

    private Image backgroundImg;

    TelaSecreta(MusicPlayer musica){
        super(musica);
        musica.playSong("assets/fog.wav", false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        worldButtons();
        returnButton();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        add(returnButton, gbc);
        
        for(int i = 0; i < 5; i++){
            gbc.gridy = i + 1;
            add(worldButtons[i], gbc);
        }
        
        start();
    }

    public void worldButtons(){
        for(int i = 0; i < 5; i++){
            final int s = i + 1;
            worldButtons[i] = new JButton("Mundo " + s);
            worldButtons[i].setPreferredSize(new Dimension(250, 80));
            worldButtons[i].setFont(new Font(NOME_FONTE, Font.BOLD, 30));
            worldButtons[i].setBackground(Color.BLACK);
            worldButtons[i].setForeground(Color.WHITE);
            worldButtons[i].setFocusPainted(false);
            worldButtons[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
            
            worldButtons[i].addActionListener(e -> {
                musica.stopSong();
                cleanUp();

                loadGame(s);
            });
        }
    }
    public void loadGame(int s){
        save = s;
        saveData();
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
    }

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

    public void carregarImagens(){
        try {
            backgroundImg = ImageIO.read(new File("assets/secretBackground.png"));
        } catch (IOException e) {
            System.out.println("Erro ao carregar imagens");
            e.printStackTrace();
        }
    }

    @Override
    public void desenharTela(Graphics g) {
        if(estado != EstadoJogo.PARADO){
            if(backgroundImg != null) {
                g.drawImage(backgroundImg, 0, 0, LARGURA_TELA, ALTURA_TELA, this);
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(0, 0, LARGURA_TELA, ALTURA_TELA);
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font(NOME_FONTE, Font.BOLD, 50));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Escolha para onde você quer ir", (LARGURA_TELA - metrics.stringWidth("Escolha para onde você quer ir")) / 2,
                g.getFont().getSize());
        }
    }

    public void cleanUp() {
        // Parar o timer
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        
        // Remover botões e seus listeners
        for(JButton b : worldButtons) for (ActionListener al : b.getActionListeners()) b.removeActionListener(al);
        for (ActionListener al : returnButton.getActionListeners()) returnButton.removeActionListener(al);
        
        // Liberar imagens
        backgroundImg = null;
        
        // Coleta de lixo explícita
        System.gc();
    }
}