package com.serverless.dal;

public enum WayToPlay {
    SKIP, DISCARD, TRICK, PASS, SHOW, DRAW, PICKUP;

    /**
     * @param way the WayToPlay to be converted to a string
     * @return the name of the WayToPlay
     */
    public static String asString(WayToPlay way) {
        if (way.equals(SKIP)) {
            return "SKIP";
        } else if (way.equals(DISCARD)) {
            return "DISCARD";
        } else if (way.equals(TRICK)) {
            return "TRICK";
        } else if (way.equals(PASS)) {
            return "PASS";
        } else if (way.equals(SHOW)) {
            return "SHOW";
        } else if (way.equals(DRAW)) {
            return "DRAW";
        } else {
            return "PICKUP";
        }
    }

    /**
     * @param s the string to be converted to a WayToPlay - must be equivalent to one of the options
     * @return s as a WayToPlay
     */
    public static WayToPlay fromString(String s) {
        if (!(s.equals("SKIP") || s.equals("DISCARD") || s.equals("TRICK") || s.equals("PASS")
                || s.equals("SHOW") || s.equals("DRAW") || s.equals("PICKUP"))) {
            throw new IllegalArgumentException("No way matches " + s);
        }
        switch (s) {
            case "SKIP":
                return SKIP;
            case "DISCARD":
                return DISCARD;
            case "TRICK":
                return TRICK;
            case "PASS":
                return PASS;
            case "DRAW":
                return DRAW;
            case "PICKUP":
                return PICKUP;
            default:
                return SHOW;
        }
    }
}
