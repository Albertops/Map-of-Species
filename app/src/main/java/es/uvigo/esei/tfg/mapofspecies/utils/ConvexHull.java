package es.uvigo.esei.tfg.mapofspecies.utils;

import java.util.ArrayList;

import es.uvigo.esei.tfg.mapofspecies.data.Occurrence;

/**
 * Permite calcular la envolvente convexa de una lista de coordenadas.
 * @author Alberto Pardellas Soto
 */
public class ConvexHull {

    /**
     * Calcula la envolvente convexa mediante el método QuickHull.
     * @param occurrences lista con las coordenadas.
     * @return lista con los vértices del polígono.
     */
    public static ArrayList<Occurrence> quickHull(ArrayList<Occurrence> occurrences) {
        ArrayList<Occurrence> lock = new ArrayList<Occurrence>();

        Occurrence min = new Occurrence();
        min.setLongitude(Double.POSITIVE_INFINITY);
        min.setLatitude(Double.POSITIVE_INFINITY);

        Occurrence max = new Occurrence();
        max.setLongitude(Double.NEGATIVE_INFINITY);
        max.setLatitude(Double.NEGATIVE_INFINITY);

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
    public static ArrayList<Occurrence> findHull(
            Occurrence a, Occurrence b, ArrayList<Occurrence> S) {

        ArrayList<Occurrence> lock = new ArrayList<Occurrence>();
        Occurrence c;

        if (!S.isEmpty()) {
            ArrayList<Occurrence> A;
            ArrayList<Occurrence> B;

            c = farthestOccurrence(a, b, S);
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
    public static ArrayList<Occurrence> occurrencesLeft(
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
    private static Boolean isLeft(Occurrence p0, Occurrence p1, Occurrence p2) {
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
    public static Occurrence farthestOccurrence(Occurrence a, Occurrence b, ArrayList<Occurrence> S) {
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
    public static double determinant (Occurrence p1, Occurrence p2, Occurrence p3) {
        double det;

        det = p2.getLatitude() * p3.getLongitude() + p1.getLatitude() * p2.getLongitude()
                + p1.getLongitude() * p3.getLatitude();

        det = p1.getLongitude() * p2.getLatitude() + p2.getLongitude() * p3.getLatitude()
                + p1.getLatitude() * p3.getLongitude() - det;

        return det;
    }
}
