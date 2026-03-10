import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

class Main {

    private static final String HOST = "localhost";
    private static final int    PORT = 8888;

    public static void main(String[] args) throws Exception {

        // --- Choisir la couleur ---
        Mark myColor = chooseColor(args);
        System.out.println("Je joue les " + (myColor == Mark.RED ? "ROUGES" : "NOIRS"));

        // --- Connexion au serveur ---
        Socket socket = new Socket(HOST, PORT);
        PrintWriter    out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Board     board = new Board();
        CPUPlayer cpu   = new CPUPlayer(myColor);

        System.out.println("Connecté au serveur. En attente...");

        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] tokens = line.split("\\s+");
            int msgType = Integer.parseInt(tokens[0]);

            if (msgType == 1 || msgType == 2) {
                String[] data = new String[64];
                for (int i = 0; i < 64; i++) {
                    data[i] = tokens[i + 1];
                }
                board.loadFromServer(data);
                System.out.println(board);

                if (msgType == 1) {
                    jouerCoup(board, cpu, myColor, out);
                }

            } else if (msgType == 3) {
                String opponentMoveStr = tokens.length > 1 ? tokens[1] : "";
                System.out.println("Message reçu: " + line);         
                System.out.println("Coup adverse: " + opponentMoveStr); 

                Move opponentMove = Move.fromString(opponentMoveStr);
                if (opponentMove != null && !isFakeMove(opponentMoveStr)) {
                    Mark opponent = Board.getOpponent(myColor);
                    board.play(opponentMove, opponent);
                    System.out.println("Adversaire joue : " + opponentMove);
                }

                System.out.println(board);

                if (board.isGameOver()) {
                    System.out.println("Partie terminée !");
                    break;
                }

                jouerCoup(board, cpu, myColor, out);

            } else if (msgType == 4) {
                System.out.println("Coup invalide reçu ! Recalcul...");
                jouerCoup(board, cpu, myColor, out);
            }
        }

        socket.close();
        System.out.println("Connexion fermée.");
    }

    private static void jouerCoup(Board board, CPUPlayer cpu, Mark myColor, PrintWriter out) {
        ArrayList<Move> bestMoves = cpu.getNextMoveAB(board);

        if (bestMoves.isEmpty()) {
            System.out.println("Aucun coup possible !");
            return;
        }

        Move move = bestMoves.get(0);
        board.play(move, myColor);

        System.out.println("Je joue : " + move +
                " (" + cpu.getNumOfExploredNodes() + " noeuds explorés)");
        System.out.println(board);

        out.println(move.toString());
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