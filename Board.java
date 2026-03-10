import java.util.ArrayList;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
class Board {
    public final static int SIZE = 8;
    
    private Mark[][] board;

    // Ne pas changer la signature de cette méthode
    public Board() {
        board = new Mark[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = Mark.EMPTY;
            }
        }

        // Placer les pièces noires (rangées 0 et 1)
        for (int j = 0; j < SIZE; j++) {
            board[0][j] = Mark.BLACK;
            board[1][j] = Mark.BLACK;
        }   

        // Placer les pièces rouges (rangées 6 et 7)
        for (int j = 0; j < SIZE; j++) {
            board[6][j] = Mark.RED;
            board[7][j] = Mark.RED;
        }
    }

    // Ne pas changer la signature de cette méthode
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
                if (board[r][c] != mark) {
                    continue;
                }

                int nr = r + dir;

                if (nr < 0 || nr >= SIZE) {
                    continue;
                }

                // Déplacement en avant (seulement si case vide)
                if (board[nr][c] == Mark.EMPTY) {
                    moves.add(new Move(r, c, nr, c));
                }

                // Diagonale gauche (vide OU capture adverse)
                if (c - 1 >= 0 && board[nr][c - 1] != mark) {
                    moves.add(new Move(r, c, nr, c - 1));
                }

                // Diagonale droite (vide OU capture adverse)
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
                if (board[0][j] == Mark.RED) {
                    return true;
                }
            }
            return !hasPieces(Mark.BLACK);
        } else {
            for (int j = 0; j < SIZE; j++) {
                if (board[SIZE - 1][j] == Mark.BLACK) {
                    return true;
                }
            }
            return !hasPieces(Mark.RED);
        }
    }

    private boolean hasPieces(Mark mark) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == mark) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isGameOver() {
        return hasWon(Mark.RED) || hasWon(Mark.BLACK);
    }

    public static Mark getOpponent(Mark mark) {
        if (mark == Mark.BLACK) {
            return Mark.RED;
        } else {
            return Mark.BLACK;
        }
    }

    public int countPieces(Mark mark) {
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == mark) {
                    count++;
                }
            }
        }
        return count;
    }

    // =============================================
    // Fonction d'évaluation améliorée
    // =============================================
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark) {
        Mark opponent = getOpponent(mark);

        // Victoire / Défaite
        if (hasWon(mark)) {
            return 10000;
        }
        if (hasWon(opponent)) {
            return -10000;
        }

        int score = 0;

        int myPieces = 0;
        int oppPieces = 0;
        int myAdvancement = 0;
        int oppAdvancement = 0;
        int myCenterControl = 0;
        int oppCenterControl = 0;
        int myProtected = 0;
        int oppProtected = 0;
        int myBestRow = -1;  // Rangée la plus avancée pour mark
        int oppBestRow = -1; // Rangée la plus avancée pour opponent

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Mark cell = board[r][c];
                if (cell == Mark.EMPTY) continue;

                if (cell == mark) {
                    myPieces++;

                    // Avancement (progression vers la rangée adverse)
                    int advancement = (mark == Mark.BLACK) ? r : (SIZE - 1 - r);
                    // Bonus exponentiel pour les pièces proches de la victoire
                    myAdvancement += advancement * advancement;

                    // Meilleure rangée
                    if (advancement > myBestRow) {
                        myBestRow = advancement;
                    }

                    // Contrôle du centre (colonnes C, D, E, F = indices 2,3,4,5)
                    if (c >= 2 && c <= 5) {
                        myCenterControl += 2;
                        if (c >= 3 && c <= 4) {
                            myCenterControl += 1; // Bonus supplémentaire pour D, E
                        }
                    }

                    // Protection : pièce protégée par une pièce amie derrière en diagonale
                    int backRow = (mark == Mark.BLACK) ? r - 1 : r + 1;
                    if (backRow >= 0 && backRow < SIZE) {
                        if (c - 1 >= 0 && board[backRow][c - 1] == mark) {
                            myProtected++;
                        }
                        if (c + 1 < SIZE && board[backRow][c + 1] == mark) {
                            myProtected++;
                        }
                    }

                } else { // cell == opponent
                    oppPieces++;

                    int advancement = (opponent == Mark.BLACK) ? r : (SIZE - 1 - r);
                    oppAdvancement += advancement * advancement;

                    if (advancement > oppBestRow) {
                        oppBestRow = advancement;
                    }

                    if (c >= 2 && c <= 5) {
                        oppCenterControl += 2;
                        if (c >= 3 && c <= 4) {
                            oppCenterControl += 1;
                        }
                    }

                    int backRow = (opponent == Mark.BLACK) ? r - 1 : r + 1;
                    if (backRow >= 0 && backRow < SIZE) {
                        if (c - 1 >= 0 && board[backRow][c - 1] == opponent) {
                            oppProtected++;
                        }
                        if (c + 1 < SIZE && board[backRow][c + 1] == opponent) {
                            oppProtected++;
                        }
                    }
                }
            }
        }

        // Avantage matériel (nombre de pièces)
        score += (myPieces - oppPieces) * 30;

        // Avancement des pièces (bonus exponentiel)
        score += (myAdvancement - oppAdvancement) * 2;

        // Pièce la plus avancée (bonus pour la menace de victoire)
        score += (myBestRow - oppBestRow) * 15;

        // Contrôle du centre
        score += (myCenterControl - oppCenterControl) * 3;

        // Protection des pièces
        score += (myProtected - oppProtected) * 5;

        return score;
    }

    public void loadFromServer(String[] data) {
        int index = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                switch (data[index++]) {
                    case "4": // RED
                        board[r][c] = Mark.RED; 
                        break;
                    case "2": // BLACK
                        board[r][c] = Mark.BLACK;
                        break;
                    default:
                        board[r][c] = Mark.EMPTY;
                        break;
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
                if (board[r][c] == Mark.RED) {
                    sb.append("R ");
                } else if (board[r][c] == Mark.BLACK) {
                    sb.append("B ");
                } else {
                    sb.append(". ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
