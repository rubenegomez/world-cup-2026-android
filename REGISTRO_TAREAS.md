# 📋 Registro de Tareas y Pendientes - world-cup-2026-android

Este documento sirve para realizar el seguimiento del desarrollo de **world-cup-2026-android** día a día. Aquí registramos lo que planificamos, lo que ya cumplimos (con su respectivo tilde), y las novedades acumuladas para colocar en el changelog.

---

## 🛠️ Estado de Tareas

| Estado | Tarea / Característica | Componente | Fecha de Cierre | Notas / Detalles |
| :---: | :--- | :---: | :---: | :--- |
| **[x]** | Pancarta de cuenta regresiva (CountdownBanner) con notificaciones y sonido personalizado | UI / Notificaciones | 2026-06-01 | Implementado banner premium en Compose, NotificationHelper, solicitud de permisos y sonido raw. |
| **[x]** | Auto-enfoque del fixture según el día actual del Mundial 2026 | UI / Navegación | 2026-06-01 | La app se abre en la pestaña Eliminación o selecciona el día de hoy dentro de la fase de grupos de manera automática. |
| **[x]** | Ocultar temporalmente el botón 'VER WIDGET EN VIVO (VIP)' | UI / Pantallas | 2026-06-12 | Se añadió una bandera condicional en `AdComponents.kt` para ocultar el botón del widget VIP hasta que sea conveniente activarlo. |
| **[x]** | Base de datos con grupos y fixture real de 48 selecciones | Servidor / Base de Datos | 2026-06-25 | Se crearon scripts para sembrar la base de datos sqlite en el servidor a partir del fixture real (`MatchData.kt`). |
| **[x]** | Sincronización automática de resultados (ESPN) en segundo plano | Servidor / API | 2026-06-25 | Se implementó una tarea periódica asíncrona en FastAPI que sincroniza scores y estado en vivo cada 5 minutos sin cron externo. |
| **[x]** | Uso de Dominio en la App Android (`ellocodelpedal.duckdns.org`) | Android / Red | 2026-06-25 | Reemplazo de la IP hardcodeada por el nombre de dominio en `NetworkModule.kt` para resiliencia ante cambios de IP. |
| **[x]** | Ajustes visuales de Fixture (Dos renglones y orden cronológico) | Android / UI | 2026-06-25 | Las pestañas de fecha muestran el día en español y fecha en 2 renglones. Los partidos de cada día se ordenan por hora. |
| **[x]** | Notificación en vivo de incidentes (Goles, Rojas, Fin de Partido) | Android / Notif. | 2026-06-25 | Detección de incidentes en tiempo real y disparo de notificaciones con IDs dinámicos para evitar pisarse. |
| **[x]** | Rediseño premium de Estadísticas VIP estilo Flashscore | Android / UI | 2026-06-25 | Rediseño completo con celdas grises redondeadas, banderas de selecciones en cabecera y tarjeta de info del partido. |
| **[x]** | Parametrización para Migración al Campeonato Local | Android y Backend | 2026-06-25 | Creación de `TournamentConfig.kt` y `tournament_config.py` con flag central `IS_WORLD_CUP` para migración instantánea. |
| **[x]** | Compilación y Firma de APK y AAB de Producción | Android / Release | 2026-06-25 | Compilación oficial firmada con llaves de El Loco del Pedal. |

---

## 🔮 Próxima Planificación (Roadmap)

### 🏆 Prode Social
*   **Enfoque Sin Registro**: Compartir desafíos 1v1 y predicciones del día directamente por WhatsApp a través de tarjetas con formato premium.
*   **Ligas Privadas (Con Registro)**: Creación de ligas de amigos por invitación mediante un código único, con tabla de posiciones y evolución del puntaje en tiempo real.

### 🌐 Distribución y Landing Page
*   **Servicio de Descarga Directa**: Subir el APK de producción al servidor de Oracle Cloud y añadir un botón premium de descarga en `index.html` servido de forma nativa mediante Caddy.


