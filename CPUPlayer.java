import java.util.ArrayList;

class CPUPlayer {

    private int numExploredNodes;
    private Mark cpuMark;
    private Mark opponentMark;

    // 3 secondes MAX pour être safe (serveur = 5s)
    private static final long TIME_LIMIT_MS = 3000;
    private static final int MAX_DEPTH_LIMIT = 30;
    private static final int TIME_CHECK_INTERVAL = 2048;

    private long startTime;
    private boolean timeUp;

    public CPUPlayer(Mark cpu) {
        this.cpuMark = cpu;
        this.opponentMark = Board.getOpponent(cpu);
    }

    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    // =============================================
    // MinMax avec limite de profondeur
    // =============================================
    public ArrayList<Move> getNextMoveMinMax(Board board) {
        numExploredNodes = 0;
        int maxDepth = 4;

        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;

        ArrayList<Move> possibleMoves = board.getPossibleMoves(cpuMark);

        for (Move move : possibleMoves) {
            Mark captured = board.getMark(move.getToRow(), move.getToCol());
            board.play(move, cpuMark);
            int score = minMax(board, maxDepth - 1, false);
            board.unplay(move, cpuMark, captured);

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

    private int minMax(Board board, int depth, boolean isMaximizing) {
        numExploredNodes++;

        if (depth == 0 || board.isGameOver()) {
            return board.evaluate(cpuMark);
        }

        if (isMaximizing) {
            ArrayList<Move> possibleMoves = board.getPossibleMoves(cpuMark);
            int bestScore = Integer.MIN_VALUE;
            for (Move move : possibleMoves) {
                Mark captured = board.getMark(move.getToRow(), move.getToCol());
                board.play(move, cpuMark);
                int score = minMax(board, depth - 1, false);
                board.unplay(move, cpuMark, captured);
                bestScore = Math.max(bestScore, score);
            }
            return bestScore;
        } else {
            ArrayList<Move> possibleMoves = board.getPossibleMoves(opponentMark);
            int bestScore = Integer.MAX_VALUE;
            for (Move move : possibleMoves) {
                Mark captured = board.getMark(move.getToRow(), move.getToCol());
                board.play(move, opponentMark);
                int score = minMax(board, depth - 1, true);
                board.unplay(move, opponentMark, captured);
                bestScore = Math.min(bestScore, score);
            }
            return bestScore;
        }
    }

    // Alpha-Beta à profondeur fixe 
    public ArrayList<Move> getNextMoveABFixedDepth(Board board, int maxDepth) {
        numExploredNodes = 0;
        timeUp = false;

        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        ArrayList<Move> possibleMoves = board.getPossibleMoves(cpuMark);

        for (Move move : possibleMoves) {
            Mark captured = board.getMark(move.getToRow(), move.getToCol());
            board.play(move, cpuMark);
            int score = alphaBeta(board, maxDepth - 1, false, alpha, beta);
            board.unplay(move, cpuMark, captured);

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

    // =============================================
    // Alpha-Beta avec Iterative Deepening
    // =============================================
    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;
        startTime = System.currentTimeMillis();
        timeUp = false;

        ArrayList<Move> possibleMoves = board.getPossibleMoves(cpuMark);

        if (possibleMoves.isEmpty()) {
            return new ArrayList<>();
        }

        if (possibleMoves.size() == 1) {
            ArrayList<Move> result = new ArrayList<>();
            result.add(possibleMoves.get(0));
            return result;
        }

        // Tri initial au niveau racine
        sortMovesRoot(possibleMoves, board);

        ArrayList<Move> bestMoves = new ArrayList<>();
        bestMoves.add(possibleMoves.get(0));

        for (int depth = 1; depth <= MAX_DEPTH_LIMIT; depth++) {
            ArrayList<Move> currentBestMoves = new ArrayList<>();
            int currentBestScore = Integer.MIN_VALUE;
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;
            boolean completedDepth = true;

            for (int i = 0; i < possibleMoves.size(); i++) {
                Move move = possibleMoves.get(i);

                if (isTimeUp()) {
                    completedDepth = false;
                    break;
                }

                Mark captured = board.getMark(move.getToRow(), move.getToCol());
                board.play(move, cpuMark);
                int score = alphaBeta(board, depth, false, alpha, beta);
                board.unplay(move, cpuMark, captured);

                if (timeUp) {
                    completedDepth = false;
                    break;
                }

                if (score > currentBestScore) {
                    currentBestScore = score;
                    currentBestMoves.clear();
                    currentBestMoves.add(move);
                } else if (score == currentBestScore) {
                    currentBestMoves.add(move);
                }

                alpha = Math.max(alpha, currentBestScore);

                if (currentBestScore >= 10000) {
                    System.out.println("  Victoire trouvee a profondeur " + depth);
                    return currentBestMoves;
                }
            }

            if (completedDepth && !currentBestMoves.isEmpty()) {
                bestMoves = currentBestMoves;

                Move bestMove = currentBestMoves.get(0);
                possibleMoves.remove(bestMove);
                possibleMoves.add(0, bestMove);

                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println("  Profondeur " + depth + " en " + elapsed + "ms, score=" + currentBestScore +
                        ", coup=" + bestMoves.get(0) + " (" + numExploredNodes + " noeuds)");
            }

            if (timeUp) break;

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > TIME_LIMIT_MS / 2) {
                System.out.println("  Arret: " + elapsed + "ms, profondeur max = " + depth);
                break;
            }
        }

        return bestMoves;
    }

    private int alphaBeta(Board board, int depth, boolean isMaximizing, int alpha, int beta) {
        numExploredNodes++;

        if ((numExploredNodes & (TIME_CHECK_INTERVAL - 1)) == 0) {
            if (isTimeUp()) return 0;
        }

        if (depth == 0 || board.isGameOver()) {
            return board.evaluate(cpuMark);
        }

        if (isMaximizing) {
            ArrayList<Move> possibleMoves = board.getPossibleMoves(cpuMark);
            int bestScore = Integer.MIN_VALUE;

            for (Move move : possibleMoves) {
                Mark captured = board.getMark(move.getToRow(), move.getToCol());
                board.play(move, cpuMark);
                int score = alphaBeta(board, depth - 1, false, alpha, beta);
                board.unplay(move, cpuMark, captured);

                if (timeUp) return bestScore;

                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) break;
            }
            return bestScore;
        } else {
            ArrayList<Move> possibleMoves = board.getPossibleMoves(opponentMark);
            int bestScore = Integer.MAX_VALUE;

            for (Move move : possibleMoves) {
                Mark captured = board.getMark(move.getToRow(), move.getToCol());
                board.play(move, opponentMark);
                int score = alphaBeta(board, depth - 1, true, alpha, beta);
                board.unplay(move, opponentMark, captured);

                if (timeUp) return bestScore;

                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) break;
            }
            return bestScore;
        }
    }

    // =============================================
    // Move Ordering — seulement au niveau racine, rapide
    // =============================================
    private void sortMovesRoot(ArrayList<Move> moves, Board board) {
        moves.sort((a, b) -> scoreMoveOrder(b, board) - scoreMoveOrder(a, board));
    }

    private int scoreMoveOrder(Move move, Board board) {
        Mark mover = board.getMark(move.getFromRow(), move.getFromCol());
        Mark target = board.getMark(move.getToRow(), move.getToCol());

        // 1. Coups gagnants
        if (mover == Mark.RED && move.getToRow() == 0) return 10000;
        if (mover == Mark.BLACK && move.getToRow() == Board.SIZE - 1) return 10000;

        int score = 0;

        // 2. Captures — capturer un pion avancé ou infiltré = très prioritaire
        if (target != Mark.EMPTY && target != mover) {
            score += 500;
            int targetAdv = (target == Mark.BLACK) ? move.getToRow() : (Board.SIZE - 1 - move.getToRow());
            score += targetAdv * 40; // Plus il est avancé, plus on veut le capturer
        }

        // 3. Avancement
        int myAdv = (mover == Mark.BLACK) ? move.getToRow() : (Board.SIZE - 1 - move.getToRow());
        score += myAdv * 8;

        // 4. Centre
        int centerDist = Math.abs(move.getToCol() - 3);
        score += (4 - centerDist) * 2;

        return score;
    }

    // =============================================
    // Gestion du temps
    // =============================================
    private boolean isTimeUp() {
        if (timeUp) return true;
        if (System.currentTimeMillis() - startTime >= TIME_LIMIT_MS) {
            timeUp = true;
            return true;
        }
        return false;
    }
}