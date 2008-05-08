/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Dokumente und Einstellungen\\Andi\\Eigene Dateien\\EclipseSVN\\openintents01i_trunk\\openintents\\src\\org\\openintents\\hardware\\services\\ISensorService.aidl
 */
package org.openintents.hardware.services;
import java.lang.String;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
/**
 * Example of defining an interface for calling on to a remote service
 * (running in another process).
 */
public interface ISensorService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.openintents.hardware.services.ISensorService
{
private static final java.lang.String DESCRIPTOR = "org.openintents.hardware.services.ISensorService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an ISensorService interface,
 * generating a proxy if needed.
 */
public static org.openintents.hardware.services.ISensorService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
org.openintents.hardware.services.ISensorService in = (org.openintents.hardware.services.ISensorService)obj.queryLocalInterface(DESCRIPTOR);
if ((in!=null)) {
return in;
}
return new org.openintents.hardware.services.ISensorService.Stub.Proxy(obj);
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
case TRANSACTION_registerCallback:
{
org.openintents.hardware.services.ISensorServiceCallback _arg0;
_arg0 = org.openintents.hardware.services.ISensorServiceCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
return true;
}
case TRANSACTION_unregisterCallback:
{
org.openintents.hardware.services.ISensorServiceCallback _arg0;
_arg0 = org.openintents.hardware.services.ISensorServiceCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
return true;
}
}
}
catch (android.os.DeadObjectException e) {
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.openintents.hardware.services.ISensorService
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
     * Often you want to allow a service to call back to its clients.
     * This shows how to do so, by registering a callback interface with
     * the service.
     */
public void registerCallback(org.openintents.hardware.services.ISensorServiceCallback cb) throws android.os.DeadObjectException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, null, 0);
}
finally {
_data.recycle();
}
}
/**
     * Remove a previously registered callback interface.
     */
public void unregisterCallback(org.openintents.hardware.services.ISensorServiceCallback cb) throws android.os.DeadObjectException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, null, 0);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_registerCallback = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregisterCallback = (IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Often you want to allow a service to call back to its clients.
     * This shows how to do so, by registering a callback interface with
     * the service.
     */
public void registerCallback(org.openintents.hardware.services.ISensorServiceCallback cb) throws android.os.DeadObjectException;
/**
     * Remove a previously registered callback interface.
     */
public void unregisterCallback(org.openintents.hardware.services.ISensorServiceCallback cb) throws android.os.DeadObjectException;
}
