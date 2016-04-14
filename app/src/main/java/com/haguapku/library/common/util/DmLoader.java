package com.haguapku.library.common.util;

import android.content.Context;

/**
 * Created by MarkYoung on 15/11/2.
 */
public class DmLoader {

    public static byte[] b;
    static {
        System.loadLibrary("jni");
//        b = bytesFromJNI();
    }

    @SuppressWarnings("JniMissingFunction")
    public static native byte[] bytesFromJNI();

    @SuppressWarnings("JniMissingFunction")
    public static native String getKey();

    @SuppressWarnings("JniMissingFunction")
    public static native String getSecret();

    /*
     * This is a private method, where we check the signature.
     * if the signature is not zapya's, we will exit some seconds later.
     */
    @SuppressWarnings("JniMissingFunction")
    public static native void aa(Context context);

}
