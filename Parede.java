import java.awt.Color;

/** 
 * Classe para as paredes
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
*/
public class Parede extends ObjetoColidivel {
    /**
     * Construtor da classe parede
     * 
     * @param x Posição x
     * @param y Posição y
     * @param largura Largura
     * @param altura Altura
     */
    public Parede(int x, int y, int largura, int altura){
        super(x, y, largura, altura, Color.GRAY, ObjetoColidivel.CollisionLayer.WALL);
    }
}
