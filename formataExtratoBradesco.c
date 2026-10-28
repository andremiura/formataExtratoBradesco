/*
 * formataExtratoBradesco.c
 *
 * Versão em C do FormataExtratoBradesco.java (+ ExtratoBradesco.java).
 * Lê o extrato CSV do Bradesco como vem do banco (ver GRAMATICA.md), confere
 * o cabeçalho e imprime uma linha por transação, com a descrição já juntada.
 * A saída é igual, byte a byte, à da versão Java.
 *
 * compilar: cc -std=c99 -Wall -Wextra -O2 formataExtratoBradesco.c -o formataExtratoBradesco
 * uso:      ./formataExtratoBradesco extrato.csv
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* padrão do cabeçalho; se o banco mudar o layout, a leitura aborta aqui */
#define LINHA1_PADRAO "^Extrato de: Ag: \\d+ \\| Conta: [\\d-]+ \\| Entre \\d{2}/\\d{2}/\\d{4} e \\d{2}/\\d{2}/\\d{4}$"
/* linha 2 em ISO-8859-1, como vem no arquivo */
#define LINHA2 "Data;Hist\xF3rico;Docto.;Cr\xE9" "dito (R$);D\xE9" "bito (R$);Saldo (R$);"
#define TOTAL ";Total;"
#define SALDO_ANTERIOR "SALDO ANTERIOR"

static const char *fileName;

static void *aloca(void *p, size_t n)
{
    p = realloc(p, n);
    if (p == NULL) {
        fprintf(stderr, "memória insuficiente\n");
        exit(1);
    }
    return p;
}

static char *copia(const char *s)
{
    size_t n = strlen(s) + 1;
    return memcpy(aloca(NULL, n), s, n);
}

/* escreve texto ISO-8859-1 convertido para UTF-8 */
static void escreveUtf8(FILE *saida, const char *s)
{
    const unsigned char *p;
    for (p = (const unsigned char *) s; *p; p++) {
        if (*p < 0x80)
            fputc(*p, saida);
        else {
            fputc(0xC0 | (*p >> 6), saida);
            fputc(0x80 | (*p & 0x3F), saida);
        }
    }
}

static void erro(int numLinha, const char *esperado, const char *encontrado)
{
    fprintf(stderr, "Layout do extrato '%s' diferente do esperado, linha %d.\n", fileName, numLinha);
    fprintf(stderr, "  esperado:   %s\n", esperado);
    fprintf(stderr, "  encontrado: ");
    escreveUtf8(stderr, encontrado);
    fprintf(stderr, "\n");
    exit(1);
}

/*
 * Lê uma linha terminada em \r, \n ou \r\n, como BufferedReader.readLine.
 * Devolve NULL no fim do arquivo. O buffer é reaproveitado entre chamadas.
 */
static char *leLinha(FILE *f)
{
    static char *buf = NULL;
    static size_t cap = 0;
    size_t n = 0;
    int c;

    while ((c = getc(f)) != EOF && c != '\r' && c != '\n') {
        if (n + 1 >= cap) {
            cap = cap ? cap * 2 : 256;
            buf = aloca(buf, cap);
        }
        buf[n++] = (char) c;
    }

    if (c == EOF && n == 0)
        return NULL;

    if (c == '\r') {
        c = getc(f);
        if (c != '\n' && c != EOF)
            ungetc(c, f);
    }

    if (buf == NULL) {
        cap = 256;
        buf = aloca(NULL, cap);
    }
    buf[n] = '\0';
    return buf;
}

/*
 * Divide a linha em ';' como String.split(";") do Java: os campos vazios no
 * fim são descartados. Altera a linha; devolve o número de campos.
 */
static int divide(char *line, char ***campos)
{
    static char **v = NULL;
    static int cap = 0;
    int n = 0;
    char *p = line;

    if (*line == '\0') {
        /* "".split(";") devolve [""] */
        if (cap < 1) {
            cap = 8;
            v = aloca(v, cap * sizeof *v);
        }
        v[0] = line;
        *campos = v;
        return 1;
    }

    for (;;) {
        char *fim = strchr(p, ';');
        if (n >= cap) {
            cap = cap ? cap * 2 : 8;
            v = aloca(v, cap * sizeof *v);
        }
        v[n++] = p;
        if (fim == NULL)
            break;
        *fim = '\0';
        p = fim + 1;
    }

    while (n > 0 && *v[n - 1] == '\0')
        n--;

    *campos = v;
    return n;
}

static int digito(char c)
{
    return c >= '0' && c <= '9';
}

/* consome exatamente n dígitos */
static int digitos(const char **p, int n)
{
    while (n-- > 0) {
        if (!digito(**p))
            return 0;
        (*p)++;
    }
    return 1;
}

static int literal(const char **p, const char *s)
{
    size_t n = strlen(s);
    if (strncmp(*p, s, n) != 0)
        return 0;
    *p += n;
    return 1;
}

static int data4(const char **p)
{
    return digitos(p, 2) && literal(p, "/") && digitos(p, 2) && literal(p, "/") && digitos(p, 4);
}

/* equivalente a LINHA1_PADRAO */
static int casaLinha1(const char *line)
{
    const char *p = line;

    if (!literal(&p, "Extrato de: Ag: ") || !digito(*p))
        return 0;
    while (digito(*p))
        p++;

    if (!literal(&p, " | Conta: ") || !(digito(*p) || *p == '-'))
        return 0;
    while (digito(*p) || *p == '-')
        p++;

    return literal(&p, " | Entre ") && data4(&p) && literal(&p, " e ") && data4(&p) && *p == '\0';
}

/* dd/mm/aa; no início da linha */
static int ehTransacao(const char *line)
{
    const char *p = line;
    return digitos(&p, 2) && literal(&p, "/") && digitos(&p, 2) && literal(&p, "/")
        && digitos(&p, 2) && *p == ';';
}

/* compara ignorando espaços e controles nas pontas, como String.trim */
static int igualSemEspacos(const char *s, const char *alvo)
{
    size_t n;
    while (*s && (unsigned char) *s <= ' ')
        s++;
    n = strlen(s);
    while (n > 0 && (unsigned char) s[n - 1] <= ' ')
        n--;
    return n == strlen(alvo) && strncmp(s, alvo, n) == 0;
}

/* {data, descricao, codigo, credito, debito} */
typedef struct {
    char *campo[5];
} Registro;

int main(int argc, char *argv[])
{
    FILE *f;
    char *line;
    char **campos;
    int numCampos;
    int numLinha = 2;
    int ignorando = 0;   /* dentro do SALDO ANTERIOR */
    Registro *registros = NULL;
    size_t numRegistros = 0, cap = 0;
    Registro *registro = NULL;
    size_t i;

    fileName = argc >= 2 ? argv[1] : "";

    f = fopen(fileName, "rb");
    if (f == NULL) {
        printf("Não foi possível abrir o arquivo '%s'\n", fileName);
        printf("uso: formataExtratoBradesco extrato.csv\n");
        return 1;
    }

    line = leLinha(f);
    if (line == NULL || !casaLinha1(line))
        erro(1, LINHA1_PADRAO, line ? line : "null");

    line = leLinha(f);
    if (line == NULL || strcmp(line, LINHA2) != 0)
        erro(2, "Data;Histórico;Docto.;Crédito (R$);Débito (R$);Saldo (R$);", line ? line : "null");

    for (;;) {
        line = leLinha(f);
        numLinha++;

        if (line == NULL)
            erro(numLinha, "linha começando com " TOTAL, "fim do arquivo");

        if (strncmp(line, TOTAL, strlen(TOTAL)) == 0)
            break;

        if (ehTransacao(line)) {
            numCampos = divide(line, &campos);

            if (igualSemEspacos(campos[1], SALDO_ANTERIOR)) {
                registro = NULL;
                ignorando = 1;
                continue;
            }

            if (numRegistros == cap) {
                cap = cap ? cap * 2 : 64;
                registros = aloca(registros, cap * sizeof *registros);
            }
            registro = &registros[numRegistros++];
            registro->campo[0] = copia(campos[0]);
            registro->campo[1] = copia(campos[1]);
            registro->campo[2] = copia(numCampos > 2 ? campos[2] : "");
            registro->campo[3] = copia(numCampos > 3 ? campos[3] : "");
            registro->campo[4] = copia(numCampos > 4 ? campos[4] : "0");
            ignorando = 0;
        }
        else if (line[0] == ';' && (registro != NULL || ignorando)) {
            /* continuação da descrição da transação anterior */
            numCampos = divide(line, &campos);
            if (registro != NULL && numCampos > 1) {
                char *d = registro->campo[1];
                size_t n = strlen(d);
                d = aloca(d, n + 1 + strlen(campos[1]) + 1);
                d[n] = ' ';
                strcpy(d + n + 1, campos[1]);
                registro->campo[1] = d;
            }
        }
        else
            erro(numLinha, "transação (dd/mm/aa;...) ou continuação (;...)", line);
    }

    fclose(f);

    for (i = 0; i < numRegistros; i++) {
        int j;
        for (j = 0; j < 5; j++) {
            escreveUtf8(stdout, registros[i].campo[j]);
            putchar(';');
        }
        printf(";\n");
    }

    return 0;
}
