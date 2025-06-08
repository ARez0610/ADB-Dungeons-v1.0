import java.awt.Color;
import java.awt.Image;

/**
 * Classe base abstrata para todos os objetos colidíveis do jogo. Fornece as funcionalidades comuns entre eles
 * 
 * @author Arthur dos Santos Rezende
 * @version 1.0
 */
public abstract class ObjetoColidivel {
    /**
     * Representa os diferentes tipos de objetos colidíveis
     */
    public enum CollisionLayer {
        /** Jogador */
        PLAYER,
        /** Inimigos e bosses */
        ENEMY,
        /** Projéteis e outros ataques inimigos */
        PROJECTILE,
        /** Paredes */
        WALL,
        /** Porta */
        DOOR,
        /** Campo de visão */
        LINE_OF_SIGHT
    }
    /** Camanda de colisão do objeto */
    protected CollisionLayer layer;
    /** Posição x do objeto */
    protected int x;
    /** Posição y do objeto */
    protected int y;
    /** Largura do objeto */
    protected int largura;
    /** Largura do objeto */
    protected int altura;
    /** Cor do objeto (caso sua imagem não carregue) */
    protected Color cor;
    /** Imagem atual do objeto */
    protected Image curImage;
    /** Largura da tela */
    private static final int LARGURA_TELA = 1300;
    /** Altura da tela */
    private static final int ALTURA_TELA = 750;
    
    /**
     * Construtor da classe ObjetoColidivel
     * 
     * @param x Posição x
     * @param y Posição y
     * @param largura Largura
     * @param altura Altura
     * @param cor Cor
     * @param layer Camada de colisão
     */
    public ObjetoColidivel(int x, int y, int largura, int altura, Color cor, CollisionLayer layer) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.cor = cor;
        this.layer = layer;
    }
    
    /**
     * Método para verificar colisão entre dois objetos
     * 
     * @param outro O objeto o qual o objeto chamando a função colidiu
     * @return {@code true} se a colisão ocorreu. Caso contrário, {@code false}
     */
    public boolean colideCom(ObjetoColidivel outro) {
        return x < outro.x + outro.largura &&
               x + largura > outro.x &&
               y < outro.y + outro.altura &&
               y + altura > outro.y;
    }

    /**
     * Relevante apenas para a colisão de projéteis. Define quais layers podem colidir com o layer {@code PROJECTILE}
     * 
     * @param other O objeto o qual o projétil chamando a função colidiu
     * @return {@code true} se a colisão pode ocorrer. Caso contrário, {@code false}
     */
    public boolean shouldCollideWith(ObjetoColidivel other) {
        if (this.layer == CollisionLayer.PROJECTILE) {
            if(
                other.layer == CollisionLayer.ENEMY ||
                other.layer == CollisionLayer.WALL ||
                other.layer == CollisionLayer.PLAYER ||
                other.layer == CollisionLayer.PROJECTILE
            ) return true;
        }
        return false;
    }

    /**
     * Verifica se o objeto está fora da tela
     * 
     * @return {@code true} se ele está, de fato, fora da tela. Caso contrário, {@code false}
     */
    public boolean foraDaTela() {
        return x < 0 || x > LARGURA_TELA - largura || y < 0 || y > ALTURA_TELA - altura;
    }
    
    // Getters

    /**
     * @return Posição x
     */
    public int getX() { return x; }

    /**
     * @return Posição y
     */
    public int getY() { return y; }

    /**
     * @return Largura
     */
    public int getLargura() { return largura; }

    /**
     * @return Altura
     */
    public int getAltura() { return altura; }

    /**
     * @return Cor
     */
    public Color getCor() { return cor; }

    /**
     * @return Camada de colisão
     */
    public CollisionLayer getLayer() { return layer; }

    /**
     * @return Imagem atual
     */
    public Image getImage() { return curImage; }

    // Setters

    /**
     * Atualiza a posição x
     * 
     * @param x a nova posição x
     */
    public void setX(int x) { this.x = x; }

    /**
     * Atualiza a posição y
     * 
     * @param y a nova posição y
     */
    public void setY(int y) { this.y = y; }

    /**
     * Atualiza a imagem
     * 
     * @param img a nova imagem
     */
    public void setImage(Image img) {this.curImage = img; }
}