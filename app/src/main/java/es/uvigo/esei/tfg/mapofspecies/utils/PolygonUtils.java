package es.uvigo.esei.tfg.mapofspecies.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import es.uvigo.esei.tfg.mapofspecies.data.Occurrence;

/**
 * Permite calcular la envolvente convexa de una lista de coordenadas.
 * @author Alberto Pardellas Soto
 */
public class PolygonUtils {
    private Occurrence centroid;

    /**
     * Calcula dos hulls. El primero normalmente, y el segundo corriendo las coordenadas 180º.
     * @param occurrences lista con las coordenadas.
     * @return lista con las coordenadas del polígono ordenadas.
     */
    public ArrayList<Occurrence> calculateHulls(ArrayList<Occurrence> occurrences) {
        ArrayList<Occurrence> convexHull = quickHull(occurrences);
        Double convexHullArea = calculateArea(convexHull);

        centroid = getCentroid(convexHull);
        Collections.sort(convexHull, new PolygonComparator());

        ArrayList<Occurrence> aux = new ArrayList<Occurrence>();

        for (int i = 0; i < convexHull.size(); i++) {
            Occurrence start = convexHull.get(i);
            Occurrence end;

            if (i + 1 == convexHull.size()) {
                end = convexHull.get(0);

            } else {
                end = convexHull.get(i + 1);
            }

            Occurrence middle = getMiddle(start, end);

            aux.add(start);
            aux.add(middle);
            aux.add(end);
        }

        convexHull.clear();
        convexHull.addAll(aux);
        aux.clear();


        for (Occurrence occurrence : occurrences) {
            if (occurrence.getLongitude() > 0) {
                occurrence.setLongitude(occurrence.getLongitude() - (double) 180);

            } else {
                occurrence.setLongitude(occurrence.getLongitude() + (double) 180);
            }
        }

        ArrayList<Occurrence> reverseConvexHull = quickHull(occurrences);
        Double reverseConvexHullArea = calculateArea(reverseConvexHull);

        centroid = getCentroid(reverseConvexHull);
        Collections.sort(reverseConvexHull, new PolygonComparator());

        for (int i = 0; i < reverseConvexHull.size(); i++) {
            Occurrence start = reverseConvexHull.get(i);
            Occurrence end;

            if (i + 1 == reverseConvexHull.size()) {
                end = reverseConvexHull.get(0);

            } else {
                end = reverseConvexHull.get(i + 1);
            }

            Occurrence middle = getMiddle(start, end);

            if (middle.getLongitude() >= 0) {
                middle.setLongitude(middle.getLongitude() - (double) 180);

            } else {
                middle.setLongitude(middle.getLongitude() + (double) 180);
            }

            aux.add(start);
            aux.add(middle);
            aux.add(end);
        }

        reverseConvexHull.clear();
        reverseConvexHull.addAll(aux);
        aux.clear();

        for (Occurrence occurrence : occurrences) {
            if (occurrence.getLongitude() >= 0) {
                occurrence.setLongitude(occurrence.getLongitude() - (double) 180);

            } else {
                occurrence.setLongitude(occurrence.getLongitude() + (double) 180);
            }
        }

        if (convexHullArea > reverseConvexHullArea) {
            return reverseConvexHull;
        }

        return convexHull;
    }

    /**
     * Calcula la envolvente convexa mediante el método QuickHull.
     * @param occurrences lista con las coordenadas.
     * @return lista con los vértices del polígono.
     */
    public ArrayList<Occurrence> quickHull(ArrayList<Occurrence> occurrences) {
        ArrayList<Occurrence> lock = new ArrayList<Occurrence>();

        Occurrence min = new Occurrence(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Occurrence max = new Occurrence(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        for (Occurrence occurrence : occurrences) {
            if (occurrence.getLongitude() < min.getLongitude()) {
                min = occurrence;
            }

            if (occurrence.getLongitude() > max.getLongitude()) {
                max = occurrence;
            }
        }

        ArrayList<Occurrence> S1 = occurrencesLeft(min, max, occurrences);
        ArrayList<Occurrence> S2 = occurrencesLeft(max, min, occurrences);

        lock.add(min);
        lock.addAll(findHull(min, max, S1));
        lock.add(max);
        lock.addAll(findHull(max, min, S2));

        return lock;
    }

    /**
     * Se encarga de procesar un subconjunto.
     * @param a punto que se encuentra más a la izquierda.
     * @param b punto que se encuentra más a la derecha.
     * @param S conjunto a procesar.
     * @return lista con las coordenadas del subconjunto procesado.
     */
    private ArrayList<Occurrence> findHull(
            Occurrence a, Occurrence b, ArrayList<Occurrence> S) {

        ArrayList<Occurrence> lock = new ArrayList<Occurrence>();
        Occurrence c;

        if (!S.isEmpty()) {
            ArrayList<Occurrence> A;
            ArrayList<Occurrence> B;

            c = farthestPoint(a, b, S);
            A = occurrencesLeft(a, c, S);
            B = occurrencesLeft(c, b, S);

            lock.addAll(findHull(a, c, A));
            lock.add(c);
            lock.addAll(findHull(c, b, B));
        }

        return lock;
    }

    /**
     * Permite obtener el subconjunto de la izquierda.
     * @param a punto que se encuentra más a la izquierda.
     * @param b punto que se encuentra más a la derecha.
     * @param S conjunto a procesar.
     * @return subconjunto con las coordenadas.
     */
    private ArrayList<Occurrence> occurrencesLeft(
            Occurrence a, Occurrence b, ArrayList<Occurrence> S) {

        ArrayList<Occurrence> subset = new ArrayList<Occurrence>();

        for (Occurrence occurrence : S) {
            if (isLeft(a, b, occurrence)) {
                if (!occurrence.equals(a) && !occurrence.equals(b)) {
                    subset.add(occurrence);
                }
            }
        }

        return subset;
    }

    /**
     * Permite comprobar si un punto está a la izquierda.
     * @param p0 punto que se encuentra más a la izquierda.
     * @param p1 punto que se encuentra más a la derecha.
     * @param p2 punto a comprobar.
     * @return true si está a la izquierda. False en caso contrario.
     */
    private Boolean isLeft(Occurrence p0, Occurrence p1, Occurrence p2) {
        return ((p1.getLongitude() - p0.getLongitude()) * (p2.getLatitude() - p0.getLatitude()) -
                (p2.getLongitude() - p0.getLongitude()) * (p1.getLatitude() - p0.getLatitude()))
                > 0;
    }

    /**
     * Devuelve el punto más lejano de un conjunto.
     * @param a punto que se encuentra más a la izquierda.
     * @param b punto que se encuentra más a la derecha.
     * @param S conjunto a procesar.
     * @return punto más lejano.
     */
    private Occurrence farthestPoint(Occurrence a, Occurrence b, ArrayList<Occurrence> S) {
        Occurrence o = new Occurrence();
        double area = Double.MIN_VALUE;

        for (Occurrence occurrence : S) {
            double areaAux = determinant(a, b, occurrence);

            if (areaAux > area) {
                area = areaAux;
                o = occurrence;
            }
        }

        return o;
    }

    /**
     * Calcula el determinante.
     * @param p1 primer punto.
     * @param p2 segundo punto.
     * @param p3 tercer punto.
     * @return resultado con el determinante.
     */
    private double determinant (Occurrence p1, Occurrence p2, Occurrence p3) {
        double det;

        det = p2.getLatitude() * p3.getLongitude() + p1.getLatitude() * p2.getLongitude()
                + p1.getLongitude() * p3.getLatitude();

        det = p1.getLongitude() * p2.getLatitude() + p2.getLongitude() * p3.getLatitude()
                + p1.getLatitude() * p3.getLongitude() - det;

        return det;
    }

    /**
     * Permite obtener el centroide de un polígono.
     * @param occurrences lista con las coordenadas.
     * @return coordenada con el centroide.
     */
    private Occurrence getCentroid(ArrayList<Occurrence> occurrences) {
        Double latitude = (double) 0;
        Double longitude = (double) 0;

        for (Occurrence occurrence : occurrences) {
            latitude += occurrence.getLatitude();
            longitude += occurrence.getLongitude();
        }

        int totalPoints = occurrences.size();

        return new Occurrence(latitude/totalPoints, longitude/totalPoints);
    }

    /**
     * Permite obtener el punto medio de un segmento.
     * @param o1 extremo inicial del segmento.
     * @param o2 extremo final del segmento.
     * @return coordenada con el punto medio.
     */
    private Occurrence getMiddle(Occurrence o1, Occurrence o2) {
        return new Occurrence(
                (o1.getLatitude() + o2.getLatitude()) / 2,
                (o1.getLongitude() + o2.getLongitude()) / 2);
    }

    /**
     * Permite calcular el área de un polígono.
     * @param points lista de coordenadas.
     * @return área del polígono.
     */
    public double calculateArea (ArrayList<Occurrence> points) {
        final double EARTH_RADIUS = 6371000;

        if (points.size() < 3) {
            return 0;
        }

        double diameter = EARTH_RADIUS * 2;
        double circumference = diameter * Math.PI;
        ArrayList<Double> listY = new ArrayList<Double>();
        ArrayList<Double> listX = new ArrayList<Double>();
        ArrayList<Double> listArea = new ArrayList<Double>();

        // calculate segment x and y in degrees for each point
        final double latitudeRef = points.get(0).getLatitude();
        final double longitudeRef = points.get(0).getLongitude();

        for (int i = 1; i < points.size(); i++) {
            double latitude = points.get(i).getLatitude();
            double longitude = points.get(i).getLongitude();

            listY.add(calculateYSegment(latitudeRef, latitude, circumference));
            listX.add(calculateXSegment(longitudeRef, longitude, latitude, circumference));
        }

        // calculate areas for each triangle segment
        for (int i = 1; i < listX.size(); i++) {
            double x1 = listX.get(i - 1);
            double y1 = listY.get(i - 1);
            double x2 = listX.get(i);
            double y2 = listY.get(i);
            listArea.add(calculateAreaInSquareMeters(x1, x2, y1, y2));
        }

        // sum areas of all triangle segments
        double areasSum = 0;
        for (Double area : listArea) {
            areasSum = areasSum + area;
        }

        // get abolute value of area, it can't be negative
        return Math.abs(areasSum);
    }

    private double calculateAreaInSquareMeters(
            double x1, double x2, double y1, double y2) {

        return (y1 * x2 - x1 * y2) / 2;
    }

    private double calculateYSegment(
            double latitudeRef, double latitude, double circumference) {

        return (latitude - latitudeRef) * circumference / 360.0;
    }

    private double calculateXSegment (double longitudeRef, double longitude,
                                            double latitude, double circumference) {

        return (longitude - longitudeRef) * circumference * Math.cos(Math.toRadians(latitude))
                / 360.0;
    }

    /**
     * Comparador para ordenar las coordenadas en el sentido de las agujas del reloj.
     */
    private class PolygonComparator implements Comparator<Occurrence> {
        @Override
        public int compare(Occurrence o1, Occurrence o2) {
            double angle1 = Math.atan2(
                    o1.getLatitude() - centroid.getLatitude(),
                    o1.getLongitude() - centroid.getLongitude());

            double angle2 = Math.atan2(
                    o2.getLatitude() - centroid.getLatitude(),
                    o2.getLongitude() - centroid.getLongitude());

            if (angle1 < angle2) return 1;
            else if (angle2 > angle1) return -1;
            return 0;
        }
    }
}
