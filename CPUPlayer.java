import java.util.ArrayList;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class CPUPlayer {

    // Contient le nombre de noeuds visités (le nombre
    // d'appel à la fonction MinMax ou Alpha Beta)
    // Normalement, la variable devrait être incrémentée
    // au début de votre MinMax ou Alpha Beta.
    private int numExploredNodes;

    // Le joueur MAX 
    private Mark cpuMark;
    // Le joueur MIN 
    private Mark opponentMark;

    // Le constructeur reçoit en paramètre le
    // joueur MAX (X ou O)
    public CPUPlayer(Mark cpu) {
        this.cpuMark = cpu;
        this.opponentMark = Board.getOpponent(cpu);
    }

    // Ne pas changer cette méthode
    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    // Retourne la liste des coups possibles. Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveMinMax(Board board) {
        numExploredNodes = 0;

        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;

        ArrayList<Move> possibleMoves = board.getPossibleMoves();

        for (Move move : possibleMoves) {
            board.play(move, cpuMark);
            int score = minMax(board, false);
            board.unplay(move);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        return bestMoves;
    }

    private int minMax(Board board, boolean isMaximizing) {
        numExploredNodes++;

        if (board.isGameOver()) {
            return board.evaluate(cpuMark);
        }

        ArrayList<Move> possibleMoves = board.getPossibleMoves();

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (Move move : possibleMoves) {
                board.play(move, cpuMark);
                int score = minMax(board, false);
                board.unplay(move);
                bestScore = Math.max(bestScore, score);
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (Move move : possibleMoves) {
                board.play(move, opponentMark);
                int score = minMax(board, true);
                board.unplay(move);
                bestScore = Math.min(bestScore, score);
            }
            return bestScore;
        }
    }

    // Retourne la liste des coups possibles. Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;

        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        ArrayList<Move> possibleMoves = board.getPossibleMoves();

        for (Move move : possibleMoves) {
            board.play(move, cpuMark);
            int score = alphaBeta(board, false, alpha, beta);
            board.unplay(move);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }

            alpha = Math.max(alpha, bestScore);
        }

        return bestMoves;
    }

    private int alphaBeta(Board board, boolean isMaximizing, int alpha, int beta) {
        numExploredNodes++;

        if (board.isGameOver()) {
            return board.evaluate(cpuMark);
        }

        ArrayList<Move> possibleMoves = board.getPossibleMoves();

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (Move move : possibleMoves) {
                board.play(move, cpuMark);
                int score = alphaBeta(board, false, alpha, beta);
                board.unplay(move);
                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, bestScore);

                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        } else {

            int bestScore = Integer.MAX_VALUE;
            for (Move move : possibleMoves) {
                board.play(move, opponentMark);
                int score = alphaBeta(board, true, alpha, beta);
                board.unplay(move);
                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, bestScore);

                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        }
    }
}