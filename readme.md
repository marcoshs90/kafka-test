# Projeto para desenvolver serviços com Kafka baixo nível

### Sobre
A aplicação é uma representação de um ecommerce que se comunica através de mensagens, com aplicação de serviços de log, persistencia em banco de dados e HTTP. Além das comunicações foi configurado para tentar diminuir a possibilidade de mensagens duplicadas, falha ao pegar os dados e clusters de serviços para redundância.

### Para atingir esse objetivo foram utlizadas as seguintes tecnologias:

Java 12
Maven
SQLite

### Sobre a criação do projeto

Foi utilizado o conceito de monorepo, onde utilizei a criação de module do Java. Abstrai camadas que era de comum utilização para que não acontecesse replicação de código, além disso mesmo não sendo o objetivo inicial foi cuidado padrões de clean code e SOLID (a muito o que melhorar, tendo em vista que o objetivo era a parte do kafka mesmo).

### Execução Local
Requisitos -> Java 11 ou superior
Git

### Execução
Clonar o repositório
Startar o Kafka Local
E rodar os serviços
