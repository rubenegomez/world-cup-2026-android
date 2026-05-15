# Plan de Modernización Final (El Último 30%) - World Cup 2026

Este plan detalla las tareas necesarias para transformar la aplicación de una herramienta funcional a una "máquina de guerra" visual y tecnológica de nivel profesional.

## 1. Interfaz Adaptable: "La App que late" 💓
Actualmente, los partidos en vivo son estáticos. Vamos a darles vida.
- [ ] **Efecto de Pulso Vivo**: Implementar una animación de resplandor (Glow) intermitente en el borde de la tarjeta para partidos con status "Live".
- [ ] **Vibrancia Dinámica**: Cambiar sutilmente el color de fondo de la tarjeta de "Live" a un gradiente más enérgico (ej. Verde neón/Rojo vibrante).
- [ ] **Ticker de Tiempo**: Asegurar que el cronómetro del partido se actualice en tiempo real con una animación de cambio de número suave.

## 2. Navegación Zero-Click (Gestos) ✋↕️
Sustituiremos la navegación rígida por una experiencia de "fluidez infinita".
- [ ] **HorizontalPager**: Migrar el `FixtureScreen` de un sistema de pestañas simple a un `HorizontalPager` de Compose. Esto permitirá al usuario deslizar lateralmente para cambiar de grupo o fecha.
- [ ] **Sincronización Pestaña-Scroll**: Vincular el indicador de pestañas superior con el gesto de deslizamiento para que se muevan en perfecta armonía.

## 3. Animaciones "Satisfactorias" (Spring Physics) 🎡
Añadiremos una capa de "física real" a los elementos táctiles.
- [ ] **Spring Buttons**: Configurar todos los botones principales y chips del Prode para que usen `spring()` specs. Al tocarlos, deben rebotar sutilmente en lugar de tener un movimiento lineal.
- [ ] **Entrada de Lista Animada**: Hacer que las tarjetas de los partidos aparezcan con un efecto de "cascada" (Staggered animation) al cargar cada pestaña, dando sensación de ligereza.

## 4. Conectividad Real (Estadísticas VIP) 💎📊
Dejaremos atrás la simulación para usar datos de la élite mundial.
- [ ] **Integración de API-Football**: 
    - Configurar el cliente Retrofit para conectar con `v3.football.api-sports.io`.
    - Mapear los endpoints de `fixtures/statistics` y `fixtures/events`.
- [ ] **Visualización Premium**: 
    - **Posesión**: Crear un gráfico circular o barra comparativa dinámica.
    - **Heatmaps**: Si la API provee coordenadas, implementar un Canvas sutil para mostrar las zonas de acción.
    - **Goleadores**: Vincular los nombres de los goleadores con sus fotos reales de la API.

## 5. Pulido de Monetización 🛡️💰
- [ ] **Intersticiales en Pausas**: Configurar un anuncio a pantalla completa que solo aparezca después de que el usuario haya guardado una tanda completa de predicciones (un momento de "logro").
- [ ] **Feedback de Recompensa**: Añadir una animación especial (ej. monedas volando o un destello dorado) cuando el usuario gana horas Ad-Free por acertar en el Prode.

---
**Estado Actual:** 70% | **Objetivo:** 100% (Premium Experience)
