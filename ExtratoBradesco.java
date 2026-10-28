import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Lê o extrato CSV do Bradesco como vem do banco (ver GRAMATICA.md).
 *
 * Confere o cabeçalho (linhas 1 e 2) contra o padrão conhecido e aborta se o
 * layout mudou. Devolve as transações entre o cabeçalho e a linha de Total,
 * seguidas das da seção "Últimos Lançamentos", se houver, com as linhas de
 * continuação já concatenadas na descrição.
 */
public class ExtratoBradesco {

    // padrão do cabeçalho; se o banco mudar o layout, a leitura aborta aqui
    static final Pattern LINHA1 = Pattern.compile(
        "^Extrato de: Ag: \\d+ \\| Conta: [\\d-]+ \\| Entre \\d{2}/\\d{2}/\\d{4} e \\d{2}/\\d{2}/\\d{4}$");
    static final String LINHA2 = "Data;Histórico;Docto.;Crédito (R$);Débito (R$);Saldo (R$);";

    static final Pattern TRANSACAO = Pattern.compile("^\\d{2}/\\d{2}/\\d{2};.*");
    static final String TOTAL = ";Total;";
    static final String SALDO_ANTERIOR = "SALDO ANTERIOR";

    // seção depois do Total com os lançamentos posteriores ao período
    static final String ULTIMOS = "Últimos Lançamentos";
    static final String LINHA_ULTIMOS = "Data;Histórico;Docto.;Crédito (R$);Débito (R$);";

    /**
     * @return lista de {data, descricao, codigo, credito, debito}
     */
    public static List<String[]> le(String fileName) throws IOException {

        List<String[]> registros = new ArrayList<>();

        // o arquivo do banco vem em ISO-8859-1, com quebra de linha CR
        BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(fileName), StandardCharsets.ISO_8859_1));

        try {
            String line = bufferedReader.readLine();
            if (line == null || !LINHA1.matcher(line).matches())
                erro(fileName, 1, LINHA1.pattern(), line);

            line = bufferedReader.readLine();
            if (line == null || !line.equals(LINHA2))
                erro(fileName, 2, LINHA2, line);

            int numLinha = leRegistros(bufferedReader, fileName, 2, registros);

            // seção opcional depois do Total; o resto do arquivo é ignorado
            while ((line = bufferedReader.readLine()) != null) {
                numLinha++;
                if (line.equals(ULTIMOS)) {
                    line = bufferedReader.readLine();
                    numLinha++;
                    if (line == null || !line.equals(LINHA_ULTIMOS))
                        erro(fileName, numLinha, LINHA_ULTIMOS, line);

                    leRegistros(bufferedReader, fileName, numLinha, registros);
                    break;
                }
            }
        }
        finally {
            bufferedReader.close();
        }

        return registros;
    }

    /**
     * Lê transações até a linha de Total, acrescentando-as em registros.
     *
     * @return número da linha de Total
     */
    static int leRegistros(BufferedReader bufferedReader, String fileName, int numLinha,
                           List<String[]> registros) throws IOException {

        String[] registro = null;
        boolean ignorando = false;   // dentro do SALDO ANTERIOR

        while (true) {
            String line = bufferedReader.readLine();
            numLinha++;

            if (line == null)
                erro(fileName, numLinha, "linha começando com " + TOTAL, "fim do arquivo");

            if (line.startsWith(TOTAL))
                break;

            String[] campos = line.split(";");

            if (TRANSACAO.matcher(line).matches()) {
                if (registro != null)
                    registros.add(registro);

                if (campos[1].trim().equals(SALDO_ANTERIOR)) {
                    registro = null;
                    ignorando = true;
                    continue;
                }

                registro = new String[] {
                    campos[0],
                    campos[1],
                    campos.length > 2 ? campos[2] : "",
                    campos.length > 3 ? campos[3] : "",
                    campos.length > 4 ? campos[4] : "0"
                };
                ignorando = false;
            }
            else if (line.startsWith(";") && (registro != null || ignorando)) {
                // continuação da descrição da transação anterior
                if (registro != null && campos.length > 1)
                    registro[1] = registro[1].concat(" " + campos[1]);
            }
            else
                erro(fileName, numLinha, "transação (dd/mm/aa;...) ou continuação (;...)", line);
        }

        if (registro != null)
            registros.add(registro);

        return numLinha;
    }

    static void erro(String fileName, int numLinha, String esperado, String encontrado) {
        System.err.println("Layout do extrato '" + fileName + "' diferente do esperado, linha " + numLinha + ".");
        System.err.println("  esperado:   " + esperado);
        System.err.println("  encontrado: " + encontrado);
        System.exit(1);
    }
} //end-class
