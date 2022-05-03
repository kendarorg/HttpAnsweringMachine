package org.kendar.dns;

interface DNSTunnelConstants {

    static enum QType {
        A ((short) 1, "A"),
        TXT ((short) 16, "TXT");

        private final short value;
        private final String name;

        QType(short value, String name) {
            this.value = value;
            this.name = name;
        }

        short getValue() {
            return value;
        }
        
        String getName() {
            return name;
        }
    }

    static enum Class {
        IN ((short) 1);

        private final short value;

        Class(short value) {
            this.value = value;
        }

        short getValue() {
            return value;
        }
    }

    static final String SEPARATOR = ":";
    static final String STOP = "stop";
    static final short QR_RESPONSE = (short) (1 << 15);
    static final short AA_BIT = 1 << 10;

    static final CrockfordBase32 BASE32 = new CrockfordBase32();
}
