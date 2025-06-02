import java.awt.Color;

public class Parede extends ObjetoColidivel {
    public Parede(int x, int y, int largura, int altura){
        super(x, y, largura, altura, Color.GRAY, ObjetoColidivel.CollisionLayer.WALL);
    }
}
