/**
 * Classe base abstrata para todas as partículas do jogo. Fornece as funcionalidades comuns entre elas
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
 */
public abstract class Particle {
    /** Posição x da partícula */
    protected int x;
    /** Posição y da partícula */
    protected int y;
    /** Tamanho da partícula */
    protected int tamanho;
    /** Indica se a partícula ainda está ativa */
    protected boolean isAtivo;

    /**
     * Construtor da classe Particle
     * 
     * @param x Posição x da partícula
     * @param y Posição y da partícula
     * @param tamanho Tamanho da partícula
     */
    public Particle(int x, int y, int tamanho){
        this.x = x;
        this.y = y;
        this.tamanho = tamanho;
        this.isAtivo = true;
    }

    /** Desativa a partícula */
    public void desativar(){ this.isAtivo = false; }

    // Getters

    /**
     * @return se está ativo ou não
     */
    public boolean isAtivo(){ return isAtivo;}

    /**
     * @return Posição x
     */
    public int getX(){ return x; }

    /**
     * @return Posição y
     */
    public int getY(){ return y; }

    /**
     * @return Se está ativo ou não
     */
    public int getTamanho(){ return tamanho; }
}
