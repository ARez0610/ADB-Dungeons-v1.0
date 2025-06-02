import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class TelaBoss extends TelaBase{
    private BossManager bossManager;
    private BossManager.BossLayout layout;
    private int bossNum;
    private ArrayList<ObjetoColidivel> objetosColidiveis = new ArrayList<>();
    private JButton pauseButton = new JButton("Pausa");
    private Player batata;
    private GameKeyAdapter gameKeyAdapter;
    private ArrayList<Projetil> cenouras = new ArrayList<>();
    private static final long INTERVALO_TIRO = 300;
    private Boss curBoss;
    private ArrayList<Parede> paredes = new ArrayList<>();
    private Porta porta;
    private ArrayList<Alert> alertas = new ArrayList<>();
    private ArrayList<Pof> pofs = new ArrayList<>();
    //Imagens
    private ImageIcon iconPause; private Image[] backgroundImgs; private Image alertImage; private Image pofImage;
    private Image paredeImg; private Image[] portaImgs; private Image[] batataImgs; private Image[] cenouraImgs;
    private Image[] gigaSlimeImgs; private Image lancaChamasImg; private Image[] sirPlatohImgs; private Image garfoImg;
    private Image[] facaImgs; private Image[] mofadaBombadaImgs; private Image bracoImg; private Image luvaImg;
    private Image mofoImg; private Image[] cerberoNimbusImgs; private Image algodaoImg; private ImageIcon vladmirCenoura;
    private Image[] larryImgs; private Image teiaImg; private Image[] fioImgs; private Image naveMareanhaImg;
    private Image[] cappuccinoImgs;
    
    public TelaBoss(MusicPlayer musica, int bossNum){
        super(musica);
        this.bossNum = bossNum;
        this.bossManager = new BossManager();

        if(bossNum == 6) musica.playSong("assets/no-mercy.wav", true);
        else musica.playSong("assets/BossTheme_ADB.wav", true);

        setLayout(null);

        pauseButton();
        add(pauseButton);
        start();
    }

    public void start(){
        cleanKeyListeners();
        resetKeyState();
        objetosColidiveis.clear();
        layout = bossManager.getCurrentBoss();

        batata = new Player(LARGURA_TELA/2, ALTURA_TELA - TAMANHO_BLOCO);
        batata.setImage(batataImgs[0]);
        objetosColidiveis.add(batata);

        gameKeyAdapter = new GameKeyAdapter();
        addKeyListener(gameKeyAdapter);

        layout.getBoss();
        layout.getParedes();

        porta = new Porta(LARGURA_TELA/2 - 50, 0, 100, 20);
        porta.setImage(portaImgs[0]);
        objetosColidiveis.add(porta);

        estado = EstadoJogo.RODANDO;
        timer = new Timer(INTERVALO, this);
        timer.start();
    }

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

    public void carregarImagens(){
        backgroundImgs = new Image[7];
        batataImgs = new Image[10]; cenouraImgs = new Image[4]; portaImgs = new Image[2]; gigaSlimeImgs = new Image[3];
        sirPlatohImgs = new Image[3]; facaImgs = new Image[8]; mofadaBombadaImgs = new Image[5]; cerberoNimbusImgs = new Image[5];
        larryImgs = new Image[2]; fioImgs = new Image[2]; cappuccinoImgs = new Image[4];

        try {
            paredeImg = ImageIO.read(new File("assets/parede.png"));
            alertImage = ImageIO.read(new File("assets/Warning.png"));
            pofImage = ImageIO.read(new File("assets/POF.png"));

            backgroundImgs[0] = ImageIO.read(new File("assets/secretBackground.png"));
            backgroundImgs[1] = ImageIO.read(new File("assets/BackgroundW1.png"));
            backgroundImgs[2] = ImageIO.read(new File("assets/background.png"));
            backgroundImgs[3] = ImageIO.read(new File("assets/BackgroundW3.png"));
            backgroundImgs[4] = ImageIO.read(new File("assets/BackgroundW4.png"));
            backgroundImgs[5] = ImageIO.read(new File("assets/BackgroundW5.png"));
            backgroundImgs[6] = ImageIO.read(new File("assets/SpaceBackground.png"));

            portaImgs[0] = ImageIO.read(new File("assets/PortaFH.png"));
            portaImgs[1] = ImageIO.read(new File("assets/PortaAH.png"));

            batataImgs[0] = ImageIO.read(new File("assets/DuqueBatataD.png"));
            batataImgs[1] = ImageIO.read(new File("assets/DuqueBatataE.png"));
            batataImgs[2] = ImageIO.read(new File("assets/DuqueBatataC.png"));
            batataImgs[3] = ImageIO.read(new File("assets/DuqueBatataAD.png"));
            batataImgs[4] = ImageIO.read(new File("assets/DuqueBatataAE.png"));
            batataImgs[5] = ImageIO.read(new File("assets/DuqueBatataAC.png"));
            batataImgs[6] = ImageIO.read(new File("assets/DuqueBatataAB.png"));
            batataImgs[7] = ImageIO.read(new File("assets/DuqueBatataX.png"));
            batataImgs[8] = ImageIO.read(new File("assets/XSalada.png"));
            batataImgs[9] = ImageIO.read(new File("assets/XSaladaX.png"));

            cenouraImgs[0] = ImageIO.read(new File("assets/CenouraD.png"));
            cenouraImgs[1] = ImageIO.read(new File("assets/CenouraE.png"));
            cenouraImgs[2] = ImageIO.read(new File("assets/CenouraC.png"));
            cenouraImgs[3] = ImageIO.read(new File("assets/CenouraB.png"));

            gigaSlimeImgs[0] = ImageIO.read(new File("assets/GigaSlimeD.png"));
            gigaSlimeImgs[1] = ImageIO.read(new File("assets/GigaSlimeP.png"));
            gigaSlimeImgs[2] = ImageIO.read(new File("assets/GigaSlimeA.png"));

            lancaChamasImg = ImageIO.read(new File("assets/Fogo.png"));

            sirPlatohImgs[0] = ImageIO.read(new File("assets/SirPlatohD.png"));
            sirPlatohImgs[1] = ImageIO.read(new File("assets/SirPlatohG.png"));
            sirPlatohImgs[2] = ImageIO.read(new File("assets/SirPlatohF.png"));

            garfoImg = ImageIO.read(new File("assets/Garfo.png"));
            facaImgs[0] = ImageIO.read(new File("assets/BigFacaDC.png"));
            facaImgs[1] = ImageIO.read(new File("assets/BigFacaD.png"));
            facaImgs[2] = ImageIO.read(new File("assets/BigFacaDB.png"));
            facaImgs[3] = ImageIO.read(new File("assets/BigFacaB.png"));
            facaImgs[4] = ImageIO.read(new File("assets/BigFacaEB.png"));
            facaImgs[5] = ImageIO.read(new File("assets/BigFacaE.png"));
            facaImgs[6] = ImageIO.read(new File("assets/BigFacaEC.png"));
            facaImgs[7] = ImageIO.read(new File("assets/BigFacaC.png"));

            mofadaBombadaImgs[0] = ImageIO.read(new File("assets/MofadaBombadaD.png"));
            mofadaBombadaImgs[1] = ImageIO.read(new File("assets/MofadaBombadaGD.png"));
            mofadaBombadaImgs[2] = ImageIO.read(new File("assets/MofadaBombadaAD.png"));
            mofadaBombadaImgs[3] = ImageIO.read(new File("assets/MofadaBombadaGE.png"));
            mofadaBombadaImgs[4] = ImageIO.read(new File("assets/MofadaBombadaAE.png"));

            bracoImg = ImageIO.read(new File("assets/BracoV.png"));
            luvaImg = ImageIO.read(new File("assets/LuvaB.png"));
            mofoImg = ImageIO.read(new File("assets/mofo.png"));

            cerberoNimbusImgs[0] = ImageIO.read(new File("assets/CerberoNimbusD.png"));
            cerberoNimbusImgs[1] = ImageIO.read(new File("assets/CerberoNimbusB1.png"));
            cerberoNimbusImgs[2] = ImageIO.read(new File("assets/CerberoNimbusB2.png"));
            cerberoNimbusImgs[3] = ImageIO.read(new File("assets/CerberoNimbusRB.png"));
            cerberoNimbusImgs[4] = ImageIO.read(new File("assets/CerberoNimbusRC.png"));

            algodaoImg = ImageIO.read(new File("assets/Algodao.png"));

            larryImgs[0] = ImageIO.read(new File("assets/LarryA.png"));
            larryImgs[1] = ImageIO.read(new File("assets/LarryD.png"));

            teiaImg = ImageIO.read(new File("assets/teia.png"));
            fioImgs[0] = ImageIO.read(new File("assets/fio.png"));
            fioImgs[1] = ImageIO.read(new File("assets/fioH.png"));

            naveMareanhaImg = ImageIO.read(new File("assets/NaveMaeranha.png"));

            cappuccinoImgs[0] = ImageIO.read(new File("assets/CappuccinoAssassinoD.png"));
            cappuccinoImgs[1] = ImageIO.read(new File("assets/CappuccinoAssassinoA.png"));
            cappuccinoImgs[2] = ImageIO.read(new File("assets/CappuccinoAssassinoX.png"));
            cappuccinoImgs[3] = ImageIO.read(new File("assets/CappuccinoAssassinoC.png"));
        } catch (IOException e) {
            System.out.println("Erro ao carregar imagens");
            e.printStackTrace();
        }
    }

    @Override
    public void desenharTela(Graphics g) {
        if(estado != EstadoJogo.PARADO){
            // Desenhar fundo (se a imagem existir)
            if(backgroundImgs[bossNum] != null) {
                g.drawImage(backgroundImgs[bossNum], 0, 0, LARGURA_TELA, ALTURA_TELA, this);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, LARGURA_TELA, ALTURA_TELA);
            }

            //Desenhar paredes
            if(!(bossNum == 6 && (curBoss == null || curBoss.getImage() != null || curBoss.hp <= 0))){
                for (Parede parede : paredes) {
                    if(paredeImg != null){
                        g.drawImage(paredeImg, parede.getX(), parede.getY(), parede.getLargura(), parede.getAltura(), this);
                    } else {
                        g.setColor(parede.getCor());
                        g.fillRect(parede.getX(), parede.getY(), parede.getLargura(), parede.getAltura());
                    }
                }
            }

            //Desenhar porta
            if(bossNum != 6){
                if(porta.getImage() != null){
                    g.drawImage(porta.getImage(), porta.getX(), porta.getY(), porta.getLargura(), porta.getAltura(), this);
                } else {
                    g.setColor(porta.getCor());
                    g.fillRect(porta.getX(), porta.getY(), porta.getLargura(), porta.getAltura());
                }
            }
            
            // Desenhar bosses e seus projéteis
            if(curBoss != null){
                // Alguns projéteis estão fora de ordem para o Boss não aparecer debaixo deles
                if(curBoss instanceof CerberoNimbus){
                    for (Projetil p : ((CerberoNimbus) curBoss).algodoes) {
                        if (p.isAtivo()) {
                            if(p.getImage() != null) {
                                g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                            } else {
                                g.setColor(p.getCor());
                                g.fillOval(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                            }
                        }
                    }
                }
                else if(curBoss instanceof NaveMaeranha){
                    for (Projetil p : ((NaveMaeranha) curBoss).fios) {
                        if (p.isAtivo()) {
                            if(p.getImage() != null) {
                                g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(),
                                    p.getAltura(), this);
                            } else {
                                g.setColor(p.getCor());
                                g.fillOval(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                            }
                        }
                    }
                    for (Projetil p : ((NaveMaeranha) curBoss).teias) {
                        if (p.isAtivo()) {
                            if(p.getImage() != null) {
                                g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                            } else {
                                g.setColor(p.getCor());
                                g.fillOval(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                            }
                        }
                    }
                }
                if(curBoss.getImage() != null){
                    if(curBoss instanceof NaveMaeranha) g.drawImage(curBoss.getImage(), 0, 0, LARGURA_TELA, ALTURA_TELA, this);
                    else g.drawImage(curBoss.getImage(), curBoss.getX(), curBoss.getY(), curBoss.getLargura(), curBoss.getAltura(), this);
                } else {
                    g.setColor(curBoss.getCor());
                    g.fillRect(curBoss.getX(), curBoss.getY(), curBoss.getLargura(), curBoss.getAltura());
                }
                if(curBoss instanceof Cappuccino){
                    for(Projetil p : ((Cappuccino) curBoss).clones){
                        if(p != null && p.isAtivo()){
                            if(p.getImage() != null){
                                g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                            } else {
                                g.setColor(p.cor);
                                g.fillRect(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                            }
                        }
                    }
                } else if(curBoss instanceof GigaSlime){
                    if(((GigaSlime) curBoss).lancaChamas != null){
                        Projetil l = ((GigaSlime) curBoss).lancaChamas;
                        if(l.isAtivo()){
                            if(l.getImage() != null){
                                g.drawImage(l.getImage(), l.getX(), l.getY() - 100, l.getLargura(), l.getAltura() + 110, this);
                            } else {
                                g.setColor(l.cor);
                                g.fillRect(l.getX(), l.getY(), l.getLargura(), l.getAltura());
                            }
                        }
                    }
                } else if(curBoss instanceof SirPlatoh){
                    if(((SirPlatoh) curBoss).garfo != null){
                        Projetil p = ((SirPlatoh) curBoss).garfo;
                        if(p.isAtivo()){
                            if(p.getImage() != null){
                                g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                            } else {
                                g.setColor(p.cor);
                                g.fillRect(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                            }
                        }
                    }
                    if(((SirPlatoh) curBoss).faca != null){
                        Projetil p = ((SirPlatoh) curBoss).faca;
                        if(p.isAtivo()){
                            if(p.getImage() != null){
                                g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                            } else {
                                g.setColor(p.cor);
                                g.fillRect(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                            }
                        }
                    }
                } else if(curBoss instanceof MofadaBombada){
                    for (Projetil p : ((MofadaBombada) curBoss).bolasDeMofo) {
                        if (p.isAtivo()) {
                            if(p.getImage() != null) {
                                g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                            } else {
                                g.setColor(p.getCor());
                                g.fillOval(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                            }
                        }
                    }
                    if(((MofadaBombada) curBoss).braco != null){
                        Projetil p = ((MofadaBombada) curBoss).braco;
                        if(p.isAtivo()){
                            if(p.getImage() != null  && luvaImg != null){
                                g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                                // Luva é desenhada separadamente
                                g.drawImage(luvaImg, p.getX(), p.getY() + p.getAltura() - TAMANHO_BLOCO + 10, TAMANHO_BLOCO, TAMANHO_BLOCO, this);
                            } else {
                                g.setColor(p.cor);
                                g.fillRect(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                                g.setColor(Color.RED);
                                // Luva é desenhada separadamente
                                g.fillRect(p.getX(), p.getY() + p.getAltura() - TAMANHO_BLOCO, TAMANHO_BLOCO, TAMANHO_BLOCO);
                            }
                        }
                    }
                } else if(curBoss instanceof Larry){
                    for (Projetil p : ((Larry) curBoss).fios) {
                        if (p.isAtivo()) {
                            if(p.getImage() != null) {
                                g.drawImage(p.getImage(), p.getX(), p.getY() - TAMANHO_BLOCO, p.getLargura(),
                                    p.getAltura() + TAMANHO_BLOCO, this);
                            } else {
                                g.setColor(p.getCor());
                                g.fillOval(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                            }
                        }
                    }
                    for (Projetil p : ((Larry) curBoss).teias) {
                        if (p.isAtivo()) {
                            if(p.getImage() != null) {
                                g.drawImage(p.getImage(), p.getX(), p.getY(), p.getLargura(), p.getAltura(), this);
                            } else {
                                g.setColor(p.getCor());
                                g.fillOval(p.getX(), p.getY(), p.getLargura(), p.getAltura());
                            }
                        }
                    }
                }
            }

            // Desenhar personagem
            if(bossNum != 6){
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
            } else {
                if(batata.isInvulnerable()){
                    if(batataImgs[9] != null){
                        if ((System.currentTimeMillis() / 100) % 2 == 0) {
                            g.drawImage(batataImgs[9], batata.getX(), batata.getY(), batata.getLargura(), batata.getAltura(), this);
                        }
                    } else {
                        g.setColor(batata.getCor());
                        g.fillOval(batata.getX(), batata.getY(), TAMANHO_BLOCO, TAMANHO_BLOCO);
                    }
                } else {
                    if(batataImgs[8] != null) {
                        g.drawImage(batataImgs[8], batata.getX(), batata.getY(), batata.getLargura(), batata.getAltura(), this);
                    } else {
                        g.setColor(batata.getCor());
                        g.fillOval(batata.getX(), batata.getY(), TAMANHO_BLOCO, TAMANHO_BLOCO);
                    }
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

            // Barra de HP do Duque
            g.setColor(Color.BLACK);
            g.fillRect(5, 5, 235, 75);
            g.setColor(Color.WHITE);
            g.setFont(new Font(NOME_FONTE, Font.BOLD, 20));
            g.drawString("Vida: " + batata.getHp(), 20, 10 + g.getFont().getSize());

            g.setColor(Color.RED);
            g.fillRect(120, 10, 115, 30);
            g.setColor(Color.GREEN);
            g.fillRect(120, 10, (int)(115 * ((float)batata.getHp() / 5)), 30);

            g.setColor(Color.WHITE);
            g.setFont(new Font(NOME_FONTE, Font.BOLD, 20));
            if(curBoss != null){
                g.drawString("Derrote o  BOSS!", 20, 50 + g.getFont().getSize());
                if(curBoss instanceof Cappuccino){
                    g.setColor(Color.BLACK);
                    g.fillRect(5, 80, 235, 35);
                    g.setColor(Color.WHITE);
                    g.drawString("Tempo: " + (System.currentTimeMillis() - ((Cappuccino) curBoss).timer) / 1000 + "s",
                    20, 90 + g.getFont().getSize());
                }
            }
            else g.drawString("BOSS  derrotado!", 20, 50 + g.getFont().getSize());

            //Barra de HP do Boss
            if(!(curBoss == null || curBoss.hp <= 0)){
                g.setColor(Color.BLACK);
                g.fillRect(LARGURA_TELA/2 - 250, 5, 500, 75);
                g.setColor(Color.RED);
                g.setFont(new Font(NOME_FONTE, Font.BOLD, 20));
                FontMetrics metrics = getFontMetrics(g.getFont());
                g.drawString(curBoss.nome, (LARGURA_TELA - metrics.stringWidth(curBoss.nome)) / 2, 10 + g.getFont().getSize());
                
                g.fillRect(LARGURA_TELA/2 - 225, 40, 450, 30);
                g.setColor(Color.GREEN);
                g.fillRect(LARGURA_TELA/2 - 225, 40, (int)(450 * ((float)curBoss.hp / curBoss.maxHp)), 30);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if(estado == EstadoJogo.RODANDO) {
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
                        // Colisão com boss
                        if (obj.getLayer() == ObjetoColidivel.CollisionLayer.ENEMY && !(obj instanceof MofadaBombada)) {
                            if(
                                obj instanceof NaveMaeranha &&
                                ((NaveMaeranha) obj).hp <= (((NaveMaeranha) obj).maxHp)/3
                            ){
                                if (!((Boss) obj).takeDamage(3)) collisionHappened = false;
                            } else if (!((Boss) obj).takeDamage(1)) collisionHappened = false;
                        }
                        // Colisão com projeteis inimigos
                        else if(obj.getLayer() == ObjetoColidivel.CollisionLayer.PROJECTILE){
                            if(!((Projetil) obj).isCollidable()) collisionHappened = false;
                            else ((Projetil) obj).desativar();
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

            // Atualiza bosses
            if(curBoss != null) {
                curBoss.atacar();
                if(!curBoss.isAlive){
                    objetosColidiveis.remove(curBoss);
                    curBoss = null;
                }
            } else if (curBoss == null && !porta.isAberta()) {
                if(bossNum < 5){
                    if(musica.isPlaying()) musica.stopSong();
                    efeito.playSong("assets/ta-Da.wav", false);
                    porta.setImage(portaImgs[1]);
                    porta.abrir();
                } else if(bossNum == 5) playCutscene();
                else youWin();
            }

            pofs.removeIf(Pof::timeOut);

            batata.updateInvulnerability();
            if(batata.getHp() <= 0){
                estado = EstadoJogo.PARADO;
                gameOver();
            }

            repaint();
        }
    }

    private void playCutscene(){
        if(!musica.isPlaying()){
            musica.playSong("assets/siren-alert.wav", true);

            String mensagem = "Vladmir Cenoura:\n\n" +
                            "\t\tAlô, Duque? Está na escuta?\n" +
                            "\tO Larry conseguiu escapar, e ativou o sistema\n" +
                            "\tde segurança da Nave Mãeranha!\n" +
                            "\tÉ melhor você sair logo daí, estou te esperando\n" +
                            "\tlá fora com a X-Salada. Bora derrubar esse lugar!";
            
            try{
                Image vladmirImg = ImageIO.read(new File("assets/VladmirCenoura.png"));
                vladmirImg = vladmirImg.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                vladmirCenoura = new ImageIcon(vladmirImg);
                JOptionPane.showMessageDialog(
                    this,
                    mensagem,
                    "Diálogo",
                    JOptionPane.INFORMATION_MESSAGE,
                    vladmirCenoura
                );
            } catch (IOException e) {
                System.out.println("Erro ao carregar imagens");
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    mensagem,
                    "Diálogo",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
            porta.setImage(portaImgs[1]);
            porta.abrir();
        }
    }

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

    private void youWin(){
        musica.stopSong();
        efeito.stopSong();
        cleanUp();

        Window window = SwingUtilities.windowForComponent(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            TelaVitoria telaVitoria = new TelaVitoria(musica);
            frame.getContentPane().removeAll();
            frame.add(telaVitoria);
            frame.revalidate();
            frame.repaint();
            telaVitoria.requestFocusInWindow();
        } else {
            System.err.println("Erro: Não foi possível encontrar o JFrame principal");
        }
    }

    private abstract class Boss extends ObjetoColidivel {
        protected int tamanho;
        protected int hp, maxHp;
        protected boolean isAlive = true;
        protected String nome, caminho;
        protected Alert alerta;
        protected long timer = 0;
        protected boolean isAttacking = false, triggered = false;
        
        public Boss(int tamanho, Color cor, int hp) {
            super(LARGURA_TELA/2 - tamanho/2, 100, tamanho, (tamanho*4)/5, cor, CollisionLayer.ENEMY); // Tamanho do inimigo
            this.tamanho = tamanho;
            this.cor = cor;
            this.hp = hp;
            this.maxHp = hp;
            this.timer = System.currentTimeMillis();
        }

        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp <= 0){
                efeito.playSong("assets/tiro.wav", false);
                pofs.add(new Pof(x - tamanho/4, y - tamanho/4, tamanho + tamanho/2));
                isAlive = false;
                if(alerta != null) {
                    alerta.desativar();
                    alertas.remove(alerta);
                }
            } else efeito.playSong(caminho, false);
            return true;
        }

        public abstract void atacar();
    }

    private class Cappuccino extends Boss {
        private Projetil[] clones = new Projetil[2];
        private int curHp, curSpot, centerPos;
        private boolean fail = false;

        public Cappuccino(){
            super(TAMANHO_BLOCO*5, new Color(87, 43, 18), 15);
            this.nome = "CAPPUCCINO  ASSASSINO";
            this.caminho = "assets/spill.wav";
            this.curImage = cappuccinoImgs[0];
            this.curHp = maxHp; // Garante que clones novos sejam criados cada vez que o cappuccino perde hp
            this.centerPos = LARGURA_TELA/2 - tamanho/2; // Guarda a posição central
        }

        public boolean takeDamage(int damage){
            if(!isAttacking) return false;
            this.hp -= damage;
            if(hp <= 0){
                efeito.playSong("assets/tiro.wav", false);
                pofs.add(new Pof(x - tamanho/4, y - tamanho/4, tamanho + tamanho/2));
                isAlive = false;
                if(alerta != null) {
                    alerta.desativar();
                    alertas.remove(alerta);
                }
            } else efeito.playSong(caminho, false);
            return true;
        }

        private Projetil criarClone(int xPos) {
            return new Projetil(xPos, y, largura, altura, 0, 0, Color.GRAY, true);
        }

        public void atacar(){
            if(!isAttacking){
                if(!triggered){
                    for(Projetil p : clones) if(p != null){
                        p.desativar();
                        objetosColidiveis.remove(p);
                        p = null;
                    }
                    fail = false;
                    timer = System.currentTimeMillis();
                    triggered = true;
                }
                if(System.currentTimeMillis() - timer >= 500 && triggered){
                    curSpot = new Random().nextInt(3);
                
                    // Atualiza posição do Cappuccino
                    switch(curSpot) {
                        case 0: // Esquerda
                            this.x = centerPos - (tamanho + TAMANHO_BLOCO);
                            clones[0] = criarClone(centerPos);
                            clones[1] = criarClone(centerPos + (tamanho + TAMANHO_BLOCO));
                            break;
                        case 1: // Centro
                            this.x = centerPos;
                            clones[0] = criarClone(centerPos - (tamanho + TAMANHO_BLOCO));
                            clones[1] = criarClone(centerPos + (tamanho + TAMANHO_BLOCO));
                            break;
                        case 2: // Direita
                            this.x = centerPos + (tamanho + TAMANHO_BLOCO);
                            clones[0] = criarClone(centerPos - (tamanho + TAMANHO_BLOCO));
                            clones[1] = criarClone(centerPos);
                            break;
                    }
                    for(Projetil p : clones){
                        objetosColidiveis.add(p);
                        p.curImage = cappuccinoImgs[3];
                    }
                    curImage = cappuccinoImgs[0];
                    timer = System.currentTimeMillis();
                    curHp = hp;
                    triggered = false;
                    isAttacking = true;
                }
            } else {
                if(System.currentTimeMillis() - timer >= 4000) fail = true;
                if(fail){
                    curImage = cappuccinoImgs[1];
                    if(!batata.isInvulnerable()){
                        efeito.playSong("assets/Ouch.wav", false);
                        batata.takeDamage(1);
                    }
                    isAttacking = false;
                }
                if(curHp > hp){
                    curImage = cappuccinoImgs[2];
                    curHp = hp;
                    isAttacking = false;
                }
                for(Projetil p : clones){
                    if(p != null){
                        if(p.colideCom(batata) && !batata.isInvulnerable()){
                            efeito.playSong("assets/Ouch.wav", false);
                            batata.takeDamage(1);
                            isAttacking = false;
                        }
                        if(!p.isAtivo()){
                            fail = true;
                            objetosColidiveis.remove(p);
                            p = null;
                        }
                    }
                }
            }
        }
    }

    private class GigaSlime extends Boss {
        private Projetil lancaChamas;

        public GigaSlime(){
            super(TAMANHO_BLOCO*5, Color.RED, 150);
            this.nome = "GIGA  SLIME";
            this.caminho = "assets/danoSlime.wav";
            this.curImage = gigaSlimeImgs[0];
        }

        public void atacar(){
            if(!isAttacking){
                if(System.currentTimeMillis() - timer >= 3500 && !triggered){
                    efeito.playSong("assets/warning.wav", false);
                    this.alerta = new Alert(LARGURA_TELA/2, ALTURA_TELA/2, TAMANHO_BLOCO);
                    alertas.add(alerta);
                    triggered = true;
                    curImage = gigaSlimeImgs[1];
                    timer = System.currentTimeMillis();
                }
                if(System.currentTimeMillis() - timer >= 1500 && triggered){
                    if(alerta != null) {
                        alerta.desativar();
                        alertas.remove(alerta);
                    }

                    lancaChamas = new Projetil(x, y + (tamanho*4)/5, tamanho, 450,
                        0, 0, Color.ORANGE, false);
                    lancaChamas.curImage = lancaChamasImg;
                    objetosColidiveis.add(lancaChamas);
                    efeito.playSong("assets/gigaBarulho.wav", false);
                    curImage = gigaSlimeImgs[2];
                    timer = System.currentTimeMillis();
                    triggered = false;
                    isAttacking = true;
                }
            } else {
                if(lancaChamas.isAtivo()){
                    if(lancaChamas.colideCom(batata) && !batata.isInvulnerable()){
                        efeito.playSong("assets/Ouch.wav", false);
                        batata.takeDamage(1);
                    }
                    if(System.currentTimeMillis() - timer >= 2000){
                        lancaChamas.desativar();
                        objetosColidiveis.remove(lancaChamas);
                        lancaChamas = null;
                        curImage = gigaSlimeImgs[0];
                        timer = System.currentTimeMillis();
                        isAttacking = false;
                    }
                }
            }
        }
    }

    private class SirPlatoh extends Boss {
        private Projetil garfo, faca;
        private int lastAttack = 1; //Guarda o último ataque usado. 0 é garfo e 1 é faca

        public SirPlatoh(){
            super(TAMANHO_BLOCO*5, Color.WHITE, 225);
            this.nome = "SIR.  PLATOH";
            this.caminho = "assets/estilhaco.wav";
            this.curImage = sirPlatohImgs[0];
        }

        public void atacar(){
            if(!isAttacking){
                if(System.currentTimeMillis() - timer >= 3000 && !triggered){
                    efeito.playSong("assets/warning.wav", false);
                    if(lastAttack == 1) this.alerta = new Alert(x + tamanho/8, ALTURA_TELA/2, TAMANHO_BLOCO);
                    else this.alerta = new Alert(x + tamanho/2, ALTURA_TELA/2, TAMANHO_BLOCO);
                    alertas.add(alerta);
                    triggered = true;
                    timer = System.currentTimeMillis();
                }
                if(System.currentTimeMillis() - timer >= 1000 && triggered){
                    if(alerta != null) {
                        alerta.desativar();
                        alertas.remove(alerta);
                    }

                    if(lastAttack == 1){
                        garfo = new Projetil(x + tamanho/8, y + (tamanho*4)/5, TAMANHO_BLOCO, TAMANHO_BLOCO*4,
                        0, 4, Color.GRAY, false);
                        garfo.curImage = garfoImg;
                        objetosColidiveis.add(garfo);
                        efeito.playSong("assets/forkThrow.wav", false);
                        curImage = sirPlatohImgs[2];
                    } else {
                        faca = new Projetil(x + tamanho/3 + TAMANHO_BLOCO, y + (tamanho*4)/5, TAMANHO_BLOCO*3, TAMANHO_BLOCO*3,
                        0, 2, Color.LIGHT_GRAY, false);
                        faca.curImage = facaImgs[(int)((System.currentTimeMillis() / 50) % 8)];
                        objetosColidiveis.add(faca);
                        efeito.playSong("assets/knifeThrow.wav", false);
                        curImage = sirPlatohImgs[1];
                    }
                    timer = System.currentTimeMillis();
                    triggered = false;
                    isAttacking = true;
                }
            } else {
                if(garfo != null && garfo.isAtivo()){
                    garfo.mover();
                    if(garfo.colideCom(batata) && !batata.isInvulnerable()){
                        efeito.playSong("assets/Ouch.wav", false);
                        batata.takeDamage(1);
                    }
                    if(garfo.getY() > 500) garfo.setDirY(-4);
                    else if(garfo.colideCom(this) && garfo.getDirY() < 0){
                        garfo.desativar();
                        objetosColidiveis.remove(garfo);
                        garfo = null;
                        curImage = sirPlatohImgs[0];
                        timer = System.currentTimeMillis();
                        lastAttack = 0;
                        isAttacking = false;
                    }
                } else if(faca != null && faca.isAtivo()){
                    faca.mover();
                    faca.curImage = facaImgs[(int)((System.currentTimeMillis() / 50) % 8)];
                    if(faca.colideCom(batata) && !batata.isInvulnerable()){
                        efeito.playSong("assets/Ouch.wav", false);
                        batata.takeDamage(1);
                    }
                    if(faca.getY() > 550 && faca.getDirY() > 0){
                        faca.setDirY(0);
                        faca.setDirX(-2);
                    }else if(verificarColisaoParede(faca) && faca.getDirX() < 0){
                        faca.setX(faca.getX() - faca.getDirX()*10);
                        faca.setDirY(-2);
                        faca.setDirX(0);
                    } else if(faca.colideCom(this) && faca.getDirY() < 0){
                        faca.desativar();
                        objetosColidiveis.remove(faca);
                        faca = null;
                        curImage = sirPlatohImgs[0];
                        timer = System.currentTimeMillis();
                        lastAttack = 1;
                        isAttacking = false;
                    }
                }
            }
        }
    }

    private class MofadaBombada extends Boss {
        private Projetil braco;
        private ArrayList<Projetil> bolasDeMofo = new ArrayList<>();
        private Direction lastAttack = Direction.LEFT; //Guarda a direção do último ataque de braço usado.
        private long ultimoTiro = 0;
        private boolean hasPunched = false;

        public MofadaBombada(){
            super(TAMANHO_BLOCO*5, Color.GREEN, 9);
            this.nome = "MOFADA  BOMBADA";
            this.caminho = "assets/cheeseOuch.wav";
            this.curImage = mofadaBombadaImgs[0];
        }

        public void atacar(){
            if(!isAttacking){
                if(System.currentTimeMillis() - timer >= 3000 && !triggered){
                    efeito.playSong("assets/warning.wav", false);
                    this.alerta = new Alert(LARGURA_TELA/2, ALTURA_TELA/2, TAMANHO_BLOCO);
                    alertas.add(alerta);
                    triggered = true;
                    timer = System.currentTimeMillis();
                }
                if(System.currentTimeMillis() - timer >= 1000 && triggered){
                    if(alerta != null) {
                        alerta.desativar();
                        alertas.remove(alerta);
                    }
                    if(lastAttack == Direction.RIGHT) curImage = mofadaBombadaImgs[3];
                    else curImage = mofadaBombadaImgs[1];
                    timer = System.currentTimeMillis();
                    triggered = false;
                    isAttacking = true;
                }
            } else {
                long now = System.currentTimeMillis();
                if(!hasPunched){
                    if (now - ultimoTiro > INTERVALO_TIRO*3) {
                        int pX = (new Random().nextInt(5))*TAMANHO_BLOCO + x;
                        Projetil p = new Projetil(pX, y + (tamanho*4)/5, TAMANHO_BLOCO, TAMANHO_BLOCO, 0, 1,
                            Color.GREEN, false);
                        p.curImage = mofoImg;
                        bolasDeMofo.add(p);
                        objetosColidiveis.add(p);
                        ultimoTiro = now;
                    }
                    if(now - timer >= 4000 && !triggered){
                        efeito.playSong("assets/warning.wav", false);
                        if(lastAttack == Direction.RIGHT) this.alerta = new Alert(x, ALTURA_TELA/2, TAMANHO_BLOCO);
                        else this.alerta = new Alert(x + (tamanho*6)/8, ALTURA_TELA/2, TAMANHO_BLOCO);
                        alertas.add(alerta);
                        triggered = true;
                        timer = System.currentTimeMillis();
                    }
                    if(now - timer >= 600 && triggered){
                        int pX = x + TAMANHO_BLOCO/2;
                        if(lastAttack == Direction.LEFT) pX += (tamanho*4)/5 - TAMANHO_BLOCO;

                        if(alerta != null) {
                            alerta.desativar();
                            alertas.remove(alerta);
                        }

                        braco = new Projetil(pX, y + (tamanho*4)/5, TAMANHO_BLOCO, 450,
                            0, 0, Color.YELLOW, true);
                        braco.curImage = bracoImg;
                        objetosColidiveis.add(braco);
                        efeito.playSong("assets/squish.wav", false);
                        if(lastAttack == Direction.RIGHT) curImage = mofadaBombadaImgs[4];
                        else curImage = mofadaBombadaImgs[2];
                        timer = System.currentTimeMillis();
                        triggered = false;
                        hasPunched = true;
                    }
                } else {
                    if(!braco.isAtivo()){
                        takeDamage(1);
                        braco.desativar();
                        objetosColidiveis.remove(braco);
                        braco = null;
                        curImage = mofadaBombadaImgs[0];
                        timer = System.currentTimeMillis();
                        if(lastAttack == Direction.RIGHT) lastAttack = Direction.LEFT;
                        else lastAttack = Direction.RIGHT;
                        hasPunched = false;
                        isAttacking = false;
                    }
                    else if(braco.colideCom(batata) && !batata.isInvulnerable()){
                        efeito.playSong("assets/Ouch.wav", false);
                        batata.takeDamage(1);
                    }
                }
            }
            Iterator<Projetil> projIt = bolasDeMofo.iterator();
            while (projIt.hasNext()) {
                Projetil p = projIt.next();
                p.mover();

                //Colisão com o Duque
                if(p.colideCom(batata) && !batata.isInvulnerable()){
                    efeito.playSong("assets/Ouch.wav", false);
                    batata.takeDamage(1);
                    p.desativar();
                }

                // Remove se inativo ou se a Mofada morreu
                if (!p.isAtivo() || !this.isAlive) {
                    p.desativar();
                    objetosColidiveis.remove(p);
                    projIt.remove();
                }
            }
        }
    }

    private class CerberoNimbus extends Boss {
        private ArrayList<Projetil> algodoes = new ArrayList<>();
        private int lastAttack = 1; //Guarda o último ataque usado. 0 é balançar e 1 é rolar
        private int skipSpot;
        private Alert[] shakeAlerts = new Alert[6];
        private Direction ultimaDirecao = Direction.DOWN;

        public CerberoNimbus(){
            super(TAMANHO_BLOCO*5, Color.PINK, 150);
            this.nome = "CÉRBERO  NIMBUS";
            this.caminho = "assets/big-dog.wav";
            this.curImage = cerberoNimbusImgs[0];
        }

        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp <= 0){
                efeito.playSong("assets/tiro.wav", false);
                pofs.add(new Pof(x, y, tamanho + tamanho / 2));
                isAlive = false;
                if(alerta != null) {
                    alerta.desativar();
                    alertas.remove(alerta);
                }
                for(int i = 0; i < 6; i++){
                    if(shakeAlerts[i] != null) {
                        shakeAlerts[i].desativar();
                        alertas.remove(shakeAlerts[i]);
                    }
                }
            } else efeito.playSong(caminho, false);
            return true;
        }

        private void genWarning(){
            skipSpot = new Random().nextInt(6);
            efeito.playSong("assets/warning.wav", false);
            for(int i = 0; i < 6; i++){
                if(i == skipSpot) continue;
                if(i < 3) this.shakeAlerts[i] = new Alert(TAMANHO_BLOCO*9, TAMANHO_BLOCO*(i*3 + 6) - TAMANHO_BLOCO/2, TAMANHO_BLOCO);
                else {
                    this.shakeAlerts[i] = new Alert(LARGURA_TELA - TAMANHO_BLOCO*10,
                        TAMANHO_BLOCO*((i - 3)*3 + 6) + TAMANHO_BLOCO/2, TAMANHO_BLOCO);
                }
                alertas.add(shakeAlerts[i]);
            }
        }
        private void desativarExistentes() {
            Iterator<Projetil> it = algodoes.iterator();
            while (it.hasNext()) {
                Projetil p = it.next();
                if (!p.isCollidable()) {
                    p.desativar();
                    objetosColidiveis.remove(p);
                    it.remove();
                }
            }
        }
        private void genShakingCotton(){
            desativarExistentes();
            for(int i = 0; i < 6; i++){
                if(i == skipSpot) continue;
                Projetil p;
                if(i < 3) p = AlgodaoFactory.createProjectile(TAMANHO_BLOCO*9, TAMANHO_BLOCO*(i*3 + 6) + TAMANHO_BLOCO/2, false);
                else {
                    p = AlgodaoFactory.createProjectile(LARGURA_TELA - TAMANHO_BLOCO*10,
                        TAMANHO_BLOCO*((i - 3)*3 + 6) + TAMANHO_BLOCO/2, false);
                }

                algodoes.add(p);
                objetosColidiveis.add(p);
                p.curImage = algodaoImg;
            }
        }
        private void genRollingCotton(){
            int a = new Random().nextInt(5);
            int b = new Random().nextInt(5);
            if(b == a){
                if(b < 4) b++;
                else b--;
            }
            a *= TAMANHO_BLOCO; b *= TAMANHO_BLOCO;

            Projetil[] p = new Projetil[2];
            p[0] = AlgodaoFactory.createProjectile(x + a, y + (tamanho*3)/5, true);
            p[1] = AlgodaoFactory.createProjectile(x + b, y + (tamanho*3)/5, true);

            for(int i = 0; i < 2; i++){
                // Verifica colisão com algodões existentes
                boolean colidiu = false;
                Iterator<Projetil> it = algodoes.iterator();
                while (it.hasNext()) {
                    Projetil existente = it.next();
                    if (p[i].colideCom(existente)) {
                        colidiu = true;
                        break;
                    }
                }
                if(!colidiu){
                    algodoes.add(p[i]);
                    objetosColidiveis.add(p[i]);
                    p[i].curImage = algodaoImg;
                }
            }
        }

        public void atacar(){
            if(colideCom(batata) && !batata.isInvulnerable()){
                efeito.playSong("assets/Ouch.wav", false);
                batata.takeDamage(1);
            }
            if(!isAttacking){
                if(lastAttack == 1){
                    if(System.currentTimeMillis() - timer >= 2000 && !triggered){
                        genWarning();
                        triggered = true;
                        timer = System.currentTimeMillis();
                    }
                    if(System.currentTimeMillis() - timer >= 500 && triggered){
                        for(int i = 0; i < 6; i++){
                            if(shakeAlerts[i] != null) {
                                shakeAlerts[i].desativar();
                                alertas.remove(shakeAlerts[i]);
                            }
                        }
                        genShakingCotton();
                        this.curImage = cerberoNimbusImgs[2];
                        efeito.playSong("assets/big-dog-shaking.wav", false);
                        timer = System.currentTimeMillis();
                        triggered = false;
                        isAttacking = true;
                    }
                } else {
                    if(System.currentTimeMillis() - timer >= 3000 && !triggered){
                        efeito.playSong("assets/warning.wav", false);
                        this.alerta = new Alert(LARGURA_TELA/2, ALTURA_TELA/2, TAMANHO_BLOCO);
                        alertas.add(alerta);
                        triggered = true;
                        timer = System.currentTimeMillis();
                    }
                    if(System.currentTimeMillis() - timer >= 1000 && triggered){
                        if(alerta != null) {
                            alerta.desativar();
                            alertas.remove(alerta);
                        }
                        curImage = cerberoNimbusImgs[3];
                        efeito.playSong("assets/big-dog-bark.wav", false);
                        timer = System.currentTimeMillis();
                        triggered = false;
                        isAttacking = true;
                    }
                }
            } else {
                if(lastAttack == 1){
                    if ((System.currentTimeMillis() / 100) % 2 == 0) this.curImage = cerberoNimbusImgs[1];
                    else this.curImage = cerberoNimbusImgs[2];

                    if(System.currentTimeMillis() - timer >= 500){
                        curImage = cerberoNimbusImgs[0];
                        lastAttack = 0;
                        timer = System.currentTimeMillis();
                        isAttacking = false;
                    }
                } else {
                    if(ultimaDirecao == Direction.DOWN){
                        this.y += 3;
                        if(foraDaTela()){
                            this.curImage = cerberoNimbusImgs[4];
                            this.y -= 3;
                            this.ultimaDirecao = Direction.UP;
                        }
                    } else {
                        this.y -= 3;
                        if(y > 49){
                            if(y % TAMANHO_BLOCO/2 == 0) genRollingCotton();
                        }
                        else {
                            y = 49;
                            this.ultimaDirecao = Direction.DOWN;
                            this.curImage = cerberoNimbusImgs[0];
                            lastAttack = 1;
                            timer = System.currentTimeMillis();
                            isAttacking = false;
                        }
                    }
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
                    
                // Remove se o Cerbero morreu
                if (!this.isAlive) p.desativar();

                // Verifica se foi destruido
                if(!p.isAtivo()){
                    objetosColidiveis.remove(p);
                    projIt.remove();
                    if(this.isAlive){
                        efeito.playSong("assets/tiro.wav", false);
                        pofs.add(new Pof(p.x, p.y, TAMANHO_BLOCO + TAMANHO_BLOCO / 2));
                    }
                }
            }
        }

        public class AlgodaoFactory {
            public static Projetil createProjectile(int x, int y, boolean isCollidable) {
                return new Projetil(x, y, TAMANHO_BLOCO, TAMANHO_BLOCO, 0, 0, Color.PINK, isCollidable);
            }
        }
    }

    private class Larry extends Boss{
        private ArrayList<Projetil> teias = new ArrayList<>();
        private ArrayList<Projetil> fios = new ArrayList<>();
        private long ultimoTiro = 0;
        private boolean inDespair = false; //Se refere ao segundo set de fios
        private Alert[] stringAlerts = new Alert[2];

        public Larry(){
            super(TAMANHO_BLOCO*5, Color.BLACK, 225);
            this.nome = "LARRY,  O  MUQUIARANHA";
            this.caminho = "assets/danoAranha.wav";
            this.curImage = larryImgs[0];
        }

        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp > 0) efeito.playSong(caminho, false);
            return true;
        }

        public void playDefeatAnimation(){
            if(!triggered){
                if(musica.isPlaying()) musica.stopSong();
                curImage = larryImgs[1];
                triggered = true;
                timer = System.currentTimeMillis();
                for(int i = 0; i < 2; i++){
                    if(stringAlerts[i] != null) {
                        stringAlerts[i].desativar();
                        alertas.remove(stringAlerts[i]);
                    }
                }
            }
            if(System.currentTimeMillis() - timer >= 2000 && triggered){
                efeito.playSong("assets/running.wav", false);
                isAlive = false;
            }
        }

        public void atacar(){
            long now = System.currentTimeMillis();
            if(hp <= 0) playDefeatAnimation();
            else {
                if(!inDespair && !triggered){
                    if (now - ultimoTiro > INTERVALO_TIRO*3) {
                        int pX = (new Random().nextInt(5))*TAMANHO_BLOCO + x;
                        Projetil p = new Projetil(pX, y + (tamanho*3)/5, TAMANHO_BLOCO, TAMANHO_BLOCO, 0, 1,
                            Color.LIGHT_GRAY, false);
                        p.curImage = teiaImg;
                        teias.add(p);
                        objetosColidiveis.add(p);
                        ultimoTiro = now;
                    }
                }
                if(!isAttacking){
                    if(hp < maxHp/2 && !triggered){
                        efeito.playSong("assets/warning.wav", false);
                        stringAlerts[0] = new Alert(x, ALTURA_TELA/2, TAMANHO_BLOCO);
                        stringAlerts[1] = new Alert(x + (tamanho*4)/5, ALTURA_TELA/2, TAMANHO_BLOCO);
                        for(int i = 0; i < 2; i++) alertas.add(stringAlerts[i]);
                        triggered = true;
                        timer = System.currentTimeMillis();
                    }
                    if(now - timer >= 1000 && triggered){
                        int pX = x;

                        for(int i = 0; i < 2; i++){
                            if(stringAlerts[i] != null) {
                                stringAlerts[i].desativar();
                                alertas.remove(stringAlerts[i]);
                            }
                        }

                        for(int i = 0; i < 2; i++){
                            if(i == 1) pX += (tamanho*9)/10;
                            Projetil f = new Projetil(pX, y + (tamanho*4)/5, TAMANHO_BLOCO/2, 450,
                                0, 0, Color.WHITE, false);
                            f.curImage = fioImgs[0];
                            fios.add(f);
                            objetosColidiveis.add(f);
                        }
                        
                        efeito.playSong("assets/squish.wav", false);
                        timer = System.currentTimeMillis();
                        triggered = false;
                        isAttacking = true;
                    }
                } else if(!inDespair){
                    if(hp < maxHp/20 && !triggered){
                        efeito.playSong("assets/warning.wav", false);
                        stringAlerts[0] = new Alert(x + tamanho/5, ALTURA_TELA/2, TAMANHO_BLOCO);
                        stringAlerts[1] = new Alert(x + (tamanho*3)/5, ALTURA_TELA/2, TAMANHO_BLOCO);
                        for(int i = 0; i < 2; i++) alertas.add(stringAlerts[i]);
                        triggered = true;
                        timer = System.currentTimeMillis();
                    }
                    if(now - timer >= 1000 && triggered){
                        int pX = x + tamanho/5;

                        for(int i = 0; i < 2; i++){
                            if(stringAlerts[i] != null) {
                                stringAlerts[i].desativar();
                                alertas.remove(stringAlerts[i]);
                            }
                        }

                        for(int i = 0; i < 2; i++){
                            if(i == 1) pX += tamanho/2;
                            Projetil f = new Projetil(pX, y + (tamanho*4)/5, TAMANHO_BLOCO/2, 450,
                                0, 0, Color.WHITE, false);
                            f.curImage = fioImgs[0];
                            fios.add(f);
                            objetosColidiveis.add(f);
                        }
                        
                        efeito.playSong("assets/squish.wav", false);
                        timer = System.currentTimeMillis();
                        triggered = false;
                        inDespair = true;
                    }
                }
            }

            Iterator<Projetil> webIt = teias.iterator();
            while (webIt.hasNext()) {
                Projetil p = webIt.next();
                p.mover();

                //Colisão com o Duque
                if(p.colideCom(batata) && !batata.isInvulnerable()){
                    efeito.playSong("assets/Ouch.wav", false);
                    batata.takeDamage(1);
                    p.desativar();
                }

                // Remove se inativo ou se o Larry foi derrotado
                if (!p.isAtivo() || hp <= 0) {
                    p.desativar();
                    objetosColidiveis.remove(p);
                    webIt.remove();
                }
            }
            Iterator<Projetil> stringIt = fios.iterator();
            while (stringIt.hasNext()) {
                Projetil p = stringIt.next();

                //Colisão com o Duque
                if(p.colideCom(batata) && !batata.isInvulnerable()){
                    efeito.playSong("assets/Ouch.wav", false);
                    batata.takeDamage(1);
                }

                // Remove se inativo ou se o Larry foi derrotado
                if (!p.isAtivo() || hp <= 0) {
                    p.desativar();
                    objetosColidiveis.remove(p);
                    stringIt.remove();
                }
            }
        }
    }

    private class NaveMaeranha extends Boss{
        private ArrayList<Projetil> teias = new ArrayList<>();
        private ArrayList<Projetil> fios = new ArrayList<>();
        private long ultimoTiro = 0;
        private Alert[] stringAlerts = new Alert[2];
        private int[] stringYs = new int[2]; //Guarda onde os fios vão aparecer
        private boolean isBlowingUp = false;

        public NaveMaeranha(){
            super(LARGURA_TELA, Color.DARK_GRAY, 450);
            this.x = 0; this.y = 0; this.altura = 250;
            this.nome = "NAVE MÃERANHA";
            this.caminho = "assets/danoAranha.wav";
            this.curImage = naveMareanhaImg;
        }

        public boolean takeDamage(int damage){
            this.hp -= damage;
            if(hp > 0) efeito.playSong(caminho, false);
            return true;
        }

        public void playDefeatAnimation(){
            if(!isBlowingUp){
                if(musica.isPlaying()) musica.stopSong();
                isBlowingUp = true;
                timer = System.currentTimeMillis();
                for(int i = 0; i < 2; i++){
                    if(stringAlerts[i] != null) {
                        stringAlerts[i].desativar();
                        alertas.remove(stringAlerts[i]);
                    }
                }
            } else {
                if(System.currentTimeMillis() % 10 == 0){
                    int pofX = new Random().nextInt(LARGURA_TELA - TAMANHO_BLOCO*2);
                    int pofY = new Random().nextInt(altura - TAMANHO_BLOCO);
                    efeito.playSong("assets/tiro.wav", false);
                    pofs.add(new Pof(pofX, pofY, TAMANHO_BLOCO*2));
                }
                if(System.currentTimeMillis() - timer >= 5000) isAlive = false;
            }
        }

        public void genTeias(){
            long now = System.currentTimeMillis();
            if (now - ultimoTiro > INTERVALO_TIRO*3) {
                for(int i = 0; i < 4; i++){
                    int pX = (new Random().nextInt(20) + 3)*TAMANHO_BLOCO;
                    Projetil p = new Projetil(pX, altura, TAMANHO_BLOCO, TAMANHO_BLOCO, 0, 1,
                            Color.LIGHT_GRAY, false);
                    p.curImage = teiaImg;
                    teias.add(p);
                    objetosColidiveis.add(p);
                    ultimoTiro = now;
                }
            }
        }

        public void genWarning(){
            efeito.playSong("assets/warning.wav", false);
            for(int i = 0; i < 2; i++){
                stringYs[i] = (new Random().nextInt(4))*TAMANHO_BLOCO*3 + TAMANHO_BLOCO/4 + altura;

                stringAlerts[i] = new Alert(LARGURA_TELA/2, stringYs[i] - TAMANHO_BLOCO/2, TAMANHO_BLOCO);
                alertas.add(stringAlerts[i]);
            }
            triggered = true;
            timer = System.currentTimeMillis();
        }

        public void genString(){
            for(int i = 0; i < 2; i++){
                Projetil f = new Projetil(TAMANHO_BLOCO*2, stringYs[i], LARGURA_TELA - TAMANHO_BLOCO*4 ,TAMANHO_BLOCO/2,
                    0, 0, Color.WHITE, false);
                f.curImage = fioImgs[1];
                fios.add(f);
                objetosColidiveis.add(f);
            }
            efeito.playSong("assets/LaserSwitch.wav", false);
            timer = System.currentTimeMillis();
            triggered = false;
            isAttacking = true;
        }

        public void atacar(){
            if(hp <= 0) playDefeatAnimation();
            else{
                genTeias();
                if(!isAttacking){
                    if(!triggered) genWarning();
                    if(System.currentTimeMillis() - timer >= 2000 && triggered){
                        for(int i = 0; i < 2; i++){
                            if(stringAlerts[i] != null) {
                                stringAlerts[i].desativar();
                                alertas.remove(stringAlerts[i]);
                            }
                        }
                        genString();
                    }
                } else {
                    if(System.currentTimeMillis() - timer >= 1000){
                        Iterator<Projetil> stringIt = fios.iterator();
                        while (stringIt.hasNext()) stringIt.next().desativar();

                        efeito.playSong("assets/LaserSwitch.wav", false);
                        timer = System.currentTimeMillis();
                        isAttacking = false;
                    }
                }
            }

            Iterator<Projetil> webIt = teias.iterator();
            while (webIt.hasNext()) {
                Projetil p = webIt.next();
                p.mover();

                //Colisão com o Duque
                if(p.colideCom(batata) && !batata.isInvulnerable()){
                    efeito.playSong("assets/Ouch.wav", false);
                    batata.takeDamage(1);
                    p.desativar();
                }

                // Remove se inativo ou se a nave foi destruída
                if (!p.isAtivo() || hp <= 0) {
                    p.desativar();
                    objetosColidiveis.remove(p);
                    webIt.remove();
                }
            }

            Iterator<Projetil> stringIt = fios.iterator();
            while (stringIt.hasNext()) {
                Projetil p = stringIt.next();

                //Colisão com o Duque
                if(p.colideCom(batata) && !batata.isInvulnerable()){
                    efeito.playSong("assets/Ouch.wav", false);
                    batata.takeDamage(1);
                }

                // Remove se inativo ou se a nave foi destruída
                if (!p.isAtivo() || hp <= 0) {
                    objetosColidiveis.remove(p);
                    stringIt.remove();
                }
            }
        }
    }

    private boolean verificarColisaoParede(ObjetoColidivel entity) {         
        if(entity.layer == ObjetoColidivel.CollisionLayer.PLAYER){
            Player jogador = (Player) entity;
            for (Parede parede : paredes) {
                if (entity.colideCom(parede)) {
                    if(bossNum == 6 && !batata.isInvulnerable()){
                        efeito.playSong("assets/Ouch.wav", false);
                        batata.takeDamage(1);
                    }

                    // Reverte o movimento
                    switch (jogador.getUltimaDirecao()) {
                        case LEFT: entity.x += batata.getVelocidade(); break;
                        case RIGHT: entity.x -= batata.getVelocidade(); break;
                        case UP: entity.y += batata.getVelocidade(); break;
                        case DOWN: entity.y -= batata.getVelocidade(); break;
                    }
                    return true;
                }
            }
            if (curBoss != null && curBoss.colideCom(jogador)) {
                if(!batata.isInvulnerable()){
                    efeito.playSong("assets/Ouch.wav", false);
                    batata.takeDamage(1);
                }
                // Reverte o movimento
                switch (batata.getUltimaDirecao()) {
                    case LEFT: batata.x += batata.getVelocidade(); break;
                    case RIGHT: batata.x -= batata.getVelocidade(); break;
                    case UP: batata.y += batata.getVelocidade(); break;
                    case DOWN: batata.y -= batata.getVelocidade(); break;
                }
                return true;
            }
        } else if(entity.layer == ObjetoColidivel.CollisionLayer.PROJECTILE){
            for (Parede parede : paredes) {
                if (entity.colideCom(parede)) return true;
            }
        }
        return false;
    }

    public class GameKeyAdapter extends KeyAdapter {
        private final Set<Integer> activeMovementKeys = new HashSet<>();
        private final Set<Integer> activeShootingKeys = new HashSet<>();
        private Direction ultimaDirecaoMovimento = Direction.RIGHT;
        private Direction ultimaDirecaoTiro = null;
        private long ultimoTiro = 0;
        private Timer continuousActionTimer;

        public GameKeyAdapter() {
            // Timer para processar ações de forma contínua (~60 FPS)
            continuousActionTimer = new Timer(16, e -> {
                processMovement();
                processShooting();
                updateAnimation();
            });
            continuousActionTimer.start();
        }

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

        private boolean isMovementKey(int code) {
            return code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT || 
                code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN;
        }

        private boolean isShootingKey(int code) {
            return code == KeyEvent.VK_A || code == KeyEvent.VK_D || 
                code == KeyEvent.VK_W || code == KeyEvent.VK_S;
        }

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
                if(bossNum < 5 && bossNum != 0){
                    save = bossNum + 1;
                    saveData();
                    returnToGame();
                }
                else if(bossNum == 5) loadBoss(6);
                else acessSecretRoom();
            }
        }

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
        
        // Método para parar o timer quando necessário
        public void dispose() {
            if (continuousActionTimer != null) {
                continuousActionTimer.stop();
            }
        }
    }

    private void acessSecretRoom(){
        saveCapuData(); // Deixa o jogo saber q o Cappuccino foi derrotado
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

    public void saveCapuData() {
        try {
            File saveFile = new File("save_data/cappuccino.dat");
            File saveDir = saveFile.getParentFile();
            
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(saveFile))) {
                writer.println(1);
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

    public void returnToGame(){
        musica.stopSong();
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

    public class BossManager {
        private ArrayList<BossLayout> bossDungeons;
        private int currentBoss;
        private int bossAmount;
        
        public BossManager() {
            bossDungeons = new ArrayList<>();
            bossAmount = 7;
            for(int i = 0; i < bossAmount; i++){
                bossDungeons.add(new BossLayout(i));
            }
            
            currentBoss = bossNum;
        }
        
        public BossLayout getCurrentBoss() {
            return bossDungeons.get(currentBoss);
        }

        
        public class BossLayout {
            int lay;
            public BossLayout(int lay){
                this.lay = lay;
            }
            
            void getParedes(){
                if(lay > 0 && lay != 6){
                    paredes.add(new Parede(0, 0, TAMANHO_BLOCO*9, ALTURA_TELA));
                    paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*9, 0, TAMANHO_BLOCO*9, ALTURA_TELA));
                }
                switch(lay){
                    case 1:
                        paredes.add(new Parede(TAMANHO_BLOCO*9, TAMANHO_BLOCO*2, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(TAMANHO_BLOCO*9, TAMANHO_BLOCO*5, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(TAMANHO_BLOCO*9, TAMANHO_BLOCO*8, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(TAMANHO_BLOCO*9, TAMANHO_BLOCO*11, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(TAMANHO_BLOCO*9, TAMANHO_BLOCO*14, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, TAMANHO_BLOCO*2, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, TAMANHO_BLOCO*5, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, TAMANHO_BLOCO*8, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, TAMANHO_BLOCO*11, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, TAMANHO_BLOCO*14, TAMANHO_BLOCO, TAMANHO_BLOCO));
                    break;
                    case 2:
                        paredes.add(new Parede(TAMANHO_BLOCO*9, 0, TAMANHO_BLOCO, ALTURA_TELA));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, 0, TAMANHO_BLOCO, ALTURA_TELA));
                    break;
                    case 3:
                        paredes.add(new Parede(TAMANHO_BLOCO*9, 0, TAMANHO_BLOCO, ALTURA_TELA));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, 0, TAMANHO_BLOCO, ALTURA_TELA));
                    break;
                    case 4:
                        paredes.add(new Parede(TAMANHO_BLOCO*9, 0, TAMANHO_BLOCO, TAMANHO_BLOCO*6));
                        paredes.add(new Parede(TAMANHO_BLOCO*9, TAMANHO_BLOCO*8, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(TAMANHO_BLOCO*9, TAMANHO_BLOCO*11, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(TAMANHO_BLOCO*9, TAMANHO_BLOCO*14, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, 0, TAMANHO_BLOCO, TAMANHO_BLOCO*6));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, TAMANHO_BLOCO*8, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, TAMANHO_BLOCO*11, TAMANHO_BLOCO, TAMANHO_BLOCO));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, TAMANHO_BLOCO*14, TAMANHO_BLOCO, TAMANHO_BLOCO));
                    break;
                    case 5:
                        paredes.add(new Parede(TAMANHO_BLOCO*9, 0, TAMANHO_BLOCO, ALTURA_TELA));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*10, 0, TAMANHO_BLOCO, ALTURA_TELA));
                    break;
                    case 6:
                        paredes.add(new Parede(0, 0, TAMANHO_BLOCO*2, ALTURA_TELA));
                        paredes.add(new Parede(LARGURA_TELA - TAMANHO_BLOCO*2, 0, TAMANHO_BLOCO*9, ALTURA_TELA));
                    break;
                }
                for (Parede paredes : paredes) objetosColidiveis.add(paredes);
            }

            void getBoss(){
                switch(lay){
                    case 0: curBoss = new Cappuccino(); break;
                    case 1: curBoss = new GigaSlime(); break;
                    case 2: curBoss = new SirPlatoh(); break;
                    case 3: curBoss = new MofadaBombada(); break;
                    case 4: curBoss = new CerberoNimbus(); break;
                    case 5: curBoss = new Larry(); break;
                    case 6: curBoss = new NaveMaeranha(); break;
                }
                objetosColidiveis.add(curBoss);
            }
        }
    }

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
        pauseButton.removeActionListener(pauseButton.getActionListeners()[0]);
        remove(pauseButton);
        
        // Liberar listas
        softClean();
        objetosColidiveis.clear();

        // Liberar imagens
        cleanImgArray(backgroundImgs); alertImage = null; pofImage = null; paredeImg = null; iconPause = null;
        cleanImgArray(batataImgs); cleanImgArray(cenouraImgs); cleanImgArray(portaImgs); cleanImgArray(gigaSlimeImgs); lancaChamasImg = null;
        cleanImgArray(sirPlatohImgs); garfoImg = null; cleanImgArray(facaImgs); cleanImgArray(mofadaBombadaImgs); bracoImg = null;
        luvaImg = null; mofoImg = null; cleanImgArray(cerberoNimbusImgs); algodaoImg = null; cleanImgArray(larryImgs);
        teiaImg = null; cleanImgArray(fioImgs); naveMareanhaImg = null; vladmirCenoura = null; cleanImgArray(cappuccinoImgs);

        // Otimização final: Garbage Colector
        System.gc();
    }

    private void cleanImgArray(Image[] imgs){
        if (imgs != null) {
            for (int i = 0; i < imgs.length; i++) imgs[i] = null;
            imgs = null;
        }
    }

    private void cleanKeyListeners() {
        for (KeyListener kl : getKeyListeners()) {
            removeKeyListener(kl);
        }
    }

    private void resetKeyState() {
        // Garante que todas as teclas são liberadas
        KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
    }

    private void softClean() {
        paredes.clear();
        cenouras.clear();
        alertas.clear();
        pofs.clear();
        curBoss = null;
    }
}
