import java.awt.Color;

/** 
 * Classe para a porta
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
 */
public class Porta extends ObjetoColidivel {
    /** Indica se a porta está aberta */
    private boolean aberta = false;
    
    /**
     * Construtor da classe Porta
     * 
     * @param x Posição x
     * @param y Posição y
     * @param largura Largura
     * @param altura Altura
     */
    public Porta(int x, int y, int largura, int altura) {
        super(x, y, largura, altura, new Color(139, 69, 19), CollisionLayer.DOOR);
    }
    
    /**
     * Abre a porta
     */
    public void abrir() {
        this.aberta = true;
        this.cor = Color.WHITE;
    }

    /**
     * Fecha a porta
     */
    public void fechar(){
        this.aberta = false;
        this.cor = new Color(139, 69, 19);
    }
    
    // Getters

    /**
     * @return Se a porta está aberta
     */
    public boolean isAberta() { return aberta; }

    /**
     * @return Cor
     */
    public Color getCor() { return cor; }
}