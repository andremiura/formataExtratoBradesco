#!/usr/bin/env python3

import sys
import os

if len(sys.argv) != 3:
    print(f"Uso: {sys.argv[0]} <opcao> <arquivo>")
    sys.exit(1)

opcao = sys.argv[1]
arquivo = sys.argv[2]

if not os.path.isfile(arquivo):
    print(f"O arquivo '{arquivo}' nao existe.")
    sys.exit(1)

def executar_programa(opcao, arquivo):
    os.system(f"java {opcao} {arquivo}")

if opcao == "-b":
    executar_programa("FormataExtratoBradesco", arquivo)
elif opcao == "-di":
    executar_programa("FormataExtratoBradescoDataInvertida", arquivo)
elif opcao == "-md5":
    executar_programa("FormataExtratoBradescoMD5", arquivo)
elif opcao == "-sha256":
    executar_programa("FormataExtratoBradescoSHA256", arquivo)
else:
    print(f"Opção inválida: {opcao}")
    sys.exit(1)
