import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/** 
 * Classe da Tela principal do jogo
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
*/
public class TelaJogo extends TelaBase {
    /** Variável usada para gerenciar a dungeon atual */
    private DungeonManager dungeonManager;
    /** O layout da dungeon atual */
    private DungeonManager.DungeonLayout layout;
    /** Uma lista contendo todos os objetos colidíveis presentes na tela */
    private ArrayList<ObjetoColidivel> objetosColidiveis = new ArrayList<>();
    /** Botão para pausar o jogo */
    private JButton pauseButton = new JButton("Pausa");
    /** O personagem jogável do jogo (Duque Batata) */
    private Player batata;
    /** O leitor te teclas responsável por controlar o jogador */
    private GameKeyAdapter gameKeyAdapter;
    /** Uma lista para gerenciar os projéteis do jogador (as cenouras) */
    private ArrayList<Projetil> cenouras = new ArrayList<>();
    /** O intervalo entre cada tiro do jogador (0,3 s) */
    private static final long INTERVALO_TIRO = 300;
    /** Uma lista contendo todos os inimigos presentes na tela */
    private ArrayList<Inimigo> inimigos = new ArrayList<>();
    /** Guarda a quantidade de inimigos na tela */
    private int enemyCount;
    /** Uma lista contendo todas as paredes presentes na tela */
    private ArrayList<Parede> paredes = new ArrayList<>();
    /** A porta onde o jogador deve entrar para passar para a próxima dungeon */
    private Porta porta;
    /** Uma lista contendo todas as partículas de alerta presentes na tela */
    private ArrayList<Alert> alertas = new ArrayList<>();
    /** Uma lista contendo todas as partículas de pof! presentes na tela */
    private ArrayList<Pof> pofs = new ArrayList<>();
    /** Imagem do ícone de pausa */
    private ImageIcon iconPause;
    /** Imagens únicas */
    private Image alertImage, pofImage, paredeImg, algodaoImg;
    /** Arrays de imagens */
    private Image[] backgroundImgs, portaImgs, batataImgs, cenouraImgs, slimeImgs, flymeImgs, pratoImgs, facaImgs, armandibulaImgs,
    morcerangoImgs, queijoBoxerImgs, bracoImgs, luvaImgs, chocochatoImgs, algodogDoceImgs, slimeBotImgs, laserImgs, gigaBotImgs,
    gLaserImgs, malandranhaImgs, alhoImgs;

    /**
     * Construtor da tela de jogo
     * 
     * @param musica Player de música compartilhado entre telas
     * @throws IOException Se ocorrer um erro de I/O durante a leitura do arquivo de save
     */
    TelaJogo(MusicPlayer musica) throws IOException {
        super(musica);
        readSaveData();
        this.dungeonManager = new DungeonManager();

        switch(save){
            case 2: musica.playSong("assets/ruins.wav", true); break;
            case 3: musica.playSong("assets/AnInvitation.wav", true); break;
            case 4: musica.playSong("assets/majesty.wav", true); break;
            case 5: musica.playSong("assets/BackgroundSound2_ADB.wav", true); break;
            default: musica.playSong("assets/BackgroundSound_ADB.wav", true); break;
        }
        
        setLayout(null);

        pauseButton();
        add(pauseButton);
        start();
    }

    /**
     * Lê o arquivo de save para determinar qual mundo deve ser carregado
     * 
     * @throws IOException Se ocorrer um erro de I/O durante a leitura do arquivo de save
     */
    public void readSaveData() throws IOException {
        Scanner s = null;
        try {
            File saveFile = new File("save_data/save.dat");
            
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
                    if (num >= 2 && num <= 5) {
                        this.save = num;
                    } else {
                        this.save = 1; // Valor padrão
                    }
                } catch (NumberFormatException e) {
                    this.save = 1;
                }
            } else {
                this.save = 1;
            }
            
        } catch (FileNotFoundException e) {
            // Arquivo não encontrado - tentar criar um
            System.out.println("Arquivo de save não encontrado. Criando novo arquivo...");
            File saveFile = new File("save_data/save.dat");
            newSave(saveFile);
            this.save = 1;
            
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

    /** 
     * Cria um novo arquivo de save com o valor padrão (1).
     * 
     * @param saveFile Arquivo a ser criado
     * @throws IOException Se ocorrer um erro de I/O durante a criação do arquivo
     */
    private void newSave(File saveFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(saveFile))) {
            writer.println("1");
            System.out.println("Arquivo de save criado com sucesso!");
        }
    }

    /**
     * Inicia o jogo
     * <p>
     * Inicia o timer e muda o estado do jogo para RODANDO, carrega o layout inicial do mundo atual, o jogador e a porta.
     * </p>
     */
    @Override
    public void start(){
        cleanKeyListeners();
        resetKeyState();
        objetosColidiveis.clear();
        layout = dungeonManager.getCurrentDungeon();

        batata = new Player(0, ALTURA_TELA/2);
        batata.setImage(batataImgs[0]);
        objetosColidiveis.add(batata);

        gameKeyAdapter = new GameKeyAdapter();
        addKeyListener(gameKeyAdapter);

        layout.getInimigos();
        layout.getParedes();

        porta = new Porta(LARGURA_TELA - 20, ALTURA_TELA/2 - 50, 20, 100);
        porta.setImage(portaImgs[0]);
        objetosColidiveis.add(porta);

        estado = EstadoJogo.RODANDO;
        timer = new Timer(INTERVALO, this);
        timer.start();
    }

    /**
     * Configura o botão de pausa. Ao ser apertado, o jogo é pausado
     */
    public void pauseButton(){
        try {
            // Carregar imagem do ícone de pausa
            Image imgPause = ImageIO.read(new File("assets/pause.png"));
            imgPause = imgPause.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            iconPause = new ImageIcon(imgPause);
            
            pauseButton = new JButton(iconPause);
            pauseButton.setBounds(LARGURA_TELA - 60, 20, 40, 40);
            pauseButton.setBackground(Color.BLACK);
            pauseButton.setFocusPainted(false);
            pauseButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        } catch (IOException e) {
            System.out.println("Erro ao carregar ícone de pausa");
            pauseButton = new JButton("P"); // Fallback
        }
        pauseButton.addActionListener(e -> {
            mostrarPausa();
        });
    }

    /**
     * Mostra o menu de pausa. A partir dele, o jogador pode continuar o jogo ou voltar para a tela inicial
     */
    private void mostrarPausa(){
        if(estado == EstadoJogo.RODANDO) {
            estado = EstadoJogo.PAUSADO;
            timer.stop();
            Object[] options = {"Continuar", "Menu Principal"};
            int opcao = JOptionPane.showOptionDialog(
                this,
                "Jogo pausado",
                "Pausa",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
            );
            
            if(opcao == 1){
                voltarParaMenu();
            } else if(estado == EstadoJogo.PAUSADO) {
                estado = EstadoJogo.RODANDO;
                timer.start();
                requestFocusInWindow();
            }

        } else {
            timer.start();
            requestFocusInWindow();
        }
    }
    
    /**
     * Carrega a tela inicial
     */
    private void voltarParaMenu(){
        musica.stopSong();
        efeito.stopSong();
        cleanUp();

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        TelaInicio telaInicio = new TelaInicio(musica);
        frame.add(telaInicio);
        frame.pack();
        frame.revalidate();
        frame.repaint();
        telaInicio.requestFocusInWindow();
    }

    /**
     * Carrega as imagens necessárias para a tela de jogo.
     */
    public void carregarImagens(){
        backgroundImgs = new Image[5];
        batataImgs = new Image[8]; cenouraImgs = new Image[4]; slimeImgs = new Image[2]; flymeImgs = new Image[6];
        pratoImgs = new Image[2]; facaImgs = new Image[4]; armandibulaImgs = new Image[2]; morcerangoImgs = new Image[5];
        queijoBoxerImgs = new Image[4]; bracoImgs = new Image[2]; luvaImgs = new Image[4]; chocochatoImgs = new Image[2];
        algodogDoceImgs = new Image[6]; slimeBotImgs = new Image[4]; laserImgs = new Image[2]; gigaBotImgs = new Image[4];
        gLaserImgs = new Image[2]; malandranhaImgs = new Image[2]; alhoImgs = new Image[5]; portaImgs = new Image[2];

        try {
            paredeImg = ImageIO.read(new File("assets/parede.png"));
            alertImage = ImageIO.read(new File("assets/Warning.png"));
            pofImage = ImageIO.read(new File("assets/POF.png"));

            backgroundImgs[0] = ImageIO.read(new File("assets/BackgroundW1.png"));
            backgroundImgs[1] = ImageIO.read(new File("assets/background.png"));
            backgroundImgs[2] = ImageIO.read(new File("assets/BackgroundW3.png"));
            backgroundImgs[3] = ImageIO.read(new File("assets/BackgroundW4.png"));
            backgroundImgs[4] = ImageIO.read(new File("assets/BackgroundW5.png"));

            batataImgs[0] = ImageIO.read(new File("assets/DuqueBatataD.png"));
            batataImgs[1] = ImageIO.read(new File("assets/DuqueBatataE.png"));
            batataImgs[2] = ImageIO.read(new File("assets/DuqueBatataC.png"));
            batataImgs[3] = ImageIO.read(new File("assets/DuqueBatataAD.png"));
            batataImgs[4] = ImageIO.read(new File("assets/DuqueBatataAE.png"));
            batataImgs[5] = ImageIO.read(new File("assets/DuqueBatataAC.png"));
            batataImgs[6] = ImageIO.read(new File("assets/DuqueBatataAB.png"));
            batataImgs[7] = ImageIO.read(new File("assets/DuqueBatataX.png"));

            cenouraImgs[0] = ImageIO.read(new File("assets/CenouraD.png"));
            cenouraImgs[1] = ImageIO.read(new File("assets/CenouraE.png"));
            cenouraImgs[2] = ImageIO.read(new File("assets/CenouraC.png"));
            cenouraImgs[3] = ImageIO.read(new File("assets/CenouraB.png"));

            slimeImgs[0] = ImageIO.read(new File("assets/SlimeD.png"));
            slimeImgs[1] = ImageIO.read(new File("assets/SlimeE.png"));

            flymeImgs[0] = ImageIO.read(new File("assets/FlymeD1.png"));
            flymeImgs[1] = ImageIO.read(new File("assets/FlymeD2.png"));
            flymeImgs[2] = ImageIO.read(new File("assets/FlymeD3.png"));
            flymeImgs[3] = ImageIO.read(new File("assets/FlymeE1.png"));
            flymeImgs[4] = ImageIO.read(new File("assets/FlymeE2.png"));
            flymeImgs[5] = ImageIO.read(new File("assets/FlymeE3.png"));

            pratoImgs[0] = ImageIO.read(new File("assets/PratoD.png"));
            pratoImgs[1] = ImageIO.read(new File("assets/PratoE.png"));

            facaImgs[0] = ImageIO.read(new File("assets/FacaD.png"));
            facaImgs[1] = ImageIO.read(new File("assets/FacaE.png"));
            facaImgs[2] = ImageIO.read(new File("assets/FacaC.png"));
            facaImgs[3] = ImageIO.read(new File("assets/FacaB.png"));
            
            armandibulaImgs[0] = ImageIO.read(new File("assets/ArmandibulaD.png"));
            armandibulaImgs[1] = ImageIO.read(new File("assets/ArmandibulaA.png"));

            morcerangoImgs[0] = ImageIO.read(new File("assets/MorcerangoD.png"));
            morcerangoImgs[1] = ImageIO.read(new File("assets/MorcerangoAD1.png"));
            morcerangoImgs[2] = ImageIO.read(new File("assets/MorcerangoAD2.png"));
            morcerangoImgs[3] = ImageIO.read(new File("assets/MorcerangoAE1.png"));
            morcerangoImgs[4] = ImageIO.read(new File("assets/MorcerangoAE2.png"));

            queijoBoxerImgs[0] = ImageIO.read(new File("assets/QueijoBoxerD.png"));
            queijoBoxerImgs[1] = ImageIO.read(new File("assets/QueijoBoxerE.png"));
            queijoBoxerImgs[2] = ImageIO.read(new File("assets/QueijoBoxerC.png"));
            queijoBoxerImgs[3] = ImageIO.read(new File("assets/QueijoBoxerB.png"));

            bracoImgs[0] = ImageIO.read(new File("assets/BracoH.png"));
            bracoImgs[1] = ImageIO.read(new File("assets/BracoV.png"));

            luvaImgs[0] = ImageIO.read(new File("assets/LuvaD.png"));
            luvaImgs[1] = ImageIO.read(new File("assets/LuvaE.png"));
            luvaImgs[2] = ImageIO.read(new File("assets/LuvaC.png"));
            luvaImgs[3] = ImageIO.read(new File("assets/LuvaB.png"));

            chocochatoImgs[0] = ImageIO.read(new File("assets/Chocochato.png"));
            chocochatoImgs[1] = ImageIO.read(new File("assets/ChocochatoD.png"));

            algodogDoceImgs[0] = ImageIO.read(new File("assets/AlgodogDoceD.png"));
            algodogDoceImgs[1] = ImageIO.read(new File("assets/AlgodogDoceE.png"));
            algodogDoceImgs[2] = ImageIO.read(new File("assets/AlgodogDoceC.png"));
            algodogDoceImgs[3] = ImageIO.read(new File("assets/AlgodogDoceB.png"));
            algodogDoceImgs[4] = ImageIO.read(new File("assets/AlgodogDoceRD.png"));
            algodogDoceImgs[5] = ImageIO.read(new File("assets/AlgodogDoceRE.png"));

            algodaoImg = ImageIO.read(new File("assets/Algodao.png"));

            slimeBotImgs[0] = ImageIO.read(new File("assets/SlimeBotD.png"));
            slimeBotImgs[1] = ImageIO.read(new File("assets/SlimeBotE.png"));
            slimeBotImgs[2] = ImageIO.read(new File("assets/SlimeBotC.png"));
            slimeBotImgs[3] = ImageIO.read(new File("assets/SlimeBotB.png"));

            laserImgs[0] = ImageIO.read(new File("assets/laserH.png"));
            laserImgs[1] = ImageIO.read(new File("assets/laserV.png"));

            gigaBotImgs[0] = ImageIO.read(new File("assets/GigaBotD.png"));
            gigaBotImgs[1] = ImageIO.read(new File("assets/GigaBotE.png"));
            gigaBotImgs[2] = ImageIO.read(new File("assets/GigaBotC.png"));
            gigaBotImgs[3] = ImageIO.read(new File("assets/GigaBotB.png"));

            gLaserImgs[0] = ImageIO.read(new File("assets/laserGH.png"));
            gLaserImgs[1] = ImageIO.read(new File("assets/laserGV.png"));

            malandranhaImgs[0] = ImageIO.read(new File("assets/MalandranhaD.png"));
            malandranhaImgs[1] = ImageIO.read(new File("assets/MalandranhaE.png"));

            alhoImgs[0] = ImageIO.read(new File("assets/Alho.png"));
            alhoImgs[1] = ImageIO.read(new File("assets/DentalhoD.png"));
            alhoImgs[2] = ImageIO.read(new File("assets/DentalhoE.png"));
            alhoImgs[3] = ImageIO.read(new File("assets/DescascalhoD.png"));
            alhoImgs[4] = ImageIO.read(new File("assets/DescascalhoE.png"));

            portaImgs[0] = ImageIO.read(new File("assets/PortaFV.png"));
            portaImgs[1] = ImageIO.read(new File("assets/PortaAV.png"));
        } catch (IOException e) {
            System.out.println("Erro ao carregar imagens");
            e.printStackTrace();
        }
    }

    /**
     * Renderiza os elementos visuais da tela de jogo, incluindo todos os objetos colidíveis e partículas.
     * 
     * @param g Contexto gráfico para renderização
     
     */
    @Override
    public void desenharTela(Graphics g) {
        if(estado != EstadoJogo.PARADO){
            // Desenhar fundo (se a imagem existir)
            if(backgroundImgs != null) {
                g.drawImage(backgroundImgs[save - 1], 0, 0, LARGURA_TELA, ALTURA_TELA, this);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, LARGURA_TELA, ALTURA_TELA);
            }

            //Desenhar paredes
            for (Parede parede : paredes) {
                if(paredeImg != null){
                    g.drawImage(paredeImg, parede.getX(), parede.getY(), parede.getLargura(), parede.getAltura(), this);
                } else {
                    g.setColor(parede.getCor());
                    g.fillRect(parede.getX(), parede.getY(), parede.getLargura(), parede.getAltura());
                }
            }

            //Desenhar porta
            if(porta.getImage() != null){
                g.drawImage(porta.getImage(), porta.getX(), porta.getY(), porta.getLargura(), porta.getAltura(), this);
            } else {
                g.setColor(porta.getCor());
                g.fillRect(porta.getX(), porta.getY(), porta.getLargura(), porta.getAltura());
            }

            
            // Desenhar inimigos e seus projéteis
            for (Inimigo ini : inimigos) {
                if(ini.isAlive){
                    if(ini instanceof Prato){
                        for (Projetil p : ((Prato) ini).facas) {
                            if (p.isAtivo()) {
                                if(p.getImage() != null) {
                                    g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                                } else {
                                    g.setColor(p.getCor());
                                    g.fillOval(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                                }
                            }
                        }
                    } else if(ini instanceof QueijoBoxer){
                        // Inimigo.CampoDeVisao c = ((QueijoBoxer) ini).campoDeVisao;
                        // g.setColor(c.cor);
                        // g.fillRect(c.getX(), c.getY(), c.getLargura(), c.getAltura());

                        if(((QueijoBoxer) ini).braco != null){
                            Projetil b = ((QueijoBoxer) ini).braco;
                            if(b.isAtivo()){
                                if(b.getImage() != null && luvaImgs != null){
                                    g.drawImage(b.getImage(), b.getX(), b.getY(), b.getLargura(), b.getAltura(), this);
                                    // Luva é desenhada separadamente
                                    switch(((QueijoBoxer) ini).ultimaDirecao){
                                        case LEFT: g.drawImage(luvaImgs[1], b.getX() - 5, b.getY(),
                                            TAMANHO_BLOCO/2, TAMANHO_BLOCO/2, this);
                                        break;
                                        case RIGHT:
                                            g.drawImage(luvaImgs[0], b.getX() + b.getLargura() - TAMANHO_BLOCO/2 + 5, b.getY(),
                                                TAMANHO_BLOCO/2, TAMANHO_BLOCO/2, this);
                                        break;
                                        case UP: g.drawImage(luvaImgs[2], b.getX(), b.getY() - 5,
                                            TAMANHO_BLOCO/2, TAMANHO_BLOCO/2, this);
                                        break;
                                        case DOWN:
                                            g.drawImage(luvaImgs[3], b.getX(), b.getY() + b.getAltura() - TAMANHO_BLOCO/2 + 5,
                                                TAMANHO_BLOCO/2, TAMANHO_BLOCO/2, this);
                                        break;                                    
                                    }
                                } else {
                                    g.setColor(b.cor);
                                    g.fillRect(b.getX(), b.getY(), b.getLargura(), b.getAltura());
                                    g.setColor(Color.RED);
                                    // Luva é desenhada separadamente
                                    switch(((QueijoBoxer) ini).ultimaDirecao){
                                        case LEFT: g.fillRect(b.getX(), b.getY(), TAMANHO_BLOCO/2, TAMANHO_BLOCO/2); break;
                                        case RIGHT:
                                            g.fillRect(b.getX() + b.getLargura() - TAMANHO_BLOCO/2, b.getY(), TAMANHO_BLOCO/2, TAMANHO_BLOCO/2);
                                        break;
                                        case UP: g.fillRect(b.getX(), b.getY(), TAMANHO_BLOCO/2, TAMANHO_BLOCO/2); break;
                                        case DOWN:
                                            g.fillRect(b.getX(), b.getY() + b.getAltura() - TAMANHO_BLOCO/2, TAMANHO_BLOCO/2, TAMANHO_BLOCO/2);
                                        break;                                    
                                    }
                                }
                            }
                        }
                    } else if(ini instanceof AlgodogDoce){
                        // Inimigo.CampoDeVisao c = ((AlgodogDoce) ini).campoDeVisao;
                        // g.setColor(c.cor);
                        // g.fillRect(c.getX(), c.getY(), c.getLargura(), c.getAltura());

                        for (Projetil p : ((AlgodogDoce) ini).algodoes) {
                            if (p.isAtivo()) {
                                if(p.getImage() != null) {
                                    g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                                } else {
                                    g.setColor(p.getCor());
                                    g.fillOval(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                                }
                            }
                        }
                    } else if(ini instanceof SlimeBot){
                        if(((SlimeBot) ini).laser != null){
                            Projetil l = ((SlimeBot) ini).laser;
                            if(l.isAtivo()){
                                if(l.getImage() != null){
                                    g.drawImage(l.getImage(), l.getX(), l.getY(), l.getLargura(), l.getAltura(), this);
                                } else {
                                    g.setColor(l.cor);
                                    g.fillRect(l.getX(), l.getY(), l.getLargura(), l.getAltura());
                                }
                            }
                        }
                    } else if(ini instanceof GigaBot){
                        if(((GigaBot) ini).laser != null){
                            Projetil l = ((GigaBot) ini).laser;
                            if(l.isAtivo()){
                                if(l.getImage() != null){
                                    g.drawImage(l.getImage(), l.getX(), l.getY(), l.getLargura(), l.getAltura(), this);
                                } else {
                                    g.setColor(l.cor);
                                    g.fillRect(l.getX(), l.getY(), l.getLargura(), l.getAltura());
                                }
                            }
                        }
                    }

                    if(ini.getImage() != null) {
                        g.drawImage(ini.getImage(), ini.getX(), ini.getY(), ini.getLargura(), ini.getAltura(), this);
                    } else {
                        g.setColor(ini.cor);
                        g.fillRect(ini.getX(), ini.getY(), ini.getLargura(), ini.getAltura());
                    }
                }
            }

            // Desenhar personagem
            if(batata.isInvulnerable()){
                if(batataImgs[7] != null){
                    if ((System.currentTimeMillis() / 100) % 2 == 0) {
                        g.drawImage(batataImgs[7], batata.getX(), batata.getY(), batata.getLargura(), batata.getAltura(), this);
                    }
                } else {
                    g.setColor(batata.getCor());
                    g.fillOval(batata.getX(), batata.getY(), TAMANHO_BLOCO, TAMANHO_BLOCO);
                }
            } else {
                if(batata.getImage() != null) {
                    g.drawImage(batata.getImage(), batata.getX(), batata.getY(), batata.getLargura(), batata.getAltura(), this);
                } else {
                    g.setColor(batata.getCor());
                    g.fillOval(batata.getX(), batata.getY(), TAMANHO_BLOCO, TAMANHO_BLOCO);
                }
            }

            // Desenhar cenouras
            for (Projetil p : cenouras) {
                if (p.isAtivo()) {
                    if(p.getImage() != null) {
                        g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                    } else {
                        g.setColor(p.getCor());
                        g.fillOval(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                    }
                }
            }

            // Particulas
            for (Pof p : pofs) {
                if(p.isAtivo()){
                    if(pofImage != null) {
                        g.drawImage(pofImage, p.getX(), p.getY(), p.getTamanho(), p.getTamanho(), this);
                    } else {
                        g.setColor(new Color(232, 67, 147));
                        g.fillRect(p.getX(), p.getY(), p.getTamanho(), p.getTamanho());
                    }
                }
            }
            for (Alert a : alertas) {
                if(a.isAtivo() && (System.currentTimeMillis() / 100) % 2 == 0){
                    if(alertImage != null) {
                        g.drawImage(alertImage, a.getX(), a.getY(), a.getLargura(), a.getAltura(), this);
                    } else {
                        g.setColor(Color.RED);
                        g.fillRect(a.getX(), a.getY(), a.getLargura(), a.getAltura());
                    }
                }
            }

            // Barra de HP do Duque e Contador de mortes
            g.setColor(Color.BLACK);
            g.fillRect(5, 5, 235, 75);
            g.setColor(Color.WHITE);
            g.setFont(new Font(NOME_FONTE, Font.BOLD, 20));
            // FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Vida: " + batata.getHp(), 20, 10 + g.getFont().getSize());

            g.setColor(Color.RED);
            g.fillRect(120, 10, 115, 30); // Fundo
            g.setColor(Color.GREEN);
            g.fillRect(120, 10, (int)(115 * ((float)batata.getHp() / 5)), 30); // HP atual

            g.setColor(Color.WHITE);
            g.setFont(new Font(NOME_FONTE, Font.BOLD, 20));
            // FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Inimigos Restantes: " + enemyCount, 20, 50 + g.getFont().getSize());
        }
    }

    /**
     * Manipula eventos de ação.
     * 
     * <p>
     * Atualiza o estado de todos os objetos colidíveis e as interações entre eles, além das partículas de pof!.
     * Solicita a repintura do componente a cada intervalo definido no timer.
     * </p>
     * 
     * @param e Evento de ação disparado
     */
    public void actionPerformed(ActionEvent e) {
        if(estado == EstadoJogo.RODANDO) {
            enemyCount = inimigos.size();
            
            // Atualiza as cenouras
            Iterator<Projetil> projIt = cenouras.iterator();
            while (projIt.hasNext()) {
                Projetil p = projIt.next();
                p.mover();

                // Verifica colisão com todos os objetos
                for (ObjetoColidivel obj : objetosColidiveis) {
                    boolean collisionHappened = true;
                    if (
                        p.shouldCollideWith(obj) &&
                        obj.getLayer() != ObjetoColidivel.CollisionLayer.PLAYER &&
                        p.colideCom(obj)
                    ){
                        // Colisão com inimigo
                        if (obj.getLayer() == ObjetoColidivel.CollisionLayer.ENEMY) {
                            if (!((Inimigo) obj).takeDamage(1)) collisionHappened = false;
                        }
                        // Colisão com projeteis inimigos
                        else if(obj.getLayer() == ObjetoColidivel.CollisionLayer.PROJECTILE){
                            if(!((Projetil) obj).isCollidable()) collisionHappened = false;
                            else {
                                ((Projetil) obj).desativar();
                            }
                        }
                        
                        // Remove o projétil em qualquer colisão ocorrida
                        if(collisionHappened){
                            p.desativar();
                            objetosColidiveis.remove(p);
                            projIt.remove();
                            break;
                        }
                    }
                }
                
                // Remove se saiu da tela
                if (p.foraDaTela()) {
                    objetosColidiveis.remove(p);
                    projIt.remove();
                }
            }

            // Atualiza inimigos
            if (inimigos.isEmpty() && !porta.isAberta()) {
                efeito.playSong("assets/ta-Da.wav", false);
                porta.setImage(portaImgs[1]);
                porta.abrir();
            }

            pofs.removeIf(Pof::timeOut);

            Iterator<Inimigo> iniIt = inimigos.iterator();
            while (iniIt.hasNext()) {
                Inimigo ini = iniIt.next();
                if(ini.isAlive){
                    ini.atacar();
                    verificarColisaoParede(ini);
                } else {
                    objetosColidiveis.remove(ini);
                    iniIt.remove();
                }
            }

            batata.updateInvulnerability();
            if(batata.getHp() <= 0){
                estado = EstadoJogo.PARADO;
                gameOver();
            }

            repaint();
        }
    }

    /**
     * Carrega a tela de Game Over
     */
    private void gameOver(){
        musica.stopSong();
        efeito.stopSong();
        cleanUp();

        Window window = SwingUtilities.windowForComponent(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            TelaGameOver telaGameOver = new TelaGameOver(musica);
            frame.getContentPane().removeAll();
            frame.add(telaGameOver);
            frame.revalidate();
            frame.repaint();
            telaGameOver.requestFocusInWindow();
        } else {
            System.err.println("Erro: Não foi possível encontrar o JFrame principal");
        }
    }

    /** 
     * Classe para os inimigos do jogo
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private abstract class Inimigo extends ObjetoColidivel {
        /** O tamanho do inimigo (o padrão é 50px) */
        protected int tamanho;
        /** Os pontos de vida do inimigo (o padrão é 1) */
        protected int hp;
        /** A velocidade do inimigo (se ele é estacionário, o valor é 0) */
        protected int velocidade;
        /** A última direção do inimigo */
        protected Direction ultimaDirecao;
        /** Indica se o inimigo pode passar por cima de paredes */
        protected boolean isFlying;
        /** Indica se o inimigo está vivo */
        protected boolean isAlive = true;
        
        /**
         * Construtor da classe Inimigo
         * 
         * @param x Posição x
         * @param y Posição y
         * @param tamanho Tamanho
         * @param cor Cor
         * @param hp Pontos de vida
         * @param velocidade Velocidade
         * @param ultimaDirecao Última direção
         * @param isFlying Voa/Escala sobre paredes?
         */
        public Inimigo(int x, int y, int tamanho, Color cor, int hp, int velocidade, Direction ultimaDirecao, boolean isFlying) {
            super(x, y, tamanho, tamanho, cor, CollisionLayer.ENEMY); // Tamanho do inimigo
            this.tamanho = tamanho;
            this.cor = cor;
            this.hp = hp;  
            this.velocidade = velocidade;
            this.ultimaDirecao = ultimaDirecao;
            this.isFlying = isFlying;
        }

        /**
         * Atualiza o dano causado ao inimigo
         * 
         * @param damage O dano causado
         * @return {@code true} (esse valor só poderá se tornar {@code false} para certos inimigos)
         */
        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp <= 0){
                efeito.playSong("assets/tiro.wav", false);
                pofs.add(new Pof(x, y, tamanho + tamanho / 2));
                isAlive = false;
            }
            return true;
        }

        /**
         * Gerencia o comportamento (movimentação e ataques) de cada inimigo
         * Deve ser implentado por subclasses para definir seu comportamento
         */
        public abstract void atacar();

        /**
         * Campo de visão usado por certos inimigos em seus ataques
         * @author Arthur dos Santos Rezende
         * @version 1.0
         */
        public class CampoDeVisao extends ObjetoColidivel{
            /**
             * Construtor do campo de visão
             * 
             * @param x Posição x
             * @param y Posição y
             * @param largura Largura
             * @param altura Altura
             */
            public CampoDeVisao(int x, int y, int largura, int altura){
                super(x, y, largura, altura, Color.WHITE, CollisionLayer.LINE_OF_SIGHT);
            }
        }
    }

    /** 
     * Classe para o Slime, o imimigo básico
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class Slime extends Inimigo {
        /**
         * Construtor da classe Slime
         * 
         * @param x Posição x
         * @param y Posição y
         * @param dirInicial Direção inicial
         */     
        public Slime(int x, int y, Direction dirInicial){
            super(x, y, TAMANHO_BLOCO, Color.GREEN, 1, 2, dirInicial, false);
        }

        /**
         * Faz com que o slime se mova de um lado para o outro
         */
        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }
            switch(this.ultimaDirecao){
                case LEFT:
                    this.x -= velocidade;
                    this.curImage = slimeImgs[1];
                break;
                case RIGHT:
                    this.x += velocidade;
                    this.curImage = slimeImgs[0];
                break;
                case UP:
                    this.y -= velocidade;
                    this.curImage = slimeImgs[1];
                break;
                case DOWN:
                    this.y += velocidade;
                    this.curImage = slimeImgs[0];
                break;
            }

            if(foraDaTela()){
                switch(this.ultimaDirecao){
                    case LEFT:
                        this.x += velocidade;
                        this.ultimaDirecao = Direction.RIGHT;
                    break;
                    case RIGHT:
                        this.x -= velocidade;
                        this.ultimaDirecao = Direction.LEFT;
                    break;
                    case UP:
                        this.y += velocidade;
                        this.ultimaDirecao = Direction.DOWN;
                    break;
                    case DOWN:
                        this.y -= velocidade;
                        this.ultimaDirecao = Direction.UP;
                    break;
                }
            }
        }
    }

    /** 
     * Classe para o Flyme, basicamente um slime que voa
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class Flyme extends Inimigo {
        /**
         * Construtor da classe Flyme
         * 
         * @param x Posição x
         * @param y Posição y
         * @param dirInicial Direção inicial
         */  
        public Flyme(int x, int y, Direction dirInicial){
            super(x, y, TAMANHO_BLOCO, Color.CYAN, 1, 2, dirInicial, true);
        }

        /**
         * Faz com que o flyme se mova de um lado para o outro
         */
        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }
            switch(this.ultimaDirecao){
                case LEFT:
                    this.x -= velocidade;
                    if ((System.currentTimeMillis() / 100) % 4 == 0) this.curImage = flymeImgs[3];
                    else if (
                        (System.currentTimeMillis() / 100) % 4 == 1 ||
                        (System.currentTimeMillis() / 100) % 4 == 3
                    ) this.curImage = flymeImgs[4];
                    else this.curImage = flymeImgs[5];
                break;
                case RIGHT:
                    this.x += velocidade;
                    if ((System.currentTimeMillis() / 100) % 4 == 0) this.curImage = flymeImgs[0];
                    else if (
                        (System.currentTimeMillis() / 100) % 4 == 1 ||
                        (System.currentTimeMillis() / 100) % 4 == 3
                    ) this.curImage = flymeImgs[1];
                    else this.curImage = flymeImgs[2];
                break;
                case UP:
                    this.y -= velocidade;
                    if ((System.currentTimeMillis() / 100) % 4 == 0) this.curImage = flymeImgs[3];
                    else if (
                        (System.currentTimeMillis() / 100) % 4 == 1 ||
                        (System.currentTimeMillis() / 100) % 4 == 3
                    ) this.curImage = flymeImgs[4];
                    else this.curImage = flymeImgs[5];
                break;
                case DOWN:
                    this.y += velocidade;
                    if ((System.currentTimeMillis() / 100) % 4 == 0) this.curImage = flymeImgs[0];
                    else if (
                        (System.currentTimeMillis() / 100) % 4 == 1 ||
                        (System.currentTimeMillis() / 100) % 4 == 3
                    ) this.curImage = flymeImgs[1];
                    else this.curImage = flymeImgs[2];
                break;
            }

            if(foraDaTela()){
                switch(this.ultimaDirecao){
                    case LEFT:
                        this.x += velocidade;
                        this.ultimaDirecao = Direction.RIGHT;
                    break;
                    case RIGHT:
                        this.x -= velocidade;
                        this.ultimaDirecao = Direction.LEFT;
                    break;
                    case UP:
                        this.y += velocidade;
                        this.ultimaDirecao = Direction.DOWN;
                    break;
                    case DOWN:
                        this.y -= velocidade;
                        this.ultimaDirecao = Direction.UP;
                    break;
                }
            }
        }
    }

    /** 
     * Classe para o Prato, um inimigo que atira facas em uma determinada direção
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class Prato extends Inimigo {
        /** Uma lista para gerenciar os projéteis do prato (as facas) */
        private ArrayList<Projetil> facas = new ArrayList<>();
        /** Guarda a última vez que o prato atirou */
        private long ultimoTiro = 0;

        /**
         * Construtor da classe Prato
         * 
         * @param x Posição x
         * @param y Posição y
         * @param dir Direção
         */
        public Prato(int x, int y, Direction dir) {
            super(x, y, TAMANHO_BLOCO, Color.WHITE, 1, 0, dir, false);
            if(dir == Direction.LEFT || dir == Direction.UP){
                this.curImage = pratoImgs[1];
            }
            else {
                this.curImage = pratoImgs[0];
            }
        }

        /**
         * Faz com que o prato atire suas facas na direção especificada e gerencia as facas
         */
        public void atacar(){
            long now = System.currentTimeMillis();
            int offsetX = 0, offsetY = 0;
            
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }

            if (now - ultimoTiro > INTERVALO_TIRO*5) {
                switch(this.ultimaDirecao){
                    case LEFT:
                        offsetY = TAMANHO_BLOCO/2;
                    break;
                    case RIGHT:
                        offsetX = TAMANHO_BLOCO;
                        offsetY = TAMANHO_BLOCO/2;
                    break;
                    case UP:
                        offsetX = TAMANHO_BLOCO/2;
                    break;
                    case DOWN:
                        offsetX = TAMANHO_BLOCO/2;
                        offsetY = TAMANHO_BLOCO;
                    break;
                }
                Projetil p = KnifeFactory.createProjectile(this.x + offsetX, this.y + offsetY, this.ultimaDirecao);
                // Verifica a direção para definir a imagem
                switch(ultimaDirecao){
                    case LEFT: p.setImage(facaImgs[1]); break;
                    case RIGHT: p.setImage(facaImgs[0]); break;
                    case UP: p.setImage(facaImgs[2]); break;
                    case DOWN: p.setImage(facaImgs[3]); break;
                }
                facas.add(p);
                objetosColidiveis.add(p);
                ultimoTiro = now;
            }

            Iterator<Projetil> projIt = facas.iterator();
            while (projIt.hasNext()) {
                Projetil p = projIt.next();
                p.mover();

                // Verifica colisão com todos os objetos
                for (ObjetoColidivel obj : objetosColidiveis) {
                    if (
                        p.shouldCollideWith(obj) &&
                        obj.getLayer() != ObjetoColidivel.CollisionLayer.ENEMY &&
                        obj.getLayer() != ObjetoColidivel.CollisionLayer.PROJECTILE &&
                        p.colideCom(obj)
                    ){
                        // Colisão com o Duque
                        if (obj.getLayer() == ObjetoColidivel.CollisionLayer.PLAYER && !batata.isInvulnerable()) {
                            efeito.playSong("assets/Ouch.wav", false);
                            batata.takeDamage(1);
                        }

                        // Remove o projétil em qualquer colisão
                        p.desativar();
                        objetosColidiveis.remove(p);
                        projIt.remove();
                        break;
                    }
                }
                
                // Remove se saiu da tela ou se o prato morreu
                if (p.foraDaTela() || !this.isAlive) {
                    p.desativar();
                    objetosColidiveis.remove(p);
                    projIt.remove();
                }
            }
        }

        /** 
         * Classe para a construção dos projéteis do prato (facas)
         * 
         * @author Arthur dos Santos Rezende
         * @version 1.0
         */
        public class KnifeFactory {
            /**
             * Cria uma nova faca
             * 
             * @param x Posição x
             * @param y Posição y
             * @param direction Direção
             * @return O projétil criado
             */
            public static Projetil createProjectile(int x, int y, Direction direction) {
                int dirX = 0, dirY = 0;
                Color color = Color.LIGHT_GRAY;

                if(direction == Direction.UP){
                    dirY = -2;
                    return new Projetil(x, y, 10, 40, dirX, dirY, color, false);
                } else if(direction == Direction.DOWN){
                    dirY = 2;
                    return new Projetil(x, y, 10, 40, dirX, dirY, color, false);
                } else if(direction == Direction.LEFT){
                    dirX = -2;
                    return new Projetil(x, y, 40, 10, dirX, dirY, color, false);
                } else {
                    dirX = 2;
                    return new Projetil(x, y, 40, 10, dirX, dirY, color, false);
                }
            }
        }
    }

    /** 
     * Classe para o Armandíbula, um inimigo que morde quem pisa nele enquanto dorme
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class Armandibula extends Inimigo {
        /** Indicam se o armandíbula está dormindo/ em alerta */
        private boolean isSleeping = true, triggered = false;
        /** Tempo de alerta */
        private long triggerTimer = 0;
        /** A partícula de alerta que aparece quando o jogador pisa no (colide com o) Armandíbula */
        private Alert alerta;

        /**
         * Construtor da classe Armandibula
         * 
         * @param x Posição x
         * @param y Posição y
         */
        public Armandibula(int x, int y){
            super(x, y, TAMANHO_BLOCO, new Color(186, 220, 88), 1, 0, Direction.UP, false);
        }

        /**
         * Atualiza o dano causado ao armandíbula
         * 
         * @param damage O dano causado
         * @return {@code true} Se o armandíbula estiver acordado. Caso contrário {@code false}
         */
        public boolean takeDamage(int damage){
            if(!isSleeping){
                this.hp -= damage;
                if(hp <= 0){
                    efeito.playSong("assets/tiro.wav", false);
                    pofs.add(new Pof(x, y, tamanho + tamanho / 2));
                    if(alerta != null) {
                        alerta.desativar();
                        alertas.remove(alerta);
                    }
                    isAlive = false;
                }
                return true;
            }

            return false;
        }

        /**
         * Faz com que o armandíbula acorde caso o jogador colida com ele.
         */
        public void atacar(){
            if(isSleeping){
                this.curImage = armandibulaImgs[0];
                if(colideCom(batata) && !triggered){
                    efeito.playSong("assets/warning.wav", false);
                    efeito.playSong("assets/bite.wav", false);
                    alerta = new Alert(x + tamanho/2 - TAMANHO_BLOCO/6, y - TAMANHO_BLOCO, (TAMANHO_BLOCO*2)/3);
                    alertas.add(alerta);
                    triggered = true;
                    triggerTimer = System.currentTimeMillis();
                }
                if(triggered && System.currentTimeMillis() - triggerTimer >= 800){
                    alerta.desativar();
                    alertas.remove(alerta);
                    isSleeping = false;
                    this.cor = new Color(22, 160, 133);
                }
            }
            else {
                this.curImage = armandibulaImgs[1];
                if(colideCom(batata) && !batata.isInvulnerable()){
                    efeito.playSong("assets/Ouch.wav", false);
                    batata.takeDamage(1);
                }
            }
        }
    }

    /** 
     * Classe para o Morcerango, um inimigo que dorme em paredes e acorda caso outro inimigo na mesma dungeon seja estourado.
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class Morcerango extends Inimigo {
        /** Indicam se o morcerango está dormindo/ em alerta */
        private boolean isSleeping = true, triggered = false;
        /** Tempo de alerta */
        private long triggerTimer = 0;
        /** Guarda quantos inimigos haviam na dungeon inicialmente */
        private int inicialEnemyCount = -1;
        /** A partícula de alerta que aparece quando um inimigo é estourado */
        private Alert alerta;
        
        /**
         * Construtor da classe Morcerango
         * 
         * @param x Posição x
         * @param y Posição y
         * @param dirInicial Direção inicial
         */
        public Morcerango(int x, int y, Direction dirInicial){
            super(x, y, TAMANHO_BLOCO, new Color(181, 52, 113), 1, 4, dirInicial, true);
        }

        /**
         * Atualiza o dano causado ao morcerango
         * 
         * @param damage O dano causado
         * @return {@code true} Se o morcerango estiver acordado. Caso contrário {@code false}
         */
        public boolean takeDamage(int damage){
            if(!isSleeping){
                this.hp -= damage;
                if(hp <= 0){
                    efeito.playSong("assets/tiro.wav", false);
                    pofs.add(new Pof(x, y, tamanho + tamanho / 2));
                    if(alerta != null) {
                        alerta.desativar();
                        alertas.remove(alerta);
                    }
                    isAlive = false;
                }
                return true;
            }

            return false;
        }

        /**
         * Gerencia o comportamento do morcerango
         * <p>
         * Atualiza a quantidade inicial de inimigos caso ela já não tenha sido definida.
         * Acorda o morcerango caso a quantidade atual de inimigos seja menor que a inicial.
         * Também faz com que o morcerango começe a se mover caso esteja acordado.
         * </p>
         */
        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }

            if(isSleeping){
                this.curImage = morcerangoImgs[0];
                if(inicialEnemyCount < 0){
                    this.inicialEnemyCount = enemyCount;
                }
                else if(enemyCount < inicialEnemyCount && !triggered){
                    efeito.playSong("assets/warning.wav", false);
                    this.alerta = new Alert(x + tamanho/2 - TAMANHO_BLOCO/6, y - TAMANHO_BLOCO, (TAMANHO_BLOCO*2)/3);
                    alertas.add(alerta);
                    triggered = true;
                    triggerTimer = System.currentTimeMillis();
                }
                if(triggered && System.currentTimeMillis() - triggerTimer >= 400){
                    if(alerta != null) {
                        alerta.desativar();
                        alertas.remove(alerta);
                    }
                    isSleeping = false;
                    this.cor = Color.RED;
                }
            }
            else {
                switch(this.ultimaDirecao){
                    case LEFT:
                        this.x -= velocidade;
                        if ((System.currentTimeMillis() / 100) % 2 == 0) this.curImage = morcerangoImgs[3];
                        else this.curImage = morcerangoImgs[4];
                    break;
                    case RIGHT:
                        this.x += velocidade;
                        if ((System.currentTimeMillis() / 100) % 2 == 0) this.curImage = morcerangoImgs[1];
                        else this.curImage = morcerangoImgs[2];
                    break;
                    case UP:
                        this.y -= velocidade;
                        if ((System.currentTimeMillis() / 100) % 2 == 0) this.curImage = morcerangoImgs[3];
                        else this.curImage = morcerangoImgs[4];
                    break;
                    case DOWN:
                        this.y += velocidade;
                        if ((System.currentTimeMillis() / 100) % 2 == 0) this.curImage = morcerangoImgs[1];
                        else this.curImage = morcerangoImgs[2];
                    break;
                }

                if(foraDaTela()){
                    switch(this.ultimaDirecao){
                        case LEFT:
                            this.x += velocidade;
                            this.ultimaDirecao = Direction.RIGHT;
                        break;
                        case RIGHT:
                            this.x -= velocidade;
                            this.ultimaDirecao = Direction.LEFT;
                        break;
                        case UP:
                            this.y += velocidade;
                            this.ultimaDirecao = Direction.DOWN;
                        break;
                        case DOWN:
                            this.y -= velocidade;
                            this.ultimaDirecao = Direction.UP;
                        break;
                    }
                }
            }
        }
    }

    /** 
     * Classe para o Queijo Boxer, um inimigo com um braço extensível que usa para socar o jogador.
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class QueijoBoxer extends Inimigo {
        /** Indicam se o queijo boxer já atacou/ está em alerta */
        private boolean hasAttacked = false, triggered = false;
        /** Tempo de alerta */
        private long triggerTimer = 0;
        /** A partícula de alerta que aparece caso o queijo boxer perceba o jogador */
        private Alert alerta;
        /** O campo de visão do queijo boxer */
        private CampoDeVisao campoDeVisao;
        /** O alcance do campo de visão/braço do queijo boxer */
        private int alcanceDeVisao;
        /** O braço extensível do queijo boxer */
        private Projetil braco;
        
        /**
         * Construtor da classe Queijo Boxer
         * 
         * @param x Posição x
         * @param y Posição y
         * @param dir Direçao
         * @param sightRange Alcançe
         */
        public QueijoBoxer(int x, int y, Direction dir, int sightRange){
            super(x, y, TAMANHO_BLOCO, Color.YELLOW, 1, 0, dir, false);
            this.alcanceDeVisao = sightRange;
            switch(dir){
                case LEFT:
                    campoDeVisao = new CampoDeVisao(x - alcanceDeVisao, y, alcanceDeVisao, TAMANHO_BLOCO);
                    this.curImage = queijoBoxerImgs[1];
                break;
                case RIGHT:
                    campoDeVisao = new CampoDeVisao(x + tamanho, y, alcanceDeVisao, TAMANHO_BLOCO);
                    this.curImage = queijoBoxerImgs[0];
                break;
                case UP:
                    campoDeVisao = new CampoDeVisao(x, y - alcanceDeVisao, TAMANHO_BLOCO, alcanceDeVisao);
                    this.curImage = queijoBoxerImgs[2];
                break;
                case DOWN:
                    campoDeVisao = new CampoDeVisao(x, y + tamanho, TAMANHO_BLOCO, alcanceDeVisao);
                    this.curImage = queijoBoxerImgs[3];
                break;
            }
        }

        /**
         * Atualiza o dano causado ao queijo boxer, e remove o braço e alerta caso seja derrotado
         * 
         * @param damage O dano causado
         * @return O retorno é sempre {@code true} nesse caso
         */
        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp <= 0){
                efeito.playSong("assets/tiro.wav", false);
                pofs.add(new Pof(x, y, tamanho + tamanho / 2));
                if(braco != null){
                    braco.desativar();
                    objetosColidiveis.remove(braco);
                }
                if(alerta != null) {
                    alerta.desativar();
                    alertas.remove(alerta);
                }
                isAlive = false;
            }
            return true;
        }

        /**
         * Faz com que o queijo boxer ataque o jogador caso ele colida com seu campo de visão. Também gerencia o braço
         */
        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }

            if(!hasAttacked){
                if(batata.colideCom(campoDeVisao) && !triggered){
                    efeito.playSong("assets/warning.wav", false);
                    this.alerta = new Alert(x + tamanho/2 - TAMANHO_BLOCO/6, y - TAMANHO_BLOCO, (TAMANHO_BLOCO*2)/3);
                    alertas.add(alerta);
                    triggered = true;
                    triggerTimer = System.currentTimeMillis();
                }
                if(triggered && System.currentTimeMillis() - triggerTimer >= 1000){
                    if(alerta != null) {
                        alerta.desativar();
                        alertas.remove(alerta);
                    }
                    switch(ultimaDirecao){
                        case LEFT:
                            braco = new Projetil(x - alcanceDeVisao + 5, y + TAMANHO_BLOCO/4, alcanceDeVisao, TAMANHO_BLOCO/2,
                                0, 0, Color.YELLOW, true);
                            braco.curImage = bracoImgs[0];
                        break;
                        case RIGHT:
                            braco = new Projetil(x + tamanho - 5, y + TAMANHO_BLOCO/4, alcanceDeVisao, TAMANHO_BLOCO/2,
                                0, 0, Color.YELLOW, true);
                            braco.curImage = bracoImgs[0];
                        break;
                        case UP:
                            braco = new Projetil(x + TAMANHO_BLOCO/4, y - alcanceDeVisao + 5, TAMANHO_BLOCO/2, alcanceDeVisao,
                                0, 0, Color.YELLOW, true);
                            braco.curImage = bracoImgs[1];
                        break;
                        case DOWN:
                            braco = new Projetil(x + TAMANHO_BLOCO/4, y + tamanho - 5, TAMANHO_BLOCO/2, alcanceDeVisao,
                                0, 0, Color.YELLOW, true);
                            braco.curImage = bracoImgs[1];
                        break;
                    }
                    objetosColidiveis.add(braco);
                    efeito.playSong("assets/squish.wav", false);
                    hasAttacked = true;
                }
            } else {
                if(!braco.isAtivo()) takeDamage(hp);
                else if(braco.colideCom(batata) && !batata.isInvulnerable()){
                    efeito.playSong("assets/Ouch.wav", false);
                    batata.takeDamage(1);
                }
            }
        }
    }

    /** 
     * Classe para o Chocochato, um inimigo resistente que não ataca o jogador, mas proteje outros inimigos atrás dele
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class Chocochato extends Inimigo {
        /** Duração de sua "animação" de dano */
        private long dmgTimer;

        /**
         * Construtor da classe Chocochato
         * 
         * @param x Posição x
         * @param y Posição y
         */
        public Chocochato(int x, int y){
            super(x, y, TAMANHO_BLOCO, new Color(87, 43, 18), 3, 0, Direction.UP, false);
            this.curImage = chocochatoImgs[0];
        }

        /**
         * Atualiza o dano causado ao chocochato.
         * 
         * @param damage O dano causado
         * @return O retorno é sempre {@code true} nesse caso
         */
        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp <= 0){
                efeito.playSong("assets/tiro.wav", false);
                pofs.add(new Pof(x, y, tamanho + tamanho / 2));
                isAlive = false;
            } else {
                efeito.playSong("assets/crunchy.wav", false);
                curImage = chocochatoImgs[1];
                dmgTimer = System.currentTimeMillis();
            }
            return true;
        }

        /**
         * Impede o jogador de passar por ele e gerencia a "animação" de dano
         */
        public void atacar(){
            if(System.currentTimeMillis() - dmgTimer >= 1000) curImage = chocochatoImgs[0];
            if (colideCom(batata)) {
                // Reverte o movimento
                switch (batata.getUltimaDirecao()) {
                    case LEFT: batata.x += batata.getVelocidade(); break;
                    case RIGHT: batata.x -= batata.getVelocidade(); break;
                    case UP: batata.y += batata.getVelocidade(); break;
                    case DOWN: batata.y -= batata.getVelocidade(); break;
                }
            }
        }
    }

    /** 
     * Classe para o Algodog Doce, um inimigo que rola na direção do jogador e espalha algodão pelo caminho
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class AlgodogDoce extends Inimigo {
        /** Indicam se o algodog está rolando/ em alerta */
        private boolean isRolling = false, triggered = false;
        /** Tempo de alerta */
        private long triggerTimer = 0;
        /** A partícula de alerta que aparece caso o algodog perceba o jogador */
        private Alert alerta;
        /** Uma variável de direção auxiliar */
        private Direction curDirection;
        /** O campo de visão do algodog */
        private CampoDeVisao campoDeVisao;
        /** O alcance do campo de visão do algodog */
        private int alcanceDeVisao;
        /** Uma lista para gerenciar os algodões deixados pelo algodog ao rolar*/
        private ArrayList<Projetil> algodoes = new ArrayList<>();
        
        /**
         * Construtor da classe AlgodogDoce
         * 
         * @param x Posição x
         * @param y Posição y
         * @param dirInicial Direção inicial
         * @param sightRange Alcançe de visão
         */
        public AlgodogDoce(int x, int y, Direction dirInicial, int sightRange){
            super(x, y, TAMANHO_BLOCO, Color.PINK, 1, 3, dirInicial, false);
            this.alcanceDeVisao = sightRange;
            changeSight();
        }

        /**
         * Atualiza o dano causado ao algodog, e remove os algodões e alerta caso seja derrotado
         * 
         * @param damage O dano causado
         * @return O retorno é sempre {@code true} nesse caso
         */
        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp <= 0){
                efeito.playSong("assets/tiro.wav", false);
                pofs.add(new Pof(x, y, tamanho + tamanho / 2));
                if(alerta != null) {
                    alerta.desativar();
                    alertas.remove(alerta);
                }
                isAlive = false;
                Iterator<Projetil> projIt = algodoes.iterator();
                while (projIt.hasNext()) {
                    Projetil p = projIt.next();
                    p.desativar();
                    objetosColidiveis.remove(p);
                    projIt.remove();
                }
            }
            return true;
        }

        /**
         * Cria o campo de visão do algodog doce, ou o altera caso ele tenha trocado de direção
         */
        public void changeSight(){
            if(campoDeVisao != null) campoDeVisao = null;
            switch(this.ultimaDirecao){
                case LEFT:
                    campoDeVisao = new CampoDeVisao(x - alcanceDeVisao, y, alcanceDeVisao, TAMANHO_BLOCO);
                    this.curImage = algodogDoceImgs[1];
                break;
                case RIGHT:
                    campoDeVisao = new CampoDeVisao(x + tamanho, y, alcanceDeVisao, TAMANHO_BLOCO);
                    this.curImage = algodogDoceImgs[0];
                break;
                case UP:
                    campoDeVisao = new CampoDeVisao(x, y - alcanceDeVisao, TAMANHO_BLOCO, alcanceDeVisao);
                    this.curImage = algodogDoceImgs[2];
                break;
                case DOWN:
                    campoDeVisao = new CampoDeVisao(x, y + tamanho, TAMANHO_BLOCO, alcanceDeVisao);
                    this.curImage = algodogDoceImgs[3];
                break;
            }
        }

        /**
         * Gerencia o comportamento do Algodog
         * 
         * <p>
         * Caso o jogador entre no campo de visão do algodog enquanto ele estiver parado, ele começa a rolar
         * até ele colidir com a borda da tela ou colidir com uma parede (usando a variável auxiliar de direção, já
         * que ele troca de direção em ambos os casos), gerando algodão de forma semi-aleatória enquanto rola.
         * Também gerencia os algodões.
         * </p>
         */
        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }
            if(!isRolling){
                if(batata.colideCom(campoDeVisao) && !triggered){
                    efeito.playSong("assets/warning.wav", false);
                    this.alerta = new Alert(x + tamanho/2 - TAMANHO_BLOCO/6, y - TAMANHO_BLOCO, (TAMANHO_BLOCO*2)/3);
                    alertas.add(alerta);
                    triggered = true;
                    triggerTimer = System.currentTimeMillis();
                }
                if(triggered && System.currentTimeMillis() - triggerTimer >= 600){
                    if(alerta != null) {
                        alerta.desativar();
                        alertas.remove(alerta);
                    }
                    efeito.playSong("assets/dog-bark.wav", false);
                    isRolling = true;
                    curDirection = ultimaDirecao;
                }
            } else {
                if(getX() % TAMANHO_BLOCO/2 == 0 && getY() % TAMANHO_BLOCO/2 == 0 && new Random().nextInt(2) == 1){
                    Projetil p = AlgodaoFactory.createProjectile(this.x, this.y);
                    algodoes.add(p);
                    objetosColidiveis.add(p);
                    p.curImage = algodaoImg;
                }
                switch(this.ultimaDirecao){
                    case LEFT:
                        this.x -= velocidade;
                        this.curImage = algodogDoceImgs[5];
                    break;
                    case RIGHT:
                        this.x += velocidade;
                        this.curImage = algodogDoceImgs[4];
                    break;
                    case UP:
                        this.y -= velocidade;
                        this.curImage = algodogDoceImgs[5];
                    break;
                    case DOWN:
                        this.y += velocidade;
                        this.curImage = algodogDoceImgs[4];
                    break;
                }

                if(foraDaTela()){
                    switch(this.ultimaDirecao){
                        case LEFT:
                            this.x += velocidade;
                            this.ultimaDirecao = Direction.RIGHT;
                        break;
                        case RIGHT:
                            this.x -= velocidade;
                            this.ultimaDirecao = Direction.LEFT;
                        break;
                        case UP:
                            this.y += velocidade;
                            this.ultimaDirecao = Direction.DOWN;
                        break;
                        case DOWN:
                            this.y -= velocidade;
                            this.ultimaDirecao = Direction.UP;
                        break;
                    }
                }

                if(curDirection != ultimaDirecao){
                    triggered = false;
                    isRolling = false;
                    changeSight();
                }
            }

            Iterator<Projetil> projIt = algodoes.iterator();
            while (projIt.hasNext()) {
                Projetil p = projIt.next();

                // Verifica colisão com o Duque
                if(p.colideCom(batata) && !batata.isInvulnerable()){
                    efeito.playSong("assets/Ouch.wav", false);
                    batata.takeDamage(1);
                }
                    
                // Remove se o Algodog morreu
                if (!this.isAlive) p.desativar();

                // Verifica se foi destruido
                if(!p.isAtivo()){
                    objetosColidiveis.remove(p);
                    projIt.remove();
                    if(this.isAlive){
                        efeito.playSong("assets/tiro.wav", false);
                        pofs.add(new Pof(p.x, p.y, tamanho + tamanho / 2));
                    }
                }
            }
        }

        /** 
         * Classe para a construção dos algodões gerados pelo Algodog
         * 
         * @author Arthur dos Santos Rezende
         * @version 1.0
         */
        public class AlgodaoFactory {
            /**
             * Cria um novo algodão
             * 
             * @param x Posição x
             * @param y Posição y
             * @return O algodão criado
             */
            public static Projetil createProjectile(int x, int y) {
                return new Projetil(x, y, TAMANHO_BLOCO, TAMANHO_BLOCO, 0, 0, Color.PINK, true);
            }
        }
    }

    /**
     * Classe para o Slimebot, um inimigo que cria um raio laser em uma determinada direção
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class SlimeBot extends Inimigo {
        /** Indica se o slimebot está atacando */
        private boolean isAttacking = false;
        /** Timer que gerencia a duração de seu laser */
        private long timer = 0;
        /** O alcance do laser do slimebot */
        private int alcanceDeLaser;
        /** O laser do slimebot */
        private Projetil laser;
        
        /**
         * Construtor da classe Slimebot
         * 
         * @param x Posição x
         * @param y Posição y
         * @param dir Direção
         * @param laserRange Alcançe do laser
         */
        public SlimeBot(int x, int y, Direction dir, int laserRange){
            super(x, y, TAMANHO_BLOCO, Color.DARK_GRAY, 1, 0, dir, false);
            this.alcanceDeLaser = laserRange;
            this.timer = System.currentTimeMillis();
            switch(dir){
                case LEFT: this.curImage = slimeBotImgs[1]; break;
                case RIGHT: this.curImage = slimeBotImgs[0]; break;
                case UP: this.curImage = slimeBotImgs[2]; break;
                case DOWN: this.curImage = slimeBotImgs[3]; break;
            }
        }

        /**
         * Atualiza o dano causado ao slimebot, e remove o laser caso seja derrotado
         * 
         * @param damage O dano causado
         * @return O retorno é sempre {@code true} nesse caso
         */
        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp <= 0){
                efeito.playSong("assets/tiro.wav", false);
                pofs.add(new Pof(x, y, tamanho + tamanho / 2));
                if(laser != null){
                    laser.desativar();
                    objetosColidiveis.remove(laser);
                }
                isAlive = false;
            }
            return true;
        }

        /**
         * Faz com que o slimebot crie um laser a cada 1,5 segundo. O laser em si dura 1s e também é gerenciado nesse método
         */
        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }

            if(!isAttacking){
                if(System.currentTimeMillis() - timer >= 1500){
                    switch(ultimaDirecao){
                        case LEFT:
                            laser = new Projetil(x - alcanceDeLaser, y + TAMANHO_BLOCO/4, alcanceDeLaser, TAMANHO_BLOCO/2,
                                0, 0, Color.RED, false);
                            laser.curImage = laserImgs[0];
                        break;
                        case RIGHT:
                            laser = new Projetil(x + tamanho, y + TAMANHO_BLOCO/4, alcanceDeLaser, TAMANHO_BLOCO/2,
                                0, 0, Color.RED, false);
                            laser.curImage = laserImgs[0];
                        break;
                        case UP:
                            laser = new Projetil(x + TAMANHO_BLOCO/4, y - alcanceDeLaser, TAMANHO_BLOCO/2, alcanceDeLaser,
                                0, 0, Color.RED, false);
                            laser.curImage = laserImgs[1];
                        break;
                        case DOWN:
                            laser = new Projetil(x + TAMANHO_BLOCO/4, y + tamanho, TAMANHO_BLOCO/2, alcanceDeLaser,
                                0, 0, Color.RED, false);
                            laser.curImage = laserImgs[1];
                        break;
                    }
                    objetosColidiveis.add(laser);
                    efeito.playSong("assets/LaserSwitch.wav", false);
                    timer = System.currentTimeMillis();
                    isAttacking = true;
                }
            } else {
                if(laser.isAtivo()){
                    if(laser.colideCom(batata) && !batata.isInvulnerable()){
                        efeito.playSong("assets/Ouch.wav", false);
                        batata.takeDamage(1);
                    }
                    if(System.currentTimeMillis() - timer >= 1000){
                        laser.desativar();
                        objetosColidiveis.remove(laser);
                        laser = null;
                        efeito.playSong("assets/LaserSwitch.wav", false);
                        timer = System.currentTimeMillis();
                        isAttacking = false;
                    }
                }
            }
        }
    }

    /**
     * Classe para o Gigabot, um "mini boss" que basicamente é um slimebot maior e mais resistente
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class GigaBot extends Inimigo {
        /** Indica se o gigabot está atacando */
        private boolean isAttacking = false;
        /** Timer que gerencia a duração de seu laser */
        private long timer = 0;
        /** O alcance do laser do gigabot */
        private int alcanceDeLaser;
        /** O laser do gigabot */
        private Projetil laser;
        
        /**
         * Construtor da classe Gigabot
         * 
         * @param x Posição x
         * @param y Posição y
         * @param dir Direção
         * @param laserRange Alcançe do laser
         */
        public GigaBot(int x, int y, Direction dir, int laserRange){
            super(x, y, TAMANHO_BLOCO*2, Color.DARK_GRAY, 3, 0, dir, false);
            this.alcanceDeLaser = laserRange;
            this.timer = System.currentTimeMillis();
            switch(dir){
                case LEFT: this.curImage = gigaBotImgs[1]; break;
                case RIGHT: this.curImage = gigaBotImgs[0]; break;
                case UP: this.curImage = gigaBotImgs[2]; break;
                case DOWN: this.curImage = gigaBotImgs[3]; break;
            }
        }

        /**
         * Atualiza o dano causado ao gigabot, e remove o laser caso seja derrotado
         * 
         * @param damage O dano causado
         * @return O retorno é sempre {@code true} nesse caso
         */
        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp <= 0){
                efeito.playSong("assets/tiro.wav", false);
                pofs.add(new Pof(x, y, tamanho + tamanho / 2));
                if(laser != null){
                    laser.desativar();
                    objetosColidiveis.remove(laser);
                }
                isAlive = false;
            } else efeito.playSong("assets/Glitch.wav", false);
            return true;
        }

        /**
         * Faz com que o gigabot crie um laser a cada 1,5 segundo. O laser em si dura 1s e também é gerenciado nesse método
         */
        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }

            if(!isAttacking){
                if(System.currentTimeMillis() - timer >= 1500){
                    switch(ultimaDirecao){
                        case LEFT:
                            laser = new Projetil(x - alcanceDeLaser, y + TAMANHO_BLOCO/2, alcanceDeLaser, TAMANHO_BLOCO,
                                0, 0, Color.ORANGE, false);
                            laser.curImage = gLaserImgs[0];
                        break;
                        case RIGHT:
                            laser = new Projetil(x + tamanho, y + TAMANHO_BLOCO/2, alcanceDeLaser, TAMANHO_BLOCO,
                                0, 0, Color.ORANGE, false);
                            laser.curImage = gLaserImgs[0];
                        break;
                        case UP:
                            laser = new Projetil(x + TAMANHO_BLOCO/2, y - alcanceDeLaser, TAMANHO_BLOCO, alcanceDeLaser,
                                0, 0, Color.ORANGE, false);
                            laser.curImage = gLaserImgs[1];
                        break;
                        case DOWN:
                            laser = new Projetil(x + TAMANHO_BLOCO/2, y + tamanho, TAMANHO_BLOCO, alcanceDeLaser,
                                0, 0, Color.ORANGE, false);
                            laser.curImage = gLaserImgs[1];
                        break;
                    }
                    objetosColidiveis.add(laser);
                    efeito.playSong("assets/LaserSwitch.wav", false);
                    timer = System.currentTimeMillis();
                    isAttacking = true;
                }
            } else {
                if(laser.isAtivo()){
                    if(laser.colideCom(batata) && !batata.isInvulnerable()){
                        efeito.playSong("assets/Ouch.wav", false);
                        batata.takeDamage(1);
                    }
                    if(System.currentTimeMillis() - timer >= 1000){
                        laser.desativar();
                        objetosColidiveis.remove(laser);
                        laser = null;
                        efeito.playSong("assets/LaserSwitch.wav", false);
                        timer = System.currentTimeMillis();
                        isAttacking = false;
                    }
                }
            }
        }
    }

    /**
     * Classe para o Malandranha, um inimigo que pode escalar paredes e persegue o jogador constantemente
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class Malandranha extends Inimigo{
        /**
         * Construtor da classe Malandranha
         * 
         * @param x Posição x
         * @param y Posição y
         */
        public Malandranha(int x, int y){
            super(x, y, TAMANHO_BLOCO, Color.BLACK, 1, 4, Direction.DOWN, true);
        }

        /**
         * Faz com que o Malandranha se mova na direção do jogador. Nesse caso o {@code ultimaDirecao} não é usado para a movimentação.
         */
        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }

            if(batata.getX() < this.x){
                this.x -= velocidade;
                this.curImage = malandranhaImgs[1];
            } else if(batata.getX() > this.x){
                this.x += velocidade;
                this.curImage = malandranhaImgs[0];
            }
            if(batata.getY() < this.y) this.y -= velocidade;
            else if(batata.getY() > this.y) this.y += velocidade;
        }
    }

    /**
     * Classe para o Alho, um "mini boss" que se divide em dois ao ser atingido
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    private class Alho extends Inimigo{
        /**
         * Representa os diferentes estágios do alho
         */
        private enum Estagio {
            /** Estágio inicial ({@code hp} = 3) */
            ALHO,
            /** Estágio alcançado após a divisão ({@code hp} = 2) */
            DENTALHO,
            /** Estágio após a casca ser perdida ({@code hp} = 1) */
            DESCASCALHO
        }
        /** O estágio atual do alho */
        private Estagio estagio;
        /** Variável auxiliar para o gerenciamento de imagens*/
        private int imgSet;
        
        /**
         * Construtor da classe Alho
         * 
         * @param x Posição x
         * @param y Posição y
         * @param dir Direção
         * @param hp Pontos de vida (será 2 especificamente na hora da divisão. O padrão é 3)
         */
        public Alho(int x, int y, Direction dir, int hp) {
            super(x, y, TAMANHO_BLOCO, Color.WHITE, hp, 0, dir, false);
            atualizarEstagio();
        }

        /**
         * Atualiza o dano causado ao alho, e cria um dentalho caso leve dano na fase de {@code ALHO}
         * 
         * @param damage O dano causado
         * @return O retorno é sempre {@code true} nesse caso
         */
        public boolean takeDamage(int damage){
            this.hp -= damage;
            if (estagio == Estagio.ALHO && hp < 3){
                Alho child;
                switch(ultimaDirecao){
                    case LEFT:
                        child = new Alho(x, y, Direction.RIGHT, 2);
                    break;
                    case RIGHT:
                        child = new Alho(x, y, Direction.LEFT, 2);
                    break;
                    case UP:
                        child = new Alho(x, y, Direction.DOWN, 2);
                    break;
                    default:
                        child = new Alho(x, y, Direction.UP, 2);
                    break;
                }
                inimigos.add(child);
                objetosColidiveis.add(child);
            }
            atualizarEstagio();

            efeito.playSong("assets/tiro.wav", false);
            pofs.add(new Pof(x, y, tamanho + tamanho / 2));

            if(hp <= 0) isAlive = false;
            return true;
        }

        /**
         * Atualiza o estágio do alho baseado no seu hp atual
         */
        private void atualizarEstagio() {
            if (hp == 3){
                estagio = Estagio.ALHO;
                curImage = alhoImgs[0];
            }
            else if (hp == 2) {
                estagio = Estagio.DENTALHO;
                velocidade = 2;
                imgSet = 1;
            } else {
                estagio = Estagio.DESCASCALHO;
                velocidade = 4;
                imgSet = 3;
            }
        }

        /**
         * Gerencia o comportamento do alho (nesse caso a movimentação).
         * 
         * <p>
         * Em seus estágios {@code DENTALHO} e {@code DESCASCALHO}, o alho se move de maneira mais errática que outros
         * inimigos, mudando de direção aleatoriamente após colidir com uma parede ou a borda da tela. Ele não se
         * move no seu estágio {@code ALHO}
         * </p>
         */
        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }

            if(estagio != Estagio.ALHO) switch(this.ultimaDirecao){
                case LEFT:
                    this.x -= velocidade;
                    this.curImage = alhoImgs[imgSet + 1];
                break;
                case RIGHT:
                    this.x += velocidade;
                    this.curImage = alhoImgs[imgSet];
                break;
                case UP:
                    this.y -= velocidade;
                    this.curImage = alhoImgs[imgSet + 1];
                break;
                case DOWN:
                    this.y += velocidade;
                    this.curImage = alhoImgs[imgSet];
                break;
            }

            if(foraDaTela()){
                switch(this.ultimaDirecao){
                    case LEFT:
                        this.x += velocidade;
                        ultimaDirecao = Direction.values()[new Random().nextInt(4)];
                    break;
                    case RIGHT:
                        this.x -= velocidade;
                        ultimaDirecao = Direction.values()[new Random().nextInt(4)];
                    break;
                    case UP:
                        this.y += velocidade;
                        ultimaDirecao = Direction.values()[new Random().nextInt(4)];
                    break;
                    case DOWN:
                        this.y -= velocidade;
                        ultimaDirecao = Direction.values()[new Random().nextInt(4)];
                    break;
                }
            }
        }
    }

    /**
     * Gerencia a colisão entre paredes e outros objetos colidíveis
     * 
     * @param entity A entidade colidindo com a parede
     */
    private void verificarColisaoParede(ObjetoColidivel entity) { 
        if(entity.layer == ObjetoColidivel.CollisionLayer.PLAYER){
            Player jogador = (Player) entity;
            for (Parede parede : paredes) {
                if (entity.colideCom(parede)) {
                    // Reverte o movimento
                    switch (jogador.getUltimaDirecao()) {
                        case LEFT: entity.x += batata.getVelocidade(); break;
                        case RIGHT: entity.x -= batata.getVelocidade(); break;
                        case UP: entity.y += batata.getVelocidade(); break;
                        case DOWN: entity.y -= batata.getVelocidade(); break;
                    }
                    break;
                }
            }
        }
        else if(
            entity.layer == ObjetoColidivel.CollisionLayer.ENEMY &&
            ((Inimigo)entity).velocidade != 0 &&
            !((Inimigo)entity).isFlying
        ){
            if(entity instanceof Alho){
                Alho alho = (Alho) entity;
                for (Parede parede : paredes) {
                    if (entity.colideCom(parede)) {
                        // Reverte o movimento
                        switch(alho.ultimaDirecao){
                            case LEFT:
                                alho.x += alho.velocidade;
                                alho.ultimaDirecao = Direction.values()[new Random().nextInt(4)];
                            break;
                            case RIGHT:
                                alho.x -= alho.velocidade;
                                alho.ultimaDirecao = Direction.values()[new Random().nextInt(4)];
                            break;
                            case UP:
                                alho.y += alho.velocidade;
                                alho.ultimaDirecao = Direction.values()[new Random().nextInt(4)];
                            break;
                            case DOWN:
                                alho.y -= alho.velocidade;
                                alho.ultimaDirecao = Direction.values()[new Random().nextInt(4)];
                            break;
                        }
                        break;
                    }
                }   
            } else {
                Inimigo inimigo = (Inimigo) entity;
                for (Parede parede : paredes) {
                    if (entity.colideCom(parede)) {
                        // Reverte o movimento
                        switch (inimigo.ultimaDirecao) {
                            case LEFT: inimigo.x += inimigo.velocidade; inimigo.ultimaDirecao = Direction.RIGHT; break;
                            case RIGHT: inimigo.x -= inimigo.velocidade; inimigo.ultimaDirecao = Direction.LEFT; break;
                            case UP: inimigo.y += inimigo.velocidade; inimigo.ultimaDirecao = Direction.DOWN; break;
                            case DOWN: inimigo.y -= inimigo.velocidade; inimigo.ultimaDirecao = Direction.UP; break;
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Classe para o leitor te teclas responsável por controlar o jogador
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    public class GameKeyAdapter extends KeyAdapter {
        /** Set guardando as teclas de movimento (teclas direcionais) ativas */
        private final Set<Integer> activeMovementKeys = new HashSet<>();
        /** Set guardando as teclas de tiro (teclas A,W,S e D) ativas */
        private final Set<Integer> activeShootingKeys = new HashSet<>();
        /** Última direção de movimento do jogador */
        private Direction ultimaDirecaoMovimento = Direction.RIGHT;
        /** Última direção de tiro do jogador */
        private Direction ultimaDirecaoTiro = null;
        /** Guarda a última vez que o jogador atirou */
        private long ultimoTiro = 0;
        /** Timer para processar ações do jogador de forma contínua (~60 FPS)*/
        private Timer continuousActionTimer;

        /**
         * Construtor da classe GameKeyAdapter
         */
        public GameKeyAdapter() {
            continuousActionTimer = new Timer(16, e -> {
                processMovement();
                processShooting();
                updateAnimation();
            });
            continuousActionTimer.start();
        }

        /**
         * Chamado quando alguma tecla é pressionada.
         * 
         * @param e O evento a ser processado
         */
        @Override
        public void keyPressed(KeyEvent e) {
            int code = e.getKeyCode();
            
            // Movimento
            if (isMovementKey(code)) {
                activeMovementKeys.add(code);
            }
            
            // Tiro
            if (isShootingKey(code)) {
                activeShootingKeys.add(code);
            }
        }

        /**
         * Chamado quando alguma tecla é solta.
         * 
         * @param e O evento a ser processado
         */
        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();
            
            // Movimento
            if (isMovementKey(code)) {
                activeMovementKeys.remove(code);
            }
            
            // Tiro
            if (isShootingKey(code)) {
                activeShootingKeys.remove(code);
            }
        }

        /**
         * Verifica se a tecla apertada é uma tecla direcional
         * 
         * @param code o código da tela apertada
         * @return {@code true} caso seja uma tecla direcional. Caso contrário, {@code false}
         */
        private boolean isMovementKey(int code) {
            return code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT || 
                code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN;
        }

        /**
         * Verifica se a tecla apertada é A,W,S ou D
         * 
         * @param code o código da tela apertada
         * @return {@code true} caso seja A,W,S ou D. Caso contrário, {@code false}
         */
        private boolean isShootingKey(int code) {
            return code == KeyEvent.VK_A || code == KeyEvent.VK_D || 
                code == KeyEvent.VK_W || code == KeyEvent.VK_S;
        }

        /**
         * Processa o movimento do jogador
         */
        private void processMovement() {
            if (activeMovementKeys.isEmpty()) {
                return;
            }
            
            int novoX = batata.getX();
            int novoY = batata.getY();
            boolean isMoving = false;
            
            // Prioridade para o movimento horizontal. Evita movimentos diagonais
            if (activeMovementKeys.contains(KeyEvent.VK_LEFT)) {
                if(batata.getX() > 0) novoX -= batata.getVelocidade();
                ultimaDirecaoMovimento = Direction.LEFT;
                isMoving = true;
            }
            else if (activeMovementKeys.contains(KeyEvent.VK_RIGHT)) {
                if(batata.getX() < LARGURA_TELA - TAMANHO_BLOCO) novoX += batata.getVelocidade();
                ultimaDirecaoMovimento = Direction.RIGHT;
                isMoving = true;
            }
            else if (activeMovementKeys.contains(KeyEvent.VK_UP)) {
                if(batata.getY() > 0) novoY -= batata.getVelocidade();
                ultimaDirecaoMovimento = Direction.UP;
                isMoving = true;
            }
            else if (activeMovementKeys.contains(KeyEvent.VK_DOWN)) {
                if(batata.getY() < ALTURA_TELA - TAMANHO_BLOCO) novoY += batata.getVelocidade();
                ultimaDirecaoMovimento = Direction.DOWN;
                isMoving = true;
            }
            
            if (isMoving) {
                batata.x = novoX;
                batata.y = novoY;
                batata.setUltimaDirecao(ultimaDirecaoMovimento);
                verificarColisaoParede(batata);
            }
            
            if (porta.isAberta() && batata.colideCom(porta)) {
                carregarProximaDungeon();
            }
        }

        /**
         * Processa os tiros do jogador
         */
        private void processShooting() {
            if (activeShootingKeys.isEmpty()) return;
            
            long now = System.currentTimeMillis();
            if (now - ultimoTiro <= INTERVALO_TIRO) return;
            
            Direction direction = null;
            int offsetX = 0, offsetY = 0;
            
            // Prioridade fixa para evitar tiros em várias direções ao mesmo tempo
            if (activeShootingKeys.contains(KeyEvent.VK_A)) {
                direction = Direction.LEFT;
                offsetY = TAMANHO_BLOCO/2;
            }
            else if (activeShootingKeys.contains(KeyEvent.VK_D)) {
                direction = Direction.RIGHT;
                offsetX = TAMANHO_BLOCO;
                offsetY = TAMANHO_BLOCO/2;
            }
            else if (activeShootingKeys.contains(KeyEvent.VK_W)) {
                direction = Direction.UP;
                offsetX = TAMANHO_BLOCO/2;
            }
            else if (activeShootingKeys.contains(KeyEvent.VK_S)) {
                direction = Direction.DOWN;
                offsetX = TAMANHO_BLOCO/2;
                offsetY = TAMANHO_BLOCO;
            }
            
            if (direction != null) {
                ultimaDirecaoTiro = direction;
                Projetil p = CarrotFactory.createProjectile(batata.getX() + offsetX, batata.getY() + offsetY, direction);
                // Verifica a direção para definir a imagem
                switch(direction){
                    case LEFT: p.setImage(cenouraImgs[1]); break;
                    case RIGHT: p.setImage(cenouraImgs[0]); break;
                    case UP: p.setImage(cenouraImgs[2]); break;
                    case DOWN: p.setImage(cenouraImgs[3]); break;
                }
                cenouras.add(p);
                objetosColidiveis.add(p);
                ultimoTiro = now;
            }
        }

        /**
         * Atualiza a imagem atual do jogador baseado no que ele está fazendo no moemento
         */
        private void updateAnimation() {
            boolean isAtirando = (System.currentTimeMillis() - ultimoTiro) < 200 && ultimaDirecaoTiro != null;
            boolean isMoving = !activeMovementKeys.isEmpty();
            
            // Prioridade para animação de tiro se estiver atirando agora
            if (isAtirando) {
                switch(ultimaDirecaoTiro) {
                    case LEFT: batata.setImage(batataImgs[4]); break;
                    case RIGHT: batata.setImage(batataImgs[3]); break;
                    case UP: batata.setImage(batataImgs[5]); break;
                    case DOWN: batata.setImage(batataImgs[6]); break;
                }
            } 
            // Se não estiver atirando mas estiver se movendo, mostra animação de movimento
            else if (isMoving) {
                switch(ultimaDirecaoMovimento) {
                    case LEFT: batata.setImage(batataImgs[1]); break;
                    case RIGHT: batata.setImage(batataImgs[0]); break;
                    case UP: batata.setImage(batataImgs[2]); break;
                    case DOWN: batata.setImage(batataImgs[0]); break;
                }
            }
        }
        
        /** Para o timer de ação contínua*/
        public void dispose() {
            if (continuousActionTimer != null) {
                continuousActionTimer.stop();
            }
        }
    }

    /**
     * Classe de gerenciamento das dungeons
     * 
     * @author Arthur dos Santos Rezende
     * @version 1.0
     */
    public class DungeonManager {
        /** Lista dos layouts de todas as dungeons */
        private ArrayList<DungeonLayout> dungeons;
        /** A dungeon atual */
        private int currentDungeon;
        /** A quantidade de dungeons (25, excluindo as dungeons de boss) */
        private int dungeonAmount;
        
        /**
         * Construtor da classe DungeonManager
         */
        public DungeonManager() {
            dungeons = new ArrayList<>();
            dungeonAmount = 25;
            for(int i = 0; i < dungeonAmount; i++){
                dungeons.add(new DungeonLayout(i));
            }
            
            currentDungeon = (save - 1)*5;
        }
        
        /**
         * Retorna a dungeon atual
         * 
         * @return a dungeon atual
         */
        public DungeonLayout getCurrentDungeon() {
            return dungeons.get(currentDungeon);
        }
        
        /**
         * Carrega a próxima dungeon
         */
        public void nextDungeon() {
            if((currentDungeon + 1) % 5 == 0){
                loadBoss(save);
            }
            currentDungeon++;
        }

        /**
         * Classe para os layouts de dungeon
         * 
         * @author Arthur dos Santos Rezende
         * @version 1.0
         */
        public class DungeonLayout {
            /** O número da dungeon atual */
            int lay;

            /**
             * Construtor da classe DungeonLayout
             * 
             * @param lay O número da dungeon a ser carregada
             */
            public DungeonLayout(int lay){
                this.lay = lay;
            }
            
            /**
             * Adciona as paredes da dungeon
             */
            void getParedes(){
                paredes.add(new Parede(0, 0, LARGURA_TELA, TAMANHO_BLOCO));
                paredes.add(new Parede(0, ALTURA_TELA - TAMANHO_BLOCO, LARGURA_TELA, TAMANHO_BLOCO));
                paredes.add(new Parede(0, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*5));
                paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*5));
                paredes.add(new Parede(0, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*5));
                paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*5));
                if((lay + 2) % 5 == 0){ //As penúltimas salas de cada mundo terão as mesmas paredes
                    paredes.add(new Parede(50, 250, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                    paredes.add(new Parede(50, 450, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                    paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*3, 250, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                    paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*3, 450, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                    paredes.add(new Parede(150, 150, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                    paredes.add(new Parede(150, 400, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                    paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*4, 150, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                    paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*4, 400, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                    paredes.add(new Parede(200, 150, TAMANHO_BLOCO*7, TAMANHO_BLOCO));
                    paredes.add(new Parede(200, 400, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                    paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 150, TAMANHO_BLOCO*7, TAMANHO_BLOCO));
                    paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*6, 400, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                    paredes.add(new Parede(300, 300, TAMANHO_BLOCO, TAMANHO_BLOCO*5));
                    paredes.add(new Parede(350, 500, TAMANHO_BLOCO*5, TAMANHO_BLOCO));
                    paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*7, 300, TAMANHO_BLOCO, TAMANHO_BLOCO*5));
                    paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 500, TAMANHO_BLOCO*5, TAMANHO_BLOCO));
                    paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 150, TAMANHO_BLOCO, TAMANHO_BLOCO*5));
                    paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 150, TAMANHO_BLOCO, TAMANHO_BLOCO*5));
                }
                switch(lay){
                    case 0:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 50, TAMANHO_BLOCO*6, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 300, TAMANHO_BLOCO*6, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 600, TAMANHO_BLOCO*6, TAMANHO_BLOCO*2));
                    break;
                    case 1:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 150, TAMANHO_BLOCO*6, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 450, TAMANHO_BLOCO*6, TAMANHO_BLOCO*3));
                    break;
                    case 2:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 50, TAMANHO_BLOCO*6, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 300, TAMANHO_BLOCO*6, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 600, TAMANHO_BLOCO*6, TAMANHO_BLOCO*2));
                    break;
                    case 4:
                        paredes.add(new Parede(50, 50, LARGURA_TELA - TAMANHO_BLOCO*2, TAMANHO_BLOCO*5));
                        paredes.add(new Parede(50, 450, LARGURA_TELA - TAMANHO_BLOCO*2, TAMANHO_BLOCO*5));
                    break;
                    case 5:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 300, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 600, TAMANHO_BLOCO*3, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2, 50, TAMANHO_BLOCO*3, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 300, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 600, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                    break;
                    case 6:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 50, TAMANHO_BLOCO*6, TAMANHO_BLOCO*5));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 450, TAMANHO_BLOCO*6, TAMANHO_BLOCO*5));
                    break;
                    case 7:
                        paredes.add(new Parede(50, ALTURA_TELA - TAMANHO_BLOCO*6, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                        paredes.add(new Parede(150, 150, TAMANHO_BLOCO, TAMANHO_BLOCO*9));
                        paredes.add(new Parede(200, 150, TAMANHO_BLOCO*16, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*3, 150, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*4, 150, TAMANHO_BLOCO, TAMANHO_BLOCO*4));
                        paredes.add(new Parede(350, 300, TAMANHO_BLOCO*15, TAMANHO_BLOCO));
                        paredes.add(new Parede(300, 300, TAMANHO_BLOCO, TAMANHO_BLOCO*6));
                        paredes.add(new Parede(350, ALTURA_TELA - TAMANHO_BLOCO*4, TAMANHO_BLOCO*15, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*4, ALTURA_TELA - TAMANHO_BLOCO*6, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                    break;
                    case 9:
                        paredes.add(new Parede(50, 250, TAMANHO_BLOCO*10, TAMANHO_BLOCO));
                        paredes.add(new Parede(50, 450, TAMANHO_BLOCO*10, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 250, TAMANHO_BLOCO*10, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 450, TAMANHO_BLOCO*10, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 200, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 200, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                    break;
                    case 10:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*7));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 550, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*7));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 550, TAMANHO_BLOCO, TAMANHO_BLOCO*3)); 
                    break;
                    case 11:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 150, TAMANHO_BLOCO*6, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 450, TAMANHO_BLOCO*6, TAMANHO_BLOCO*3));
                    break;
                    case 12:
                        paredes.add(new Parede(50, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(150, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(250, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(350, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(450, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(550, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(650, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(750, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(850, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(950, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(1050, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(1150, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                    break;
                    case 14:
                        paredes.add(new Parede(50, 250, TAMANHO_BLOCO*10, TAMANHO_BLOCO));
                        paredes.add(new Parede(50, 450, TAMANHO_BLOCO*10, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 250, TAMANHO_BLOCO*10, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 450, TAMANHO_BLOCO*10, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 200, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 200, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(150, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(300, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(450, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(800, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(950, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(1100, 50, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(150, ALTURA_TELA - TAMANHO_BLOCO*2, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(300, ALTURA_TELA - TAMANHO_BLOCO*2, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(450, ALTURA_TELA - TAMANHO_BLOCO*2, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(800, ALTURA_TELA - TAMANHO_BLOCO*2, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(950, ALTURA_TELA - TAMANHO_BLOCO*2, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(1100, ALTURA_TELA - TAMANHO_BLOCO*2, TAMANHO_BLOCO, TAMANHO_BLOCO));
                    break;
                    case 15:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*10, 250, TAMANHO_BLOCO*20, TAMANHO_BLOCO*5));
                    break;
                    case 16:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 300, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 600, TAMANHO_BLOCO*3, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2, 50, TAMANHO_BLOCO*3, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 300, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 600, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                    break;
                    case 17:
                        paredes.add(new Parede(50, 100, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                        paredes.add(new Parede(50, ALTURA_TELA - TAMANHO_BLOCO*3, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*3, 250, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*3, ALTURA_TELA - TAMANHO_BLOCO*6, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*4, 250, TAMANHO_BLOCO*8, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*4, ALTURA_TELA - TAMANHO_BLOCO*6, TAMANHO_BLOCO*8, TAMANHO_BLOCO));
                    break;
                    case 19:
                        paredes.add(new Parede(50, 250, TAMANHO_BLOCO*4, TAMANHO_BLOCO));
                        paredes.add(new Parede(50, 450, TAMANHO_BLOCO*4, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*4, 250, TAMANHO_BLOCO*8, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*4, 450, TAMANHO_BLOCO*8, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*5, 250, TAMANHO_BLOCO*4, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*5, 450, TAMANHO_BLOCO*4, TAMANHO_BLOCO));
                        paredes.add(new Parede(250, 200, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(400, 200, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(250, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(400, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*4, 200, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*7, 200, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*4, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*7, 450, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                    break;
                    case 20:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 50, TAMANHO_BLOCO*6, TAMANHO_BLOCO*5));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 450, TAMANHO_BLOCO*6, TAMANHO_BLOCO*5));
                    break;
                    case 21:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*4, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO*3, 50, TAMANHO_BLOCO, TAMANHO_BLOCO*2));
                    break;
                    case 22:
                        paredes.add(new Parede(LARGURA_TELA/2 - TAMANHO_BLOCO*2, 550, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 550, TAMANHO_BLOCO, TAMANHO_BLOCO*3));
                        paredes.add(new Parede(50, 600, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*3, 600, TAMANHO_BLOCO*2, TAMANHO_BLOCO));
                    break;
                    case 24:
                        paredes.add(new Parede(50, 200, TAMANHO_BLOCO*11, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(50, 450, TAMANHO_BLOCO*11, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 200, TAMANHO_BLOCO*11, TAMANHO_BLOCO*2));
                        paredes.add(new Parede(LARGURA_TELA/2 + TAMANHO_BLOCO, 450, TAMANHO_BLOCO*11, TAMANHO_BLOCO*2));
                    break;
                }
                for (Parede paredes : paredes) objetosColidiveis.add(paredes);
            }

            /**
             * Adiciona os inimigos da dungeon
             */
            void getInimigos(){
                switch(lay){
                    case 0:
                        inimigos.add(new Slime(LARGURA_TELA/2 - TAMANHO_BLOCO/2, 200, Direction.UP));
                        inimigos.add(new Slime(LARGURA_TELA/2 - TAMANHO_BLOCO/2, 500, Direction.DOWN));
                    break;
                    case 1:
                        inimigos.add(new Flyme(LARGURA_TELA/2 - TAMANHO_BLOCO/2, 200, Direction.DOWN));
                    break;
                    case 2:
                        inimigos.add(new Slime(LARGURA_TELA/2 - TAMANHO_BLOCO/2, 200, Direction.UP));
                        inimigos.add(new Slime(LARGURA_TELA/2 - TAMANHO_BLOCO/2, 500, Direction.DOWN));
                        inimigos.add(new Flyme(LARGURA_TELA/2 - TAMANHO_BLOCO/2, 200, Direction.DOWN));
                    break;
                    case 3:
                        inimigos.add(new Slime(50, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Slime(LARGURA_TELA - TAMANHO_BLOCO*2, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Flyme(50, 50, Direction.RIGHT));
                        inimigos.add(new Flyme(LARGURA_TELA - TAMANHO_BLOCO*2, 50, Direction.LEFT));
                    break;
                    case 4:
                        inimigos.add(new Alho(LARGURA_TELA/2 - TAMANHO_BLOCO/2, ALTURA_TELA/2 - TAMANHO_BLOCO/2, Direction.LEFT, 3));
                    break;
                    case 5:
                        inimigos.add(new Prato(LARGURA_TELA/2 - TAMANHO_BLOCO, 50, Direction.DOWN));
                        inimigos.add(new Prato(LARGURA_TELA/2 + TAMANHO_BLOCO, 650, Direction.UP));
                    break;
                    case 6:
                        inimigos.add(new Armandibula(LARGURA_TELA/2 - TAMANHO_BLOCO/2, ALTURA_TELA/2 - TAMANHO_BLOCO/2));
                    break;
                    case 7:
                        inimigos.add(new Prato(75, 50, Direction.DOWN));
                        inimigos.add(new Prato(LARGURA_TELA - TAMANHO_BLOCO*2, 75, Direction.LEFT));
                        inimigos.add(new Prato(75, ALTURA_TELA - TAMANHO_BLOCO*5, Direction.DOWN));
                        inimigos.add(new Prato(LARGURA_TELA - TAMANHO_BLOCO*2, ALTURA_TELA - (TAMANHO_BLOCO*5)/2, Direction.LEFT));
                        inimigos.add(new Prato(LARGURA_TELA - (TAMANHO_BLOCO*5)/2, 200, Direction.DOWN));
                        inimigos.add(new Prato(200, 225, Direction.RIGHT));
                        inimigos.add(new Prato(TAMANHO_BLOCO*7, 375, Direction.RIGHT));
                        inimigos.add(new Prato(TAMANHO_BLOCO*7, 475, Direction.RIGHT));
                        inimigos.add(new Armandibula(LARGURA_TELA/2 - TAMANHO_BLOCO/2, 75));
                        inimigos.add(new Armandibula(LARGURA_TELA - TAMANHO_BLOCO*4, 375));
                        inimigos.add(new Armandibula(150, ALTURA_TELA - (TAMANHO_BLOCO*5)/2));
                    break;
                    case 8:
                        inimigos.add(new Slime(50, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Slime(LARGURA_TELA - TAMANHO_BLOCO*2, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Flyme(50, 50, Direction.RIGHT));
                        inimigos.add(new Flyme(LARGURA_TELA - TAMANHO_BLOCO*2, 50, Direction.LEFT));
                        inimigos.add(new Prato(75, 200, Direction.UP));
                        inimigos.add(new Armandibula(150, ALTURA_TELA/2 - TAMANHO_BLOCO));
                        inimigos.add(new Armandibula(LARGURA_TELA - TAMANHO_BLOCO*4, ALTURA_TELA/2 - TAMANHO_BLOCO));
                    break;
                    case 9:
                        inimigos.add(new Alho(LARGURA_TELA/2 - TAMANHO_BLOCO/2, ALTURA_TELA/2 - TAMANHO_BLOCO/2, Direction.UP, 3));
                        inimigos.add(new Alho(LARGURA_TELA - TAMANHO_BLOCO*2, 200, Direction.LEFT, 3));
                        inimigos.add(new Alho(LARGURA_TELA - TAMANHO_BLOCO*2, 500, Direction.LEFT, 3));
                    break;
                    case 10:
                        inimigos.add(new QueijoBoxer(LARGURA_TELA/2 - TAMANHO_BLOCO/2, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                    break;
                    case 11:
                        inimigos.add(new Slime(LARGURA_TELA/2 - TAMANHO_BLOCO/2, ALTURA_TELA/2 - TAMANHO_BLOCO/2, Direction.UP));
                        inimigos.add(new Morcerango(LARGURA_TELA/2 - TAMANHO_BLOCO/2, ALTURA_TELA/2 - TAMANHO_BLOCO*4, Direction.UP));
                        inimigos.add(new Morcerango(LARGURA_TELA/2 - TAMANHO_BLOCO*3, ALTURA_TELA/2 - TAMANHO_BLOCO*4, Direction.DOWN));
                        inimigos.add(new Morcerango(LARGURA_TELA/2 + TAMANHO_BLOCO*2, ALTURA_TELA/2 - TAMANHO_BLOCO*4, Direction.DOWN));
                    break;
                    case 12:
                        inimigos.add(new QueijoBoxer(100, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(200, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(300, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(400, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(500, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(600, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(700, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(800, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(900, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(1000, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(1100, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(1200, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new Morcerango(0, 250, Direction.DOWN));
                        inimigos.add(new Morcerango(LARGURA_TELA - TAMANHO_BLOCO, 250, Direction.DOWN));
                    break;
                    case 13:
                        inimigos.add(new Slime(50, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Slime(LARGURA_TELA - TAMANHO_BLOCO*2, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Flyme(50, 50, Direction.RIGHT));
                        inimigos.add(new Flyme(LARGURA_TELA - TAMANHO_BLOCO*2, 50, Direction.LEFT));
                        inimigos.add(new Prato(75, 200, Direction.UP));
                        inimigos.add(new Armandibula(150, ALTURA_TELA/2 - TAMANHO_BLOCO));
                        inimigos.add(new Armandibula(LARGURA_TELA - TAMANHO_BLOCO*4, ALTURA_TELA/2 - TAMANHO_BLOCO));
                        inimigos.add(new QueijoBoxer(350, 450, Direction.UP, TAMANHO_BLOCO*5));
                        inimigos.add(new QueijoBoxer(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 200, Direction.DOWN, TAMANHO_BLOCO*5));
                        inimigos.add(new Morcerango(150, ALTURA_TELA - TAMANHO_BLOCO*5, Direction.RIGHT));
                        inimigos.add(new Morcerango(LARGURA_TELA - TAMANHO_BLOCO*4, ALTURA_TELA - TAMANHO_BLOCO*5, Direction.LEFT));
                        inimigos.add(new Morcerango(300, 300, Direction.RIGHT));
                        inimigos.add(new Morcerango(LARGURA_TELA - TAMANHO_BLOCO*7, 300, Direction.LEFT));
                    break;
                    case 14:
                        inimigos.add(new Alho(LARGURA_TELA/2 - TAMANHO_BLOCO/2, ALTURA_TELA/2 - TAMANHO_BLOCO/2, Direction.UP, 3));
                        inimigos.add(new Alho(LARGURA_TELA - TAMANHO_BLOCO*2, 200, Direction.LEFT, 3));
                        inimigos.add(new Alho(LARGURA_TELA - TAMANHO_BLOCO*2, 500, Direction.LEFT, 3));
                        inimigos.add(new Alho(50, 200, Direction.RIGHT, 3));
                        inimigos.add(new Alho(50, 500, Direction.RIGHT, 3));
                    break;
                    case 15:
                        inimigos.add(new AlgodogDoce(50, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new AlgodogDoce(50, 650, Direction.UP, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new AlgodogDoce(LARGURA_TELA - TAMANHO_BLOCO*2, 200, Direction.LEFT, LARGURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new AlgodogDoce(LARGURA_TELA - TAMANHO_BLOCO*2, 500, Direction.LEFT, LARGURA_TELA - TAMANHO_BLOCO*3));
                    break;
                    case 16:
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO, 100));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 + TAMANHO_BLOCO, 600));
                        inimigos.add(new Prato(LARGURA_TELA/2 - TAMANHO_BLOCO, 50, Direction.DOWN));
                        inimigos.add(new Prato(LARGURA_TELA/2 + TAMANHO_BLOCO, 650, Direction.UP));
                    break;
                    case 17:
                        inimigos.add(new Prato(LARGURA_TELA - TAMANHO_BLOCO*2, 300, Direction.LEFT));
                        inimigos.add(new Prato(LARGURA_TELA - TAMANHO_BLOCO*2, 400, Direction.LEFT));
                        inimigos.add(new QueijoBoxer(50, 50, Direction.RIGHT, LARGURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(50, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.RIGHT, LARGURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new QueijoBoxer(LARGURA_TELA - TAMANHO_BLOCO*2, 350, Direction.LEFT, LARGURA_TELA - TAMANHO_BLOCO*2));
                        inimigos.add(new AlgodogDoce(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 300, Direction.LEFT, TAMANHO_BLOCO*15));
                        inimigos.add(new AlgodogDoce(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 350, Direction.LEFT, TAMANHO_BLOCO*15));
                        inimigos.add(new AlgodogDoce(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 400, Direction.LEFT, TAMANHO_BLOCO*15));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO*4, 300));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO*4, 350));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO*4, 400));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 + TAMANHO_BLOCO*3, 300));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 + TAMANHO_BLOCO*3, 350));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 + TAMANHO_BLOCO*3, 400));
                        inimigos.add(new Chocochato(LARGURA_TELA - TAMANHO_BLOCO*3, 300));
                        inimigos.add(new Chocochato(LARGURA_TELA - TAMANHO_BLOCO*3, 350));
                        inimigos.add(new Chocochato(LARGURA_TELA - TAMANHO_BLOCO*3, 400));
                        inimigos.add(new Chocochato(100, 50));
                        inimigos.add(new Chocochato(100, ALTURA_TELA - TAMANHO_BLOCO*2));
                    break;
                    case 18:
                        inimigos.add(new Slime(50, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Slime(LARGURA_TELA - TAMANHO_BLOCO*2, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Flyme(50, 50, Direction.RIGHT));
                        inimigos.add(new Flyme(LARGURA_TELA - TAMANHO_BLOCO*2, 50, Direction.LEFT));
                        inimigos.add(new Prato(75, 200, Direction.UP));
                        inimigos.add(new Armandibula(150, ALTURA_TELA/2 - TAMANHO_BLOCO));
                        inimigos.add(new Armandibula(LARGURA_TELA - TAMANHO_BLOCO*4, ALTURA_TELA/2 - TAMANHO_BLOCO));
                        inimigos.add(new QueijoBoxer(350, 450, Direction.UP, TAMANHO_BLOCO*5));
                        inimigos.add(new QueijoBoxer(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 200, Direction.DOWN, TAMANHO_BLOCO*5));
                        inimigos.add(new Morcerango(150, ALTURA_TELA - TAMANHO_BLOCO*5, Direction.RIGHT));
                        inimigos.add(new Morcerango(LARGURA_TELA - TAMANHO_BLOCO*4, ALTURA_TELA - TAMANHO_BLOCO*5, Direction.LEFT));
                        inimigos.add(new Morcerango(300, 300, Direction.RIGHT));
                        inimigos.add(new Morcerango(LARGURA_TELA - TAMANHO_BLOCO*7, 300, Direction.LEFT));
                        inimigos.add(new AlgodogDoce(250, 350, Direction.LEFT, TAMANHO_BLOCO*5));
                        inimigos.add(new AlgodogDoce(LARGURA_TELA - TAMANHO_BLOCO, 300, Direction.LEFT, TAMANHO_BLOCO*5));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO, 150));
                        inimigos.add(new Chocochato(LARGURA_TELA/2, 150));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO, 350));
                        inimigos.add(new Chocochato(LARGURA_TELA/2, 350));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO, 500));
                        inimigos.add(new Chocochato(LARGURA_TELA/2, 500));
                        inimigos.add(new Chocochato(50, 150));
                        inimigos.add(new Chocochato(100, 150));
                        inimigos.add(new Chocochato(LARGURA_TELA - TAMANHO_BLOCO*2, 150));
                        inimigos.add(new Chocochato(LARGURA_TELA - TAMANHO_BLOCO*3, 150));
                    break;
                    case 19:
                        inimigos.add(new Alho(LARGURA_TELA/4 - TAMANHO_BLOCO/2, ALTURA_TELA/2 - TAMANHO_BLOCO/2, Direction.UP, 3));
                        inimigos.add(new Alho((LARGURA_TELA*3)/4 - TAMANHO_BLOCO/2, ALTURA_TELA/2 - TAMANHO_BLOCO/2, Direction.UP, 3));
                        inimigos.add(new Alho(LARGURA_TELA - TAMANHO_BLOCO*2, 200, Direction.LEFT, 3));
                        inimigos.add(new Alho(LARGURA_TELA - TAMANHO_BLOCO*2, 500, Direction.LEFT, 3));
                        inimigos.add(new Alho(50, 200, Direction.RIGHT, 3));
                        inimigos.add(new Alho(50, 500, Direction.RIGHT, 3));
                    break;
                    case 20:
                        inimigos.add(new Malandranha(50, 50));
                        inimigos.add(new Malandranha(LARGURA_TELA - TAMANHO_BLOCO*2, 50));
                        inimigos.add(new Malandranha(LARGURA_TELA - TAMANHO_BLOCO*2, 650));
                    break;
                    case 21:
                        inimigos.add(new SlimeBot(LARGURA_TELA/2 - TAMANHO_BLOCO/2, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new SlimeBot(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new SlimeBot(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*3));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO, 100));
                        inimigos.add(new Chocochato(LARGURA_TELA/2, 100));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO*3, 100));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 100));
                    break;
                    case 22:
                        inimigos.add(new Chocochato(100, ALTURA_TELA - TAMANHO_BLOCO*2));
                        inimigos.add(new Chocochato(LARGURA_TELA - TAMANHO_BLOCO*3, ALTURA_TELA - TAMANHO_BLOCO*2));
                        inimigos.add(new SlimeBot(50, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.RIGHT, TAMANHO_BLOCO*9));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO, 550));
                        inimigos.add(new Chocochato(LARGURA_TELA/2, 550));
                        inimigos.add(new Malandranha(LARGURA_TELA/2  - TAMANHO_BLOCO, 650));
                        inimigos.add(new Malandranha(LARGURA_TELA/2, 650));
                        inimigos.add(new SlimeBot(LARGURA_TELA - TAMANHO_BLOCO*2, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.LEFT, TAMANHO_BLOCO*9));
                        inimigos.add(new GigaBot(LARGURA_TELA/2 - TAMANHO_BLOCO, ALTURA_TELA - TAMANHO_BLOCO*3, Direction.UP, ALTURA_TELA - TAMANHO_BLOCO*4));
                    break;
                    case 23:
                        inimigos.add(new Slime(50, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Slime(LARGURA_TELA - TAMANHO_BLOCO*2, ALTURA_TELA - TAMANHO_BLOCO*2, Direction.UP));
                        inimigos.add(new Flyme(50, 50, Direction.RIGHT));
                        inimigos.add(new Flyme(LARGURA_TELA - TAMANHO_BLOCO*2, 50, Direction.LEFT));
                        inimigos.add(new Prato(75, 200, Direction.UP));
                        inimigos.add(new Armandibula(150, ALTURA_TELA/2 - TAMANHO_BLOCO));
                        inimigos.add(new Armandibula(LARGURA_TELA - TAMANHO_BLOCO*4, ALTURA_TELA/2 - TAMANHO_BLOCO));
                        inimigos.add(new QueijoBoxer(350, 450, Direction.UP, TAMANHO_BLOCO*5));
                        inimigos.add(new QueijoBoxer(LARGURA_TELA/2 + TAMANHO_BLOCO*2, 200, Direction.DOWN, TAMANHO_BLOCO*5));
                        inimigos.add(new Morcerango(150, ALTURA_TELA - TAMANHO_BLOCO*5, Direction.RIGHT));
                        inimigos.add(new Morcerango(LARGURA_TELA - TAMANHO_BLOCO*4, ALTURA_TELA - TAMANHO_BLOCO*5, Direction.LEFT));
                        inimigos.add(new Morcerango(300, 300, Direction.RIGHT));
                        inimigos.add(new Morcerango(LARGURA_TELA - TAMANHO_BLOCO*7, 300, Direction.LEFT));
                        inimigos.add(new AlgodogDoce(250, 350, Direction.LEFT, TAMANHO_BLOCO*5));
                        inimigos.add(new AlgodogDoce(LARGURA_TELA - TAMANHO_BLOCO, 300, Direction.LEFT, TAMANHO_BLOCO*5));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO, 150));
                        inimigos.add(new Chocochato(LARGURA_TELA/2, 150));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO, 350));
                        inimigos.add(new Chocochato(LARGURA_TELA/2, 350));
                        inimigos.add(new Chocochato(LARGURA_TELA/2 - TAMANHO_BLOCO, 500));
                        inimigos.add(new Chocochato(LARGURA_TELA/2, 500));
                        inimigos.add(new Chocochato(50, 150));
                        inimigos.add(new Chocochato(100, 150));
                        inimigos.add(new Chocochato(LARGURA_TELA - TAMANHO_BLOCO*2, 150));
                        inimigos.add(new Chocochato(LARGURA_TELA - TAMANHO_BLOCO*3, 150));
                        inimigos.add(new Malandranha(250, 450));
                        inimigos.add(new Malandranha(LARGURA_TELA - TAMANHO_BLOCO*6, 450));
                        inimigos.add(new SlimeBot(LARGURA_TELA - (TAMANHO_BLOCO*5)/2, 200, Direction.UP, TAMANHO_BLOCO*3));
                        inimigos.add(new GigaBot(LARGURA_TELA/2 - TAMANHO_BLOCO, 50, Direction.DOWN, ALTURA_TELA - TAMANHO_BLOCO*4));
                    break;
                    case 24:
                        inimigos.add(new Alho(LARGURA_TELA/2 - TAMANHO_BLOCO/2, ALTURA_TELA/2 - TAMANHO_BLOCO/2, Direction.UP, 3));
                        inimigos.add(new GigaBot(50, 100, Direction.RIGHT, LARGURA_TELA - TAMANHO_BLOCO*4));
                        inimigos.add(new GigaBot(LARGURA_TELA - TAMANHO_BLOCO*3, ALTURA_TELA - TAMANHO_BLOCO*4, Direction.LEFT, LARGURA_TELA - TAMANHO_BLOCO*4));
                    break;
                }
                for (Inimigo inimigo : inimigos) objetosColidiveis.add(inimigo);
            }
        }
    }

    /**
     * Carrega a próxima dungeon
     */
    private void carregarProximaDungeon() {
        dungeonManager.nextDungeon();
        if(dungeonManager.currentDungeon < dungeonManager.dungeonAmount){
            softClean();
            Iterator<ObjetoColidivel> it = objetosColidiveis.iterator();
            while (it.hasNext()) {
                ObjetoColidivel obj = it.next();
                if (obj.getLayer() != ObjetoColidivel.CollisionLayer.PLAYER && obj.getLayer() != ObjetoColidivel.CollisionLayer.DOOR) {
                    it.remove();
                }
            }

            batata.setX(0);
            batata.setY(ALTURA_TELA/2);

                    
            layout = dungeonManager.getCurrentDungeon();
            layout.getInimigos();
            layout.getParedes();
            porta.setImage(portaImgs[0]);
            porta.fechar();
        }
    }

    /**
     * Carrega a tela de batalha contra um boss.
     * 
     * @param bossNum Número identificador do boss
     */
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

    /**
     * Realiza limpeza de recursos antes da tela ser descartada.
     */
    public void cleanUp() {
        // Parar sistemas
        estado = EstadoJogo.PARADO;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        if (gameKeyAdapter != null) {
            gameKeyAdapter.dispose();
            gameKeyAdapter = null;
        }
        
        // Remover listeners e componentes relacionados
        cleanKeyListeners();
        resetKeyState();
        for (ActionListener al : pauseButton.getActionListeners()) pauseButton.removeActionListener(al);
        remove(pauseButton);
        
        // Liberar listas
        softClean();
        objetosColidiveis.clear();

        // Liberar imagens
        cleanImgArray(backgroundImgs); algodaoImg = null; alertImage = null; pofImage = null; paredeImg = null; iconPause = null;
        cleanImgArray(batataImgs); cleanImgArray(cenouraImgs); cleanImgArray(slimeImgs); cleanImgArray(flymeImgs);
        cleanImgArray(pratoImgs); cleanImgArray(facaImgs); cleanImgArray(armandibulaImgs); cleanImgArray(morcerangoImgs);
        cleanImgArray(queijoBoxerImgs); cleanImgArray(bracoImgs); cleanImgArray(luvaImgs); cleanImgArray(chocochatoImgs);
        cleanImgArray(algodogDoceImgs); cleanImgArray(slimeBotImgs); cleanImgArray(laserImgs); cleanImgArray(gigaBotImgs);
        cleanImgArray(gLaserImgs); cleanImgArray(malandranhaImgs); cleanImgArray(alhoImgs); cleanImgArray(portaImgs);

        // Otimização final: Garbage Colector
        System.gc();
    }

    /**
     * Limpa um array de imagem
     * 
     * @param imgs o array a ser limpo
     */
    private void cleanImgArray(Image[] imgs){
        if (imgs != null) {
            for (int i = 0; i < imgs.length; i++) imgs[i] = null;
            imgs = null;
        }
    }

    /**
     * Limpa o leitor de teclas
     */
    private void cleanKeyListeners() {
        for (KeyListener kl : getKeyListeners()) {
            removeKeyListener(kl);
        }
    }

    /**
     * Garante que todas as teclas são liberadas
     */
    private void resetKeyState() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
    }

    /**
     * Método de limpeza parcial, usado individualmente para a transição entre dungeons
     */
    private void softClean() {
        inimigos.clear();
        paredes.clear();
        cenouras.clear();
        alertas.clear();
        pofs.clear();
    }
}