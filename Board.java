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
        // Initialiser toutes les cases à EMPTY
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = Mark.EMPTY;
            }
        }

        // Placer les pièces noires
        for (int j = 0; j < SIZE; j++) {
            board[0][j] = Mark.BLACK;
            board[1][j] = Mark.BLACK;
        }   

        // Placer les pièces rouges
        for (int j = 0; j < SIZE; j++) {
            board[6][j] = Mark.RED;
            board[7][j] = Mark.RED;
        }
    }

    // Place la pièce 'mark' sur le plateau, à la
    // position spécifiée dans Move
    //
    // Ne pas changer la signature de cette méthode
    public void play(Move m, Mark mark) {
        board[m.getFromRow()][m.getFromCol()] = Mark.EMPTY; // Enlever la pièce de la position d'origine    
        board[m.getToRow()][m.getToCol()] = mark;
    }

    // Annule un coup 
    public void unplay(Move m, Mark movedMark, Mark capturedMark) {
        board[m.getFromRow()][m.getFromCol()] = movedMark; // Remettre la pièce à la position d'origine
        board[m.getToRow()][m.getToCol()] = capturedMark; // Remettre la pièce capturée (ou EMPTY si aucune pièce n'a été capturée)
    }

    public void unplay(Move m, Mark movedMark) {
        unplay(m, movedMark, Mark.EMPTY);
    }

    // Retourne la liste des coups possibles (cases vides)
    // Parcours de gauche à droite, de haut en bas
    public ArrayList<Move> getPossibleMoves(Mark mark) {
        ArrayList<Move> moves = new ArrayList<>();
        int dir = (mark == Mark.BLACK) ? 1 : -1; // Direction de déplacement pour chaque joueur
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] != mark) {
                    continue; // Ignorer les cases qui ne contiennent pas la pièce du joueur
                }

                int nr = r + dir;

                if(nr < 0 || nr >= SIZE) {
                    continue; // Ignorer les déplacements hors du plateau
                }

                if (board[nr][c] == Mark.EMPTY) {
                    moves.add(new Move(r, c, nr, c)); // Déplacement vertical
                }

                if (c - 1 >= 0 && board[nr][c - 1] != mark) {
                    moves.add(new Move(r, c, nr, c - 1)); // Déplacement diagonal gauche
                }

                if (c + 1 < SIZE && board[nr][c + 1] != mark) {
                    moves.add(new Move(r, c, nr, c + 1)); // Déplacement diagonal droit
                }
            }
        }
        return moves;
    }

    public Mark getMark(int row, int col) {
        return board[row][col];
    }

    private boolean hasWon(Mark mark) {
        if (mark == Mark.RED) {
            for (int j = 0; j < SIZE; j++) {
                if (board[0][j] == Mark.RED) {
                    return true;
                }
            }
            return !hasPieces(Mark.BLACK); // Si les rouges n'ont plus de pièces, les rouges gagnent
        } else { // mark == Mark.BLACK
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

    // retourne  100 pour une victoire
    //          -100 pour une défaite
    //           0   pour un match nul
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark) {
        if (hasWon(mark)) {
            return 100;
        } 

        if (hasWon(getOpponent(mark))) {
            return -100;
        }

        int score = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Mark cell = board[r][c];
                if (cell == Mark.EMPTY) {
                    continue;
                } 
                if (cell == mark) {
                    score += (mark == Mark.BLACK) ? r : (SIZE - 1 - r); // Plus la pièce est avancée, plus elle vaut
                } else {
                    score -= (cell == Mark.BLACK) ? r : (SIZE - 1 - r); // Plus la pièce adverse est avancée, plus elle pénalise
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
                    case "R": // RED
                        board[r][c] = Mark.RED; 
                        break;
                    case "B": // BLACK
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
        sb.append(" A B C D E F G H\n");
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