package com.ssing;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class SsingDatabase {
    public static ArrayList<Ssing> list;
    public static void initialize() {
        list = new ArrayList<Ssing>();
    }
    public static void generateTestData() {
        initialize();
        list.add(new Ssing(37.62648896058849,127.09526422378007,"0001",100, "Ready" ));
        list.add(new Ssing(37.64448896058842,127.09826422378006,"0002",19, "Repair" ));
        list.add(new Ssing(37.62748896058841,127.09426422378005,"0003",80, "Ready" ));
        list.add(new Ssing(37.68648896058855,127.03726422378005,"0004",70, "Ready" ));
        list.add(new Ssing(37.62648896058859,127.02226422378004,"0005",100, "Ready" ));
        list.add(new Ssing(37.62648896058449,127.06726422378012,"0006",100, "Ready" ));
        list.add(new Ssing(37.65648896058349,127.02326422378023,"0007",30, "Repair" ));
        list.add(new Ssing(37.69648896058859,127.01226422378022,"0008",90, "Ready" ));
        list.add(new Ssing(37.64648896058849,127.09326422378022,"0009",100, "Ready" ));
        list.add(new Ssing(37.67648896058889,127.09826422378099,"0010",0, "Repair" ));
    }

}
