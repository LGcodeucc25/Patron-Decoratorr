package com.museum.factory;

import com.museum.model.MuseumPass;
import java.util.ArrayList;
import java.util.List;

/** Concrete Factory Method product and base component for optional decorators. */
public class StudentMuseumPass implements MuseumPass {
    /** Returns the student product description for decorators to extend. */
    @Override
    public String getDescription() {
        return "Student Museum Pass";
    }

    /** Returns the student base price before decorators add their prices. */
    @Override
    public double getPrice() {
        return 9.00;
    }

    /** Supplies a fresh list of base services for decorator composition. */
    @Override
    public List<String> getServices() {
        return new ArrayList<>(List.of("Museum Entry", "Student Discount"));
    }

    /** Activates the student product before decorators append their activation. */
    @Override
    public String activate() {
        return "Student museum pass activated successfully.";
    }
}
