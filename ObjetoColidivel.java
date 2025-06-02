import java.awt.Color;
import java.awt.Image;

public abstract class ObjetoColidivel {
    public enum CollisionLayer {
        PLAYER,
        ENEMY,
        PROJECTILE,
        WALL,
        DOOR,
        LINE_OF_SIGHT
    }
    protected CollisionLayer layer;
    protected int x;
    protected int y;
    protected int largura;
    protected int altura;
    protected Color cor;
    protected Image curImage;
    private static final int LARGURA_TELA = 1300;
    private static final int ALTURA_TELA = 750;
    
    public ObjetoColidivel(int x, int y, int largura, int altura, Color cor, CollisionLayer layer) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.cor = cor;
        this.layer = layer;
    }
    
    // Método para verificar colisão entre dois objetos
    public boolean colideCom(ObjetoColidivel outro) {
        return x < outro.x + outro.largura &&
               x + largura > outro.x &&
               y < outro.y + outro.altura &&
               y + altura > outro.y;
    }

    public boolean shouldCollideWith(ObjetoColidivel other) {
        // Defina quais layers colidem entre si
        if (this.layer == CollisionLayer.PROJECTILE) {
            if(
                other.layer == CollisionLayer.ENEMY ||
                other.layer == CollisionLayer.WALL ||
                other.layer == CollisionLayer.PLAYER ||
                other.layer == CollisionLayer.PROJECTILE
            ) return true;
        }
        if (this.layer == CollisionLayer.PLAYER) {
            if(
                other.layer == CollisionLayer.ENEMY ||
                other.layer == CollisionLayer.WALL ||
                other.layer == CollisionLayer.DOOR ||
                other.layer == CollisionLayer.LINE_OF_SIGHT
            ) return true;
        }
        if(this.layer == CollisionLayer.ENEMY && other.layer == CollisionLayer.WALL) return true;
        
        return false;
    }

    public boolean foraDaTela() {
        return x < 0 || x > LARGURA_TELA - largura || y < 0 || y > ALTURA_TELA - altura;
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getLargura() { return largura; }
    public int getAltura() { return altura; }
    public Color getCor() { return cor; }
    public CollisionLayer getLayer() { return layer; }
    public Image getImage() { return curImage; }

    // Setters
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setImage(Image img) {this.curImage = img; }
}