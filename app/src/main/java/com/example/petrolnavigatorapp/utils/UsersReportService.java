package com.example.petrolnavigatorapp.utils;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UsersReportService {

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private DocumentReference currentPetrolDocument;
    private final int MINIMAL_CONFIRMATION_NUMBER_TO_ACCEPT_REPORT_VALUES = 2;
    private final int DAYS_UNITL_REPORT_EXPIRES = 2;

    public UsersReportService(DocumentReference documentReference) {
        currentPetrolDocument = documentReference;
    }

    public void sendAvailableFuelsReport(final HashMap<String, Boolean> availableFuels, final HashMap<String, Boolean> newAvailableFuels) {
        currentPetrolDocument.collection("fuelTypeReports").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                HashMap<String, Boolean> differences = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // get differences between report and original data
                for (String name : availableFuels.keySet()) {
                    if (!availableFuels.get(name).equals(newAvailableFuels.get(name)))
                        differences.put(name, newAvailableFuels.get(name));
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
                                return;
                            }
                            currentPetrolDocument.collection("fuelTypeReports").document(query.getId())
                                    .update("counter", report.getCounter() + 1);
                            differences.remove(report.getTargetName());
                    }
                    else
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

    private void removeOutdatedReport(UserReport<Boolean> report, String reportsCollection, String queryId) {
        String date = report.getLastReportDate();
        long diff = getDaysDifference(date);
        if (diff >= DAYS_UNITL_REPORT_EXPIRES) {
            currentPetrolDocument.collection(reportsCollection).document(queryId).delete();
        }
    }

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
