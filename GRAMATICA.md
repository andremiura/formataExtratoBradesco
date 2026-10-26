# Gramática do extrato CSV

Gramática da entrada aceita por `FormataExtratoBradesco.java`, deduzida do laço de leitura
(`main` → `BufferedReader` → `do/while`). As outras variantes usam o mesmo laço, então valem as mesmas regras.

O arquivo já deve estar sem o cabeçalho do banco e sem as linhas a partir do "total" (ver README).

## Gramática pretendida (EBNF)

```ebnf
arquivo            = { registro } ;
registro           = linha_transacao , [ linha_continuacao ] ;

linha_transacao    = data , ";" , descricao , ";" , codigo , ";" , credito ,
                     [ ";" , debito , [ ";" , saldo , { ";" , campo } ] ] , FIM_LINHA ;
linha_continuacao  = campo , ";" , texto_desc , FIM_LINHA ;   (* exatamente 2 campos *)

data               = dig , dig , "/" , dig , dig , "/" , dig , dig ;   (* dd/mm/aa *)
descricao          = campo ;
codigo             = campo ;
credito            = valor | "" ;
debito             = valor | "" ;        (* ausente → "0" *)
saldo              = campo ;             (* lido, mas ignorado *)
valor              = dig , { dig | "." } , [ "," , dig , dig ] ;   (* ex.: 1.234,56 *)

campo              = { qualquer caractere exceto ";" e FIM_LINHA } ;
texto_desc         = campo não vazio ;
FIM_LINHA          = "\n" | "\r\n" | "\r" ;
```

A `descricao` de saída é `descricao + " " + texto_desc` quando há linha de continuação.
O primeiro campo da continuação é descartado.

## O que o código aceita de fato

O programa não valida conteúdo: `data` e `valor` podem ter qualquer texto. O que decide como cada linha
é tratada é só a quantidade de campos devolvida por `line.split(";")`:

| Campos | Tratamento |
|---|---|
| 1 (inclui linha em branco) | `ArrayIndexOutOfBoundsException`, o programa aborta |
| 2 | continuação, se vier logo após uma transação |
| 3 | `ArrayIndexOutOfBoundsException`, o programa aborta |
| 4 | transação, `debito = "0"` |
| 5 ou mais | transação, do 6º campo em diante é ignorado |

Armadilhas:

- `split` descarta campos vazios no fim: `a;b;c;;` tem 3 campos (aborta) e `x;y;;;` tem 2 (vira continuação).
  Por isso, uma transação sem crédito e sem débito derruba o programa.
- Só uma continuação por registro. Se vierem duas seguidas, a segunda é tratada como transação de 2 campos:
  nenhuma variável é atualizada e o registro anterior é impresso **de novo**, sem o texto da segunda continuação.
- Se o arquivo começar com uma linha de 2 campos, a saída é `null;null;...`. Se a linha seguinte também
  tiver 2 campos, ocorre `NullPointerException`.
- Aspas não são tratadas: um `;` dentro de um campo entre aspas divide o campo.
