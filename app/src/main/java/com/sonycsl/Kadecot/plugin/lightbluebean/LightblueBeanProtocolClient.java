/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin.lightbluebean;

import android.os.Handler;

import com.sonycsl.Kadecot.plugin.DeviceData;
import com.sonycsl.Kadecot.plugin.KadecotProtocolClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampCallee.WampInvocationReplyListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LightblueBeanProtocolClient extends KadecotProtocolClient {
    static final String PROTOCOL_NAME = "lightbluebean";

    static final String DEVICE_TYPE_BEAN = "bean";

    static final String LOCALHOST = "127.0.0.1";

    private static final String PRE_FIX = "com.sonycsl.kadecot." + PROTOCOL_NAME;
    private static final String PROCEDURE = ".procedure.";
    private static final String TOPIC = ".topic.";

    public static final String SERIAL_PUBLISH_TOPIC = PRE_FIX + TOPIC + "serial";

    public static final String DEVICE_UUID_PREFIX = PRE_FIX+"."+DEVICE_TYPE_BEAN ;

    private Handler mHandler;

    private LightblueBeanPluginService mService ;

    public static enum Procedure {
        PROCEDURE_SERIAL("serial","" ),
        ;

        private final String mUri;
        private final String mServiceName;
        private final String mDescription;

        /**
         * @param servicename
         * @param description is displayed on JSONP called /v
         */
        Procedure(String servicename, String description) {
            mUri = PRE_FIX + PROCEDURE + servicename;
            mServiceName = servicename;
            mDescription = description;
        }

        public String getUri() {
            return mUri;
        }

        public String getServiceName() {
            return mServiceName;
        }

        public String getDescription() {
            return mDescription;
        }

        public static Procedure getEnum(String procedure) {
            for (Procedure p : Procedure.values()) {
                if (p.getUri().equals(procedure)) {
                    return p;
                }
            }
            return null;
        }
    }

    public LightblueBeanProtocolClient(LightblueBeanPluginService service) {
        mHandler = new Handler();
        this.mService = service ;
    }

    /**
     * Get the topics this plug-in want to SUBSCRIBE <br>
     */
    @Override
    public Set<String> getTopicsToSubscribe() {
        return new HashSet<String>();
    }

    /**
     * Get the procedures this plug-in supported. <br>
     */
    @Override
    public Map<String, String> getRegisterableProcedures() {
        Map<String, String> procs = new HashMap<String, String>();
        for (Procedure p : Procedure.values()) {
            procs.put(p.getUri(), p.getDescription());
        }
        return procs;
    }

    /**
     * Get the topics this plug-in supported. <br>
     */
    @Override
    public Map<String, String> getSubscribableTopics() {
        Map<String,String> topics = new HashMap<String, String>() ;
        topics.put(SERIAL_PUBLISH_TOPIC,"") ;
        return topics ;
    }


    @Override
    public void onSearchEvent(WampEventMessage eventMsg) {
        mService.beanStop();
        mService.beanStart();
    }

    public static String getUuidFromBeanAddress(String beanAddress){
        return "punchthrough:lbb:punchthrough:0:"+beanAddress.replace(":","_") ;
    }
    public static String getBeanAddressFromUuid(String uuid){
        return uuid.split(":")[4].replace("_",":") ;
    }

    @Override
    protected void onInvocation(final int requestId, String procedure, final String uuid,
            final JSONObject argumentsKw, final WampInvocationReplyListener listener) {

        String beanAddress = getBeanAddressFromUuid(uuid) ;
        try {
            final Procedure proc = Procedure.getEnum(procedure);
            if (proc == Procedure.PROCEDURE_SERIAL) {
                //String beanAddress = uuid.substring(uuid.indexOf(":")+1) ;
                BeanController.beanSendSerial(beanAddress,argumentsKw.getString("value"));
                listener.replyYield(WampMessageFactory.createYield(requestId, new JSONObject(),
                        new JSONArray(),
                        new JSONObject().put("value", argumentsKw.getString("value")))
                        .asYieldMessage());
                return;
            }


            /**
             * Return YIELD message as a result of INVOCATION.
             */
            JSONObject argumentKw = new JSONObject().put("targetDevice", beanAddress).put(
                    "calledProcedure", procedure);

            listener.replyYield(WampMessageFactory.createYield(requestId, new JSONObject(),
                    new JSONArray(), argumentKw).asYieldMessage());
        } catch (JSONException e) {
            listener.replyError(WampMessageFactory
                    .createError(WampMessageType.INVOCATION, requestId,
                            new JSONObject(), WampError.INVALID_ARGUMENT, new JSONArray(),
                            new JSONObject()).asErrorMessage());
        }
    }
}
