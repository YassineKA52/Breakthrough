import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

class Main {

    private static final String HOST = "localhost";
    private static final int    PORT = 8888;

    public static void main(String[] args) throws Exception {

        Mark myColor = chooseColor(args);
        System.out.println("Je joue les " + (myColor == Mark.RED ? "ROUGES" : "NOIRS"));

        Socket socket = new Socket(HOST, PORT);
        BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
        BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());

        Board     board = new Board();
        CPUPlayer cpu   = new CPUPlayer(myColor);

        System.out.println("Connecté au serveur. En attente...");

        while (true) {
            // Lecture bloquante du premier octet (type de message)
            int cmdByte = input.read();
            if (cmdByte == -1) {
                System.out.println("Connexion fermée par le serveur.");
                break;
            }
            char cmd = (char) cmdByte;
            System.out.println("=== Commande reçue: " + cmd + " ===");

            if (cmd == '1' || cmd == '2') {
                // Lire le plateau (64 valeurs séparées par des espaces)
                String boardData = readBoardData(input);
                System.out.println("Plateau brut: " + boardData);

                String[] boardValues = boardData.trim().split("\\s+");
                String[] data = new String[64];
                for (int i = 0; i < 64 && i < boardValues.length; i++) {
                    data[i] = boardValues[i];
                }
                board.loadFromServer(data);
                System.out.println(board);

                if (cmd == '1') {
                    jouerCoup(board, cpu, myColor, output);
                } else {
                    System.out.println("On joue les noirs, en attente du coup rouge...");
                }

            } else if (cmd == '3') {
                // Lire le coup adverse (quelques octets comme " D6-D5")
                String moveData = readMoveData(input);
                System.out.println("Données coup adverse brut: [" + moveData + "]");

                String moveStr = moveData.trim();
                System.out.println("Coup adverse parsé: [" + moveStr + "]");

                if (!moveStr.isEmpty() && !isFakeMove(moveStr)) {
                    Move opponentMove = Move.fromString(moveStr);
                    if (opponentMove != null) {
                        Mark opponent = Board.getOpponent(myColor);
                        board.play(opponentMove, opponent);
                        System.out.println("Adversaire joue : " + opponentMove);
                    } else {
                        System.out.println("ERREUR: Impossible de parser le coup: [" + moveStr + "]");
                    }
                } else {
                    System.out.println("Coup fake ou vide, ignoré.");
                }

                System.out.println(board);

                if (board.isGameOver()) {
                    System.out.println("Partie terminée !");
                    break;
                }

                jouerCoup(board, cpu, myColor, output);

            } else if (cmd == '4') {
                System.out.println("Coup invalide reçu ! Recalcul...");
                jouerCoup(board, cpu, myColor, output);

            } else if (cmd == '5') {
                String moveData = readMoveData(input);
                System.out.println("Partie terminée. Dernier coup: " + moveData.trim());
                break;
            }
        }

        socket.close();
        System.out.println("Connexion fermée.");
    }

    /**
     * Lit les données du plateau depuis le serveur (64 valeurs).
     * Utilise une lecture bloquante en boucle jusqu'à avoir assez de données.
     */
    private static String readBoardData(BufferedInputStream input) throws IOException {
        byte[] buffer = new byte[1024];
        int totalRead = 0;

        // Lire en boucle jusqu'à avoir les 64 valeurs
        while (totalRead < buffer.length) {
            int n = input.read(buffer, totalRead, buffer.length - totalRead);
            if (n <= 0) break;
            totalRead += n;

            // Vérifier si on a assez de données (64 valeurs)
            String temp = new String(buffer, 0, totalRead).trim();
            String[] parts = temp.split("\\s+");
            if (parts.length >= 64) break;
        }

        return new String(buffer, 0, totalRead);
    }

    /**
     * Lit les données d'un coup depuis le serveur (ex: " D6-D5").
     * Lecture bloquante : attend que les données arrivent.
     */
    private static String readMoveData(BufferedInputStream input) throws IOException {
        byte[] buffer = new byte[32];
        int totalRead = 0;

        // Première lecture bloquante — attend que les données arrivent
        int n = input.read(buffer, 0, buffer.length);
        if (n > 0) {
            totalRead = n;
        }

        // Petite pause puis vérifier s'il reste des données
        try { Thread.sleep(50); } catch (InterruptedException e) {}
        int extra = input.available();
        if (extra > 0 && totalRead + extra <= buffer.length) {
            int n2 = input.read(buffer, totalRead, extra);
            if (n2 > 0) totalRead += n2;
        }

        return new String(buffer, 0, totalRead);
    }

    private static void jouerCoup(Board board, CPUPlayer cpu, Mark myColor, BufferedOutputStream output) throws IOException {
        ArrayList<Move> bestMoves = cpu.getNextMoveAB(board);

        if (bestMoves.isEmpty()) {
            System.out.println("Aucun coup possible !");
            return;
        }

        Move move = bestMoves.get(0);
        board.play(move, myColor);

        String moveStr = move.toString();
        System.out.println("Je joue : " + moveStr +
                " (" + cpu.getNumOfExploredNodes() + " noeuds explorés)");
        System.out.println(board);

        output.write(moveStr.getBytes(), 0, moveStr.length());
        output.flush();
    }

    private static boolean isFakeMove(String moveStr) {
        return moveStr.equals("A8-A8") || moveStr.equals("A8A8");
    }

    private static Mark chooseColor(String[] args) {
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            if (arg.equals("rouge") || arg.equals("red") || arg.equals("r")) return Mark.RED;
            if (arg.equals("noir") || arg.equals("black") || arg.equals("n")) return Mark.BLACK;
        }
        Scanner sc = new Scanner(System.in);
        System.out.print("Jouer les rouges ou les noirs ? (rouge/noir) : ");
        String choice = sc.nextLine().trim().toLowerCase();
        return choice.startsWith("r") ? Mark.RED : Mark.BLACK;
    }
}