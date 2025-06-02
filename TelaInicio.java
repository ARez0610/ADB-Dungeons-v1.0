import javax.imageio.ImageIO;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class TelaInicio extends TelaBase{
    private JButton startButton = new JButton("Começar");
    private JButton instrucoesButton = new JButton("Instruções");
    private Image backgroundImg;
    private Image logoImg;
    private boolean wasCappuccinoDefeated;
    private JTextField secreteTextField = new JTextField(30);
    private final String SECRET_CODE = "cappuccino";

    TelaInicio(MusicPlayer musica){
        super(musica);
        // Quando a vinheta terminar, toca a música de fundo
        efeito.playSong("assets/Intro.wav", false);
        efeito.addLineListener(new LineListener() {
            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    SwingUtilities.invokeLater(() -> {
                        musica.playSong("assets/urban.wav", true);
                    });
                }
            }
        });
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        setPreferredSize(new Dimension(LARGURA_TELA, ALTURA_TELA));
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        
        startButton();
        instrucoesButton();
        secreteTextField();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        add(startButton, gbc);
        
        gbc.gridy = 1;
        add(instrucoesButton, gbc);

        gbc.gridy = 2;
        add(secreteTextField, gbc);
        start();
    }

    public void startButton(){
        startButton.setPreferredSize(new Dimension(200, 80));
        startButton.setFont(new Font(NOME_FONTE, Font.BOLD, 30));
        startButton.setBackground(Color.BLACK);
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        
        startButton.addActionListener(e -> {            
            musica.stopSong();
            // Remove o listener da vinheta para evitar chamadas posteriores
            for (LineListener listener : efeito.getLineListeners()) {
                efeito.removeLineListener(listener);
            }
            efeito.stopSong();
                
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
    public void instrucoesButton(){
        instrucoesButton.setPreferredSize(new Dimension(200, 80));
        instrucoesButton.setFont(new Font(NOME_FONTE, Font.BOLD, 30));
        instrucoesButton.setBackground(Color.BLACK);
        instrucoesButton.setForeground(Color.WHITE);
        instrucoesButton.setFocusPainted(false);
        instrucoesButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        instrucoesButton.addActionListener(e -> mostrarInstrucoes());
    }
    private void mostrarInstrucoes() {
        String mensagem = "CONTROLES DO JOGO:\n\n" +
                         "• Teclas direcionais para mover\n" +
                         "• Teclas AWSD para atirar\n\n" +
                         "OBJETIVO:\n\n" +
                         "Derrote cada em inimigo em uma dungeon \n" +
                         "e a porta para a próxima dungeon se abrirá!";
        
        JOptionPane.showMessageDialog(
            this,
            mensagem,
            "Instruções",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void secreteTextField(){
        secreteTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {checkText();}
            @Override
            public void removeUpdate(DocumentEvent e) {checkText();}
            @Override
            public void changedUpdate(DocumentEvent e) {}

            private void checkText() {
                //Obter texto e normalizar
                String input = secreteTextField.getText().replaceAll("\\s+", "").toLowerCase();

                //Verificar correspondência
                if (input.equals(SECRET_CODE.toLowerCase())) {
                    triggerEvent();
                }
            }
        });
    }

    private void triggerEvent() {
        JOptionPane.showMessageDialog(null, "Comando reconhecido! Entrando na sala secreta.");
        try {
            readSaveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!wasCappuccinoDefeated) loadBoss(0);
        else acessSecretRoom();
        
    }

    public void loadBoss(int bossNum){
        musica.stopSong();
        efeito.stopSong();
        cleanUp();

        Window window = SwingUtilities.windowForComponent(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            TelaBoss telaBoss = new TelaBoss(musica, bossNum);
            frame.getContentPane().removeAll();
            frame.add(telaBoss);
            frame.revalidate();
            frame.repaint();
            telaBoss.requestFocusInWindow();
        } else {
            System.err.println("Erro: Não foi possível encontrar o JFrame principal");
        }
    }

    private void acessSecretRoom(){
        musica.stopSong();
        efeito.stopSong();
        cleanUp();

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        TelaSecreta telaSecreta = new TelaSecreta(musica);
        frame.add(telaSecreta);
        frame.pack();
        frame.revalidate();
        frame.repaint();
        telaSecreta.requestFocusInWindow();
    }

    public void readSaveData() throws IOException {
        Scanner s = null;
        try {
            File saveFile = new File("save_data/cappuccino.dat");
            
            // Verifica se o diretório existe, se não, cria
            File saveDir = saveFile.getParentFile();
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            
            // Se o arquivo não existe, cria um
            if (!saveFile.exists()) {
                newSave(saveFile);
            }

            // Ler os dados do save
            s = new Scanner(new BufferedReader(new FileReader(saveFile)));
            if (s.hasNext()) {
                String saveNum = s.next().trim();
                try {
                    int num = Integer.parseInt(saveNum);
                    if (num == 1) this.wasCappuccinoDefeated = true;
                    else this.wasCappuccinoDefeated = false; // Valor padrão
                } catch (NumberFormatException e) {
                    this.wasCappuccinoDefeated = false;
                }
            } else {
                this.wasCappuccinoDefeated = false;
            }
            
        } catch (FileNotFoundException e) {
            // Arquivo não encontrado - tentar criar um
            System.out.println("Arquivo de save não encontrado. Criando novo arquivo...");
            File saveFile = new File("save_data/cappuccino.dat");
            newSave(saveFile);
            this.wasCappuccinoDefeated = false;
            
        } catch (IOException e) {
            // Erro de I/O
            System.err.println("Erro ao ler arquivo de save: " + e.getMessage());
            this.save = 1;
            throw e;
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }
    private void newSave(File saveFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(saveFile))) {
            writer.println("0");
            System.out.println("Arquivo de save criado com sucesso!");
        }
    }

    public void carregarImagens(){
        try {
            backgroundImg = ImageIO.read(new File("assets/background.png"));
            logoImg = ImageIO.read(new File("assets/Logo.png"));
        } catch (IOException e) {
            System.out.println("Erro ao carregar imagens");
            e.printStackTrace();
        }
    }

    @Override
    public void desenharTela(Graphics g) {
        if(estado != EstadoJogo.PARADO){
            // Desenhar fundo (se a imagem existir)
            if(backgroundImg != null) {
                g.drawImage(backgroundImg, 0, 0, LARGURA_TELA, ALTURA_TELA, this);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, LARGURA_TELA, ALTURA_TELA);
            }

            if(logoImg != null) {
                g.drawImage(logoImg, (LARGURA_TELA - 650)/2, 5, 650, 150, this);
            } else {
                g.setColor(new Color(232, 67, 147));
                g.fillRect((LARGURA_TELA - 650)/2, 5, 650, 150);

                g.setColor(Color.BLACK);
                g.setFont(new Font(NOME_FONTE, Font.BOLD, 50));
                FontMetrics metrics = getFontMetrics(g.getFont());
                g.drawString("ADB: Dungeons", (LARGURA_TELA - metrics.stringWidth("ADB: Dungeons")) / 2, g.getFont().getSize() + 35);
            }
        }
    }

    public void cleanUp() {
        // Parar o timer
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        
        // Remover botões e seus listeners
        for (ActionListener al : startButton.getActionListeners()) startButton.removeActionListener(al);
        remove(startButton);
        for (ActionListener al : instrucoesButton.getActionListeners()) instrucoesButton.removeActionListener(al);
        remove(instrucoesButton);
        
        // Liberar imagens
        backgroundImg = null;
        logoImg = null;
        
        // Coleta de lixo explícita
        System.gc();
    }
}
