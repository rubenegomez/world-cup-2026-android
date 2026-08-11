import re

with open(r'app\src\main\java\com\example\worldcup2026\ui\FixtureScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Extract the VIP Stats block (695-707)
# 2. Extract the PRODE section (709-862)
# 3. Extract the TANDA DE PENALES section (864-999)

vip_stats_pattern = re.compile(r'(            if \(match\.status\.uppercase\(\) == "FINISHED"\) \{.*?\}\n)', re.DOTALL)
vip_stats_match = vip_stats_pattern.search(content)

prode_start = content.find('            // SECCIÓN PRODE (Predicción)')
tanda_penales_start = content.find('            val isLivePenalties')
tanda_penales_end = content.find('            // Referencia sutil del Estadio')

if prode_start != -1 and tanda_penales_start != -1 and tanda_penales_end != -1:
    part1 = content[:prode_start]
    prode_part = content[prode_start:tanda_penales_start]
    tanda_part = content[tanda_penales_start:tanda_penales_end]
    part3 = content[tanda_penales_end:]

    # Modify PRODE part to add EDITAR/GUARDAR logic
    # Find start of SECCIÓN PRODE
    # We will add isEditingProde variable
    # Then wrap the PredictionChip and PredictionInput with enabled = isEditingProde
    # And add a button
    
    modified_prode = prode_part.replace('if (match.status.uppercase() == "SCHEDULED") {', 
'''val matchHasStarted = match.status.uppercase() != "SCHEDULED"
                var isEditingProde by remember { mutableStateOf(false) }
                
                if (!matchHasStarted) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (!isEditingProde) {
                            Button(onClick = { isEditingProde = true }, modifier = Modifier.height(28.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))) {
                                Text("EDITAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(onClick = { 
                                isEditingProde = false
                                // onPredictionChange ya guarda en BD y hace submit a API a traves del ViewModel si esta autenticado
                                // Asi que solo con guardarlo aca esta bien, pero para asegurarnos, forzamos la accion de guardar con los valores actuales.
                                onPredictionChange(match.id, match.predictedWinner, match.predictedHomeScore, match.predictedAwayScore, match.predictedHomePenalties, match.predictedAwayPenalties)
                            }, modifier = Modifier.height(28.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                                Text("GUARDAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
''')
    
    # Now replace enabled = true with enabled = isEditingProde
    modified_prode = modified_prode.replace('PredictionChip(label = "L", selected = match.predictedWinner == "L") {', 'PredictionChip(label = "L", selected = match.predictedWinner == "L", enabled = isEditingProde) {')
    modified_prode = modified_prode.replace('PredictionChip(label = "E", selected = match.predictedWinner == "E") {', 'PredictionChip(label = "E", selected = match.predictedWinner == "E", enabled = isEditingProde) {')
    modified_prode = modified_prode.replace('PredictionChip(label = "V", selected = match.predictedWinner == "V") {', 'PredictionChip(label = "V", selected = match.predictedWinner == "V", enabled = isEditingProde) {')
    modified_prode = modified_prode.replace('value = match.predictedHomeScore, \n                            onValueChange', 'value = match.predictedHomeScore, \n                            enabled = isEditingProde,\n                            onValueChange')
    modified_prode = modified_prode.replace('value = match.predictedAwayScore, \n                            onValueChange', 'value = match.predictedAwayScore, \n                            enabled = isEditingProde,\n                            onValueChange')
    modified_prode = modified_prode.replace('value = match.predictedHomePenalties,\n                        onValueChange', 'value = match.predictedHomePenalties,\n                        enabled = isEditingProde,\n                        onValueChange')
    modified_prode = modified_prode.replace('value = match.predictedAwayPenalties,\n                        onValueChange', 'value = match.predictedAwayPenalties,\n                        enabled = isEditingProde,\n                        onValueChange')
    
    modified_prode = modified_prode.replace('if (match.status.uppercase() == "SCHEDULED") {', 'if (!matchHasStarted) {')

    new_content = part1 + tanda_part + modified_prode + part3

    with open(r'app\src\main\java\com\example\worldcup2026\ui\FixtureScreen.kt', 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Reorder successful!")
else:
    print("Failed to find sections.")
