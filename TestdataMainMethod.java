package cengage.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.test.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Testdata extends YamlReader {
    public static Map<String, Object> dataMap;
    static Logger log = LogManager.getLogger(Testdata.class.getName());

    /*****
     *
     */
    public static void loadData() {
        try {
            String strEnv = "";
            String strTestDataFileName = "";
            if (System.getProperties().containsKey(Constants.ENVIRONMENT.toUpperCase().trim())) {
                strEnv = System.getProperties().get(Constants.ENVIRONMENT.toUpperCase()).toString();
            } else {
                strEnv = ExecutionConfig.getValue(Constants.ENVIRONMENT);
            }
            switch (strEnv) {
                case Constants.QA4_ENVIRONMENT:
                    strTestDataFileName = Constants.QA4_TESTDATA_FILE;
                    break;
                case Constants.QA3_ENVIRONMENT:
                    strTestDataFileName = Constants.QA3_TESTDATA_FILE;
                    break;
                case Constants.QA2_ENVIRONMENT:
                    strTestDataFileName = Constants.QA2_TESTDATA_FILE;
                    break;
                case Constants.STG_ENVIRONMENT:
                    strTestDataFileName = Constants.STG_TESTDATA_FILE;
                    break;
                case Constants.PERF_ENVIRONMENT:
                    strTestDataFileName = Constants.STG_TESTDATA_FILE;
                    break;
                case Constants.PROD_ENVIRONMENT:
                    strTestDataFileName = Constants.PROD_TESTDATA_FILE;
                    break;
                case Constants.SAMP_ENVIRONMENT:
                    strTestDataFileName = Constants.PROD_TESTDATA_FILE;
                    break;
                case Constants.QA1_ENVIRONMENT:
                    strTestDataFileName = Constants.QA1_TESTDATA_FILE;
                    break;
                case Constants.QA10_ENVIRONMENT:
                    strTestDataFileName = Constants.QA2_TESTDATA_FILE;
                    break;
                case Constants.DEV3_ENVIRONMENT:
                    strTestDataFileName = Constants.DEV_TESTDATA_FILE;
                    break;
            }
            String strTestDataFile = new StringBuilder()
                    .append(Constants.RESOURCES_FOLDER).append(File.separator)
                    .append(Constants.TESTDATA_FOLDER).append(File.separator)
                    .append(strTestDataFileName).toString();
            dataMap = getYamlAsMap(strTestDataFile);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static String getValue(String strKey) {
        String strValue = strKey;
        String strRandomPattern = Constants.RANDOM_STRING;
        String strPattern = "DP:";
        String strRegularExpressionPattern = "&(.*)&";
        String strRegularExpression2Pattern = "#(.*)#";
        String strRegularExpressionPatternForIhubIntegration = "&IHUB_(.*)&";       //To retrieve the 'Course Key' from Testdata map.

        try {

            // "DP:" - Pattern
            if (strKey.contains(strPattern)) {
                strValue = strKey.split(strPattern)[1];
                if (System.getProperties().containsKey(strValue.toUpperCase().trim())) {
                    strValue = System.getProperties().get(strValue.toUpperCase().trim()).toString();
                } else {
                    strValue = getMapValue(dataMap, strValue);
                }
            } else if (strValue.matches(strRegularExpressionPattern) && !(strValue.matches(strRegularExpressionPatternForIhubIntegration))) {
                strValue = patternHandler(strRegularExpressionPattern, strValue);

                // Get a value Goals options
                if (System.getProperties().containsKey(strValue.toUpperCase().trim())) {
                    strValue = System.getProperties().get(strValue.toUpperCase().trim()).toString();
                }
                // 'strKey' value add a Random Value(Execution ID)
                else if (strValue.contains(strRandomPattern)) {
                    strValue = strValue.split(strRandomPattern)[0];
                    strValue = getMapValue(dataMap, strValue);
                    strValue = strValue.concat(Helper.getExecutionID());
                } else {
                    strValue = getMapValue(dataMap, strValue);
                }
            } else if (strKey.contains(strRandomPattern) && !(strValue.matches(strRegularExpressionPattern))
                    && strValue.matches(strRegularExpression2Pattern) == false) {
                strValue = strValue.split(strRandomPattern)[0];
                strValue = strValue.concat(Helper.getExecutionID());
            }
            /*else if (strValue.matches(strRegularExpression2Pattern) && !(strValue.matches(strRegularExpressionPatternForIhubIntegration))) {
                strValue = patternHandler(strRegularExpression2Pattern, strValue);
                if (strValue.contains(strRandomPattern)) {
                    strValue = strValue.split(strRandomPattern)[0];
                    try {
                        strValue = getMapValue(dataMap, strValue);
                    } catch (Exception e) {
                        strValue = strValue.split(strRandomPattern)[0];
                    }
                    strValue = strValue.concat(Helper.getExecutionID());
                    strValue = Testdata.getValue("&" + strValue + "&");
                } else {
                    strValue = getMapValue(dataMap, strValue);
                }
            } */
            else if (strValue.matches(strRegularExpressionPatternForIhubIntegration)) {
                strValue = patternHandler(strRegularExpressionPatternForIhubIntegration, strValue);
                if (strValue.contains(strRandomPattern)) {
                    strValue = strValue.split(strRandomPattern)[0];
                    try {
                        strValue = getMapValue(dataMap, strValue);
                    } catch (Exception e) {
                        strValue = strValue.split(strRandomPattern)[0];
                    }
                    strValue = strValue.concat(Helper.getExecutionID());
                    strValue = Testdata.getValue("&" + strValue + "&");
                } else {
                    strValue = getMapValue(dataMap, strValue);
                }

            }
        } catch (Exception e) {
            log.error(e);
        }
        return strValue;
    }

    /**
     * @param strKey
     * @return
     * @Author Abdul Rahman
     */
    public static String getValue(String strKey, String strUIDFlag) {
        String strValue = strKey;
        try {
            strValue = getValue(strKey);
        } catch (Exception e) {
            log.error(e);
        }
        if (strUIDFlag.equalsIgnoreCase("Y")) {
            strValue = new StringBuilder(strValue).append(Helper.getExecutionID()).toString();
        }
        return strValue;
    }

    /*@Description: Pattern value handle using '&(.*)&'
     *@Date: 8-01-2017
     */
    public static String patternHandler(String strPattern, String strValue) {
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strValue);
        String strData = "";
        if (m.find()) {
            strData = m.group(1);
        }
        return strData;
    }

    /*@Description: Get Testdata Values, Return Map
     *@Date: 19-07-2016
     */
    public static Map<String, Object> getDataMap(String strKey) {
        Map<String, Object> subDataMap = new HashMap<String, Object>();
        String strValue = strKey;
        String strRegularExpressionPattern = "&(.*)&";
        try {
            if (strValue.matches(strRegularExpressionPattern)) {
                strValue = patternHandler(strRegularExpressionPattern, strValue);

                if (strValue.contains(Constants.RANDOM_STRING)) {
                    strValue = strValue.split(Constants.RANDOM_STRING)[0];
                }
//                strValue = getMapValue(dataMap, strValue);
//                strValue = strValue.substring(1, strValue.length() - 1);
//                subDataMap = Splitter.on(", ").withKeyValueSeparator("=").split(strValue);

                subDataMap = (Map) getMapAsObject(dataMap, strValue);

            } else {
                if (strValue == "null") {
                    strValue = strKey;
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return subDataMap;
    }


    public static String getValueFromDataMap(String strKey) {
        String strDataFromMap = dataMap.get(strKey).toString();
        return strDataFromMap;
    }

    public static String getValueFromDataMap(String strKey, Map<String, Object> subMap) {
        String strDataFromMap = subMap.get(strKey).toString();
        return strDataFromMap;
    }

    public static boolean putValueIndataMap(String strKeyValue, String strValue) {
        boolean isValueEntered = false;
        try {
            dataMap.put(strKeyValue, strValue);
            isValueEntered = true;
        } catch (Exception e) {
            //System.out.println("Exception occured: ".concat(e.getMessage()));
            isValueEntered = false;
        }
        return isValueEntered;
    }

    /*@Description: Get a List from Datamap*/
    public static List<String> getListValue(String strKey) {
        List lstValue = new ArrayList<String>();
        String strRegularExpressionPattern = "&(.*)&";

        try {
            if (strKey.matches(strRegularExpressionPattern)) {
                strKey = patternHandler(strRegularExpressionPattern, strKey);
                lstValue = (List) getMapAsObject(dataMap, strKey);
            } else {
                lstValue = (List) getMapAsObject(dataMap, strKey);
            }
        } catch (Exception e) {
            log.error(e);
        }
        return lstValue;
    }


    public static void main(String args[]) {
        ExecutionConfig.loadConfig();
        loadData();
        System.out.println("\n >> >> Enter Main Clas :: ");
//        System.out.println("Get Sample : " + Testdata.getValue("&SECTIONS.SECTIONNAME_RANDOM&"));
//        System.out.println("Check Up 1 : " + Testdata.getValue("&SECTIONS.SECTIONNAME1_RANDOM&"));
        System.out.println("Put Value ");
        Testdata.putValueIndataMap(Testdata.getValue("&SECTIONS.SECTIONNAME_RANDOM&"), "0SAM-4edc-DEC");
        Testdata.putValueIndataMap(Testdata.getValue("&SECTIONS.SECTIONNAME1_RANDOM&"), "1SAM-6TE-ERCF");
        Testdata.putValueIndataMap(Testdata.getValue("TestSection13_RANDOM"), "Dummy-Course13-Key");


//        System.out.println("\n >>>> &IHUB_SECTIONS.SECTIONNAME1_RANDOM& : " + Testdata.getValue("&IHUB_SECTIONS.SECTIONNAME1_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& : " + Testdata.getValue("&IHUB_TestSection13_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& : " + Testdata.getValue("&IHUB_INSTRUCTORS.INSTRUCTOR1_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& 1212 : " + Testdata.getValue("&INSTRUCTORS.INSTRUCTOR1_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& 2121 : " + Testdata.getValue("&INSTRUCTORS.INSTRUCTOR1.USERNAME_RANDOM&"));
        
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& : " + Testdata.getValue("&IHUB_TestSection13_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& : " + Testdata.getValue("&IHUB_INSTRUCTORS.INSTRUCTOR1_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& 1212 : " + Testdata.getValue("&INSTRUCTORS.INSTRUCTOR1_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& 2121 : " + Testdata.getValue("&INSTRUCTORS.INSTRUCTOR1.USERNAME_RANDOM&"));
        
        
         System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& : " + Testdata.getValue("&IHUB_TestSection13_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& : " + Testdata.getValue("&IHUB_INSTRUCTORS.INSTRUCTOR1_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& 1212 : " + Testdata.getValue("&INSTRUCTORS.INSTRUCTOR1_RANDOM&"));
        System.out.println("\n >>>> &IHUB_TestSection13_RANDOM& 2121 : " + Testdata.getValue("&INSTRUCTORS.INSTRUCTOR1.USERNAME_RANDOM&"));

//        System.out.println("Get Course Key 1  : " + Testdata.getValue("&SECTIONS.SECTIONNAME_RANDOM&"));
//        System.out.println("Get Course Key 2 : " + Testdata.getValue("&IHUB_SECTIONS.SECTIONNAME_RANDOM&"));
    }
}
