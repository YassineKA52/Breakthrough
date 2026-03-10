import java.util.ArrayList;
import java.util.Comparator;

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

    private static final long TIME_LIMIT_MS = 4000;
    private static final int MAX_DEPTH_LIMIT = 30;
    // Vérifier le temps seulement tous les 2048 noeuds (évite l'overhead de System.currentTimeMillis)
    private static final int TIME_CHECK_INTERVAL = 2048;

    private long startTime;
    private boolean timeUp;

    // Le constructeur reçoit en paramètre le joueur MAX
    public CPUPlayer(Mark cpu) {
        this.cpuMark = cpu;
        this.opponentMark = Board.getOpponent(cpu);
    }

    // Ne pas changer cette méthode
    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    // =============================================
    // MinMax avec limite de profondeur
    // =============================================

    // Retourne la liste des coups possibles. Cette liste contient
    // plusieurs coups possibles si et seulement si plusieurs coups
    // ont le même score.
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

    // =============================================
    // Alpha-Beta à profondeur fixe (pour comparaison avec MinMax)
    // =============================================
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
    // Alpha-Beta avec Iterative Deepening + Gestion du temps
    // =============================================

    // Retourne la liste des coups possibles. Cette liste contient
    // plusieurs coups possibles si et seulement si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;
        startTime = System.currentTimeMillis();
        timeUp = false;

        ArrayList<Move> possibleMoves = board.getPossibleMoves(cpuMark);

        if (possibleMoves.isEmpty()) {
            return new ArrayList<>();
        }

        // Si un seul coup possible, le jouer directement
        if (possibleMoves.size() == 1) {
            ArrayList<Move> result = new ArrayList<>();
            result.add(possibleMoves.get(0));
            return result;
        }

        // Tri initial des coups au niveau racine (move ordering)
        sortMoves(possibleMoves, board);

        ArrayList<Move> bestMoves = new ArrayList<>();
        bestMoves.add(possibleMoves.get(0)); // Fallback au cas où

        // Iterative Deepening : on augmente la profondeur tant qu'on a du temps
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

                // Si on a trouvé une victoire, pas besoin de chercher plus
                if (currentBestScore >= 10000) {
                    System.out.println("  Victoire trouvee a profondeur " + depth);
                    return currentBestMoves;
                }
            }

            // Seulement mettre à jour si on a complété cette profondeur
            if (completedDepth && !currentBestMoves.isEmpty()) {
                bestMoves = currentBestMoves;

                // Mettre le meilleur coup en premier pour la prochaine itération
                Move bestMove = currentBestMoves.get(0);
                possibleMoves.remove(bestMove);
                possibleMoves.add(0, bestMove);

                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println("  Profondeur " + depth + " en " + elapsed + "ms, score=" + currentBestScore +
                        ", coup=" + bestMoves.get(0) + " (" + numExploredNodes + " noeuds)");
            }

            if (timeUp) {
                break;
            }

            // Ne pas commencer une nouvelle profondeur si on a utilisé plus de la moitié du temps
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > TIME_LIMIT_MS / 2) {
                System.out.println("  Arret: temps ecoule a " + elapsed + "ms, profondeur max atteinte = " + depth);
                break;
            }
        }

        return bestMoves;
    }

    private int alphaBeta(Board board, int depth, boolean isMaximizing, int alpha, int beta) {
        numExploredNodes++;

        // Vérifier le temps périodiquement (tous les 2048 noeuds)
        if ((numExploredNodes & (TIME_CHECK_INTERVAL - 1)) == 0) {
            if (isTimeUp()) {
                return 0;
            }
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

                if (beta <= alpha) {
                    break;
                }
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

                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        }
    }

    // =============================================
    // Move Ordering — trier les coups pour un meilleur élagage
    // (appliqué seulement au niveau racine)
    // =============================================
    private void sortMoves(ArrayList<Move> moves, Board board) {
        moves.sort(new Comparator<Move>() {
            @Override
            public int compare(Move a, Move b) {
                return scoreMoveOrder(b, board) - scoreMoveOrder(a, board);
            }
        });
    }

    private int scoreMoveOrder(Move move, Board board) {
        int score = 0;
        Mark target = board.getMark(move.getToRow(), move.getToCol());

        // Captures en premier (priorité haute)
        if (target != Mark.EMPTY) {
            score += 100;
        }

        // Coups qui avancent vers la rangée de victoire
        Mark mover = board.getMark(move.getFromRow(), move.getFromCol());
        if (mover == Mark.RED) {
            // Rouge avance vers le haut (row 0)
            score += (Board.SIZE - 1 - move.getToRow()) * 5;
            // Bonus si on atteint la dernière rangée
            if (move.getToRow() == 0) score += 500;
        } else if (mover == Mark.BLACK) {
            // Noir avance vers le bas (row 7)
            score += move.getToRow() * 5;
            if (move.getToRow() == Board.SIZE - 1) score += 500;
        }

        // Préférer les coups au centre
        int centerDist = Math.abs(move.getToCol() - 3);
        score += (4 - centerDist);

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