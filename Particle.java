public abstract class Particle {
    protected int x;
    protected int y;
    protected int tamanho;
    protected boolean isAtivo;

    public Particle(int x, int y, int tamanho){
        this.x = x;
        this.y = y;
        this.tamanho = tamanho;
        this.isAtivo = true;
    }

    public void desativar(){ this.isAtivo = false; }
    public boolean isAtivo(){ return isAtivo;}
    public int getX(){ return x; }
    public int getY(){ return y; }
    public int getTamanho(){ return tamanho; }
}
