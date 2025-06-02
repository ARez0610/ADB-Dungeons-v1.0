import java.awt.Color;

public class Porta extends ObjetoColidivel {
    private boolean aberta = false;
    
    public Porta(int x, int y, int largura, int altura) {
        super(x, y, largura, altura, new Color(139, 69, 19), CollisionLayer.DOOR);
    }
    
    public void abrir() {
        this.aberta = true;
        this.cor = Color.WHITE;
    }

    public void fechar(){
        this.aberta = false;
        this.cor = new Color(139, 69, 19);
    }
    
    public boolean isAberta() { return aberta; }
    public Color getCor() { return cor; }
}