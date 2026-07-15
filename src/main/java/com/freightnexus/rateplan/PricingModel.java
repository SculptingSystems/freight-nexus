package com.freightnexus.rateplan;

public enum PricingModel {
    FLAT,      // fixed charge per load
    PER_KG,    // base_rate × shipment weight_kg
    PER_KM     // base_rate × lane distance_km
}
