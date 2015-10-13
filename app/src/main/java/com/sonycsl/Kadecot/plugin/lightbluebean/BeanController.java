package com.sonycsl.Kadecot.plugin.lightbluebean;


import android.content.Context;

import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;
import com.punchthrough.bean.sdk.BeanListener;
import com.punchthrough.bean.sdk.BeanManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by o on 2015/07/21.
 */
public class BeanController {
    static class BeanInfo {
        Bean bean ;
        String name ;
        //Context context ;
        BeanListener beanlistener ;
        int rssi ;
//        BeanDiscoveryListener beanDiscoveryListener ;
    }
    private static Context context ;
    public static Vector<BeanInfo> beans = new Vector<>();
    private static Map<BeanListener,BeanInfo> mapBeanListenerToBeanInfo = new HashMap<>();
    private static Map<String,BeanInfo> mapNameToBean = new HashMap<>();

    public static BeanInfo getBeanInfo(BeanListener bi){
        if( mapBeanListenerToBeanInfo.containsKey(bi) )
            return mapBeanListenerToBeanInfo.get(bi) ;
        return null ;
    }
    public static BeanInfo getBeanInfo(String beanName){
        if( mapNameToBean.containsKey(beanName) )
            return mapNameToBean.get(beanName) ;
        return null ;
    }
    /*public static BeanInfo getBeanInfo(BeanDiscoveryListener beanDiscoveryListener){
        if( mapBeanDiscoveryListenerToBeanInfo.containsKey(beanDiscoveryListener) )
            return mapBeanDiscoveryListenerToBeanInfo.get(beanDiscoveryListener) ;
        return null ;
    }*/

    public interface BeanFoundCallback {
       BeanListener onBeanFound(String beanName) ;
    } ;

    //public static void findBean(Context c , String[] beanNames , BeanListener[] beanListeners){
    private static BeanFoundCallback beanFoundCallback ;
    public static void findBean(Context c ,BeanFoundCallback bfc){
        if( beanFoundCallback != null ) return ;    // Already finding

        BeanController.context = c ;
        beanFoundCallback = bfc ;

        BeanManager.getInstance().startDiscovery( new BeanDiscoveryListener() {
            @Override
            public void onBeanDiscovered(Bean bean, int rssi) {
                if( beanFoundCallback == null ) return ;

                String beanName = bean.getDevice().getName() ;
                if( getBeanInfo(beanName) != null ) return ;    // Already found

                BeanListener bl = beanFoundCallback.onBeanFound(beanName) ;
                if( bl == null ) return ;

                BeanInfo bi = new BeanInfo() ;
                bi.name = beanName ;
                bi.beanlistener = bl ;
                bi.bean = bean ;
                bi.rssi = rssi ;
                mapBeanListenerToBeanInfo.put(bi.beanlistener , bi) ;
                mapNameToBean.put(beanName,bi) ;
                beans.add(bi) ;

                bean.connect(BeanController.context , bl);
            }

            @Override
            public void onDiscoveryComplete() {
                beanFoundCallback = null ;
            }
        } ) ;
    }

    public static void disconnectBeans(){
        beanFoundCallback = null ;
        mapBeanListenerToBeanInfo.clear();
        mapNameToBean.clear();
        for( BeanInfo bi : beans){
            if( bi.bean != null)
                bi.bean.disconnect(); ;
        }
        beans.clear();
    }

    public static void beanSendSerial(String beanName,String msg){
        BeanInfo bi = getBeanInfo(beanName) ;
        if( bi == null ) return ;

        bi.bean.sendSerialMessage(msg.getBytes());
    }
}
