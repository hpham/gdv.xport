package gdv.xport.satz.feld.common;

public enum TeildatensatzNummer {

    BEZUGSRECHTE(6), AUSZAHLUNGEN(7), ZUKUENFTIGE_SUMMENAENDERUNG(8), WERTUNGSSUMMEN(9);

    private int nummer = -1;

    private TeildatensatzNummer(int nummer) {
        this.nummer = nummer;
    }

    public int getNummer() {
        return nummer;
    }

    public void setNummer(int nummer) {
        this.nummer = nummer;
    }

}
