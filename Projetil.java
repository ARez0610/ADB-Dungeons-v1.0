import java.awt.Color;

public class Projetil extends ObjetoColidivel{
        private int direcaoX, direcaoY;
        private boolean ativo = true;
        private boolean isCollidable;
        private static final int VELOCIDADE_PROJETIL = 5;

        public Projetil(int x, int y, int largura, int altura, int direcaoX, int direcaoY, Color cor, boolean isCollidable) {
            super(x, y, largura, altura, cor, ObjetoColidivel.CollisionLayer.PROJECTILE);
            this.direcaoX = direcaoX;
            this.direcaoY = direcaoY;
            this.isCollidable = isCollidable;
        }

        public void mover() {
            this.x += direcaoX * VELOCIDADE_PROJETIL;
            this.y += direcaoY * VELOCIDADE_PROJETIL;
            
            if (foraDaTela()) {
                desativar();
            }
        }
        
        public int getDirX(){ return direcaoX; }
        public int getDirY(){ return direcaoY; }
        public boolean isAtivo() { return ativo; }
        public void desativar() { this.ativo = false;}
        public boolean isCollidable(){ return isCollidable; }

        public void setDirX(int direcaoX){ this.direcaoX = direcaoX; }
        public void setDirY(int direcaoY){ this.direcaoY = direcaoY; }
    }