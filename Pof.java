/** 
 * Classe da Partícula de pof! (mostrada ao derrotar um inimigo)
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
*/
public class Pof extends Particle {
    /** Timer de duração da partícula */
    private long pofTimer;

    /**
     * Construtor da classe Pof
     * 
     * @param x Posição x
     * @param y Posição y
     * @param tamanho Tamanho
     */
    public Pof(int x, int y, int tamanho) {
        super(x, y, tamanho);
        this.pofTimer = System.currentTimeMillis();
    }

    /**
     * Indica se o tempo de duração da partícula acabou
     * 
     * @return {@code true} caso o tempo tenha acabado
     */
    public boolean timeOut() {
        return System.currentTimeMillis() - pofTimer >= 300;
    }
}