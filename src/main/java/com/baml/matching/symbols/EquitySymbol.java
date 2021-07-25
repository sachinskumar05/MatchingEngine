package com.baml.matching.symbols;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Immutable static data reference loaded at start up marked as good for trade
 * This class will be also used for trade time locking (write lock) on its orderBook
 */
@Data
@Log4j2
public class EquitySymbol implements Symbol { /// when extended with more attributes, make it immutable

    private static final long serialVersionUID = 2405172041950251807L;

    private final String name;

    private double openingPx = 0.0d;

    public EquitySymbol(String name, Double openingPx ) {
        this.name = name;
        this.openingPx = openingPx;
    }

    public Double getOpeningPx() {
        return openingPx;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EquitySymbol equitySymbol = (EquitySymbol) o;

        return name.equals(equitySymbol.name);
    }

}
