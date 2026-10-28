utilização: java FormataExtratoBradesco extrato.csv

    extrato.csv é usado como vem do banco (Bradesco_*.csv), com cabeçalho e linha de total.
    O cabeçalho é conferido contra o padrão conhecido; se o banco mudar o formato, o programa
    mostra a linha divergente e termina com erro. Ver GRAMATICA.md.

ExtratoBradesco.java
    - leitura do extrato usada por todos os programas: confere o cabeçalho e junta as linhas de continuação.

FormataExtratoBradesco.java
    - formata extrato em csv do Bradesco, transformando o campo da descrição em uma única linha.

FormataExtratoBradescoMD5.java
    - formata extrato em csv do Bradesco, transformando o campo da descrição em uma única linha e adiciona um campo a mais 
    com o hash md5 da concatenação dos dados do registro a fim de se gerar um ID para o registro.

FormataExtratoBradescoDataInvertida.java
    - formata extrato em csv do Bradesco, transformando o campo da descrição em uma única linha, inverte a data para ANO-MES-DIA  e adiciona um campo a mais com o hash md5 da concatenação dos dados do registro a fim de se gerar um ID para o registro.
GRAMATICA.md
    - gramática do CSV de entrada e os casos que fazem os programas falhar.

formataExtratoBradesco.c
    - versão em C do FormataExtratoBradesco (mesma saída), para usar como um comando único, sem Java.
    Compilar em cada máquina (Linux ou Mac; no Mac, instalar antes as ferramentas com xcode-select --install):

        cc -std=c99 -Wall -Wextra -O2 formataExtratoBradesco.c -o formataExtratoBradesco
        ./formataExtratoBradesco extrato.csv
