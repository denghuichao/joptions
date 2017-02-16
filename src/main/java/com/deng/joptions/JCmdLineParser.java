/**
 * Copyright (c) 2017 Deng Huichao.
 * <p/>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the Form of the copyright holder nor the Forms
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.deng.joptions;


import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Largely GNU-compatible command-line options parser. It supports short (-v) and
 * long-form (--verbose) option , and also allows options with
 * associated values (-d 2, --debug 2, --debug=2). Option processing
 * can be explicitly terminated by the argument '--'.
 *
 * @author Deng Huichao
 * @version 1.0
 * @see com.deng.joptions
 */
public class JCmdLineParser {

    private Map<String, Option<?>> options = new HashMap<String, Option<?>>();
    private Map<String, List<?>> values = new HashMap<String, List<?>>();
    private String[] remainingOptions = null;

    public static abstract class OptionException extends Exception{
        OptionException(String msg){super(msg);}
    }

    public static class UnknownOptionException extends OptionException{
        private String optionName;
        public UnknownOptionException(String optionName, String msg){
            super(msg);
            this.optionName = optionName;
        }
        public UnknownOptionException(String optionName){
            this(optionName, "UnknownOption '"+optionName+"'");
        }

        public String getOptionName(){
            return this.optionName;
        }
    }

    public static class UnknownSuboptionException extends UnknownOptionException{
        private char suboption;

        public UnknownSuboptionException( String option, char suboption ) {
            super(option, "Illegal option: '"+suboption+"' in '"+option+"'");
            this.suboption=suboption;
        }
        public char getSuboption() {
            return suboption;
        }
    }

    public static class NotFlagException extends UnknownOptionException {
        private char notflag;

        public NotFlagException( String option, char unflaggish ) {
            super(option, "Illegal option: '"+option+"', '"+ unflaggish+"' requires a value");
            notflag=unflaggish;
        }

        /**
         * @return the first character which wasn't a boolean (e.g 'c')
         */
        public char getOptionChar() {
            return notflag;
        }
    }

    public static class IllegalOptionValueException extends OptionException{
        private final Option<?> option;
        private final String value;

        public <T> IllegalOptionValueException(Option<T> option, String value) {
            super("Illegal value '" + value + "' for option " +
                    (option.shortForm() != null ? "-" + option.shortForm() + "/" : "") +
                    "--" + option.longForm());
            this.option = option;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public Option<?> getOption() {
            return option;
        }
    }

    public static abstract class Option<T> {
        private String shortForm;
        private String longForm;
        private boolean requiresValue;

        private Option(String shortForm, String longForm, boolean requiresValue) {
            if (longForm == null) {
                throw new IllegalArgumentException("Null longForm not allowed");
            }
            this.shortForm = shortForm;
            this.longForm = longForm;
            this.requiresValue = requiresValue;
        }

        protected Option(String longForm, boolean requiresValue) {
            this(null, longForm, requiresValue);
        }

        protected Option(char shortForm, String longForm,
                         boolean requiresValue) {
            this(new String(new char[]{shortForm}), longForm, requiresValue);
        }

        public String longForm() {
            return this.longForm;
        }

        public String shortForm() {
            return this.shortForm;
        }

        public boolean requiresValue() {
            return this.requiresValue;
        }

        public final T getValue(String arg, Locale locale)
                throws IllegalOptionValueException {
            if (this.requiresValue) {
                if (arg == null) throw new IllegalOptionValueException(this, "");
                else return parseValue(arg, locale);
            } else return getDefaultValue();
        }

        protected T parseValue(String arg, Locale locale)
                throws IllegalOptionValueException {
            return null;
        }

        protected T getDefaultValue() {
            return null;
        }

        public static class BooleanOption extends Option<Boolean> {
            public BooleanOption(char shortForm, String longForm) {
                super(shortForm, longForm, false);
            }

            public BooleanOption(String longForm) {
                super(longForm, false);
            }

            @Override
            public Boolean getDefaultValue() {
                return Boolean.TRUE;
            }

            @Override
            public Boolean parseValue(String arg, Locale locale) {
                return Boolean.TRUE;
            }
        }

        public static class IntegerOption extends Option<Integer> {
            public IntegerOption(char shortForm, String longForm) {
                super(shortForm, longForm, true);
            }

            public IntegerOption(String longForm) {
                super(longForm, true);
            }

            @Override
            protected Integer parseValue(String arg, Locale locale)
                    throws IllegalOptionValueException {
                try {
                    return new Integer(arg);
                } catch (NumberFormatException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        public static class LongOption extends Option<Long> {
            public LongOption(char shortForm, String longForm) {
                super(shortForm, longForm, true);
            }

            public LongOption(String longForm) {
                super(longForm, true);
            }

            @Override
            protected Long parseValue(String arg, Locale locale)
                    throws IllegalOptionValueException {
                try {
                    return new Long(arg);
                } catch (NumberFormatException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        public static class DoubleOption extends Option<Double> {
            public DoubleOption(char shortForm, String longForm) {
                super(shortForm, longForm, true);
            }

            public DoubleOption(String longForm) {
                super(longForm, true);
            }

            @Override
            protected Double parseValue(String arg, Locale locale)
                    throws IllegalOptionValueException {
                try {
                    NumberFormat format = NumberFormat.getInstance(locale);
                    Number number =  format.parse(arg);
                    return number.doubleValue();
                } catch (ParseException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        public static class StringOption extends Option<String> {
            public StringOption(char shortForm, String longForm) {
                super(shortForm, longForm, true);
            }

            public StringOption(String longForm) {
                super(longForm, true);
            }

            @Override
            protected String parseValue(String arg, Locale locale) {
                return arg;
            }
        }
    }

    public final <T> Option<T> addOption(Option<T> option){
        if(option.shortForm != null){
            options.put("-"+option.shortForm, option);
        }
        options.put("--"+option.longForm, option);

        return option;
    }

    public final Option<Boolean> addBooleanOption(char shortForm, String longForm){
        return addOption(new Option.BooleanOption(shortForm,longForm));
    }

    public final Option<Boolean> addBooleanOption(String longForm){
        return addOption(new Option.BooleanOption(longForm));
    }

    public final Option<Integer> addIntegerOption(char shortForm, String longForm){
        return addOption(new Option.IntegerOption(shortForm,longForm));
    }

    public final Option<Integer> addIntegerOption(String longForm){
        return addOption(new Option.IntegerOption(longForm));
    }

    public final Option<Long> addLongOption(char shortForm, String longForm){
        return addOption(new Option.LongOption(shortForm,longForm));
    }

    public final Option<Long> addLongOption(String longForm){
        return addOption(new Option.LongOption(longForm));
    }

    public final Option<Double> addDoubleOption(char shortForm, String longForm){
        return addOption(new Option.DoubleOption(shortForm,longForm));
    }

    public final Option<Double> addDoubleOption(String longForm){
        return addOption(new Option.DoubleOption(longForm));
    }

    public final Option<String> addStringOption(char shortForm, String longForm){
        return addOption(new Option.StringOption(shortForm,longForm));
    }

    public final Option<String> addStringOption(String longForm){
        return addOption(new Option.StringOption(longForm));
    }

    public final <T> T getOptionValue(Option<T> option){
        return getOptionValue(option,null);
    }

    public final <T> T getOptionValue(Option<T> option, T def){
        List<?> vs = values.get(option.longForm());
        if(vs == null)return def;
        if(vs.isEmpty())return null;

        @SuppressWarnings("unchecked")
        T result = (T)values.remove(0);
        return result;
    }

    public final <T>Collection<T> getOptionValues(Option<T> option){
        Collection<T> result = new ArrayList<T>();
        while(true){
            T t = getOptionValue(option);
            if(t == null)return result;
            else result.add(t);
        }
    }

    public final String[] getRemainingOptions(){
        return this.remainingOptions;
    }

    public final void parse(String []args) throws OptionException{
        parse(args, Locale.getDefault());
    }

    public final void parse(String []args, Locale locale) throws OptionException{
        List<String> otherOptions = new ArrayList<String>();
        int position = 0;
        this.values = new HashMap<String, List<?>>();
        while(position < args.length){
            String curAgr = args[position];
            if(curAgr.startsWith("-")){
                if(curAgr.equals("--")){
                    position += 1;
                    break;
                }
                String valueArg = null;
                if(curAgr.startsWith("--")){  // handle --arg=value
                    int equalsPos = curAgr.indexOf('=');
                    if(equalsPos != -1){
                        valueArg = curAgr.substring(equalsPos+1);
                        curAgr = curAgr.substring(0,equalsPos);
                    }
                }
                else if(curAgr.length() >2){  //handle -abcd
                    for(int i=1; i<curAgr.length(); i++){
                        Option<?> option = this.options.get('-'+String.valueOf(curAgr.charAt(i)));
                        if(option == null)
                            throw new UnknownSuboptionException(curAgr,curAgr.charAt(i));
                        if(option.requiresValue())
                            throw new NotFlagException(curAgr,curAgr.charAt(i));

                        addValue(option,null,locale);
                    }
                    position++;
                    continue;
                }
                Option<?> option = this.options.get(curAgr);
                if(option == null) throw new UnknownOptionException(curAgr);

                if(option.requiresValue()){
                    if(valueArg == null){
                        position += 1;
                        if(position < args.length) {
                            valueArg = args[position];
                        }
                    }
                    addValue(option, valueArg, locale);
                }
                else{
                    addValue(option, null, locale);
                }

                position += 1;
            }
            else{
                otherOptions.add(curAgr);
                position += 1;
            }
        }

        for ( ; position < args.length; ++position ) {
            otherOptions.add(args[position]);
        }

        this.remainingOptions = new String[otherOptions.size()];
        remainingOptions = otherOptions.toArray(remainingOptions);
    }


    private <T> void addValue(Option<T> opt, String valueArg, Locale locale)
            throws IllegalOptionValueException {

        T value = opt.getValue(valueArg, locale);
        String longForm = opt.longForm();


        @SuppressWarnings("unchecked")
        List<T> v = (List<T>) values.get(longForm);

        if (v == null) {
            v = new ArrayList<T>();
            values.put(longForm, v);
        }

        v.add(value);
    }
}
