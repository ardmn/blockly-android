/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.blockly.model;

import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;

import com.google.blockly.utils.BlockLoadingException;

import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Adds a date picker to an Input. Dates must be in the format "YYYY-MM-DD"
 */
public final class FieldDate extends Field<FieldDate.Observer> {
    private static final String TAG = "FieldDate";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final Date mDate = new Date();

    public FieldDate(String name) {
        super(name, TYPE_DATE);
    }

    public FieldDate(String name, long milliseconds) {
        this(name);
        mDate.setTime(milliseconds);
    }

    @VisibleForTesting
    public FieldDate(String name, String dateString) {
        this(name);
        if (!setFromString(dateString)) {
            throw new IllegalArgumentException("Invalid date: " + dateString);
        }
    }

    public static FieldDate fromJson(JSONObject json) throws BlockLoadingException {
        String name = json.optString("name");
        if (TextUtils.isEmpty(name)) {
            throw new BlockLoadingException("field_date \"name\" attribute must not be empty.");
        }
        FieldDate field = new FieldDate(name);
        String dateStr = json.optString("date");
        if (!TextUtils.isEmpty(dateStr) && !field.setFromString(dateStr)) {
            throw new BlockLoadingException("Unable to parse date: " + dateStr);
        }
        return field;
    }

    @Override
    public FieldDate clone() {
        return new FieldDate(getName(), mDate.getTime());
    }

    @Override
    public boolean setFromString(String text) {
        Date date = null;
        try {
            date = DATE_FORMAT.parse(text);
            setDate(date);
            return true;
        } catch (ParseException e) {
            Log.e(TAG, "Unable to parse date " + text, e);
            return false;
        }
    }

    /**
     * @return The date in this field.
     */
    public Date getDate() {
        return mDate;
    }

    /**
     * Sets this field to the specified {@link Date}.
     */
    public void setDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date may not be null.");
        }
        setTime(date.getTime());
    }

    /**
     * @return The string format for the date in this field.
     */
    public String getDateString() {
        return DATE_FORMAT.format(mDate);
    }

    /**
     * Sets this field to a specific time.
     *
     * @param millis The time in millis since UNIX Epoch.
     */
    public void setTime(long millis) {
        long oldTime = mDate.getTime();
        if (millis != oldTime) {
            mDate.setTime(millis);
            onDateChanged(oldTime, millis);
        }
    }

    @Override
    protected void serializeInner(XmlSerializer serializer) throws IOException {
        serializer.text(DATE_FORMAT.format(mDate));
    }

    private void onDateChanged(long oldMillis, long newMillis) {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).onDateChanged(this, oldMillis, newMillis);
        }
    }

    /**
     * Observer for listening to changes to a date field.
     */
    public interface Observer {
        /**
         * Called when the field's date changed.
         *
         * @param field The field that changed.
         * @param oldMillis The field's previous time in UTC millis since epoch.
         * @param newMillis The field's new time in UTC millis since epoch.
         */
        void onDateChanged(Field field, long oldMillis, long newMillis);
    }
}
