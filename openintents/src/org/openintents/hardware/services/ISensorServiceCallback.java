/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Dokumente und Einstellungen\\Andi\\Eigene Dateien\\EclipseSVN\\openintents01i_trunk\\openintents\\src\\org\\openintents\\hardware\\services\\ISensorServiceCallback.aidl
 */
package org.openintents.hardware.services;
import java.lang.String;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
/**
 * Callback interface for generic sensor events.
 */
public interface ISensorServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.openintents.hardware.services.ISensorServiceCallback
{
private static final java.lang.String DESCRIPTOR = "org.openintents.hardware.services.ISensorServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an ISensorServiceCallback interface,
 * generating a proxy if needed.
 */
public static org.openintents.hardware.services.ISensorServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
org.openintents.hardware.services.ISensorServiceCallback in = (org.openintents.hardware.services.ISensorServiceCallback)obj.queryLocalInterface(DESCRIPTOR);
if ((in!=null)) {
return in;
}
return new org.openintents.hardware.services.ISensorServiceCallback.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags)
{
try {
switch (code)
{
case TRANSACTION_onISensorEvent:
{
int _arg0;
_arg0 = data.readInt();
float _arg1;
_arg1 = data.readFloat();
float _arg2;
_arg2 = data.readFloat();
float _arg3;
_arg3 = data.readFloat();
long _arg4;
_arg4 = data.readLong();
this.onISensorEvent(_arg0, _arg1, _arg2, _arg3, _arg4);
return true;
}
}
}
catch (android.os.DeadObjectException e) {
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.openintents.hardware.services.ISensorServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
/**
     * Called when the service has a new value.
     * 
     */
public void onISensorEvent(int action, float x, float y, float z, long eventTime) throws android.os.DeadObjectException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInt(action);
_data.writeFloat(x);
_data.writeFloat(y);
_data.writeFloat(z);
_data.writeLong(eventTime);
mRemote.transact(Stub.TRANSACTION_onISensorEvent, _data, null, 0);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_onISensorEvent = (IBinder.FIRST_CALL_TRANSACTION + 0);
}
/**
     * Called when the service has a new value.
     * 
     */
public void onISensorEvent(int action, float x, float y, float z, long eventTime) throws android.os.DeadObjectException;
}
