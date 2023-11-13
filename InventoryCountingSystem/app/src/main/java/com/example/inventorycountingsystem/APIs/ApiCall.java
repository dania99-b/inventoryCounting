package com.example.inventorycountingsystem.APIs;

import android.util.Pair;

public interface ApiCall {

    Pair<Integer, String>  processFinish(int responseCode, String response);

}
