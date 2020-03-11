package com.rcn.pat.Global;

import com.rcn.pat.ViewModels.ServiceInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class SortbyDate implements Comparator<ServiceInfo> {
    // Used for sorting in ascending order of
    // roll number
    public int compare(ServiceInfo a, ServiceInfo b) {
        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(a.getFechaInicial());
            Date date2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(b.getFechaInicial());

            if (date1.before(date2))
                return -1;
            else if (date1.after(date2)) {
                return 1;
            } else {
                return 0;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}