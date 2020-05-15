package nice.org;

import de.vandermeer.asciitable.AsciiTable;
import javafx.util.Pair;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Program {
    /**
     * Точка входа в приложение, управляет меню приложения.
     */
    public static void main(String... args) {
        boolean mainDone = false;
        while (!mainDone) {
            System.out.println();
            System.out.println("=============================");
            System.out.println("Введите выражение и нажмите ENTER:");
            Scanner sc = new Scanner(System.in);
            String expression = sc.nextLine();

            boolean actionDone = false;
            while (!actionDone) {
                System.out.println();
                System.out.println("=============================");
                System.out.println(expression);
                System.out.println("1. Вывести таблицу истинности");
                System.out.println("2. Вычисление значения функции");
                System.out.println("3. Получить СДНФ");
                System.out.println("4. Получить СКНФ");
                System.out.println("5. Ввести другое выражение");
                System.out.print("Введите действие и нажмите ENTER:");

                try {
                    switch (sc.nextInt()) {
                        case 1:
                            calcTruthTable(expression);
                            break;
                        case 2:
                            calcFunction(expression);
                            break;
                        case 3:
                            calcPDNF(expression);
                            break;
                        case 4:
                            calcPCNF(expression);
                            break;
                        case 5:
                            actionDone = true;
                            break;
                        default:
                    }
                } catch (Exception ex) {
                    sc = new Scanner(System.in);
                }
            }
        }
    }

    /**
     * Вычисляет и печатает СДНФ указанной булевской функции.
     *
     * @param expression Выражение.
     */
    private static void calcPDNF(String expression) {
        System.out.println();
        System.out.println("=============================");
        System.out.println("СДНФ:");

        StringBuilder sb = new StringBuilder();

        Pair<String, Boolean>[] variablesWithConstants = getVariablesWithConstants(expression);
        boolean isFirst = true;
        for (Pair<Integer[], Integer> p : Arrays.stream(getTruthTableWithConstants(expression, variablesWithConstants)).filter(p -> p.getValue() == 1).collect(Collectors.toList())) {
            if (!isFirst) sb.append(" || ");
            sb.append("(");
            String[] part = new String[variablesWithConstants.length];
            for (int i = 0; i < variablesWithConstants.length; i++) {
                if (p.getKey()[i] == 0) {
                    part[i] = '!' + variablesWithConstants[i].getKey();
                } else {
                    part[i] = variablesWithConstants[i].getKey();
                }
            }

            sb.append(String.join(" && ", part));
            sb.append(")");

            isFirst = false;
        }

        System.out.printf("F(%s) = %s%n", String.join(", ", Arrays.stream(variablesWithConstants).filter(n -> n.getValue()).map(n -> n.getKey()).collect(Collectors.toList())), sb.toString());
    }

    /**
     * Вычисляет и печатает СКНФ указанного выражения.
     *
     * @param expression Выражение.
     */
    private static void calcPCNF(String expression) {
        System.out.println();
        System.out.println("=============================");
        System.out.println("СКНФ:");

        StringBuilder sb = new StringBuilder();

        Pair<String, Boolean>[] variablesWithConstants = getVariablesWithConstants(expression);
        boolean isFirst = true;
        for (Pair<Integer[], Integer> p : Arrays.stream(getTruthTableWithConstants(expression, variablesWithConstants)).filter(p -> p.getValue() == 0).collect(Collectors.toList())) {
            if (!isFirst) sb.append(" && ");
            sb.append("(");
            String[] part = new String[variablesWithConstants.length];
            for (int i = 0; i < variablesWithConstants.length; i++) {
                if (p.getKey()[i] == 1) {
                    part[i] = '!' + variablesWithConstants[i].getKey();
                } else {
                    part[i] = variablesWithConstants[i].getKey();
                }
            }

            sb.append(String.join(" || ", part));
            sb.append(")");

            isFirst = false;
        }

        System.out.printf("F(%s) = %s%n", String.join(", ", Arrays.stream(variablesWithConstants).filter(n -> n.getValue()).map(n -> n.getKey()).collect(Collectors.toList())), sb.toString());
    }

    /**
     * Вычисляет и печатает таблицу истинности для указанного выражения.
     *
     * @param expression Выражение.
     */
    private static void calcTruthTable(String expression) {
        String[] varNames = getVariables(expression);
        Pair<Integer[], Integer>[] table = getTruthTable(expression, varNames);

        AsciiTable renderTable = new AsciiTable();

        renderTable.addRule();
        renderTable.addRow(appendWith(varNames, "Значение"));
        renderTable.addRule();

        for (Pair<Integer[], Integer> p : table) {
            renderTable.addRow(appendWith(p.getKey(), p.getValue()));
            renderTable.addRule();
        }

        System.out.println(renderTable.render());
    }

    /**
     * Конкатенирует к указанному массиву указанный элемент в конец.
     *
     * @param source Исходный массив.
     * @param last   Элемент.
     * @param <T>    Тип массива.
     * @return Вовзращает новый дополненный массив.
     */
    private static <T> T[] appendWith(T[] source, T last) {
        T[] res = (T[]) new Object[source.length + 1];
        for (int i = 0; i < source.length; i++) {
            res[i] = source[i];
        }

        res[res.length - 1] = last;

        return res;
    }

    /**
     * Получает таблицу истинности.
     *
     * @param expression Исходное выражение.
     * @param varNames   Массив названий переменных внутри выражения.
     * @return Массив пар вида "массив значений переменных - значение функции при них"
     */
    private static Pair<Integer[], Integer>[] getTruthTable(String expression, String[] varNames) {
        int length = (int) Math.pow(2, varNames.length);

        Pair<Integer[], Integer>[] table = new Pair[length];
        for (int i = 0; i < length; i++) {
            Integer[] varValues = new Integer[varNames.length];
            for (int j = 0; j < varNames.length; j++) {
                varValues[j] = (i >> j) & 1;
            }
            Constant[] cons = IntStream.range(0, varValues.length).mapToObj(index -> new Constant(varNames[index], varValues[index])).collect(Collectors.toList()).toArray(new Constant[0]);
            Expression ex = new Expression(expression, cons);
            table[i] = new Pair(varValues, (int) ex.calculate());
        }

        return table;
    }

    /**
     * Вычисляет и печатает значение функции с помощью заданных пользователем значений переменных.
     *
     * @param expression Выражение.
     */
    private static void calcFunction(String expression) {
        System.out.println();
        System.out.println("=============================");
        System.out.println("Вычисление значения функции " + expression);

        String[] variables = getVariables(expression);
        List<Constant> mappedValues = new ArrayList(variables.length);
        Scanner sc = new Scanner(System.in);
        for (String varName : variables) {
            System.out.printf("Введите значение (1 или 0) переменной '%s':", varName);
            int v = sc.nextInt();
            mappedValues.add(new Constant(varName, v));
        }

        Expression ex = new Expression(expression, mappedValues.toArray(new Constant[0]));
        System.out.println(ex.getExpressionString() + " = " + (int) ex.calculate());
    }

    /**
     * Получает все уникальные имена переменных в порядке возникновения в выражении.
     *
     * @param expression Выражение.
     * @return Массив уникальных имён переменных.
     */
    private static String[] getVariables(String expression) {
        Pattern varPattern = Pattern.compile("([a-z]+[0-9]*)+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = varPattern.matcher(expression);

        List<String> vars = new ArrayList<String>();
        while (matcher.find()) {
            vars.add(expression.substring(matcher.start(), matcher.end()));
        }

        List<String> listDistinct = vars.stream().distinct().collect(Collectors.toList());
        String[] result = new String[listDistinct.size()];
        result = listDistinct.toArray(result);
        return result;
    }

    /**
     * Получает все имена переменных и констант в порядке возникновения в выражении.
     *
     * @param expression Выражение.
     * @return Массив пар вида "имя переменной или значение константы - признак того, что это является переменной".
     */
    private static Pair<String, Boolean>[] getVariablesWithConstants(String expression) {
        Pattern varPattern = Pattern.compile("(([a-z]+[0-9]*)+)|(([a-z]*[0-9]+)+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = varPattern.matcher(expression);

        List<Pair<String, Boolean>> vars = new ArrayList();
        while (matcher.find()) {
            String value = expression.substring(matcher.start(), matcher.end());
            try {
                int constant = Integer.parseInt(value);
                vars.add(new Pair(String.valueOf(constant), false));
            } catch (NumberFormatException ex) {
                Pair<String, Boolean> var = new Pair(value, true);
                if (!vars.contains(var))
                    vars.add(var);
            }
        }

        Pair<String, Boolean>[] result = new Pair[vars.size()];
        result = vars.toArray(result);
        return result;
    }

    /**
     * Получает таблицу истинности, учитывая константы.
     *
     * @param expression            Выражение.
     * @param varNamesWithConstants Имена переменных и константы.
     * @return Таблицу истинности, учитывающих константы.
     */
    private static Pair<Integer[], Integer>[] getTruthTableWithConstants(String expression, Pair<String, Boolean>[] varNamesWithConstants) {
        int length = (int) Math.pow(2, Arrays.stream(varNamesWithConstants).filter(n -> n.getValue()).count());

        Pair<Integer[], Integer>[] table = new Pair[length];
        for (int i = 0; i < length; i++) {
            Integer[] varValues = new Integer[varNamesWithConstants.length];

            int iterator = 0;
            for (int j = 0; j < varNamesWithConstants.length; j++) {
                if (varNamesWithConstants[j].getValue()) {
                    varValues[j] = (i >> iterator) & 1;
                    iterator++;
                } else {
                    varValues[j] = Integer.parseInt(varNamesWithConstants[j].getKey());
                }
            }

            Constant[] cons = IntStream.range(0, varNamesWithConstants.length).filter(index -> varNamesWithConstants[index].getValue()).mapToObj(index -> new Constant(varNamesWithConstants[index].getKey(), varValues[index])).collect(Collectors.toList()).toArray(new Constant[0]);
            Expression ex = new Expression(expression, cons);
            table[i] = new Pair(varValues, (int) ex.calculate());
        }

        return table;
    }
}
