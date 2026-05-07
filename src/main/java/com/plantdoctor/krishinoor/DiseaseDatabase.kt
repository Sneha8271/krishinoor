package com.plantdoctor.krishinoor

object DiseaseDatabase {
    data class Info(val severity: String, val solution: String)

    private val healthy = Info("None",
        "✅ Your plant is healthy!\n\n" +
                "• Maintain regular watering schedule\n" +
                "• Balanced NPK fertilization\n" +
                "• Monitor weekly for early signs\n" +
                "• Ensure proper field drainage\n" +
                "• Keep field clean of weeds"
    )

    private val db = mapOf(
        "Apple___Apple_scab" to Info("Medium",
            "• Remove and destroy infected leaves\n" +
                    "• Apply Captan or Mancozeb fungicide every 7–10 days\n" +
                    "• Avoid overhead watering\n" +
                    "• Prune for better air circulation"),

        "Apple___Black_rot" to Info("High",
            "• Remove mummified fruits and dead wood immediately\n" +
                    "• Apply copper-based fungicide\n" +
                    "• Disinfect pruning tools with bleach\n" +
                    "• Destroy all infected plant debris"),

        "Apple___Cedar_apple_rust" to Info("Medium",
            "• Apply Myclobutanil fungicide at bud break\n" +
                    "• Remove nearby juniper/cedar trees if possible\n" +
                    "• Use rust-resistant apple varieties next season"),

        "Apple___healthy" to healthy,
        "Blueberry___healthy" to healthy,

        "Cherry_(including_sour)___Powdery_mildew" to Info("Low",
            "• Apply sulfur-based fungicide\n" +
                    "• Prune infected shoots\n" +
                    "• Improve air circulation\n" +
                    "• Avoid excess nitrogen fertilizer"),

        "Cherry_(including_sour)___healthy" to healthy,

        "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot" to Info("Medium",
            "• Plant resistant hybrids\n" +
                    "• Apply Strobilurin fungicide\n" +
                    "• Reduce crop density for better airflow\n" +
                    "• Rotate with non-host crops"),

        "Corn_(maize)___Common_rust_" to Info("Low",
            "• Apply fungicide if infection is severe\n" +
                    "• Plant rust-resistant varieties\n" +
                    "• Early planting reduces rust risk\n" +
                    "• Monitor fields regularly"),

        "Corn_(maize)___Northern_Leaf_Blight" to Info("Medium",
            "• Apply Propiconazole fungicide\n" +
                    "• Use resistant varieties\n" +
                    "• Crop rotation with non-grass crops\n" +
                    "• Till infected debris after harvest"),

        "Corn_(maize)___healthy" to healthy,

        "Grape___Black_rot" to Info("High",
            "• Apply Myclobutanil fungicide before bloom\n" +
                    "• Remove mummified berries from vine and ground\n" +
                    "• Prune for open canopy\n" +
                    "• Destroy all infected material"),

        "Grape___Esca_(Black_Measles)" to Info("High",
            "• No chemical cure — remove infected wood\n" +
                    "• Paint pruning wounds with sealant\n" +
                    "• Avoid water stress\n" +
                    "• Keep irrigation consistent"),

        "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)" to Info("Medium",
            "• Apply Mancozeb fungicide\n" +
                    "• Remove infected leaves\n" +
                    "• Improve air circulation through canopy management"),

        "Grape___healthy" to healthy,

        "Orange___Haunglongbing_(Citrus_greening)" to Info("High",
            "• No cure — remove and destroy infected trees\n" +
                    "• Control Asian citrus psyllid population\n" +
                    "• Use certified disease-free planting material\n" +
                    "• Report to local agricultural authority"),

        "Peach___Bacterial_spot" to Info("Medium",
            "• Apply copper bactericide in spring\n" +
                    "• Avoid overhead irrigation\n" +
                    "• Prune for good air circulation\n" +
                    "• Use resistant peach varieties"),

        "Peach___healthy" to healthy,

        "Pepper,_bell___Bacterial_spot" to Info("Medium",
            "• Apply copper-based bactericide\n" +
                    "• Use disease-free transplants\n" +
                    "• Avoid working in wet fields\n" +
                    "• Rotate crops every 2–3 years"),

        "Pepper,_bell___healthy" to healthy,
        "Raspberry___healthy" to healthy,
        "Soybean___healthy" to healthy,

        "Squash___Powdery_mildew" to Info("Low",
            "• Apply potassium bicarbonate or neem oil\n" +
                    "• Remove heavily infected leaves\n" +
                    "• Plant in full sun with good air circulation\n" +
                    "• Avoid overhead watering"),

        "Strawberry___Leaf_scorch" to Info("Medium",
            "• Apply Myclobutanil fungicide\n" +
                    "• Remove infected leaves\n" +
                    "• Avoid overhead irrigation\n" +
                    "• Renovate beds after harvest"),

        "Strawberry___healthy" to healthy,

        "Tomato___Bacterial_spot" to Info("High",
            "• Apply copper-based bactericide weekly\n" +
                    "• Avoid working with plants when wet\n" +
                    "• Remove heavily infected plants\n" +
                    "• Use disease-free seeds next season"),

        "Tomato___Early_blight" to Info("Medium",
            "• Remove lower infected leaves\n" +
                    "• Apply Mancozeb or Chlorothalonil fungicide\n" +
                    "• Mulch around plants to prevent soil splash\n" +
                    "• Water at base, not on leaves"),

        "Tomato___Late_blight" to Info("High",
            "• Apply Metalaxyl fungicide immediately\n" +
                    "• Remove and destroy all infected plants\n" +
                    "• Do not compost infected material\n" +
                    "• Avoid overhead irrigation"),

        "Tomato___Leaf_Mold" to Info("Medium",
            "• Improve ventilation\n" +
                    "• Apply Chlorothalonil fungicide\n" +
                    "• Reduce humidity below 85%\n" +
                    "• Remove infected leaves"),

        "Tomato___Septoria_leaf_spot" to Info("Medium",
            "• Remove infected leaves immediately\n" +
                    "• Apply Mancozeb or copper fungicide\n" +
                    "• Avoid wetting foliage when watering\n" +
                    "• Rotate crops annually"),

        "Tomato___Spider_mites Two-spotted_spider_mite" to Info("Medium",
            "• Spray plants with water to dislodge mites\n" +
                    "• Apply insecticidal soap or neem oil\n" +
                    "• Introduce predatory mites\n" +
                    "• Keep plants well watered"),

        "Tomato___Target_Spot" to Info("Medium",
            "• Apply Azoxystrobin fungicide\n" +
                    "• Remove and destroy infected leaves\n" +
                    "• Avoid overhead irrigation\n" +
                    "• Crop rotation recommended"),

        "Tomato___Tomato_Yellow_Leaf_Curl_Virus" to Info("High",
            "• No cure — remove and destroy infected plants\n" +
                    "• Control whitefly population\n" +
                    "• Use reflective mulch to repel whiteflies\n" +
                    "• Plant virus-resistant varieties next season"),

        "Tomato___Tomato_mosaic_virus" to Info("High",
            "• Remove and destroy infected plants\n" +
                    "• Wash hands and disinfect tools frequently\n" +
                    "• Do not smoke near plants\n" +
                    "• Use resistant varieties next season"),

        "Tomato___healthy" to healthy
    )

    fun getInfo(classKey: String): Info {
        return db[classKey] ?: Info("Unknown",
            "• Consult local agricultural officer\n" +
                    "• Remove severely infected parts\n" +
                    "• Apply broad-spectrum fungicide\n" +
                    "• Monitor surrounding plants")
    }
}