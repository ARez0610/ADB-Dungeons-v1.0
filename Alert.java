/** 
 * Classe da Partícula de alerta
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
*/
public class Alert extends Particle{
    /**
     * Construtor da classe Alert
     * 
     * @param x Posição x
     * @param y Posição y
     * @param tamanho Tamanho
     */
    public Alert(int x, int y, int tamanho){
        super(x, y, tamanho);
    }

    // Getters

    /**
     * @return Largura
     */
    public int getLargura(){ return tamanho/2; }


    /**
     * @return Altura
     */
    public int getAltura(){ return tamanho; }
}
