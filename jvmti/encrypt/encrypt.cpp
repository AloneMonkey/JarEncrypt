#include "jni.h"
#include <iostream>
 
extern"C" JNIEXPORT jbyteArray JNICALL 
Java_Encrypt_encrypt(
    JNIEnv * _env, 
    jobject _obj,
    jbyteArray _buf
)
{
    jsize len =_env->GetArrayLength(_buf);   

    unsigned char* dst = (unsigned char*)_env->GetByteArrayElements(_buf, 0);
 	
 	for (int i = 0; i < len; ++i)
 	{
 		dst[i] = dst[i] ^ 0x07;
 	}
 
    _env->SetByteArrayRegion(_buf, 0, len, (jbyte *)dst);
    return _buf;
}