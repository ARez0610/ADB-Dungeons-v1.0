import java.awt.Color;

public class Player extends ObjetoColidivel {
        private Direction ultimaDirecao;
        private static int tamanhoBloco = 50;
        private int hp;
        private boolean inPain = false; // Estado temporário de invunerbilidade
        private long painTimer = 0;
        private static final long PAIN_DURATION = 2000;
        private static final int PLAYER_SPEED = 4;
        
        public Player(int x, int y){
            super(x, y, tamanhoBloco, tamanhoBloco, Color.YELLOW, ObjetoColidivel.CollisionLayer.PLAYER);
            this.ultimaDirecao = Direction.RIGHT;
            this.hp = 5;
        }

        public void takeDamage(int damage){
            this.hp -= damage;
            this.inPain = true;
            this.painTimer = System.currentTimeMillis();
            this.cor = Color.RED;
        }

        public void updateInvulnerability() {
            if (inPain && System.currentTimeMillis() - painTimer >= PAIN_DURATION){
                inPain = false;
                this.cor = Color.YELLOW;
            }
        }
        
        public boolean isInvulnerable() { return inPain; }
        public int getHp() { return hp; }
        public Direction getUltimaDirecao(){ return ultimaDirecao;}
        public int getVelocidade() { return PLAYER_SPEED; }
        public void setUltimaDirecao(Direction direcao) {this.ultimaDirecao = direcao;}
    }