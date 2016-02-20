/*Author: Username - Pinhassi (StackOverflow)
*URL: http://stackoverflow.com/questions/5357455/limit-decimal-places-in-android-edittext
*Date resource created: 25/11/2011
*Date class created: 20/02/2016
*Edited by: Chris Brown, 20/02/2016
*Edit Description: Changed Regex slightly and the match value
*Description: A filter class to limit the number of digits before and after a decimal point*/
package com.cfbrownweb.fuelmemo;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalDigitsInputFilter implements InputFilter {

    Pattern mPattern;

    public DecimalDigitsInputFilter(int digitsBeforeDecimal,int digitsAfterDecimal) {
        mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeDecimal) + "}+((\\.[0-9]{0," + (digitsAfterDecimal) + "})?)||(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence match = TextUtils.concat(dest.subSequence(0, dstart), source.subSequence(start, end), dest.subSequence(dend, dest.length()));
        Matcher matcher=mPattern.matcher(match);
        if(!matcher.matches())
            return "";
        return null;
    }

}
