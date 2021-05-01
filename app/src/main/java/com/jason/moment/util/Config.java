package com.jason.moment.util;

public class Config {
    public static final long    _timer_period   = 10000; // 1 sec (최초이후 실행 주기)
    public static final long    _timer_delay    = 1000; // 1 sec (최초실행)
    public static boolean       _start_service  = false; // start location service
    public static boolean       _start_timer    = true; // start timer background scheduler
}
