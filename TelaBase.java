import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Classe base abstrata para todas as telas do jogo. Fornece as funcionalidades comuns entre as telas
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
 */
public abstract class TelaBase extends JPanel implements ActionListener {
    /** Largura padrão da tela (em pixels) */
    protected static final int LARGURA_TELA = 1300;
    /** Altura padrão da tela (em pixels) */
    protected static final int ALTURA_TELA = 750;
    /** Tamanho padrão (em pixels) para objetos colidíveis */
    protected static final int TAMANHO_BLOCO = 50;
    /** Intervalo de tempo (em milissegundos) usado pelo timer do jogo */
    protected static final int INTERVALO = 10;
    /** Fonte padrão usada na maioria dos textos do jogo */
    protected static final String NOME_FONTE = "Papyrus";
    
    /**
     * Representa os possíveis estados do jogo
     */
    protected enum EstadoJogo {
        /** Jogo em execução normal */
        RODANDO, 
        /** Jogo pausado (execução suspensa) */
        PAUSADO, 
        /** Jogo parado (inativo) */
        PARADO
    }
    
    /** Estado atual do jogo (inicialmente PARADO) */
    protected EstadoJogo estado = EstadoJogo.PARADO;
    /** Reprodutor de música de fundo */
    protected MusicPlayer musica;
    /** Reprodutor de efeitos sonoros */
    protected MusicPlayer efeito = new MusicPlayer();
    /** Slot de save atual (determina qual mundo será carregado) */
    protected int save = 1;
    /** Timer principal para atualização do jogo */
    Timer timer;

    /**
     * Construtor da tela base
     * 
     * @param musica Player de música compartilhado entre telas
     */
    TelaBase(MusicPlayer musica){
        this.musica = musica;

        setPreferredSize(new Dimension(LARGURA_TELA, ALTURA_TELA));
        setBackground(Color.BLACK);
        setFocusable(true);

        carregarImagens();
    }

    /**
     * Salva o valor atual de {@code save} no arquivo de save
     */
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

    /**
     * Inicia o timer e muda o estado do jogo para RODANDO
     */
    public void start(){
        estado = EstadoJogo.RODANDO;
        timer = new Timer(INTERVALO, this);
        timer.start();
    }

    /**
     * Carrega os recursos gráficos necessários para a tela.
     * Deve ser implementado por subclasses para carregar imagens específicas.
     */
    public abstract void carregarImagens();

    /**
     * Método de renderização do Swing. Chama {@code desenharTela()} após
     * a preparação básica do componente.
     * 
     * @param g Contexto gráfico para renderização
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        desenharTela(g);
    }

    /**
     * Renderiza os elementos visuais específicos da tela.
     * Deve ser implementado por subclasses para definir a renderização personalizada.
     * 
     * @param g Contexto gráfico para renderização
     */
    public abstract void desenharTela(Graphics g);

    /**
     * Manipula eventos de ação, principalmente do timer, solicitando
     * repintura do componente a cada intervalo definido.
     * 
     * @param e Evento de ação disparado
     */
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    /**
     * Realiza limpeza de recursos antes da tela ser descartada.
     * Deve ser implementado por subclasses para liberar recursos específicos
     */
    public abstract void cleanUp();
}
