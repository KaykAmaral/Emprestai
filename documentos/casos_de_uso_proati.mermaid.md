flowchart LR
    SUPREMO[ADM SUPREMO]
    PROATI[ADM PROATI]
    PROFESSOR[PROFESSOR]

    subgraph SISTEMA["Sistema PROATI"]
        UC0(["Fazer login"])
        UC1(["Cadastrar unidade escolar"])
        UC2(["Editar/inativar unidade"])
        UC3(["Cadastrar PROATI"])
        UC4(["Visualizar dashboard geral"])
        UC5(["Cadastrar professor"])
        UC6(["Editar/inativar professor"])
        UC7(["Cadastrar equipamento"])
        UC8(["Editar/dar baixa equipamento"])
        UC9(["Analisar solicitação de reserva"])
        UC9a(["Aprovar reserva"])
        UC9b(["Recusar reserva"])
        UC10(["Registrar retirada"])
        UC11(["Registrar devolução"])
        UC12(["Consultar histórico de reservas"])
        UC13(["Consultar disponibilidade por tipo"])
        UC14(["Solicitar reserva"])
        UC15(["Cancelar reserva"])
        UC16(["Acompanhar status da reserva"])
    end

    SUPREMO --> UC0
    SUPREMO --> UC1
    SUPREMO --> UC2
    SUPREMO --> UC3
    SUPREMO --> UC4

    PROATI --> UC0
    PROATI --> UC5
    PROATI --> UC6
    PROATI --> UC7
    PROATI --> UC8
    PROATI --> UC9
    PROATI --> UC10
    PROATI --> UC11
    PROATI --> UC12
    PROATI --> UC15

    PROFESSOR --> UC0
    PROFESSOR --> UC13
    PROFESSOR --> UC14
    PROFESSOR --> UC15
    PROFESSOR --> UC16

    UC9a -.->|generaliza| UC9
    UC9b -.->|generaliza| UC9
    UC14 -.->|include| UC13

    classDef ator fill:#E6F1FB,stroke:#185FA5,color:#0C447C
    class SUPREMO,PROATI,PROFESSOR ator
