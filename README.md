utilização: java FormataExtratoBradesco extrato.csv

    extrato.csv não deve ter cabeçalho, nem os dados abaixo da linha de total inclusive.
    Isso é para se observar o arquivo a fim de ver se não foi alterado o formato, visto que já ocorreu antes.

FormataExtratoBradesco.java
    - formata extrato em csv do Bradesco, transformando o campo da descrição em uma única linha.

FormataExtratoBradescoMD5.java
    - formata extrato em csv do Bradesco, transformando o campo da descrição em uma única linha e adiciona um campo a mais 
    com o hash md5 da concatenação dos dados do registro a fim de se gerar um ID para o registro.

FormataExtratoBradescoDataInvertida.java
    - formata extrato em csv do Bradesco, transformando o campo da descrição em uma única linha, inverte a data para ANO-MES-DIA  e adiciona um campo a mais com o hash md5 da concatenação dos dados do registro a fim de se gerar um ID para o registro.