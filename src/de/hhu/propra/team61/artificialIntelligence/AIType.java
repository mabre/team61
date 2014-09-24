package de.hhu.propra.team61.artificialIntelligence;

/**
 * Created by markus on 29.08.14. TODO IMPORTANT
 */
public enum AIType {
    NULL(0),
    SIMPLE(1),
    DUMMY(-1);

    private int value;

    public static AIType fromInteger(int value) {
        switch(value) {
            case 1:
                return SIMPLE;
            case -1:
                return DUMMY;
            case 0:
            default:
                return NULL;
        }
    }

    private AIType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
