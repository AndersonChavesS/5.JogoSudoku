package br.com.dio;

import br.com.dio.model.Board;
import br.com.dio.model.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import static br.com.dio.util.BoardTemplate.BOARD_TEMPLATE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

public class Main {
    private final static Scanner teclado = new Scanner(System.in);
    private static Board board;

    private final static int BOARD_LIMIT = 9;

    public static void main(String[] args) {
        final var positions = Stream.of(args).collect(toMap(
                k -> k.split(";")[0],
                v -> v.split(";")[1]
        ));
        var option = -1;

        while (true) {
            System.out.println("Selecione uma das opções a seguir");
            System.out.println("1 - Iniciar um novo Jogo");
            System.out.println("2 - Colocar um novo número");
            System.out.println("3 - Remover um número");
            System.out.println("4 - Visualizar jogo atual");
            System.out.println("5 - Verificar status do jogo");
            System.out.println("6 - Limpar jogo");
            System.out.println("7 - Finalizar jogo");
            System.out.println("8 - Sair");

            option = teclado.nextInt();
            teclado.nextLine();

            switch (option) {
                case 1 -> startGame(positions);
                case 2 -> inputNumber();
                case 3 -> removeNumber();
                case 4 -> showCurrentGame();
                case 5 -> showGameStatus();
                case 6 -> clearGame();
                case 7 -> finishGame();
                case 8 -> System.exit(0);
                default -> System.out.println("Opção Inválida!");
            }
        }
    }

    private static void startGame(Map<String, String> positions) {
        if (nonNull(board)) {
            System.out.println("Jogo já iniciado");
            return;
        }

        List<List<Space>> spaces = new ArrayList<>();
        for (int i = 0; i < BOARD_LIMIT; i++) {
            spaces.add(new ArrayList<>());
            for (int j = 0; j < BOARD_LIMIT; j++) {
                var positionConfig = positions.get("%s,%s".formatted(i, j));
                var expected = Integer.parseInt(positionConfig.split(",")[0]);
                var fixed = Boolean.parseBoolean(positionConfig.split(",")[1]);
                var currentSpace = new Space(expected, fixed);
                spaces.get(i).add(currentSpace);
            }
        }
        board = new Board(spaces);
        System.out.println("O jogo está pronto para começar");
    }

    private static void inputNumber() {
        if (isNull(board)) {
            System.out.println("Jogo ainda não iniciado.");
            return;
        }
        System.out.println("Informe a coluna (0 a 8) que o número será inserido.");
        var col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha (0 a 8) que o número será inserido.");
        var row = runUntilGetValidNumber(0, 8);
        System.out.printf("Informe o número (1 a 9) para inserir na posição [%s,%s]\n", col, row);
        var value = runUntilGetValidNumber(1, 9);

        if (!board.changeValue(col, row, value)) {
            System.out.printf("A posição [%s,%s] tem um valor fixo\n", col, row);
        }
    }

    private static void removeNumber() {
        if (isNull(board)) {
            System.out.println("Jogo ainda não iniciado");
            return;
        }
        System.out.println("Informe a coluna que o número está inserido:");
        var col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha que o número está inserido:");
        var row = runUntilGetValidNumber(0, 8);

        var currentValue = board.getValue(col, row);
        if (isNull(currentValue)) {
            System.out.printf("A posição [%s,%s] está vazia.\n", col, row);
            return;
        }
        System.out.printf("Informe o número que está na posição [%s,%s]:\n", col, row);
        var value = runUntilGetValidNumber(1, 9);

        if (currentValue != value) {
            System.out.printf("O número informado (%s) não corresponde ao número na posição [%s,%s] (%s).\n",
                    value, col, row, currentValue);
            return;
        }
        if (!board.clearValue(col, row)) {
            System.out.printf("A posição [%s,%s] tem um valor fixo e não pode ser alterada.\n", col, row);
        } else {
            System.out.printf("Número removido da posição [%s,%s].\n", col, row);
        }
    }

    private static void showCurrentGame() {
        if (isNull(board)) {
            System.out.println("Jogo ainda não iniciado");
            return;
        }

        var args = new Object[81];
        var argPos = 0;
        for (int i = 0; i < BOARD_LIMIT; i++) {
            for (var col : board.getSpaces()) {
                args[argPos++] = " " + ((isNull(col.get(i).getActual())) ? " " : col.get(i).getActual());
            }
        }
        System.out.println("Seu jogo se encontra da seguinte forma");
        System.out.printf((BOARD_TEMPLATE) + "\n", args);
    }

    private static void showGameStatus() {
        if (isNull(board)) {
            System.out.println("Jogo ainda não iniciado");
            return;
        }

        System.out.printf("Status do Jogo: %s\n", board.getStatus().getLabel());
        if (board.hasErrors()) {
            System.out.println("O jogo contém erros");
        } else {
            System.out.println("O jogo não contém erros");
        }
    }

    private static void clearGame() {
        if (isNull(board)) {
            System.out.println("Jogo ainda não iniciado");
            return;
        }

        System.out.println("Tem certeza que deseja limpar seu jogo?" +
                "\nEsta ação é irreversível, e você perderá todo o seu progresso.");
        var confirm = teclado.next();
        while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não")) {
            System.out.println("Informe 'sim' ou 'não'");
            confirm = teclado.next();
        }
        if (confirm.equalsIgnoreCase("sim")) {
            board.reset();
            System.out.println("Jogo limpo com sucesso.");
        } else {
            System.out.println("Ação de limpar jogo cancelada.");
        }
    }

    private static void finishGame() {
        if (isNull(board)) {
            System.out.println("Jogo ainda não iniciado");
            return;
        }

        if (board.gameIsFinished()) {
            System.out.println("Parabéns você concluiu o jogo!");
            showCurrentGame();
            board = null;
        } else if (board.hasErrors()) {
            System.out.println("O seu jogo contém erros, verifique seu board e ajuste-o.");
        } else {
            System.out.println("Você ainda precisa preencher algum espaço.");

        }

        System.out.println("Deseja realmente finalizar o jogo? (sim/não)");
        var confirm = teclado.next();
        while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não")) {
            System.out.println("Informe 'sim' ou 'não'");
            confirm = teclado.next();
        }
        if (confirm.equalsIgnoreCase("sim")) {
            board = null; // Reseta o jogo
            System.out.println("Jogo finalizado.");
        } else {
            System.out.println("Ação de finalizar jogo cancelada.");
        }
    }

    private static int runUntilGetValidNumber(final int min, final int max) {
        var current = -1;
        while (true) {
            try {

                current = Integer.parseInt(teclado.nextLine());
                if (current >= min && current <= max) {
                    break;
                } else {
                    System.out.printf("Número fora do intervalo [%s, %s]. Tente novamente.\n", min + 1, max + 1);
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, insira um número.");
            }
        }
        return current;
    }


}