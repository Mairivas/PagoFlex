package com.equipo22.pagoflex.data

object Roles {
    const val USUARIO_FINAL = "USUARIO_FINAL"
    const val AGENTE = "AGENTE"
    const val EJECUTIVO = "EJECUTIVO"

    fun label(role: String): String = when (role) {
        USUARIO_FINAL -> "Usuario final"
        AGENTE -> "Agente PagoFlex"
        EJECUTIVO -> "Ejecutivo de empresa"
        else -> role
    }
}

object CommitmentStatus {
    const val PENDIENTE = "Pendiente"
    const val VENCIDO = "Vencido"
    const val PAGADO = "Pagado"
    const val EN_REVISION = "En revisión"
    const val ANULADO = "Anulado"

    val all = listOf(PENDIENTE, VENCIDO, PAGADO, EN_REVISION, ANULADO)
    val payable = listOf(PENDIENTE, VENCIDO)
}

object ReportStatus {
    const val ABIERTO = "Abierto"
    const val EN_GESTION = "En gestión"
    const val RESUELTO = "Resuelto"

    val all = listOf(ABIERTO, EN_GESTION, RESUELTO)
}

object RecommendationStatus {
    const val RECIBIDA = "Recibida"
    const val CONTACTADA = "Contactada"
    const val EN_EVALUACION = "En evaluación"
    const val SE_UNIO = "Se unió"
    const val DESCARTADA = "Descartada"

    val flow = listOf(RECIBIDA, CONTACTADA, EN_EVALUACION)
    val all = listOf(RECIBIDA, CONTACTADA, EN_EVALUACION, SE_UNIO, DESCARTADA)
}

object Channels {
    const val APP = "App"
    const val PORTAL = "Portal de pagos"
    const val AGENTE = "Agente conversacional"
}

object Catalogs {
    val paymentMethods = listOf("Tarjeta de débito", "Tarjeta de crédito", "Transferencia", "Pago simulado")

    val reportMotives = listOf(
        "Ya pagué por otro medio",
        "No reconozco este cobro",
        "El monto no corresponde",
        "Otro"
    )

    val annulMotives = listOf(
        "Repactación",
        "Error de registro",
        "Pagado directamente a la empresa",
        "Otro"
    )

    val rubros = listOf(
        "Inmobiliaria",
        "Educación escolar",
        "Educación superior",
        "Educación parvularia",
        "Educación artística",
        "Crédito de consumo",
        "Seguros",
        "ONG",
        "Club deportivo",
        "Administración de arriendos",
        "Administración de condominio",
        "Deporte",
        "Comercio",
        "Salud",
        "Otro"
    )

    val comunas = listOf(
        "Maipú",
        "Puente Alto",
        "San Miguel",
        "La Florida",
        "Ñuñoa",
        "Quilicura",
        "San Bernardo",
        "Estación Central",
        "Santiago",
        "Providencia",
        "Las Condes",
        "La Reina",
        "Peñalolén",
        "Macul",
        "Otra"
    )
}
