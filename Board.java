import java.util.ArrayList;

class Board {
    public final static int SIZE = 8;
    
    private Mark[][] board;

    public Board() {
        board = new Mark[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = Mark.EMPTY;
            }
        }
        for (int j = 0; j < SIZE; j++) {
            board[0][j] = Mark.BLACK;
            board[1][j] = Mark.BLACK;
        }   
        for (int j = 0; j < SIZE; j++) {
            board[6][j] = Mark.RED;
            board[7][j] = Mark.RED;
        }
    }

    public void play(Move m, Mark mark) {
        board[m.getFromRow()][m.getFromCol()] = Mark.EMPTY;
        board[m.getToRow()][m.getToCol()] = mark;
    }

    public void unplay(Move m, Mark movedMark, Mark capturedMark) {
        board[m.getFromRow()][m.getFromCol()] = movedMark;
        board[m.getToRow()][m.getToCol()] = capturedMark;
    }

    public void unplay(Move m, Mark movedMark) {
        unplay(m, movedMark, Mark.EMPTY);
    }

    public ArrayList<Move> getPossibleMoves(Mark mark) {
        ArrayList<Move> moves = new ArrayList<>();
        int dir = (mark == Mark.BLACK) ? 1 : -1;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] != mark) continue;

                int nr = r + dir;
                if (nr < 0 || nr >= SIZE) continue;

                if (board[nr][c] == Mark.EMPTY) {
                    moves.add(new Move(r, c, nr, c));
                }
                if (c - 1 >= 0 && board[nr][c - 1] != mark) {
                    moves.add(new Move(r, c, nr, c - 1));
                }
                if (c + 1 < SIZE && board[nr][c + 1] != mark) {
                    moves.add(new Move(r, c, nr, c + 1));
                }
            }
        }
        return moves;
    }

    public Mark getMark(int row, int col) {
        return board[row][col];
    }

    public boolean hasWon(Mark mark) {
        if (mark == Mark.RED) {
            for (int j = 0; j < SIZE; j++) {
                if (board[0][j] == Mark.RED) return true;
            }
            return !hasPieces(Mark.BLACK);
        } else {
            for (int j = 0; j < SIZE; j++) {
                if (board[SIZE - 1][j] == Mark.BLACK) return true;
            }
            return !hasPieces(Mark.RED);
        }
    }

    private boolean hasPieces(Mark mark) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == mark) return true;
            }
        }
        return false;
    }

    public boolean isGameOver() {
        return hasWon(Mark.RED) || hasWon(Mark.BLACK);
    }

    public static Mark getOpponent(Mark mark) {
        return (mark == Mark.BLACK) ? Mark.RED : Mark.BLACK;
    }

    public int countPieces(Mark mark) {
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == mark) count++;
            }
        }
        return count;
    }


    // ÉVALUATION 
    // Principes :
    //  - Matériel = 100 pts par pièce (perdre une pièce est très grave)
    //  - Avancement modéré (ne pas survaloriser l'avancement seul)
    //  - Pièce attaquée + non défendue = valeur réduite
    //  - Adversaire infiltré dans notre base = danger
    public int evaluate(Mark mark) {
        Mark opponent = getOpponent(mark);

        if (hasWon(mark))     return 10000;
        if (hasWon(opponent)) return -10000;

        int score = 0;

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Mark cell = board[r][c];
                if (cell == Mark.EMPTY) continue;

                boolean isMine = (cell == mark);
                int adv, backR, atkR;

                if (cell == Mark.BLACK) {
                    adv = r;            // BLACK avance vers row 7
                    backR = r - 1;      // Derrière pour BLACK
                    atkR = r + 1;       // D'où RED attaque (RED va vers le haut)
                } else {
                    adv = SIZE - 1 - r; // RED avance vers row 0
                    backR = r + 1;      // Derrière pour RED
                    atkR = r - 1;       // D'où BLACK attaque (BLACK va vers le bas)
                }

                int pieceVal = 100; // Valeur de base par pièce

                // --- Avancement (modéré pour ne pas encourager les suicides) ---
                // adv: 0=base, 1-2=début, 3-4=milieu, 5=avancé, 6=menace, 7=victoire
                if (adv <= 2) {
                    pieceVal += adv * 3;
                } else if (adv <= 4) {
                    pieceVal += 6 + (adv - 2) * 8;
                } else {
                    pieceVal += 22 + (adv - 4) * 18;
                }

                // --- Sécurité : est-ce que la pièce est attaquée ? ---
                boolean attacked = false;
                if (atkR >= 0 && atkR < SIZE) {
                    Mark attacker = getOpponent(cell);
                    if (c - 1 >= 0 && board[atkR][c - 1] == attacker) attacked = true;
                    if (c + 1 < SIZE && board[atkR][c + 1] == attacker) attacked = true;
                }

                // --- Sécurité : est-ce que la pièce est défendue ? ---
                boolean defended = false;
                if (backR >= 0 && backR < SIZE) {
                    if (c - 1 >= 0 && board[backR][c - 1] == cell) defended = true;
                    if (c + 1 < SIZE && board[backR][c + 1] == cell) defended = true;
                }

                // Pièce attaquée et non défendue : on va probablement la perdre
                if (attacked && !defended) {
                    pieceVal -= 40; // Forte pénalité
                }
                // Pièce défendue : bonus de stabilité
                if (defended) {
                    pieceVal += 8;
                }

                // --- Centre (colonnes 2-5) ---
                if (c >= 2 && c <= 5) {
                    pieceVal += 4;
                }

                // --- Colonne (ami directement derrière) : formation forte ---
                if (backR >= 0 && backR < SIZE && board[backR][c] == cell) {
                    pieceVal += 6;
                }

                // --- Phalanxe (ami à côté) : formation forte ---
                if (c - 1 >= 0 && board[r][c - 1] == cell) pieceVal += 4;
                if (c + 1 < SIZE && board[r][c + 1] == cell) pieceVal += 4;

                // --- Menace de victoire (adv >= 6 = à 1 case de gagner) ---
                if (adv >= 6) {
                    pieceVal += 60;
                }

                // --- INFILTRATION : adversaire profond dans notre territoire ---
                // Un adversaire à adv >= 5 est extrêmement dangereux
                // On ajoute un malus supplémentaire pour nous rappeler de le capturer
                if (!isMine && adv >= 5) {
                    pieceVal += 30; // L'adversaire infiltré vaut encore PLUS (= plus de malus pour nous)
                }

                if (isMine) {
                    score += pieceVal;
                } else {
                    score -= pieceVal;
                }
            }
        }

        return score;
    }

    public void loadFromServer(String[] data) {
        int index = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                switch (data[index++]) {
                    case "4": board[r][c] = Mark.RED;   break;
                    case "2": board[r][c] = Mark.BLACK; break;
                    default:  board[r][c] = Mark.EMPTY; break;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  A B C D E F G H\n");
        for (int r = 0; r < SIZE; r++) {
            sb.append(8 - r).append(" ");
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == Mark.RED) sb.append("R ");
                else if (board[r][c] == Mark.BLACK) sb.append("B ");
                else sb.append(". ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}