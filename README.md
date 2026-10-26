# PagoFlex

Aplicacion movil nativa para Android desarrollada para el caso DSY1105. PagoFlex centraliza compromisos de pago, permite simular pagos, adjuntar comprobantes, gestionar reportes y apoyar la cobranza desde tres perfiles: usuario final, agente PagoFlex y ejecutivo de empresa cliente.

## Integrante

- Maicol Rivas

## Planificacion

- Tablero Trello: (https://trello.com/invite/b/6adeb5e4128183a3082b230b/ATTIa4849e1579ce97201e69501e7329e002481B4918/pagoflex)
- Repositorio Github: https://github.com/Mairivas/PagoFlex.git

## Funcionalidades

### Usuario final

- Dashboard con compromisos vigentes, monto pendiente, proximo vencimiento y estado de cumplimiento.
- Pago simulado con seleccion de medio, confirmacion y comprobante inmutable.
- Captura de fotografia del comprobante mediante camara y FileProvider.
- Historial de pagos con filtros por empresa y periodo.
- Detalle de compromisos, reportes de problemas y seguimiento de su resolucion.
- Recordatorios locales de vencimiento con notificaciones y permiso runtime en Android 13+.
- Reconocimientos por pago puntual, recomendaciones de organizaciones y perfil con RUT enmascarado.

### Agente PagoFlex

- Bandeja de reportes con filtros por estado.
- Toma y resolucion de reportes, con respuesta al usuario.
- Marcador de atraso para reportes abiertos por mas de 2 dias habiles.
- Gestion de referidos y perfil profesional.

### Ejecutivo de empresa cliente

- Resumen mensual con tasa de cobranza, recaudacion, compromisos pagados, pendientes y vencidos.
- Cartera filtrable por estado y periodo de vencimiento.
- Registro y anulacion de compromisos con trazabilidad del ejecutivo responsable.
- Detalle de compromisos, pagos y reportes asociados.

## Arquitectura y tecnologias

- Kotlin 1.9.22 y Android Gradle Plugin 8.2.2.
- Jetpack Compose con Material Design 3.
- MVVM con ViewModels y flujos reactivos StateFlow.
- Room para persistencia local de usuarios, empresas, compromisos, pagos, reportes y recomendaciones.
- Jetpack Navigation Compose para la navegacion.
- Coil para la carga de imagenes.
- AlarmManager y NotificationManager para recordatorios locales.
- Compose Animation para transiciones y confirmacion de pago.

## Estructura principal

```text
app/src/main/kotlin/com/equipo22/pagoflex/
|-- data/          Entidades, DAOs, base Room, repositorio y datos de demostracion
|-- ui/components/ Componentes reutilizables de interfaz
|-- ui/screens/    Flujos de usuario, agente, ejecutivo y seleccion de perfil
|-- ui/theme/      Tema y paleta visual
|-- utils/         Formatos, validaciones, fechas y notificaciones
|-- viewmodel/     Estado y logica de presentacion
`-- MainActivity.kt
```

## Requisitos

- Android Studio Ladybug o superior.
- JDK 17.
- Android SDK 34.
- Emulador o dispositivo con Android 7.0 (API 24) o superior.

## Compilacion y ejecucion

En Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

En macOS o Linux:

```bash
./gradlew :app:assembleDebug
```

Luego abrir el proyecto en Android Studio, seleccionar un emulador o dispositivo y ejecutar la configuracion `app`.

Para instalar el APK generado:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Uso de la demostracion

1. Abrir la aplicacion y elegir uno de los perfiles disponibles en la pantalla inicial.
2. Recorrer los modulos desde la barra de navegacion inferior.
3. Cerrar sesion desde el perfil para cambiar entre usuario final, agente y ejecutivo.

La base local se inicializa con empresas, usuarios, compromisos, pagos, reportes y recomendaciones de ejemplo. No se requiere conexion a un servidor ni credenciales.

## Verificacion

```powershell
.\gradlew.bat :app:lintDebug
```

El proyecto compila con `assembleDebug` y el analisis de Android Lint finaliza correctamente.
