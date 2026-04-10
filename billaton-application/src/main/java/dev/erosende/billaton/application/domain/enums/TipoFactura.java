package dev.erosende.billaton.application.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoFactura {
    F1("F1", "Factura completa", "F"),
    F3("F3", "Factura emitida en sustitución de tickets", "F"),
    R1("R1", "Factura rectificativa (Art. 80.1/80.2 y error fundado)", "R"),
    R4("R4", "Factura rectificativa (resto de supuestos)", "R");

    private final String code;
    private final String description;
    private final String seriesPrefix;

    public static TipoFactura fromCode(String code) {
        for (TipoFactura tipo : values()) {
            if (tipo.code.equals(code)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Unknown TipoFactura code: " + code);
    }

    public boolean isRectificativa() {
        return code.startsWith("R");
    }
}
