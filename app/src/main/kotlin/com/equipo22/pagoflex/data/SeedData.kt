package com.equipo22.pagoflex.data

object SeedData {

    data class SeedPayment(
        val folio: String,
        val numeroComprobante: String,
        val fechaHora: String,
        val montoPagado: Long,
        val canal: String,
        val medioPago: String,
        val aTiempo: Boolean
    )

    data class SeedReport(
        val folio: String,
        val numero: String,
        val motivo: String,
        val comentario: String?,
        val fechaCreacion: String,
        val estado: String,
        val agenteId: Int?,
        val fechaToma: String?,
        val respuesta: String?,
        val fechaResolucion: String?
    )

    data class ComplianceTarget(val total: Int, val onTime: Int)

    val companies = listOf(
        CompanyEntity(1, "EC-01", "Inmobiliaria Altos del Maipo SpA", "Altos del Maipo", "76.845.213-K", "Inmobiliaria", "atencion@altosmaipo.ejemplo", "+56 2 0000 0201", 8500),
        CompanyEntity(2, "EC-02", "Corporación Educacional Bosque Nativo", "Colegio Bosque Nativo", "65.132.874-8", "Educación escolar", "pagos@bosquenativo.ejemplo", "+56 2 0000 0202", 5000),
        CompanyEntity(3, "EC-03", "Crédito Andino S.A.", "Crédito Andino", "96.571.438-3", "Crédito de consumo", "clientes@creditoandino.ejemplo", "+56 2 0000 0203", 12000),
        CompanyEntity(4, "EC-04", "Seguros Horizonte Sur S.A.", "Horizonte Sur", "96.814.205-4", "Seguros", "contacto@horizontesur.ejemplo", "+56 2 0000 0204", 3000),
        CompanyEntity(5, "EC-05", "Fundación Manos Abiertas", "Manos Abiertas", "65.247.190-0", "ONG", "socios@manosabiertas.ejemplo", "+56 2 0000 0205", 0),
        CompanyEntity(6, "EC-06", "Club Deportivo Los Halcones", "Club Los Halcones", "65.398.412-K", "Club deportivo", "tesoreria@loshalcones.ejemplo", "+56 2 0000 0206", 2000),
        CompanyEntity(7, "EC-07", "Instituto Técnico Cordillera SpA", "Instituto Cordillera", "77.214.583-7", "Educación superior", "finanzas@itcordillera.ejemplo", "+56 2 0000 0207", 6000),
        CompanyEntity(8, "EC-08", "Arriendos Plaza Oriente Ltda.", "Plaza Oriente", "76.432.871-K", "Administración de arriendos", "cobranza@plazaoriente.ejemplo", "+56 2 0000 0208", 7500)
    )

    val users = listOf(
        UserEntity(1, "UF-01", Roles.USUARIO_FINAL, "15.834.207-3", "Camila Andrea", "Rojas Peña", "crojas@correo.ejemplo", "+56 9 0000 0101", "Maipú", "14-03-2023", true, 3),
        UserEntity(2, "UF-02", Roles.USUARIO_FINAL, "12.476.318-5", "Luis Alberto", "Muñoz Tapia", "lmunoz@correo.ejemplo", "+56 9 0000 0102", "Puente Alto", "02-11-2023", true, 1),
        UserEntity(3, "UF-03", Roles.USUARIO_FINAL, "6.893.541-5", "Rosa Elena", "Contreras Díaz", "rcontreras@correo.ejemplo", "+56 9 0000 0103", "San Miguel", "20-06-2024", true, 5),
        UserEntity(4, "UF-04", Roles.USUARIO_FINAL, "21.045.873-5", "Matías Ignacio", "Soto Vergara", "msoto@correo.ejemplo", "+56 9 0000 0104", "La Florida", "07-03-2025", false, 3),
        UserEntity(5, "UF-05", Roles.USUARIO_FINAL, "17.562.904-1", "Fernanda Paz", "Godoy Lagos", "fgodoy@correo.ejemplo", "+56 9 0000 0105", "Ñuñoa", "15-01-2024", true, 3),
        UserEntity(6, "UF-06", Roles.USUARIO_FINAL, "10.318.265-4", "Héctor Manuel", "Pizarro Campos", "hpizarro@correo.ejemplo", "+56 9 0000 0106", "Quilicura", "28-08-2023", true, 3),
        UserEntity(7, "UF-07", Roles.USUARIO_FINAL, "19.287.430-0", "Javiera Constanza", "Reyes Molina", "jreyes@correo.ejemplo", "+56 9 0000 0107", "San Bernardo", "03-05-2025", true, 1),
        UserEntity(8, "UF-08", Roles.USUARIO_FINAL, "18.704.396-4", "Benjamín Esteban", "Araya Fuentes", "baraya@correo.ejemplo", "+56 9 0000 0108", "Estación Central", "11-09-2026", true, 3),
        UserEntity(101, "AG-01", Roles.AGENTE, "16.248.317-K", "Andrés Felipe", "Lagos Meza", "alagos@pagoflex.ejemplo", "", "", "", true, 3, "Soporte"),
        UserEntity(102, "AG-02", Roles.AGENTE, "18.352.091-1", "Daniela Sofía", "Ortiz Bravo", "dortiz@pagoflex.ejemplo", "", "", "", true, 3, "Soporte"),
        UserEntity(103, "AG-03", Roles.AGENTE, "13.907.264-2", "Ricardo José", "Salinas Cáceres", "rsalinas@pagoflex.ejemplo", "", "", "", false, 3, "Soporte"),
        UserEntity(104, "AG-04", Roles.AGENTE, "17.781.435-0", "Valentina Ignacia", "Paredes Silva", "vparedes@pagoflex.ejemplo", "", "", "", true, 3, "Comercial"),
        UserEntity(105, "AG-05", Roles.AGENTE, "19.564.028-9", "Tomás Alejandro", "Vidal Rojas", "tvidal@pagoflex.ejemplo", "", "", "", true, 3, "Comercial"),
        UserEntity(201, "EJ-01", Roles.EJECUTIVO, "14.629.853-2", "Paula Andrea", "Saavedra Núñez", "psaavedra@altosmaipo.ejemplo", "+56 9 0000 0401", "", "", true, 3, null, 1, "Ejecutiva de cobranza"),
        UserEntity(202, "EJ-02", Roles.EJECUTIVO, "11.853.720-3", "Marco Antonio", "Fuentes Riquelme", "mfuentes@bosquenativo.ejemplo", "+56 9 0000 0402", "", "", true, 3, null, 2, "Encargado de finanzas"),
        UserEntity(203, "EJ-03", Roles.EJECUTIVO, "15.370.946-7", "Claudia Beatriz", "Henríquez Soto", "chenriquez@creditoandino.ejemplo", "+56 9 0000 0403", "", "", true, 3, null, 3, "Jefa de cobranza"),
        UserEntity(204, "EJ-04", Roles.EJECUTIVO, "17.095.482-3", "Felipe Ignacio", "Morales Tapia", "fmorales@creditoandino.ejemplo", "+56 9 0000 0404", "", "", true, 3, null, 3, "Ejecutivo de cobranza"),
        UserEntity(205, "EJ-05", Roles.EJECUTIVO, "9.846.217-1", "Gloria Inés", "Carrasco Vega", "gcarrasco@loshalcones.ejemplo", "+56 9 0000 0405", "", "", true, 3, null, 6, "Tesorera"),
        UserEntity(206, "EJ-06", Roles.EJECUTIVO, "18.930.571-0", "Sebastián Andrés", "Núñez Olivares", "snunez@itcordillera.ejemplo", "+56 9 0000 0406", "", "", true, 3, null, 7, "Analista de finanzas"),
        UserEntity(207, "EJ-07", Roles.EJECUTIVO, "12.584.396-4", "Mónica Patricia", "Leiva Castro", "mleiva@plazaoriente.ejemplo", "+56 9 0000 0407", "", "", true, 3, null, 8, "Administradora de cobranza")
    )

    val commitments = listOf(
        CommitmentEntity(0, "CP-2026-0101", 1, 8, "Arriendo departamento octubre", 10, 12, 520000, "10-10-2026", CommitmentStatus.PENDIENTE, 0, "30-09-2026", 207, "30-09-2026"),
        CommitmentEntity(0, "CP-2026-0102", 1, 6, "Cuota social octubre", 10, 12, 15000, "05-10-2026", CommitmentStatus.VENCIDO, 2000, "06-10-2026", 205, "25-09-2026"),
        CommitmentEntity(0, "CP-2026-0103", 1, 5, "Aporte mensual octubre", null, null, 10000, "15-10-2026", CommitmentStatus.PAGADO, 0, "14-10-2026", null, "01-10-2026"),
        CommitmentEntity(0, "CP-2026-0104", 1, 3, "Crédito de consumo", 7, 24, 86400, "25-09-2026", CommitmentStatus.PAGADO, 0, "24-09-2026", 203, "01-09-2026"),
        CommitmentEntity(0, "CP-2026-0105", 1, 3, "Crédito de consumo", 8, 24, 86400, "25-10-2026", CommitmentStatus.PENDIENTE, 0, "25-09-2026", 203, "25-09-2026"),
        CommitmentEntity(0, "CP-2026-0106", 2, 1, "Cuota pie departamento", 14, 36, 310000, "30-09-2026", CommitmentStatus.VENCIDO, 8500, "01-10-2026", 201, "01-09-2026"),
        CommitmentEntity(0, "CP-2026-0107", 2, 1, "Cuota pie departamento", 15, 36, 310000, "30-10-2026", CommitmentStatus.PENDIENTE, 0, "30-09-2026", 201, "30-09-2026"),
        CommitmentEntity(0, "CP-2026-0108", 3, 4, "Seguro de hogar octubre", 10, 12, 18900, "01-10-2026", CommitmentStatus.PAGADO, 0, "30-09-2026", null, "01-09-2026"),
        CommitmentEntity(0, "CP-2026-0109", 4, 7, "Arancel octubre", 8, 10, 142000, "05-10-2026", CommitmentStatus.EN_REVISION, 0, "03-10-2026", 206, "01-10-2026"),
        CommitmentEntity(0, "CP-2026-0110", 5, 2, "Mensualidad octubre", 8, 10, 185000, "10-10-2026", CommitmentStatus.PENDIENTE, 0, "01-10-2026", 202, "01-10-2026"),
        CommitmentEntity(0, "CP-2026-0111", 6, 3, "Crédito de consumo", 19, 36, 54200, "20-09-2026", CommitmentStatus.ANULADO, 0, "22-09-2026", 204, "20-08-2026", "Repactación", "Repactación acordada con la empresa", 204, "22-09-2026"),
        CommitmentEntity(0, "CP-2026-0112", 7, 6, "Cuota social octubre", 10, 12, 15000, "05-10-2026", CommitmentStatus.PAGADO, 0, "04-10-2026", 205, "01-10-2026"),
        CommitmentEntity(0, "CP-2026-0095", 8, 6, "Cuota social septiembre", 10, 12, 15000, "30-09-2026", CommitmentStatus.EN_REVISION, 0, "30-09-2026", 205, "01-09-2026"),
        CommitmentEntity(0, "CP-2026-0081", 5, 2, "Matrícula 2026", null, null, 120000, "15-08-2026", CommitmentStatus.ANULADO, 0, "05-09-2026", 202, "01-07-2026", "Error de registro", "Cobro duplicado de matrícula", 202, "05-09-2026"),
        CommitmentEntity(0, "CP-2026-0064", 1, 8, "Arriendo departamento agosto", 8, 12, 520000, "10-08-2026", CommitmentStatus.PAGADO, 0, "30-07-2026", 207, "01-07-2026"),
        CommitmentEntity(0, "CP-2026-0072", 2, 1, "Cuota pie departamento", 13, 36, 310000, "30-08-2026", CommitmentStatus.PAGADO, 8500, "02-09-2026", 201, "01-08-2026"),
        CommitmentEntity(0, "CP-2026-0089", 1, 8, "Arriendo departamento septiembre", 9, 12, 520000, "10-09-2026", CommitmentStatus.PAGADO, 0, "09-09-2026", 207, "01-09-2026"),
        CommitmentEntity(0, "CP-2026-0090", 1, 6, "Cuota social septiembre", 9, 12, 15000, "05-09-2026", CommitmentStatus.PAGADO, 2000, "07-09-2026", 205, "01-09-2026"),
        CommitmentEntity(0, "CP-2026-0091", 1, 5, "Aporte mensual septiembre", null, null, 10000, "15-09-2026", CommitmentStatus.PAGADO, 0, "15-09-2026", null, "01-09-2026"),
        CommitmentEntity(0, "CP-2026-0098", 5, 2, "Mensualidad septiembre", 7, 10, 185000, "10-09-2026", CommitmentStatus.PAGADO, 0, "10-09-2026", 202, "01-09-2026")
    )

    val payments = listOf(
        SeedPayment("CP-2026-0064", "CPR-000380", "09-08-2026 12:10", 520000, Channels.PORTAL, "Transferencia", true),
        SeedPayment("CP-2026-0072", "CPR-000377", "02-09-2026 21:14", 318500, Channels.AGENTE, "Transferencia", false),
        SeedPayment("CP-2026-0089", "CPR-000398", "09-09-2026 13:05", 520000, Channels.PORTAL, "Tarjeta de débito", true),
        SeedPayment("CP-2026-0090", "CPR-000402", "07-09-2026 18:40", 17000, Channels.PORTAL, "Tarjeta de crédito", false),
        SeedPayment("CP-2026-0091", "CPR-000405", "15-09-2026 08:22", 10000, Channels.AGENTE, "Transferencia", true),
        SeedPayment("CP-2026-0104", "CPR-000451", "24-09-2026 10:42", 86400, Channels.PORTAL, "Tarjeta de débito", true),
        SeedPayment("CP-2026-0103", "CPR-000459", "14-10-2026 09:30", 10000, Channels.APP, "Pago simulado", true),
        SeedPayment("CP-2026-0108", "CPR-000452", "30-09-2026 19:05", 18900, Channels.AGENTE, "Transferencia", true),
        SeedPayment("CP-2026-0112", "CPR-000453", "04-10-2026 08:15", 15000, Channels.APP, "Pago simulado", true),
        SeedPayment("CP-2026-0098", "CPR-000447", "10-09-2026 22:31", 185000, Channels.PORTAL, "Tarjeta de débito", true)
    )

    val reports = listOf(
        SeedReport("CP-2026-0109", "REP-0011", "Ya pagué por otro medio", "Pagué por transferencia directa al instituto el 03-10.", "03-10-2026", ReportStatus.EN_GESTION, 102, "04-10-2026", null, null),
        SeedReport("CP-2026-0111", "REP-0009", "El monto no corresponde", "Repacté el crédito y me siguen cobrando la cuota antigua.", "18-09-2026", ReportStatus.RESUELTO, 101, "19-09-2026", "La empresa anuló la cuota por repactación (22-09-2026).", "23-09-2026"),
        SeedReport("CP-2026-0081", "REP-0007", "No reconozco este cobro", "Me aparece una matrícula que ya pagué en marzo.", "02-09-2026", ReportStatus.RESUELTO, 102, "02-09-2026", "Cobro duplicado anulado por el colegio (05-09-2026).", "05-09-2026"),
        SeedReport("CP-2026-0064", "REP-0005", "Otro", "El concepto dice agosto y es la cuota de julio.", "28-07-2026", ReportStatus.RESUELTO, 101, "28-07-2026", "Se corrigió el concepto; el monto era correcto (30-07-2026).", "30-07-2026"),
        SeedReport("CP-2026-0095", "REP-0010", "No reconozco este cobro", "Me llegó un cobro de un club al que no pertenezco.", "30-09-2026", ReportStatus.ABIERTO, null, null, null, null)
    )

    val recommendations = listOf(
        RecommendationEntity(0, "REC-0001", 1, "Jardín Infantil Pequeños Exploradores", null, "Educación parvularia", "Maipú", "Paula Herrera", "pherrera@pexploradores.ejemplo", null, true, "12-08-2026", RecommendationStatus.SE_UNIO, null, 104, "25-09-2026"),
        RecommendationEntity(0, "REC-0002", 1, "Comunidad Edificio Parque Los Olmos", null, "Administración de condominio", "Maipú", null, null, null, false, "02-09-2026", RecommendationStatus.CONTACTADA, null, 105, "10-09-2026"),
        RecommendationEntity(0, "REC-0003", 3, "Gimnasio Fuerza Sur", null, "Deporte", "San Miguel", "Raúl Ibáñez", null, "+56 9 0000 0301", true, "15-09-2026", RecommendationStatus.EN_EVALUACION, null, 104, "28-09-2026"),
        RecommendationEntity(0, "REC-0004", 5, "Academia de Música Clave de Sol", null, "Educación artística", "Ñuñoa", null, null, null, false, "20-09-2026", RecommendationStatus.RECIBIDA, null, null, null),
        RecommendationEntity(0, "REC-0005", 4, "Colegio Bosque Nativo", null, "Educación escolar", "La Florida", null, null, null, false, "22-09-2026", RecommendationStatus.DESCARTADA, "La organización ya es cliente de PagoFlex", 105, "23-09-2026"),
        RecommendationEntity(0, "REC-0006", 7, "Corporación Deportiva Cordillera", null, "Club deportivo", "San Bernardo", "Ana Valdés", "avaldes@cdcordillera.ejemplo", null, true, "01-10-2026", RecommendationStatus.RECIBIDA, null, null, null)
    )

    val recognitions = listOf(
        RecognitionEntity(1, "Embajador PagoFlex", "La organización Jardín Infantil Pequeños Exploradores se unió a PagoFlex", "25-09-2026"),
        RecognitionEntity(1, "Tres meses al día", "Tres meses consecutivos con todos tus compromisos pagados a tiempo", "30-09-2026"),
        RecognitionEntity(7, "Pago puntual", "Pagaste a tiempo tu Cuota social octubre", "04-10-2026")
    )

    val complianceTargets = mapOf(
        1 to ComplianceTarget(36, 33),
        2 to ComplianceTarget(12, 8),
        3 to ComplianceTarget(28, 28),
        4 to ComplianceTarget(7, 6),
        5 to ComplianceTarget(20, 19),
        6 to ComplianceTarget(14, 12),
        7 to ComplianceTarget(5, 5),
        8 to ComplianceTarget(0, 0)
    )
}
