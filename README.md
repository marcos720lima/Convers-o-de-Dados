# Conversão de Dados - Teste 

Este projeto foi desenvolvido como parte de um teste pessoal, com o objetivo de extrair, transformar e processar dados de um PDF da ANS (Agência Nacional de Saúde Suplementar).

## Funcionalidades

- Extração de dados de tabelas de PDF
- Conversão para formato CSV
- Compactação em arquivo ZIP
- Substituição de abreviações por descrições completas

## Requisitos

- Java 17 ou superior
- Maven
- Dependências listadas no `pom.xml`

## Estrutura do Projeto

```
Conversao_de_Dados/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── meuapp/
│                   └── TesteTransformacaoDados.java
├── pom.xml
└── README.md
```

## Como Executar

1. Clone o repositório
2. Execute `mvn clean install`
3. Execute `mvn exec:java -Dexec.mainClass="com.meuapp.TesteTransformacaoDados"`

## Saída

O programa gera:
- Um arquivo CSV com os dados extraídos
- Um arquivo ZIP contendo o CSV
- Substituição das abreviações OD e AMB por suas descrições completas

## Autor
MARCOS VINICIUS VIANA LIMA
