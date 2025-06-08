import java.awt.Color;

/** 
 * Classe para os projéteis e outros ataques inimigos
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
 */
public class Projetil extends ObjetoColidivel{
    /** Direção horizontal do projétil */
    private int direcaoX;
    /** Direção verical do projétil */
    private int direcaoY;
    /** Indica se o projétil está ativo */
    private boolean ativo = true;
    /** Indica se ele é colidível com projéteis do jogador ou não */
    private boolean isCollidable;
    /** Velocidade padrão do projétil */
    private static final int VELOCIDADE_PROJETIL = 5;

    /**
     * Construtor da classe projétil
     * 
     * @param x Posição x
     * @param y Posição y
     * @param largura Largura
     * @param altura Altura
     * @param direcaoX Direção horizontal
     * @param direcaoY Direção vertical
     * @param cor Cor
     * @param isCollidable Indica se ele é colidível com projéteis do jogador ou não
     */
    public Projetil(int x, int y, int largura, int altura, int direcaoX, int direcaoY, Color cor, boolean isCollidable) {
        super(x, y, largura, altura, cor, ObjetoColidivel.CollisionLayer.PROJECTILE);
        this.direcaoX = direcaoX;
        this.direcaoY = direcaoY;
        this.isCollidable = isCollidable;
    }

    /**
     * Move o projétil
     */
    public void mover() {
        this.x += direcaoX * VELOCIDADE_PROJETIL;
        this.y += direcaoY * VELOCIDADE_PROJETIL;
            
        if (foraDaTela()) {
            desativar();
        }
    }
    
    // Getters

    /**
     * @return Direção horizontal
     */
    public int getDirX(){ return direcaoX; }

    /**
     * @return Direção vertical
     */
    public int getDirY(){ return direcaoY; }

    /**
     * @return Se está ativo
     */
    public boolean isAtivo() { return ativo; }

    /**
     * @return Se ele é colidível com projéteis do jogador
     */
    public boolean isCollidable(){ return isCollidable; }

    // Setters

    /**
     * Desativa o projétil
     */
    public void desativar() { this.ativo = false;}

    /**
     * Atualiza a direção horizontal
     * 
     * @param direcaoX a nova direção
     */
    public void setDirX(int direcaoX){ this.direcaoX = direcaoX; }

    /**
     * Atualiza a direção vertical
     * 
     * @param direcaoY a nova direção
     */
    public void setDirY(int direcaoY){ this.direcaoY = direcaoY; }
}