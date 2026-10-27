# Gramática do extrato CSV

Gramática da entrada aceita por `ExtratoBradesco.le`, a leitura usada por todos os programas.
O arquivo é usado como vem do banco (ex.: `Bradesco_27102026_202404.csv`): ISO-8859-1, linhas terminadas em CR.

## Gramática (EBNF)

```ebnf
arquivo            = cabecalho1 , cabecalho2 , { registro } , linha_total , { qualquer_linha } ;

cabecalho1         = "Extrato de: Ag: " , num , " | Conta: " , num_conta ,
                     " | Entre " , data4 , " e " , data4 , FIM_LINHA ;
cabecalho2         = "Data;Histórico;Docto.;Crédito (R$);Débito (R$);Saldo (R$);" , FIM_LINHA ;

registro           = linha_transacao , { linha_continuacao } ;
linha_transacao    = data , ";" , descricao , ";" , codigo , ";" , credito , ";" , debito ,
                     ";" , saldo , { ";" , campo } , FIM_LINHA ;
linha_continuacao  = ";" , texto_desc , { ";" , campo } , FIM_LINHA ;
linha_total        = ";Total;" , { qualquer caractere } , FIM_LINHA ;

data               = dig , dig , "/" , dig , dig , "/" , dig , dig ;   (* dd/mm/aa *)
data4              = dig , dig , "/" , dig , dig , "/" , dig , dig , dig , dig ;
credito, debito    = valor | "" ;
saldo              = valor | "" ;        (* lido, mas ignorado *)
valor              = '"' , [ "-" ] , dig , { dig | "." } , "," , dig , dig , '"' ;   (* ex.: "-1.234,56" *)

campo              = { qualquer caractere exceto ";" e FIM_LINHA } ;
FIM_LINHA          = "\r" | "\n" | "\r\n" ;
```

## Regras

- As linhas 1 e 2 são conferidas contra o padrão acima. Se não baterem, o programa escreve em stderr a linha,
  o esperado e o encontrado, e termina com código 1. Isso serve para perceber quando o banco mudar o layout de novo.
- O corpo vai da linha 3 até a linha anterior ao primeiro `;Total;`. O que vem depois ("Últimos Lançamentos",
  "Saldos Invest Fácil") é ignorado. Se não houver `;Total;`, é erro.
- A transação com descrição `SALDO ANTERIOR` (normalmente a linha 3) é ignorada.
- Cada linha de continuação (começa com `;`) acrescenta `" " + texto_desc` à descrição da transação anterior.
- Qualquer outra linha no corpo é erro.
- Os valores saem como vêm, com aspas e com o débito negativo. Exceções: `FormataExtratoBradescoSHA256` e `geraSQL`
  tiram as aspas e o `.`, e o `geraSQL` também troca `,` por `.`.
- `debito` vale `"0"` quando a linha não tem o 5º campo. Isso acontece porque `split` descarta campos vazios no fim
  (ex.: `07/08/26; Rent.inv.facil;0000002;"0,01";;`). Com saldo presente, o débito vazio fica `""`.
- A descrição mantém o espaço inicial que o banco coloca (` Pix Enviado`).
- Aspas não são interpretadas: um `;` dentro de um campo entre aspas divide o campo.
