[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)
![GitHub stars](https://img.shields.io/github/stars/guto-alves/loterias-api)

<p align="center">
  <img src="https://user-images.githubusercontent.com/48946749/147809259-e7b15a3b-2e90-42c2-abaf-a6cacdc77e03.png">
  <h2 align="center">API Loterias CAIXA</h2>
  <p align="center">
    API de resultados de jogos das <a href="https://loterias.caixa.gov.br/wps/portal/loterias">Loterias da CAIXA</a>.<br>
  </p>
</p>

## Doação (*Importante!*)

A hospedagem desta API é feita em um servidor que **não é gratuito**.  
Para que o projeto continue online, funcional e acessível a todos, existem custos mensais inevitáveis.

Por isso, pedimos uma **gentil contribuição** de sua parte.  
Se este projeto é útil para você de alguma forma, sua ajuda faz toda a diferença para mantê-lo vivo.

### Contribuições via PIX (valores sugeridos)

| R$ 1.000 | R$ 100 | R$ 35 |
|----------|--------|-------|
| <div align="center"><img src="https://github.com/user-attachments/assets/ad4bb15d-9b82-4e50-a5c9-b035ccaffff8" width="140"><br><strong>Apoio premium</strong></div> | <div align="center"><img src="https://github.com/user-attachments/assets/7d0c1a16-67ef-452a-945c-bdd4b8e0ceed" width="140"><br><strong>Apoio fundamental</strong></div> | <div align="center"><img src="https://github.com/user-attachments/assets/11681c54-7775-4c46-88ae-9b22a71d164c" width="140"><br><strong>Apoio básico</strong></div> |
| Apoio forte para garantir a continuidade do projeto por 1 ano | Contribuição significativa para os custos mensais | Ajuda simbólica para manter o serviço ativo |
| Para quem depende do serviço | Para quem usa com alguma frequência | Ideal para quem quer apenas colaborar |

<img width="397" height="410" alt="image" src="https://github.com/user-attachments/assets/ad4bb15d-9b82-4e50-a5c9-b035ccaffff8" />


Após fazer sua contribuição entre em contato através do email [support@quantingo.com](support@quantingo.com).

Caso queira contribuir com um valor maior, entre em contato conosco.

## Endereço base da API
O BASEADDRESS da API será informado quando você fizer sua doação. Nos campos abaixo, substitua `<BASEADDRESS>` pelo endereço fornecido.

## Exemplos de Retorno
Atualmente o banco de dados contém os jogos das loterias ...

`<BASEADDRESS>`/api

```
[
  "maismilionaria",
  "megasena",
  "lotofacil",
  "quina",
  "lotomania",
  "timemania",
  "duplasena",
  "federal",
  "diadesorte",
  "supersete"
]
```

### **Obtendo o Resultado Mais Recente**

URL BASE: ```https://<BASEADDRESS>/api/<loteria>/latest```

Apenas substitua ```<loteria>``` pelo nome da loteria desejada. 

```
{
  "loteria": "megasena",
  "concurso": 2620,
  "data": "12/08/2023",
  "local": "ESPAÇO DA SORTE em SÃO PAULO, SP",
  "dezenasOrdemSorteio": [
    "26",
    "21",
    "13",
    "04",
    "06",
    "28"
  ],
  "dezenas": [
    "04",
    "06",
    "13",
    "21",
    "26",
    "28"
  ],
  "trevos": [
    
  ],
  "timeCoracao": null,
  "mesSorte": null,
  "premiacoes": [
    {
      "descricao": "6 acertos",
      "faixa": 1,
      "ganhadores": 4,
      "valorPremio": 29058128.28
    },
    {
      "descricao": "5 acertos",
      "faixa": 2,
      "ganhadores": 404,
      "valorPremio": 23042.04
    },
    {
      "descricao": "4 acertos",
      "faixa": 3,
      "ganhadores": 21667,
      "valorPremio": 613.76
    }
  ],
  "estadosPremiados": [
    
  ],
  "observacao": "",
  "acumulou": false,
  "proximoConcurso": 2621,
  "dataProximoConcurso": "16/08/2023",
  "localGanhadores": [
    {
      "ganhadores": 1,
      "municipio": "CANAL ELETRONICO",
      "nomeFatansiaUL": "",
      "serie": "",
      "posicao": 1,
      "uf": "--"
    },
    {
      "ganhadores": 1,
      "municipio": "UBERABA",
      "nomeFatansiaUL": "",
      "serie": "",
      "posicao": 1,
      "uf": "MG"
    },
    {
      "ganhadores": 2,
      "municipio": "SINOP",
      "nomeFatansiaUL": "",
      "serie": "",
      "posicao": 1,
      "uf": "MT"
    }
  ],
  "valorArrecadado": 161458740,
  "valorAcumuladoConcurso_0_5": 10778824.03,
  "valorAcumuladoConcursoEspecial": 72866210.44,
  "valorAcumuladoProximoConcurso": 0.0,
  "valorEstimadoProximoConcurso": 3500000.0
}
```

-  **Observações**: Os campos <i><b>timeCoracao</b></i> e <i><b>mesSorte</b></i> só terão algum valor quando a loteria pesquisada for Timemania (timemania) ou Dia de Sorte (diadesorte) respectivamente.

## Documentação da API
 
Para mais informações sobre todas as operações da API acesse: 

**https://<BASEADDRESS>/swagger-ui/#/Loterias**

![image](https://user-images.githubusercontent.com/48946749/144352143-7140d64d-43a9-465c-b12c-7d5d3514ccd5.png)

## Contribuição

Quaisquer contribuições para este repositório são bem-vindas! 
