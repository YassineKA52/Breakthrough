import java.util.ArrayList;

class Test {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  TEST - LOG320 Breakthrough");
        System.out.println("  MinMax, Alpha-Beta, Évaluation statique");
        System.out.println("==============================================\n");
        testComparaisonMinMaxAlphaBeta();
        testEvaluationStatique();
    }

    // =============================================
    // Test : Comparaison MinMax vs Alpha-Beta
    //          (même profondeur pour une comparaison juste)
    // =============================================
    static void testComparaisonMinMaxAlphaBeta() {
        System.out.println("----------------------------------------------");
        System.out.println("TEST  : Comparaison MinMax vs Alpha-Beta");
        System.out.println("----------------------------------------------");

        Board board = new Board();
        int depth = 4; // Même profondeur pour les deux

        // MinMax 
        CPUPlayer cpuMM = new CPUPlayer(Mark.RED);
        long startMM = System.currentTimeMillis();
        ArrayList<Move> movesMM = cpuMM.getNextMoveMinMax(board);
        long elapsedMM = System.currentTimeMillis() - startMM;

        // Alpha-Beta 
        CPUPlayer cpuAB = new CPUPlayer(Mark.RED);
        long startAB = System.currentTimeMillis();
        ArrayList<Move> movesAB = cpuAB.getNextMoveABFixedDepth(board, depth);
        long elapsedAB = System.currentTimeMillis() - startAB;

        System.out.println("MinMax (profondeur " + depth + ") :");
        System.out.println("  Noeuds   : " + cpuMM.getNumOfExploredNodes());
        System.out.println("  Temps    : " + elapsedMM + " ms");

        System.out.println("Alpha-Beta (profondeur " + depth + ") :");
        System.out.println("  Noeuds   : " + cpuAB.getNumOfExploredNodes());
        System.out.println("  Temps    : " + elapsedAB + " ms\n");
    }

    // =============================================
    // Test : Évaluation statique sur plusieurs plateaux
    //        (appel direct à board.evaluate(), sans recherche)
    // =============================================
    static void testEvaluationStatique() {
        System.out.println("----------------------------------------------");
        System.out.println("TEST  : Évaluation statique (board.evaluate)");
        System.out.println("----------------------------------------------\n");

        // --- Scénario 1 : Plateau initial (symétrique) ---
        System.out.println("Scénario 1 : Plateau initial (symétrique)");
        Board board1 = new Board();
        System.out.println(board1);
        int score1 = board1.evaluate(Mark.RED);
        System.out.println("evaluate(RED) = " + score1);
        System.out.println("Attendu : 0 (position égale)\n");

        // --- Scénario 2 : Rouge a un avantage matériel (plus de pièces) ---
        System.out.println("Scénario 2 : Rouge a plus de pièces (3 rouges vs 1 noir)");
        Board board2 = new Board();
        clearBoard(board2);
        setMark(board2, 5, 2, Mark.RED);    // C3
        setMark(board2, 5, 3, Mark.RED);    // D3
        setMark(board2, 5, 4, Mark.RED);    // E3
        setMark(board2, 2, 3, Mark.BLACK);  // D6
        System.out.println(board2);
        int score2 = board2.evaluate(Mark.RED);
        System.out.println("evaluate(RED) = " + score2);
        System.out.println("Attendu : positif (avantage matériel rouge)\n");

        // --- Scénario 3 : Rouge a des pièces plus avancées ---
        System.out.println("Scénario 3 : Rouge avancé vs Noir reculé (1 pièce chacun)");
        Board board3 = new Board();
        clearBoard(board3);
        setMark(board3, 2, 3, Mark.RED);    // D6 (rouge avancé, row 2 = 5 cases d'avancement)
        setMark(board3, 2, 0, Mark.BLACK);  // A6 (noir peu avancé, row 2 = 2 cases d'avancement)
        System.out.println(board3);
        int score3 = board3.evaluate(Mark.RED);
        System.out.println("evaluate(RED) = " + score3);
        System.out.println("Attendu : positif (rouge plus avancé que noir)\n");

        // --- Scénario 4 : Victoire Rouge (pion sur la rangée 8) ---
        System.out.println("Scénario 4 : Victoire Rouge (pion sur la rangée 8)");
        Board board4 = new Board();
        clearBoard(board4);
        setMark(board4, 0, 3, Mark.RED);    // D8 (rangée de victoire pour rouge)
        setMark(board4, 7, 0, Mark.BLACK);  // A1
        System.out.println(board4);
        int score4 = board4.evaluate(Mark.RED);
        System.out.println("evaluate(RED) = " + score4);
        System.out.println("Attendu : 10000 (victoire rouge)\n");

        // --- Scénario 5 : Victoire Noir (pion sur la rangée 1) ---
        System.out.println("Scénario 5 : Victoire Noir (pion sur la rangée 1)");
        Board board5 = new Board();
        clearBoard(board5);
        setMark(board5, 1, 3, Mark.RED);    // D7
        setMark(board5, 7, 4, Mark.BLACK);  // E1 (rangée de victoire pour noir)
        System.out.println(board5);
        int score5 = board5.evaluate(Mark.RED);
        System.out.println("evaluate(RED) = " + score5);
        System.out.println("Attendu : -10000 (victoire noir)\n");
    }

    // =============================================
    // Méthodes utilitaires
    // =============================================

    static boolean isValidMove(Board board, Move move, Mark mark) {
        ArrayList<Move> possibleMoves = board.getPossibleMoves(mark);
        for (Move m : possibleMoves) {
            if (m.getFromRow() == move.getFromRow() && m.getFromCol() == move.getFromCol()
                    && m.getToRow() == move.getToRow() && m.getToCol() == move.getToCol()) {
                return true;
            }
        }
        return false;
    }

    static String movesToString(ArrayList<Move> moves) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moves.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(moves.get(i));
        }
        return sb.toString();
    }

    static void clearBoard(Board board) {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (board.getMark(r, c) != Mark.EMPTY) {
                    Move fake = new Move(r, c, r, c);
                    board.play(fake, Mark.EMPTY);
                }
            }
        }
    }

    static void setMark(Board board, int row, int col, Mark mark) {
        Move fake = new Move(row, col, row, col);
        board.play(fake, mark);
    }
}