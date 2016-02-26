/*Author: Chris Brown
* Date: 26/02/2016
* Description: Input filter class that prevents whitespace*/
package com.cfbrownweb.fuelmemo;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoWhitespaceInputFilter implements InputFilter {

    private Pattern platePattern;

    public NoWhitespaceInputFilter(){
        platePattern = Pattern.compile("[\\S ]*");
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
