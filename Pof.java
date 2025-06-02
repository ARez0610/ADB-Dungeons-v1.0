public class Pof extends Particle {
    private long pofTimer;

    public Pof(int x, int y, int tamanho) {
        super(x, y, tamanho);
        this.pofTimer = System.currentTimeMillis();
    }

    public boolean timeOut() {
        return System.currentTimeMillis() - pofTimer >= 300;
    }
}