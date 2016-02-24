/*Author: Chris Brown
* Date: 21/02/2016
* Description: Input filter class that limits to letters and numbers
* with the option of allowing spaces*/
package com.cfbrownweb.fuelmemo;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LettersNumberInputFilter implements InputFilter {

    private Pattern platePattern;

    public LettersNumberInputFilter(boolean allowSpaces){
        if(allowSpaces){
            platePattern = Pattern.compile("[\\w ]*");
        }
        else {
            platePattern = Pattern.compile("\\w*");
        }
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence match = TextUtils.concat(dest.subSequence(0, dstart), source.subSequence(start, end), dest.subSequence(dend, dest.length()));
        Matcher matcher=platePattern.matcher(match);
        if(!matcher.matches())
            return "";
        return null;
    }
}
