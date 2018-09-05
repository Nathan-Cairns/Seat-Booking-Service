package nz.ac.auckland.concert.utility;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatRow;

public class SeatUtility {

    /**
     * Helper method used for determining price band based on seat row.
     *
     * @return Corresponding price band to this seat row
     */
    public static PriceBand determinePriceBand(SeatRow seatRow) {
        switch (seatRow) {
            case A:
                return PriceBand.PriceBandB;
            case B:
                return PriceBand.PriceBandB;
            case C:
                return PriceBand.PriceBandB;
            case D:
                return PriceBand.PriceBandB;
            case E:
                return PriceBand.PriceBandA;
            case F:
                return PriceBand.PriceBandA;
            case G:
                return PriceBand.PriceBandA;
            case H:
                return PriceBand.PriceBandC;
            case I:
                return PriceBand.PriceBandA;
            case J:
                return PriceBand.PriceBandA;
            case K:
                return PriceBand.PriceBandA;
            case L:
                return PriceBand.PriceBandA;
            case M:
                return PriceBand.PriceBandA;
            case N:
                return PriceBand.PriceBandC;
            case O:
                return PriceBand.PriceBandC;
            case P:
                return PriceBand.PriceBandC;
            case Q:
                return PriceBand.PriceBandC;
            case R:
                return PriceBand.PriceBandC;
        }

        // Return null if row was invalid
        return null;
    }
}
