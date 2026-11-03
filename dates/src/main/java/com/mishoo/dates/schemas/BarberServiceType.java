package com.mishoo.dates.schemas;

import lombok.Getter;

@Getter
public enum BarberServiceType {
    HAIRCUT(30),                 // Corte de cabello: 30 min
    BEARD_TRIM(20),              // Arreglo de barba: 20 min
    HAIRCUT_AND_BEARD(50),       // Corte y barba: 50 min
    SHAVE(30),                   // Afeitado tradicional: 30 min
    HAIR_DYE(60),                // Tinte / Coloración: 60 min
    FACIAL_TREATMENT(40),        // Tratamiento facial: 40 min
    EYEBROW_DESIGN(15),          // Perfilado de cejas: 15 min
    HAIR_WASH_AND_STYLING(25);   // Lavado y peinado: 25 min

    private final int durationMinutes;

    BarberServiceType(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
