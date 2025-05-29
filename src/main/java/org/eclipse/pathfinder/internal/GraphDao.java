package org.eclipse.pathfinder.internal;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.List;

@ApplicationScoped
public class GraphDao implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SecureRandom random = new SecureRandom();

    public List<String> listLocations() {
        return List.of("CNHKG", "AUMEL", "SESTO", "FIHEL", "USCHI", "JNTKO", "DEHAM", "CNSHA", "NLRTM", "SEGOT",
                "CNHGH", "USNYC", "USDAL");
    }

    public String getVoyageNumber(String from, String to) {
        int i = random.nextInt(5);

        return switch (i) {
            case 0 -> "0100S";
            case 1 -> "0200T";
            case 2 -> "0300A";
            case 3 -> "0301S";
            default -> "0400S";
        };
    }

}
