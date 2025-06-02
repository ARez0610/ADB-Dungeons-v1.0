public class Alert extends Particle{
    public Alert(int x, int y, int tamanho){
        super(x, y, tamanho);
    }

    public int getLargura(){ return tamanho/2; }
    public int getAltura(){ return tamanho; }
}
