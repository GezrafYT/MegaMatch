// הקובץ הזה משומש לאחסון מבני נתונים שמנתחים את קובץ מסד הנתונים של בתי הספר בכלל הארץ

package com.example.megamatch;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class schoolsDB {

    private static final HashMap<Integer, School> schoolsMap = new HashMap<>();

    // Call this method in an Activity to initialize the database
    public static void loadSchoolsFromCSV(Context context) {
        schoolsMap.clear();  // Reset data to avoid duplicates

        try (InputStream is = context.getResources().openRawResource(R.raw.schools);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;  // Skip header row

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] columns = line.split(","); // Adjust if CSV uses a different delimiter

                if (columns.length < 3) {
                    Log.e("SchoolDB", "Skipping invalid line: " + line);
                    continue;
                }

                try {
                    int schoolId = Integer.parseInt(columns[1].trim());  // Ensure we're reading the **correct** column for ID
                    String schoolName = columns[0].trim();  // School name
                    String principalName = columns[2].trim();  // Principal's name

                    schoolsMap.put(schoolId, new School(schoolName, principalName));

                } catch (NumberFormatException e) {
                    Log.e("SchoolDB", "Skipping invalid school ID: " + columns[1].trim());  // Log actual invalid ID
                }
            }

            Log.d("SchoolDB", "Total schools loaded: " + schoolsMap.size());

        } catch (IOException e) {
            Log.e("SchoolDB", "Error reading CSV file", e);
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

    // Inner class representing a school
    public static class School {
        private final String schoolName;
        private final String principalName;

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

    }
}
