package com.freightnexus.tracking;

/**
 * Calculates great-circle distance between two points using the Haversine formula.
 * Accurate to within ~0.5%, sufficient for ETA estimation at road freight distances.
 */
public final class HaversineCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private HaversineCalculator() {}

    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static double etaHours(double distanceKm, double avgSpeedKmh) {
        if (avgSpeedKmh <= 0) return Double.MAX_VALUE;
        return distanceKm / avgSpeedKmh;
    }
}
