package com.example.petrolnavigatorapp.services;

import android.location.Location;

import com.example.petrolnavigatorapp.utils.Fuel;
import com.example.petrolnavigatorapp.utils.Petrol;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class that holds sorting elements in PetrolStationsListFragment.
 * Its modified version of Quick Sort algorithm.
 */
public class QuickSortService {

    public QuickSortService() {
    }

    /**
     * Sorts list of petrol stations objects from closest to furthest.
     *
     * @param list List of petrol stations to be sorted.
     * @param lat  Latitude location of the user.
     * @param lon  Longitude location of the user.
     * @return Sorted list of petrol stations.
     */
    public LinkedList<Petrol> getSortedByDistance(List<Petrol> list, double lat, double lon) {
        LinkedList<Petrol> sortedList = new LinkedList<>();
        Map<Double, Integer> map = new HashMap<>();
        double[] distanceArray = new double[list.size()];
        int i = 0;
        MathService mathService = new MathService();
        for (Petrol p : list) {
            double distance = mathService.getDistanceBetweenTwoPoints(lat, lon, p.getLat(), p.getLon());
            distanceArray[i] = distance;
            map.put(distance, i);
            i++;
        }
        sorting(distanceArray, 0, distanceArray.length - 1);

        for (int j = 0; j < distanceArray.length; j++) {
            Petrol p = list.get(map.get(distanceArray[j]));
            sortedList.add(p);
        }
        return sortedList;
    }

    /**
     * Sorts petrol stations from this with cheapest fuel preferred by user to the most expensive one.
     *
     * @param list     List of petrol stations to be sorted.
     * @param prefFuel Preferred by user fuel name.
     * @param prefType Preferred by user fuel type.
     * @return Sorted list of petrol stations.
     */
    public LinkedList<Petrol> getSortedByPrice(List<Petrol> list, String prefFuel, String prefType) {
        LinkedList<Petrol> sortedList = new LinkedList<>();
        LinkedList<Petrol> unknownDateList = new LinkedList<>();
        Map<Double, Integer> map = new HashMap<>();
        double[] priceArray = new double[list.size()];
        int i = 0;

        for (Petrol p : list) {
            Fuel f = getPrefFuel(p, prefFuel, prefType);
            if (f == null) {
                unknownDateList.add(p);
                continue;
            }
            double price = Double.parseDouble(f.getPrice());
            if (price == -1 || price == 0)
                price = 999 + i;

            priceArray[i] = price;
            map.put(price, i);
            i++;
        }

        boolean noData = false;
        for (int j = 0; j < priceArray.length; j++) {
            if (priceArray[j] != 0.0) {
                noData = true;
                break;
            }

        }
        if (priceArray.length == 0 || priceArray.equals(null) || !noData) {
            LinkedList<Petrol> covertedList = new LinkedList<>();
            covertedList.addAll(list);
            return covertedList;
        }

        sorting(priceArray, 0, priceArray.length - 1);

        for (int j = 0; j < priceArray.length; j++) {
            Petrol p = list.get(map.get(priceArray[j]));
            sortedList.add(p);
        }

        sortedList.addAll(unknownDateList);

        return sortedList;
    }

    /**
     * Sorts petrol stations list from this one with the newest preferred
     * fuel price report date to the oldest one.
     *
     * @param list     List of petrol stations to be sorted.
     * @param prefFuel Preferred by user fuel name.
     * @param prefType Preferred by user fuel type.
     * @return Sorted list of petrol stations.
     */
    public LinkedList<Petrol> getSortedByLastReportDate(List<Petrol> list, String prefFuel, String prefType) {
        LinkedList<Petrol> sortedList = new LinkedList<>();
        LinkedList<Petrol> unknownDateList = new LinkedList<>();
        Map<Date, Integer> map = new HashMap<>();
        Date[] arrayOfDates = new Date[list.size()];
        int i = 0;

        for (Petrol p : list) {
            Fuel f = getPrefFuel(p, prefFuel, prefType);
            if (f == null) {
                unknownDateList.add(p);
                continue;
            }
            String dateString = f.getLastReportDate();
            if (dateString == null) {
                unknownDateList.add(p);
                continue;
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            try {
                Date date = dateFormat.parse(dateString);
                arrayOfDates[i] = date;
                map.put(date, i);
            } catch (ParseException e) {
                e.getLocalizedMessage();
            }
            i++;
        }

        boolean noData = false;
        for (int j = 0; j < arrayOfDates.length; j++) {
            if (arrayOfDates[j] == null) {
                noData = true;
                break;
            }
        }
        if (arrayOfDates.length == 0 || arrayOfDates.equals(null) || noData) {
            LinkedList<Petrol> covertedList = new LinkedList<>();
            covertedList.addAll(list);
            return covertedList;
        }

        Arrays.sort(arrayOfDates, Collections.reverseOrder());

        for (int j = 0; j < arrayOfDates.length; j++) {
            Petrol p = list.get(map.get(arrayOfDates[j]));
            sortedList.add(p);
        }

        sortedList.addAll(unknownDateList);

        return sortedList;
    }

    /**
     * Gets preferred fuel from petrol station's fuels HashMap.
     *
     * @param petrol   Petrol object.
     * @param prefFuel Preferred by user fuel name.
     * @param prefType Preferred by user fuel type.
     * @return Preferred fuel object.
     */
    private Fuel getPrefFuel(Petrol petrol, String prefFuel, String prefType) {
        HashMap<String, Boolean> types = petrol.getAvailableFuels();
        List<Fuel> fuels = petrol.getFuels();

        if (prefFuel.equals("Wszystko")) {
            if (prefType.equals("Wszystko")) {
                String availableType = null;
                for (String name : types.keySet())
                    if (types.get(name)) {
                        availableType = name;
                        break;
                    }
                for (Fuel f : fuels)
                    if (f.getType().equals(availableType))
                        return f;
            } else {
                for (Fuel f : fuels)
                    if (f.getType().equals(prefType))
                        return f;
            }
        } else {
            for (Fuel f : fuels)
                if (f.getName().equals(prefFuel))
                    return f;
        }
        return null;
    }

    private int partition(double[] array, int bottom, int top) {
        double pivot = array[top];
        int i = (bottom - 1);
        for (int j = bottom; j < top; j++) {
            if (array[j] < pivot) {
                double temp = array[++i];
                array[i] = array[j];
                array[j] = temp;
            }
        }

        double temp = array[i + 1];
        array[i + 1] = array[top];
        array[top] = temp;

        return i + 1;
    }

    public void sorting(double[] array, int bottom, int top) {
        if (bottom < top) {
            int i = partition(array, bottom, top);
            sorting(array, bottom, i - 1);
            sorting(array, i + 1, top);
        }
    }
}
