/**
 *  IntegerGeoPoint
 *  Copyright 2009 by Michael Peter Christen; mc@yacy.net, Frankfurt a. M., Germany
 *  first published 08.10.2009 on http://yacy.net
 *
 *  This file is part of YaCy Content Integration
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.kumar.geo;

/**
 * GeoPoint implementation with Integer accuracy
 */
public class IntegerGeoPoint extends AbstractGeoPoint implements GeoPoint {
    private final long latlon; // using one variable for the coordinate pair saves some space

    /**
     * Generates an IntegerGeoPoint from doubles
     * @param lat	The latitude e.g. 54.123
     * @param lon	The longitude e.g. 13.123
     */
    public IntegerGeoPoint(double lat, double lon) {
        this.latlon = (((long) coord2int(lat)) << 32) | (coord2int(lon));
    }

    /**
     * Generates an IntegerGeoPoint from integers
     * @param lat	Eight digit latitude, e.g. (Brandenburger Tor, Berlin) 52516389
     * @param lon	Eight digit longitude, e.g. (Brandenburger Tor, Berlin) 13377778
     */
    public IntegerGeoPoint(int lat, int lon) {
        this.latlon = (((long) coord2int(lat / 1e6d)) << 32) | (coord2int(lon / 1e6d));
    }

    /**
     * Returns the latitude as a double
     */
    @Override
    public double lat() {
        return int2coord((int) (this.latlon >>> 32));
    }


    @Override
    public double lon() {
        return int2coord((int) (this.latlon & (Integer.MAX_VALUE)));
    }
    
    /**
     * get the implementation-dependent accuracy of the latitude
     * @return
     */
    @Override
    public double accuracyLat() {
        return Math.abs(int2coord(1) - int2coord(2));
    }

    /**
     * get the implementation-dependent accuracy of the longitude
     * @return
     */
    @Override
    public double accuracyLon() {
        return Math.abs(int2coord(1) - int2coord(2));
    }

    private static final double maxint = new Double(Integer.MAX_VALUE).doubleValue();
    private static final double upscale = maxint / 360.0d;

    private static final int coord2int(double coord) {
        return (int) ((coord + 180.0d) * upscale);
    }

    private static final double int2coord(int z) {
        return (z / upscale) - 180.0d;
    }

    /**
     * compute the hash code of a coordinate
     * this produces identical hash codes for locations that are close to each other
     */
    @Override
    public int hashCode() {
        return (int) ((this.latlon & Integer.MAX_VALUE) >> 1) + (int) (this.latlon >> 33);
    }

    /**
     * equality test that is needed to use the class inside HashMap/HashSet
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof IntegerGeoPoint)) return false;
        IntegerGeoPoint oo = (IntegerGeoPoint) o;
        return (this.latlon == oo.latlon);
    }

    @Override
    public String toString() {
        return "[" + this.lat() + "," + this.lon() + "]";
    }
}
