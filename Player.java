import java.awt.Color;

/** 
 * Classe para o jogador (Duque Batata)
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
 */
public class Player extends ObjetoColidivel {
    /** Última direção do jogador */
    private Direction ultimaDirecao;
    /** Tamanho do jogador */
    private static int tamanho = 50;
    /** Ponto de vida do jogador (o máximo é 5)*/
    private int hp;
    /** Indica se o jogador está no estado temporário de invunerbilidade*/
    private boolean inPain = false;
    /** Timer de duração da invunerabilidade */
    private long painTimer = 0;
    /** Duração da invunerabilidade (2 segundos)*/
    private static final long PAIN_DURATION = 2000;
    /** Velocidade de movimento do jogador */
    private static final int PLAYER_SPEED = 4;
        
    /**
     * Construtor da classe Player 
     * 
     * @param x Posição x
     * @param y Posição y
     */
    public Player(int x, int y){
        super(x, y, tamanho, tamanho, Color.YELLOW, ObjetoColidivel.CollisionLayer.PLAYER);
        this.ultimaDirecao = Direction.RIGHT;
        this.hp = 5;
    }

    /**
     * Atualiza o dano causado ao jogador e ativa o estado de invunerabilidade
     * 
     * @param damage o dano causado ao jogador
     */
    public void takeDamage(int damage){
        this.hp -= damage;
        this.inPain = true;
        this.painTimer = System.currentTimeMillis();
        this.cor = Color.RED;
    }

    /**
     * Atualiza a invunerabilidade do jogador, caso o tempo tiver acabado.
     */
    public void updateInvulnerability() {
        if (inPain && System.currentTimeMillis() - painTimer >= PAIN_DURATION){
            inPain = false;
            this.cor = Color.YELLOW;
        }
    }
        

    /**
     * Atualiza a direção do jogador
     * 
     * @param direcao A nova direção
     */
    public void setUltimaDirecao(Direction direcao) {this.ultimaDirecao = direcao;}


    // Getters

    /**
     * @return se o jogador está invunerável
     */
    public boolean isInvulnerable() { return inPain; }

    /**
     * @return Pontos de vida
     */
    public int getHp() { return hp; }

    /**
     * @return Última direção
     */
    public Direction getUltimaDirecao(){ return ultimaDirecao;}

    /**
     * @return Velocidade
     */
    public int getVelocidade() { return PLAYER_SPEED; }
}