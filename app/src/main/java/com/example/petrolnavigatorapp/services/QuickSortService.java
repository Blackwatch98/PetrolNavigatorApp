package com.example.petrolnavigatorapp.services;

import android.location.Location;

import com.example.petrolnavigatorapp.utils.Fuel;
import com.example.petrolnavigatorapp.utils.Petrol;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QuickSortService {

    public QuickSortService() {
    }

    public LinkedList<Petrol> getSortedByDistance(List<Petrol> list, double lat, double lon) {
        LinkedList<Petrol> sortedList = new LinkedList<>();
        Map<Double, Integer> map = new HashMap<>();
        double[] distanceArray = new double[list.size()];
        int i = 0;

        for (Petrol p : list) {
            double distance = getDistance(lat, lon, p.getLat(), p.getLon());
            distanceArray[i] = distance;
            map.put(distance, i);
            i++;
        }
        sort(distanceArray, 0, distanceArray.length - 1);

        for (int j = 0; j < distanceArray.length; j++) {
            Petrol p = list.get(map.get(distanceArray[j]));
            sortedList.add(p);
        }
        return sortedList;
    }

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

        sort(priceArray, 0, priceArray.length - 1);

        for (int j = 0; j < priceArray.length; j++) {
            Petrol p = list.get(map.get(priceArray[j]));
            sortedList.add(p);
        }

        sortedList.addAll(unknownDateList);

        return sortedList;
    }

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


    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        return location1.distanceTo(location2);
    }

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

    private int partition(double[] array, int low, int high) {
        double pivot = array[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (array[j] < pivot) {
                double temp = array[++i];
                array[i] = array[j];
                array[j] = temp;
            }
        }

        double temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;

        return i + 1;
    }

    public void sort(double[] array, int low, int high) {
        if (low < high) {
            int pi = partition(array, low, high);

            sort(array, low, pi - 1);
            sort(array, pi + 1, high);
        }
    }
}
