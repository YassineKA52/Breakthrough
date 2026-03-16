class Move
{
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;

    public Move(){
        fromRow = -1;
        fromCol = -1;
        toRow = -1;
        toCol = -1;
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol){
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
    }

    public int getFromRow(){
        return fromRow;
    }

    public int getFromCol(){
        return fromCol;
    }

    public int getToRow(){
        return toRow;
    }

    public int getToCol(){
        return toCol;
    }

    public void setFromRow(int r){
        fromRow = r;
    }

    public void setFromCol(int c){
        fromCol = c;
    }

    public void setToRow(int r){
        toRow = r;
    }

    public void setToCol(int c){
        toCol = c;
    }



    public int getRow(){
        return toRow;
    }
    public int getCol(){
        return toCol;
    }
    public void setRow(int r){
        toRow = r;
    }
    public void setCol(int c){
        toCol = c;
    }

    @Override
    public String toString() {
        char fc = (char) ('A' + fromCol);
        int fr = 8 - fromRow;
        char tc = (char) ('A' + toCol);
        int tr = 8 - toRow;
        return "" + fc + fr + "-" + tc + tr;
    }

    public static Move fromString(String s) {
        if (s == null) {
            return null;
        }

        s = s.trim().toUpperCase().replace(" ", "").replace("-", "");

        if (s.length() != 4) {
            return null;
        }

        try {
            char fc = s.charAt(0);
            int fr = 8 - Character.getNumericValue(s.charAt(1));
            char tc = s.charAt(2);
            int tr = 8 - Character.getNumericValue(s.charAt(3));

            if (fc < 'A' || fc > 'H' || tc < 'A' || tc > 'H' || fr < 0 || fr > 7 || tr < 0 || tr > 7) {
                return null;
            }

            return new Move(fr, fc - 'A', tr, tc - 'A');
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
}