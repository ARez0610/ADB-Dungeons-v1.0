import java.awt.Color;

/** 
 * Classe para a construção dos projéteis do jogador (cenouras)
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
 */
public class CarrotFactory {
    /**
     * Cria uma nova cenoura
     * 
     * @param x Posição x
     * @param y Posição y
     * @param direction Direção
     * @return O projétil criado
     */
    public static Projetil createProjectile(int x, int y, Direction direction) {
        int dirX = 0, dirY = 0;
        Color color = Color.ORANGE;

        if(direction == Direction.UP){
            dirY = -2;
            return new Projetil(x, y, 16, 24, dirX, dirY, color, false);
        } else if(direction == Direction.DOWN){
            dirY = 2;
            return new Projetil(x, y, 16, 24, dirX, dirY, color, false);
        } else if(direction == Direction.LEFT){
            dirX = -2;
            return new Projetil(x, y, 24, 16, dirX, dirY, color, false);
        } else {
            dirX = 2;
            return new Projetil(x, y, 24, 16, dirX, dirY, color, false);
        }
    }
}