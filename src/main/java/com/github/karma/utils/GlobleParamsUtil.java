package com.github.karma.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * GlobalParamsUtil
 *
 * @author lujunming
 * @version 2025/03/16 19:31
 **/
public class GlobleParamsUtil {

    private static ThreadLocal<Map<String,String>> globalParamsHolder = new ThreadLocal<>();

    public static void setGlobalParams(Map<String,String> globalParams){
        globalParamsHolder.set(globalParams);
    }

    public static String getValue(String localValue){
        Map<String,String> globalParams = globalParamsHolder.get();
        if(null == globalParams){
            return localValue;
        }
        if(!StringUtils.startsWith(localValue,"#")){
            return localValue;
        }
        String globalKey = localValue.substring(1);
        return globalParams.get(globalKey);
    }

    public static void clear(){
        globalParamsHolder.remove();
    }
}
