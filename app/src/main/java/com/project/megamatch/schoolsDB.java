// הקובץ הזה משומש לאחסון מבני נתונים שמנתחים את קובץ מסד הנתונים של בתי הספר בכלל הארץ

package com.project.megamatch;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class schoolsDB {

    private static final HashMap<Integer, School> schoolsMap = new HashMap<>();

    // Call this method in an Activity to initialize the database
    public static void loadSchoolsFromCSV(Context context) {
        schoolsMap.clear();

        try (InputStream is = context.getResources().openRawResource(R.raw.schools);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (columns.length < 3) continue;

                String schoolIdStr = columns[0].replace("\uFEFF", "").trim();

                try {
                    int schoolId = Integer.parseInt(schoolIdStr);
                    String schoolName = columns[1].trim();
                    String principalName = columns[2].trim();

                    schoolsMap.put(schoolId, new School(schoolName, principalName));

                } catch (NumberFormatException ignored) {}
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Method to retrieve school details by ID
    public static School getSchoolById(int schoolId) {
        return schoolsMap.get(schoolId);
    }

    public static int getTotalSchoolsCount()
    {
        return schoolsMap.size();
    }

    public static HashMap<Integer, School> getSchoolsMap() {
        return schoolsMap;
    }

    // Method to get all schools as a List with IDs
    public static List<School> getAllSchools() {
        List<School> schoolsList = new ArrayList<>();
        for (Map.Entry<Integer, School> entry : schoolsMap.entrySet()) {
            School school = entry.getValue();
            // Create a school with ID
            School schoolWithId = new School(school.getSchoolName(), school.getPrincipalName());
            schoolWithId.setSchoolId(entry.getKey());
            schoolsList.add(schoolWithId);
        }
        return schoolsList;
    }

    // Inner class representing a school
    public static class School {
        private final String schoolName;
        private final String principalName;
        private int schoolId;

        public School(String schoolName, String principalName) {
            this.schoolName = schoolName;
            this.principalName = principalName;
        }

        public String getSchoolName() {
            return schoolName;
        }

        public String getPrincipalName() {
            return principalName;
        }
        
        public void setSchoolId(int schoolId) {
            this.schoolId = schoolId;
        }
        
        public int getSchoolId() {
            return schoolId;
        }
        
        @Override
        public String toString() {
            return schoolName;
        }
    }
}
