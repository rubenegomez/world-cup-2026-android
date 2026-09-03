package com.example.worldcup2026.data.model

data class MasterTeam(
    val name: String,
    val flagUrl: String,
    val tournamentId: Int,
    val categoryName: String
)

data class MasterTournament(
    val id: Int,
    val name: String,
    val displayName: String,
    val category: String,
    val isActive: Boolean = false
)

object MasterTeamCatalog {

    // 0. LISTA CANÓNICA OFICIAL DE TORNEOS (17)
    val MASTER_TOURNAMENTS = listOf(
        MasterTournament(5, "Torneo Clausura Liga Profesional", "🏆 Torneo Clausura Liga Profesional", "Nacional", isActive = true),
        MasterTournament(6, "Copa Argentina", "🇦🇷 Copa Argentina", "Nacional", isActive = true),
        MasterTournament(17, "Torneo Apertura Liga Profesional", "🏆 Torneo Apertura Liga Profesional (Concluido)", "Nacional", isActive = false),
        MasterTournament(7, "Torneo Primera Nacional", "⚽ Torneo Primera Nacional", "Nacional", isActive = true),
        MasterTournament(15, "Torneo Federal A", "🏔️ Torneo Federal A", "Nacional", isActive = true),
        MasterTournament(8, "Torneo Clausura B Metropolitana", "🏟️ Torneo Clausura B Metropolitana", "Nacional", isActive = true),
        MasterTournament(18, "Torneo Apertura B Metropolitana", "🏟️ Torneo Apertura B Metropolitana (Concluido)", "Nacional", isActive = false),
        MasterTournament(19, "Amistosos AFA", "🇦🇷 Amistosos AFA", "Nacional", isActive = false),
        MasterTournament(3, "Copa Conmebol Libertadores", "🏆 Copa Conmebol Libertadores", "Internacional", isActive = true),
        MasterTournament(4, "Copa Conmebol Sudamericana", "🏆 Copa Conmebol Sudamericana", "Internacional", isActive = true),
        MasterTournament(2, "Eliminatorias Conmebol", "🌎 Eliminatorias Conmebol", "Selecciones", isActive = false),
        MasterTournament(21, "Amistosos Clubes Conmebol", "🤝 Amistosos Clubes Conmebol", "Internacional", isActive = false),
        MasterTournament(1, "Campeonato Mundial De Fútbol", "🌍 Campeonato Mundial De Fútbol", "Selecciones", isActive = false),
        MasterTournament(12, "Finalísima", "👑 Finalísima", "Selecciones", isActive = false),
        MasterTournament(22, "Campeonato Mundial de Clubes", "🌐 Campeonato Mundial de Clubes", "Internacional", isActive = false),
        MasterTournament(23, "Copa Intercontinental", "🌐 Copa Intercontinental", "Internacional", isActive = false),
        MasterTournament(14, "Amistosos FIFA", "⚽ Amistosos FIFA", "Selecciones", isActive = false)
    )

    val ACTIVE_TOURNAMENTS = MASTER_TOURNAMENTS.filter { it.isActive }
    val ACTIVE_TOURNAMENT_IDS = ACTIVE_TOURNAMENTS.map { it.id }.toSet()

    // 1. SELECCIONES OFICIALES CONMEBOL (10)
    val CONMEBOL_SELECTIONS = listOf(
        MasterTeam("Argentina", "https://a.espncdn.com/i/teamlogos/soccer/500/202.png", 2, "CONMEBOL"),
        MasterTeam("Bolivia", "https://a.espncdn.com/i/teamlogos/soccer/500/203.png", 2, "CONMEBOL"),
        MasterTeam("Brasil", "https://a.espncdn.com/i/teamlogos/soccer/500/205.png", 2, "CONMEBOL"),
        MasterTeam("Chile", "https://a.espncdn.com/i/teamlogos/soccer/500/206.png", 2, "CONMEBOL"),
        MasterTeam("Colombia", "https://a.espncdn.com/i/teamlogos/soccer/500/207.png", 2, "CONMEBOL"),
        MasterTeam("Ecuador", "https://a.espncdn.com/i/teamlogos/soccer/500/209.png", 2, "CONMEBOL"),
        MasterTeam("Paraguay", "https://a.espncdn.com/i/teamlogos/soccer/500/216.png", 2, "CONMEBOL"),
        MasterTeam("Perú", "https://a.espncdn.com/i/teamlogos/soccer/500/217.png", 2, "CONMEBOL"),
        MasterTeam("Uruguay", "https://a.espncdn.com/i/teamlogos/soccer/500/219.png", 2, "CONMEBOL"),
        MasterTeam("Venezuela", "https://a.espncdn.com/i/teamlogos/soccer/500/220.png", 2, "CONMEBOL")
    )

    // 2. LIGA PROFESIONAL DE FÚTBOL (30)
    val LIGA_PROFESIONAL = listOf(
        MasterTeam("Aldosivi", "https://a.espncdn.com/i/teamlogos/soccer/500/9739.png", 5, "Liga Profesional"),
        MasterTeam("Argentinos Juniors", "https://a.espncdn.com/i/teamlogos/soccer/500/3.png", 5, "Liga Profesional"),
        MasterTeam("Atlético Tucumán", "https://a.espncdn.com/i/teamlogos/soccer/500/9785.png", 5, "Liga Profesional"),
        MasterTeam("Banfield", "https://a.espncdn.com/i/teamlogos/soccer/500/235.png", 5, "Liga Profesional"),
        MasterTeam("Barracas Central", "https://a.espncdn.com/i/teamlogos/soccer/500/10060.png", 5, "Liga Profesional"),
        MasterTeam("Belgrano", "https://a.espncdn.com/i/teamlogos/soccer/500/4.png", 5, "Liga Profesional"),
        MasterTeam("Boca Juniors", "https://a.espncdn.com/i/teamlogos/soccer/500/5.png", 5, "Liga Profesional"),
        MasterTeam("Central Córdoba (Santiago del Estero)", "https://a.espncdn.com/i/teamlogos/soccer/500/11989.png", 5, "Liga Profesional"),
        MasterTeam("Defensa y Justicia", "https://a.espncdn.com/i/teamlogos/soccer/500/8950.png", 5, "Liga Profesional"),
        MasterTeam("Deportivo Riestra", "https://a.espncdn.com/i/teamlogos/soccer/500/17702.png", 5, "Liga Profesional"),
        MasterTeam("Estudiantes de La Plata", "https://a.espncdn.com/i/teamlogos/soccer/500/8.png", 5, "Liga Profesional"),
        MasterTeam("Gimnasia y Esgrima La Plata", "https://a.espncdn.com/i/teamlogos/soccer/500/9.png", 5, "Liga Profesional"),
        MasterTeam("Godoy Cruz", "https://a.espncdn.com/i/teamlogos/soccer/500/6756.png", 5, "Liga Profesional"),
        MasterTeam("Huracán", "https://a.espncdn.com/i/teamlogos/soccer/500/10.png", 5, "Liga Profesional"),
        MasterTeam("Independiente", "https://a.espncdn.com/i/teamlogos/soccer/500/11.png", 5, "Liga Profesional"),
        MasterTeam("Independiente Rivadavia", "https://a.espncdn.com/i/teamlogos/soccer/500/9744.png", 5, "Liga Profesional"),
        MasterTeam("Instituto", "https://a.espncdn.com/i/teamlogos/soccer/500/2975.png", 5, "Liga Profesional"),
        MasterTeam("Lanús", "https://a.espncdn.com/i/teamlogos/soccer/500/12.png", 5, "Liga Profesional"),
        MasterTeam("Newell's Old Boys", "https://a.espncdn.com/i/teamlogos/soccer/500/14.png", 5, "Liga Profesional"),
        MasterTeam("Platense", "https://a.espncdn.com/i/teamlogos/soccer/500/7764.png", 5, "Liga Profesional"),
        MasterTeam("Racing Club", "https://a.espncdn.com/i/teamlogos/soccer/500/15.png", 5, "Liga Profesional"),
        MasterTeam("River Plate", "https://a.espncdn.com/i/teamlogos/soccer/500/16.png", 5, "Liga Profesional"),
        MasterTeam("Rosario Central", "https://a.espncdn.com/i/teamlogos/soccer/500/17.png", 5, "Liga Profesional"),
        MasterTeam("San Lorenzo", "https://a.espncdn.com/i/teamlogos/soccer/500/18.png", 5, "Liga Profesional"),
        MasterTeam("San Martín (San Juan)", "https://a.espncdn.com/i/teamlogos/soccer/500/9745.png", 5, "Liga Profesional"),
        MasterTeam("Sarmiento (Junín)", "https://a.espncdn.com/i/teamlogos/soccer/500/10158.png", 5, "Liga Profesional"),
        MasterTeam("Talleres (Córdoba)", "https://a.espncdn.com/i/teamlogos/soccer/500/19.png", 5, "Liga Profesional"),
        MasterTeam("Tigre", "https://a.espncdn.com/i/teamlogos/soccer/500/7767.png", 5, "Liga Profesional"),
        MasterTeam("Unión de Santa Fe", "https://a.espncdn.com/i/teamlogos/soccer/500/20.png", 5, "Liga Profesional"),
        MasterTeam("Vélez Sarsfield", "https://a.espncdn.com/i/teamlogos/soccer/500/21.png", 5, "Liga Profesional")
    )

    // 3. PRIMERA NACIONAL (36)
    val PRIMERA_NACIONAL = listOf(
        MasterTeam("Agropecuario", "https://a.espncdn.com/i/teamlogos/soccer/500/17700.png", 7, "Primera Nacional"),
        MasterTeam("All Boys", "https://a.espncdn.com/i/teamlogos/soccer/500/234.png", 7, "Primera Nacional"),
        MasterTeam("Almagro", "https://a.espncdn.com/i/teamlogos/soccer/500/2972.png", 7, "Primera Nacional"),
        MasterTeam("Almirante Brown", "https://a.espncdn.com/i/teamlogos/soccer/500/9740.png", 7, "Primera Nacional"),
        MasterTeam("Alvarado", "https://a.espncdn.com/i/teamlogos/soccer/500/19686.png", 7, "Primera Nacional"),
        MasterTeam("Atlanta", "https://a.espncdn.com/i/teamlogos/soccer/500/10059.png", 7, "Primera Nacional"),
        MasterTeam("Atlético de Rafaela", "https://a.espncdn.com/i/teamlogos/soccer/500/2384.png", 7, "Primera Nacional"),
        MasterTeam("Central Norte (Salta)", "https://a.espncdn.com/i/teamlogos/soccer/500/10061.png", 7, "Primera Nacional"),
        MasterTeam("Chacarita Juniors", "https://a.espncdn.com/i/teamlogos/soccer/500/6.png", 7, "Primera Nacional"),
        MasterTeam("Chaco For Ever", "https://a.espncdn.com/i/teamlogos/soccer/500/10156.png", 7, "Primera Nacional"),
        MasterTeam("Ciudad de Bolívar", "https://a.espncdn.com/i/teamlogos/soccer/500/20340.png", 7, "Primera Nacional"),
        MasterTeam("Colegiales", "https://a.espncdn.com/i/teamlogos/soccer/500/10062.png", 7, "Primera Nacional"),
        MasterTeam("Colón de Santa Fe", "https://a.espncdn.com/i/teamlogos/soccer/500/7.png", 7, "Primera Nacional"),
        MasterTeam("Defensores de Belgrano", "https://a.espncdn.com/i/teamlogos/soccer/500/9741.png", 7, "Primera Nacional"),
        MasterTeam("Deportivo Madryn", "https://a.espncdn.com/i/teamlogos/soccer/500/17701.png", 7, "Primera Nacional"),
        MasterTeam("Deportivo Maipú", "https://a.espncdn.com/i/teamlogos/soccer/500/19688.png", 7, "Primera Nacional"),
        MasterTeam("Deportivo Morón", "https://a.espncdn.com/i/teamlogos/soccer/500/9742.png", 7, "Primera Nacional"),
        MasterTeam("Estudiantes de Buenos Aires", "https://a.espncdn.com/i/teamlogos/soccer/500/10063.png", 7, "Primera Nacional"),
        MasterTeam("Estudiantes de Río Cuarto", "https://a.espncdn.com/i/teamlogos/soccer/500/19685.png", 7, "Primera Nacional"),
        MasterTeam("Ferro Carril Oeste", "https://a.espncdn.com/i/teamlogos/soccer/500/9743.png", 7, "Primera Nacional"),
        MasterTeam("Ferrocarril Midland", "https://a.espncdn.com/i/teamlogos/soccer/500/10157.png", 7, "Primera Nacional"),
        MasterTeam("Gimnasia y Esgrima (Jujuy)", "https://a.espncdn.com/i/teamlogos/soccer/500/2973.png", 7, "Primera Nacional"),
        MasterTeam("Gimnasia y Esgrima (Mendoza)", "https://a.espncdn.com/i/teamlogos/soccer/500/11972.png", 7, "Primera Nacional"),
        MasterTeam("Gimnasia y Tiro (Salta)", "https://a.espncdn.com/i/teamlogos/soccer/500/2974.png", 7, "Primera Nacional"),
        MasterTeam("Güemes (Santiago del Estero)", "https://a.espncdn.com/i/teamlogos/soccer/500/19687.png", 7, "Primera Nacional"),
        MasterTeam("Los Andes", "https://a.espncdn.com/i/teamlogos/soccer/500/2976.png", 7, "Primera Nacional"),
        MasterTeam("Mitre (Santiago del Estero)", "https://a.espncdn.com/i/teamlogos/soccer/500/17703.png", 7, "Primera Nacional"),
        MasterTeam("Nueva Chicago", "https://a.espncdn.com/i/teamlogos/soccer/500/2978.png", 7, "Primera Nacional"),
        MasterTeam("Patronato", "https://a.espncdn.com/i/teamlogos/soccer/500/9746.png", 7, "Primera Nacional"),
        MasterTeam("Quilmes", "https://a.espncdn.com/i/teamlogos/soccer/500/239.png", 7, "Primera Nacional"),
        MasterTeam("Racing de Córdoba", "https://a.espncdn.com/i/teamlogos/soccer/500/2980.png", 7, "Primera Nacional"),
        MasterTeam("San Martín de Tucumán", "https://a.espncdn.com/i/teamlogos/soccer/500/9747.png", 7, "Primera Nacional"),
        MasterTeam("San Miguel", "https://a.espncdn.com/i/teamlogos/soccer/500/10065.png", 7, "Primera Nacional"),
        MasterTeam("San Telmo", "https://a.espncdn.com/i/teamlogos/soccer/500/10066.png", 7, "Primera Nacional"),
        MasterTeam("Temperley", "https://a.espncdn.com/i/teamlogos/soccer/500/9748.png", 7, "Primera Nacional"),
        MasterTeam("Tristán Suárez", "https://a.espncdn.com/i/teamlogos/soccer/500/9749.png", 7, "Primera Nacional")
    )

    // 4. PRIMERA B METROPOLITANA (22)
    val PRIMERA_B_METRO = listOf(
        MasterTeam("Acassuso", "https://a.espncdn.com/i/teamlogos/soccer/500/10058.png", 8, "Primera B Metropolitana"),
        MasterTeam("Argentino de Merlo", "https://a.espncdn.com/i/teamlogos/soccer/500/10155.png", 8, "Primera B Metropolitana"),
        MasterTeam("Argentino de Quilmes", "https://a.espncdn.com/i/teamlogos/soccer/500/10159.png", 8, "Primera B Metropolitana"),
        MasterTeam("Arsenal de Sarandí", "https://a.espncdn.com/i/teamlogos/soccer/500/2383.png", 8, "Primera B Metropolitana"),
        MasterTeam("Camioneros", "https://a.espncdn.com/i/teamlogos/soccer/500/20341.png", 8, "Primera B Metropolitana"),
        MasterTeam("Comunicaciones", "https://a.espncdn.com/i/teamlogos/soccer/500/10068.png", 8, "Primera B Metropolitana"),
        MasterTeam("Defensores Unidos (Zárate)", "https://a.espncdn.com/i/teamlogos/soccer/500/18843.png", 8, "Primera B Metropolitana"),
        MasterTeam("Deportivo Armenio", "https://a.espncdn.com/i/teamlogos/soccer/500/10069.png", 8, "Primera B Metropolitana"),
        MasterTeam("Deportivo Laferrere", "https://a.espncdn.com/i/teamlogos/soccer/500/10070.png", 8, "Primera B Metropolitana"),
        MasterTeam("Deportivo Liniers", "https://a.espncdn.com/i/teamlogos/soccer/500/20342.png", 8, "Primera B Metropolitana"),
        MasterTeam("Deportivo Merlo", "https://a.espncdn.com/i/teamlogos/soccer/500/9749.png", 8, "Primera B Metropolitana"),
        MasterTeam("Excursionistas", "https://a.espncdn.com/i/teamlogos/soccer/500/10071.png", 8, "Primera B Metropolitana"),
        MasterTeam("Flandria", "https://a.espncdn.com/i/teamlogos/soccer/500/10072.png", 8, "Primera B Metropolitana"),
        MasterTeam("Ituzaingó", "https://a.espncdn.com/i/teamlogos/soccer/500/10073.png", 8, "Primera B Metropolitana"),
        MasterTeam("Real Pilar", "https://a.espncdn.com/i/teamlogos/soccer/500/20343.png", 8, "Primera B Metropolitana"),
        MasterTeam("San Martín de Burzaco", "https://a.espncdn.com/i/teamlogos/soccer/500/18844.png", 8, "Primera B Metropolitana"),
        MasterTeam("Sportivo Dock Sud", "https://a.espncdn.com/i/teamlogos/soccer/500/10074.png", 8, "Primera B Metropolitana"),
        MasterTeam("Sportivo Italiano", "https://a.espncdn.com/i/teamlogos/soccer/500/10075.png", 8, "Primera B Metropolitana"),
        MasterTeam("Talleres de Remedios de Escalada", "https://a.espncdn.com/i/teamlogos/soccer/500/10076.png", 8, "Primera B Metropolitana"),
        MasterTeam("UAI Urquiza", "https://a.espncdn.com/i/teamlogos/soccer/500/10077.png", 8, "Primera B Metropolitana"),
        MasterTeam("Villa Dálmine", "https://a.espncdn.com/i/teamlogos/soccer/500/9750.png", 8, "Primera B Metropolitana"),
        MasterTeam("Villa San Carlos", "https://a.espncdn.com/i/teamlogos/soccer/500/10078.png", 8, "Primera B Metropolitana")
    )

    // 5. TORNEO FEDERAL A (36)
    val TORNEO_FEDERAL_A = listOf(
        MasterTeam("9 de Julio (Rafaela)", "https://a.espncdn.com/i/teamlogos/soccer/500/19689.png", 15, "Torneo Federal A"),
        MasterTeam("Atenas (Río Cuarto)", "https://a.espncdn.com/i/teamlogos/soccer/500/19690.png", 15, "Torneo Federal A"),
        MasterTeam("Atlético Club San Martín (Mendoza)", "https://a.espncdn.com/i/teamlogos/soccer/500/11974.png", 15, "Torneo Federal A"),
        MasterTeam("Bartolomé Mitre (Posadas)", "https://a.espncdn.com/i/teamlogos/soccer/500/20344.png", 15, "Torneo Federal A"),
        MasterTeam("Boca Unidos (Corrientes)", "https://a.espncdn.com/i/teamlogos/soccer/500/9748.png", 15, "Torneo Federal A"),
        MasterTeam("Círculo Deportivo (Otamendi)", "https://a.espncdn.com/i/teamlogos/soccer/500/19691.png", 15, "Torneo Federal A"),
        MasterTeam("Cipolletti (Río Negro)", "https://a.espncdn.com/i/teamlogos/soccer/500/11975.png", 15, "Torneo Federal A"),
        MasterTeam("Costa Brava (General Pico)", "https://a.espncdn.com/i/teamlogos/soccer/500/20345.png", 15, "Torneo Federal A"),
        MasterTeam("Defensores de Belgrano (Villa Ramallo)", "https://a.espncdn.com/i/teamlogos/soccer/500/11976.png", 15, "Torneo Federal A"),
        MasterTeam("Defensores de Vilelas (Chaco)", "https://a.espncdn.com/i/teamlogos/soccer/500/20346.png", 15, "Torneo Federal A"),
        MasterTeam("Deportivo Argentino (Monte Maíz)", "https://a.espncdn.com/i/teamlogos/soccer/500/19692.png", 15, "Torneo Federal A"),
        MasterTeam("Deportivo Rincón (Neuquén)", "https://a.espncdn.com/i/teamlogos/soccer/500/20347.png", 15, "Torneo Federal A"),
        MasterTeam("Douglas Haig (Pergamino)", "https://a.espncdn.com/i/teamlogos/soccer/500/10064.png", 15, "Torneo Federal A"),
        MasterTeam("El Linqueño (Lincoln)", "https://a.espncdn.com/i/teamlogos/soccer/500/19693.png", 15, "Torneo Federal A"),
        MasterTeam("Escobar FC", "https://a.espncdn.com/i/teamlogos/soccer/500/20348.png", 15, "Torneo Federal A"),
        MasterTeam("FADEP (Mendoza)", "https://a.espncdn.com/i/teamlogos/soccer/500/20349.png", 15, "Torneo Federal A"),
        MasterTeam("Germinal (Rawson)", "https://a.espncdn.com/i/teamlogos/soccer/500/19694.png", 15, "Torneo Federal A"),
        MasterTeam("Gimnasia y Esgrima (Concepción del Uruguay)", "https://a.espncdn.com/i/teamlogos/soccer/500/11977.png", 15, "Torneo Federal A"),
        MasterTeam("Gimnasia y Esgrima (Chivilcoy)", "https://a.espncdn.com/i/teamlogos/soccer/500/20350.png", 15, "Torneo Federal A"),
        MasterTeam("Guillermo Brown (Puerto Madryn)", "https://a.espncdn.com/i/teamlogos/soccer/500/10160.png", 15, "Torneo Federal A"),
        MasterTeam("Huracán Las Heras", "https://a.espncdn.com/i/teamlogos/soccer/500/17704.png", 15, "Torneo Federal A"),
        MasterTeam("Independiente (Chivilcoy)", "https://a.espncdn.com/i/teamlogos/soccer/500/19695.png", 15, "Torneo Federal A"),
        MasterTeam("Juventud Antoniana (Salta)", "https://a.espncdn.com/i/teamlogos/soccer/500/2979.png", 15, "Torneo Federal A"),
        MasterTeam("Juventud Unida Universitario (San Luis)", "https://a.espncdn.com/i/teamlogos/soccer/500/11978.png", 15, "Torneo Federal A"),
        MasterTeam("Kimberley (Mar del Plata)", "https://a.espncdn.com/i/teamlogos/soccer/500/20351.png", 15, "Torneo Federal A"),
        MasterTeam("Olimpo (Bahía Blanca)", "https://a.espncdn.com/i/teamlogos/soccer/500/13.png", 15, "Torneo Federal A"),
        MasterTeam("Ramón Santamarina (Tandil)", "https://a.espncdn.com/i/teamlogos/soccer/500/10067.png", 15, "Torneo Federal A"),
        MasterTeam("Sarmiento (La Banda)", "https://a.espncdn.com/i/teamlogos/soccer/500/20352.png", 15, "Torneo Federal A"),
        MasterTeam("Sarmiento (Resistencia)", "https://a.espncdn.com/i/teamlogos/soccer/500/11979.png", 15, "Torneo Federal A"),
        MasterTeam("Sol de América (Formosa)", "https://a.espncdn.com/i/teamlogos/soccer/500/19696.png", 15, "Torneo Federal A"),
        MasterTeam("Sol de Mayo (Viedma)", "https://a.espncdn.com/i/teamlogos/soccer/500/17705.png", 15, "Torneo Federal A"),
        MasterTeam("Sportivo Belgrano (San Francisco)", "https://a.espncdn.com/i/teamlogos/soccer/500/10079.png", 15, "Torneo Federal A"),
        MasterTeam("Sportivo Las Parejas", "https://a.espncdn.com/i/teamlogos/soccer/500/17706.png", 15, "Torneo Federal A"),
        MasterTeam("Sportivo San Martín (Formosa)", "https://a.espncdn.com/i/teamlogos/soccer/500/17707.png", 15, "Torneo Federal A"),
        MasterTeam("Tucumán Central", "https://a.espncdn.com/i/teamlogos/soccer/500/20353.png", 15, "Torneo Federal A"),
        MasterTeam("Villa Mitre (Bahía Blanca)", "https://a.espncdn.com/i/teamlogos/soccer/500/10080.png", 15, "Torneo Federal A")
    )

    // 6. CLUBES INTERNACIONALES (CONMEBOL - LIBERTADORES Y SUDAMERICANA SIN ARGENTINOS)
    val INTERNATIONAL_CLUBS = listOf(
        // Bolivia
        MasterTeam("Always Ready", "https://a.espncdn.com/i/teamlogos/soccer/500/19697.png", 3, "Bolivia"),
        MasterTeam("Blooming", "https://a.espncdn.com/i/teamlogos/soccer/500/2201.png", 4, "Bolivia"),
        MasterTeam("Bolívar", "https://a.espncdn.com/i/teamlogos/soccer/500/2202.png", 3, "Bolivia"),
        MasterTeam("Guabirá", "https://a.espncdn.com/i/teamlogos/soccer/500/2204.png", 4, "Bolivia"),
        MasterTeam("Independiente Petrolero", "https://a.espncdn.com/i/teamlogos/soccer/500/20355.png", 4, "Bolivia"),
        MasterTeam("Nacional Potosí", "https://a.espncdn.com/i/teamlogos/soccer/500/11983.png", 3, "Bolivia"),
        MasterTeam("San Antonio Bulo Bulo", "https://a.espncdn.com/i/teamlogos/soccer/500/20356.png", 4, "Bolivia"),
        MasterTeam("The Strongest", "https://a.espncdn.com/i/teamlogos/soccer/500/2207.png", 3, "Bolivia"),

        // Brasil
        MasterTeam("Atlético Mineiro", "https://a.espncdn.com/i/teamlogos/soccer/500/814.png", 4, "Brasil"),
        MasterTeam("Bahía", "https://a.espncdn.com/i/teamlogos/soccer/500/3457.png", 3, "Brasil"),
        MasterTeam("Botafogo", "https://a.espncdn.com/i/teamlogos/soccer/500/815.png", 3, "Brasil"),
        MasterTeam("Corinthians", "https://a.espncdn.com/i/teamlogos/soccer/500/816.png", 3, "Brasil"),
        MasterTeam("Cruzeiro", "https://a.espncdn.com/i/teamlogos/soccer/500/817.png", 3, "Brasil"),
        MasterTeam("Flamengo", "https://a.espncdn.com/i/teamlogos/soccer/500/819.png", 3, "Brasil"),
        MasterTeam("Fluminense", "https://a.espncdn.com/i/teamlogos/soccer/500/820.png", 3, "Brasil"),
        MasterTeam("Grêmio", "https://a.espncdn.com/i/teamlogos/soccer/500/821.png", 4, "Brasil"),
        MasterTeam("Mirassol", "https://a.espncdn.com/i/teamlogos/soccer/500/10081.png", 3, "Brasil"),
        MasterTeam("Palmeiras", "https://a.espncdn.com/i/teamlogos/soccer/500/824.png", 3, "Brasil"),
        MasterTeam("Red Bull Bragantino", "https://a.espncdn.com/i/teamlogos/soccer/500/10082.png", 4, "Brasil"),
        MasterTeam("Santos", "https://a.espncdn.com/i/teamlogos/soccer/500/826.png", 4, "Brasil"),
        MasterTeam("São Paulo", "https://a.espncdn.com/i/teamlogos/soccer/500/825.png", 4, "Brasil"),
        MasterTeam("Vasco da Gama", "https://a.espncdn.com/i/teamlogos/soccer/500/3454.png", 4, "Brasil"),

        // Chile
        MasterTeam("Audax Italiano", "https://a.espncdn.com/i/teamlogos/soccer/500/2209.png", 4, "Chile"),
        MasterTeam("Cobresal", "https://a.espncdn.com/i/teamlogos/soccer/500/2210.png", 4, "Chile"),
        MasterTeam("Coquimbo Unido", "https://a.espncdn.com/i/teamlogos/soccer/500/2211.png", 3, "Chile"),
        MasterTeam("Huachipato", "https://a.espncdn.com/i/teamlogos/soccer/500/2212.png", 3, "Chile"),
        MasterTeam("O'Higgins", "https://a.espncdn.com/i/teamlogos/soccer/500/2214.png", 3, "Chile"),
        MasterTeam("Palestino", "https://a.espncdn.com/i/teamlogos/soccer/500/2215.png", 4, "Chile"),
        MasterTeam("Universidad Católica", "https://a.espncdn.com/i/teamlogos/soccer/500/2218.png", 3, "Chile"),
        MasterTeam("Universidad de Chile", "https://a.espncdn.com/i/teamlogos/soccer/500/2219.png", 4, "Chile"),

        // Colombia
        MasterTeam("América de Cali", "https://a.espncdn.com/i/teamlogos/soccer/500/2221.png", 4, "Colombia"),
        MasterTeam("Atlético Bucaramanga", "https://a.espncdn.com/i/teamlogos/soccer/500/2222.png", 4, "Colombia"),
        MasterTeam("Atlético Nacional", "https://a.espncdn.com/i/teamlogos/soccer/500/2220.png", 4, "Colombia"),
        MasterTeam("Deportes Tolima", "https://a.espncdn.com/i/teamlogos/soccer/500/2223.png", 3, "Colombia"),
        MasterTeam("Independiente Medellín", "https://a.espncdn.com/i/teamlogos/soccer/500/2225.png", 3, "Colombia"),
        MasterTeam("Independiente Santa Fe", "https://a.espncdn.com/i/teamlogos/soccer/500/2228.png", 3, "Colombia"),
        MasterTeam("Junior", "https://a.espncdn.com/i/teamlogos/soccer/500/2226.png", 3, "Colombia"),
        MasterTeam("Millonarios", "https://a.espncdn.com/i/teamlogos/soccer/500/2227.png", 4, "Colombia"),

        // Ecuador
        MasterTeam("Barcelona SC", "https://a.espncdn.com/i/teamlogos/soccer/500/2229.png", 3, "Ecuador"),
        MasterTeam("Deportivo Cuenca", "https://a.espncdn.com/i/teamlogos/soccer/500/2232.png", 4, "Ecuador"),
        MasterTeam("Independiente del Valle", "https://a.espncdn.com/i/teamlogos/soccer/500/11980.png", 3, "Ecuador"),
        MasterTeam("Libertad FC", "https://a.espncdn.com/i/teamlogos/soccer/500/20357.png", 4, "Ecuador"),
        MasterTeam("Liga de Quito", "https://a.espncdn.com/i/teamlogos/soccer/500/2230.png", 3, "Ecuador"),
        MasterTeam("Macará", "https://a.espncdn.com/i/teamlogos/soccer/500/11984.png", 4, "Ecuador"),
        MasterTeam("Orense", "https://a.espncdn.com/i/teamlogos/soccer/500/20358.png", 4, "Ecuador"),
        MasterTeam("Universidad Católica (Ecuador)", "https://a.espncdn.com/i/teamlogos/soccer/500/11982.png", 3, "Ecuador"),

        // Paraguay
        MasterTeam("Cerro Porteño", "https://a.espncdn.com/i/teamlogos/soccer/500/2232.png", 3, "Paraguay"),
        MasterTeam("Deportivo Recoleta", "https://a.espncdn.com/i/teamlogos/soccer/500/20359.png", 4, "Paraguay"),
        MasterTeam("Guaraní", "https://a.espncdn.com/i/teamlogos/soccer/500/2233.png", 3, "Paraguay"),
        MasterTeam("Libertad", "https://a.espncdn.com/i/teamlogos/soccer/500/2234.png", 3, "Paraguay"),
        MasterTeam("Nacional (Paraguay)", "https://a.espncdn.com/i/teamlogos/soccer/500/2236.png", 4, "Paraguay"),
        MasterTeam("Olimpia", "https://a.espncdn.com/i/teamlogos/soccer/500/2237.png", 4, "Paraguay"),
        MasterTeam("Sportivo 2 de Mayo", "https://a.espncdn.com/i/teamlogos/soccer/500/20354.png", 3, "Paraguay"),
        MasterTeam("Sportivo Trinidense", "https://a.espncdn.com/i/teamlogos/soccer/500/11985.png", 4, "Paraguay"),

        // Perú
        MasterTeam("Alianza Atlético", "https://a.espncdn.com/i/teamlogos/soccer/500/2238.png", 4, "Perú"),
        MasterTeam("Alianza Lima", "https://a.espncdn.com/i/teamlogos/soccer/500/2240.png", 3, "Perú"),
        MasterTeam("Cienciano", "https://a.espncdn.com/i/teamlogos/soccer/500/2239.png", 4, "Perú"),
        MasterTeam("Cusco FC", "https://a.espncdn.com/i/teamlogos/soccer/500/11986.png", 3, "Perú"),
        MasterTeam("Deportivo Garcilaso", "https://a.espncdn.com/i/teamlogos/soccer/500/20360.png", 4, "Perú"),
        MasterTeam("FBC Melgar", "https://a.espncdn.com/i/teamlogos/soccer/500/2241.png", 4, "Perú"),
        MasterTeam("Sporting Cristal", "https://a.espncdn.com/i/teamlogos/soccer/500/2242.png", 3, "Perú"),
        MasterTeam("Universitario", "https://a.espncdn.com/i/teamlogos/soccer/500/2244.png", 3, "Perú"),

        // Uruguay
        MasterTeam("Boston River", "https://a.espncdn.com/i/teamlogos/soccer/500/11987.png", 4, "Uruguay"),
        MasterTeam("Defensor Sporting", "https://a.espncdn.com/i/teamlogos/soccer/500/2247.png", 4, "Uruguay"),
        MasterTeam("Juventud de Las Piedras", "https://a.espncdn.com/i/teamlogos/soccer/500/2249.png", 3, "Uruguay"),
        MasterTeam("Liverpool (Uruguay)", "https://a.espncdn.com/i/teamlogos/soccer/500/2250.png", 3, "Uruguay"),
        MasterTeam("Montevideo City Torque", "https://a.espncdn.com/i/teamlogos/soccer/500/18845.png", 4, "Uruguay"),
        MasterTeam("Nacional (Uruguay)", "https://a.espncdn.com/i/teamlogos/soccer/500/2251.png", 3, "Uruguay"),
        MasterTeam("Peñarol", "https://a.espncdn.com/i/teamlogos/soccer/500/2253.png", 3, "Uruguay"),
        MasterTeam("Racing Club de Montevideo", "https://a.espncdn.com/i/teamlogos/soccer/500/2252.png", 4, "Uruguay"),

        // Venezuela
        MasterTeam("Academia Puerto Cabello", "https://a.espncdn.com/i/teamlogos/soccer/500/19698.png", 4, "Venezuela"),
        MasterTeam("Carabobo", "https://a.espncdn.com/i/teamlogos/soccer/500/2255.png", 3, "Venezuela"),
        MasterTeam("Caracas FC", "https://a.espncdn.com/i/teamlogos/soccer/500/2254.png", 4, "Venezuela"),
        MasterTeam("Deportivo La Guaira", "https://a.espncdn.com/i/teamlogos/soccer/500/11988.png", 3, "Venezuela"),
        MasterTeam("Deportivo Táchira", "https://a.espncdn.com/i/teamlogos/soccer/500/2256.png", 3, "Venezuela"),
        MasterTeam("Metropolitanos", "https://a.espncdn.com/i/teamlogos/soccer/500/19699.png", 4, "Venezuela"),
        MasterTeam("Monagas", "https://a.espncdn.com/i/teamlogos/soccer/500/2257.png", 4, "Venezuela"),
        MasterTeam("Universidad Central de Venezuela (UCV)", "https://a.espncdn.com/i/teamlogos/soccer/500/20361.png", 3, "Venezuela")
    )
}
