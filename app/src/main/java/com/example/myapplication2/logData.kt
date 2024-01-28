package com.example.myapplication2

class logData(_date: String, _time: String, _mealMode: String,
                _glucoseReading: String, _carbsConsumed: String,
                _insulinTaken: String) {
    private var date: String = _date;
    private var time: String = _time;
    private var mealType: String = _mealMode;
    private var glucoseReading: String = _glucoseReading;
    private var carbsConsumed: String = _carbsConsumed;
    private var insulinTaken: String = _insulinTaken;

    fun getDate(): String{
        return date;
    }

    fun getTime(): String{
        return time;
    }

    fun getMealType(): String{
        return mealType;
    }

    fun getGlucoseReading(): String{
        return glucoseReading;
    }
    fun getCarbsConsumed(): String{
        return carbsConsumed;
    }
    fun getInsulinTaken(): String{
        return insulinTaken;
    }
    override fun toString(): String{
        return getGlucoseReading() + " mg/dL";
    }
}