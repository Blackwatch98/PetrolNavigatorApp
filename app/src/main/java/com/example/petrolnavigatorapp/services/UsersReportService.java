package com.example.petrolnavigatorapp.services;

import com.example.petrolnavigatorapp.utils.UserReport;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This call holds whole reports service.
 * It validates all of them and change data about
 * petrol station or fuel if enough reports has been gathered.
 * When something reported also database is searched for outdated reports and these are being deleted.
 */
public class UsersReportService {

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private DocumentReference currentPetrolDocument;
    private final int MINIMAL_CONFIRMATION_NUMBER_TO_ACCEPT_REPORT_VALUES = 2;
    private final int MINIMAL_CONFIRMATION_NUMBER_TO_ACCEPT_NAME_REPORT = 2;
    private final int DAYS_UNITL_REPORT_EXPIRES = 2;

    public UsersReportService(DocumentReference documentReference) {
        currentPetrolDocument = documentReference;
    }

    /**
     * Validates reports that concern available on particular petrol station fuel types.
     * It looks for differences between current available fuel types and reported ones.
     * It also changes data if enough reports gathered.
     *
     * @param availableFuels    Old fuel types HashMap.
     * @param newAvailableFuels New fuel types HashMap.
     */
    public void sendAvailableFuelsReport(final HashMap<String, Boolean> availableFuels, final HashMap<String, Boolean> newAvailableFuels) {
        currentPetrolDocument.collection("fuelTypeReports").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                HashMap<String, Boolean> differences = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // get differences between report and original data
                for (String name : availableFuels.keySet()) {
                    if (!availableFuels.get(name).equals(newAvailableFuels.get(name))) {
                        differences.put(name, newAvailableFuels.get(name));
                    }
                }
                // looking for report equal to differences
                for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
                    UserReport<Boolean> report = new UserReport<>(
                            query.get("targetType").toString(),
                            query.get("targetName").toString(),
                            (List<String>) query.get("senders"),
                            (Boolean) query.get("data"),
                            query.get("lastReportDate").toString(),
                            Integer.parseInt(query.get("counter").toString())
                    );
                    // check if report is already in database and if this user hasn't already report it
                    if (differences.containsKey(report.getTargetName()) && !report.getSenders().contains(currentUser.getUid())) {
                        if (report.getCounter() + 1 >= MINIMAL_CONFIRMATION_NUMBER_TO_ACCEPT_REPORT_VALUES) {
                            currentPetrolDocument.update("availableFuels." + report.getTargetName(), report.getData(),
                                    "lastReportDate", sdf.format(new Date()));
                            currentPetrolDocument.collection("fuelTypeReports").document(query.getId()).delete();
                        }
                        report.getSenders().add(currentUser.getUid());
                        currentPetrolDocument.collection("fuelTypeReports").document(query.getId())
                                .update("counter", report.getCounter() + 1,
                                        "senders", report.getSenders(),
                                        "lastReportDate", sdf.format(new Date()));
                        differences.remove(report.getTargetName());
                    } else
                        differences.remove(report.getTargetName());
                    removeOutdatedReport(report, "fuelTypeReports", query.getId());
                }
                // creating new reports
                for (String name : differences.keySet()) {
                    List<String> users = new LinkedList<>();
                    users.add(currentUser.getUid());
                    currentPetrolDocument.collection("fuelTypeReports").document().set(new UserReport(
                            "availableFuels",
                            name,
                            users,
                            differences.get(name),
                            sdf.format(new Date()),
                            1
                    ));
                }
            }
        });
    }

    /**
     * Validates reports that concern name of particular petrol station.
     * Also changes data if enough reports gathered.
     *
     * @param name New petrol station name as string.
     */
    public void sendPetrolNameReport(final String name) {
        currentPetrolDocument.collection("petrolNameReports").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                boolean isReportExisting = false;
                for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
                    UserReport<String> report = new UserReport<>(
                            query.get("targetType").toString(),
                            query.get("targetName").toString(),
                            (List<String>) query.get("senders"),
                            (String) query.get("data"),
                            query.get("lastReportDate").toString(),
                            Integer.parseInt(query.get("counter").toString())
                    );
                    if (report.getData().equals(name)) {
                        if (!report.getSenders().contains(currentUser.getUid()))
                            if (report.getCounter() + 1 >= MINIMAL_CONFIRMATION_NUMBER_TO_ACCEPT_NAME_REPORT) {
                                currentPetrolDocument.update("name", report.getTargetName());
                                currentPetrolDocument.collection("petrolNameReports").document(query.getId()).delete();
                            } else {
                                report.getSenders().add(currentUser.getUid());
                                currentPetrolDocument.collection("petrolNameReports").document(query.getId())
                                        .update("counter", report.getCounter() + 1,
                                                "senders", report.getSenders(),
                                                "lastReportDate", new Date());
                            }
                        isReportExisting = true;
                    }
                    removeOutdatedReport(report, "petrolNameReports", query.getId());
                }

                if (isReportExisting)
                    return;

                List<String> users = new LinkedList<>();
                users.add(currentUser.getUid());
                currentPetrolDocument.collection("petrolNameReports").document().set(new UserReport(
                        "petrolName",
                        "name",
                        users,
                        name,
                        sdf.format(new Date()),
                        1
                ));
            }
        });
    }

    /**
     * Validated reports that particular petrol station does not exist.
     * When enough reports gathered, the petrol station is being removed from database.
     */
    public void sendPetrolNotExistReport() {
        currentPetrolDocument.collection("petrolNotExistReports").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                boolean isReportExisting = false;
                for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
                    UserReport<String> report = new UserReport<>(
                            query.get("targetType").toString(),
                            (List<String>) query.get("senders"),
                            (String) query.get("data"),
                            query.get("lastReportDate").toString(),
                            Integer.parseInt(query.get("counter").toString())
                    );

                    if (report.getData().equals(currentPetrolDocument.getId())) {
                        if (!report.getSenders().contains(currentUser.getUid()))
                            if (report.getCounter() + 1 >= MINIMAL_CONFIRMATION_NUMBER_TO_ACCEPT_REPORT_VALUES) {
                                currentPetrolDocument.delete();
                                currentPetrolDocument.collection("petrolNotExistReports").document(query.getId()).delete();
                            } else {
                                report.getSenders().add(currentUser.getUid());
                                currentPetrolDocument.collection("petrolNotExistReports").document(query.getId())
                                        .update("counter", report.getCounter() + 1,
                                                "senders", report.getSenders(),
                                                "lastReportDate", new Date());
                            }
                        isReportExisting = true;
                    }
                    removeOutdatedReport(report, "petrolNotExistReports", query.getId());
                }
                if (isReportExisting)
                    return;
                List<String> users = new LinkedList<>();
                users.add(currentUser.getUid());
                currentPetrolDocument.collection("petrolNotExistReports").document().set(new UserReport(
                        "petrolNotExist",
                        users,
                        currentPetrolDocument.getId(),
                        sdf.format(new Date()),
                        1
                ));
            }
        });
    }

    /**
     * Validates reports about new prices of fuels.
     * If somebody reports the same price as current only last report date is updated.
     *
     * @param price           New reported fuel price.
     * @param fuelName        Name of the fuel that report is concerned about.
     * @param isSameAsCurrent Flag that tells is the price same as current.
     */
    public void sendNewPriceReport(final String price, final String fuelName, boolean isSameAsCurrent) {
        if (isSameAsCurrent) {
            updatePrice(price, fuelName);
            return;
        }

        currentPetrolDocument.collection("fuelPriceChangeReports").document(fuelName + "_Reports")
                .collection("reports").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        boolean isReportExisting = false;
                        for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
                            UserReport<String> report = new UserReport<>(
                                    query.get("targetType").toString(),
                                    query.get("targetName").toString(),
                                    (List<String>) query.get("senders"),
                                    (String) query.get("data"),
                                    query.get("lastReportDate").toString(),
                                    Integer.parseInt(query.get("counter").toString())
                            );

                            if (report.getData().equals(price) && report.getTargetName().equals(fuelName)) {
                                if (!report.getSenders().contains(currentUser.getUid()))
                                    if (report.getCounter() + 1 >= MINIMAL_CONFIRMATION_NUMBER_TO_ACCEPT_REPORT_VALUES) {
                                        updatePrice(price, fuelName);
                                        currentPetrolDocument.collection("fuelPriceChangeReports").document(fuelName + "_Reports")
                                                .collection("reports")
                                                .document(query.getId()).delete();
                                    } else {
                                        report.getSenders().add(currentUser.getUid());
                                        currentPetrolDocument.collection("fuelPriceChangeReports").document(fuelName + "_Reports")
                                                .collection("reports")
                                                .document(query.getId())
                                                .update("counter", report.getCounter() + 1,
                                                        "senders", report.getSenders(),
                                                        "lastReportDate", new Date());
                                    }
                                isReportExisting = true;
                            }
                            removeOutdatedPriceReport(report, "fuelPriceChangeReports", query.getId(), fuelName);
                        }
                        if (isReportExisting)
                            return;

                        List<String> users = new LinkedList<>();
                        users.add(currentUser.getUid());
                        currentPetrolDocument.collection("fuelPriceChangeReports").document(fuelName + "_Reports")
                                .collection("reports").document().set(new UserReport(
                                "petrolNotExist",
                                fuelName,
                                users,
                                price,
                                sdf.format(new Date()),
                                1
                        ));
                    }

                });
    }

    /**
     * It is called update price because it also do it.
     * But mainly is focused on correct new date.
     *
     * @param price    Price of the fuel.
     * @param fuelName Fuel name.
     */
    private void updatePrice(final String price, final String fuelName) {
        currentPetrolDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    List<HashMap<String, Object>> lista = (List<HashMap<String, Object>>) documentSnapshot.get("fuels");

                    for (HashMap<String, Object> item : lista) {
                        if (item.get("name").toString().equals(fuelName)) {
                            item.put("price", price);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            item.put("lastReportDate", sdf.format(new Date()));
                            break;
                        }
                    }
                    currentPetrolDocument.update("fuels", lista);
                }
            }
        });
    }

    /**
     * Removes outdated reports from database.
     *
     * @param report            Class that represents particular report.
     * @param reportsCollection Name of the collections that report is part of. Examples: fuelReports, petrolNameReports etc.
     * @param queryId           Id of report's document.
     */
    private void removeOutdatedReport(UserReport report, String reportsCollection, String queryId) {
        String date = report.getLastReportDate();
        long diff = getDaysDifference(date);
        if (diff >= DAYS_UNITL_REPORT_EXPIRES) {
            currentPetrolDocument.collection(reportsCollection).document(queryId).delete();
        }
    }

    /**
     * Removes outdated reports from database that concerns about particular fuel price like Pb95.
     *
     * @param report            Class that represents particular report.
     * @param reportsCollection Name of the collections that report is part of. Examples: Pb95Reports etc.
     * @param queryId           Id of report's document.
     * @param fuelName          Name of the fuel.
     */
    private void removeOutdatedPriceReport(UserReport report, String reportsCollection, String queryId, String fuelName) {
        String date = report.getLastReportDate();
        long diff = getDaysDifference(date);
        if (diff >= DAYS_UNITL_REPORT_EXPIRES) {
            currentPetrolDocument.collection(reportsCollection).document(fuelName + "Reports")
                    .collection("reports").document(queryId).delete();
        }
    }

    /**
     * Counts difference between one date and current in days number.
     *
     * @param date Date that difference between it and today should be checked.
     * @return Difference in days numbers.
     */
    private long getDaysDifference(String date) {
        long diff = -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date firstDate = sdf.parse(date);
            Date secondDate = new Date();

            long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
            diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        } catch (ParseException e) {
            e.getMessage();
        }

        return diff;
    }
}
