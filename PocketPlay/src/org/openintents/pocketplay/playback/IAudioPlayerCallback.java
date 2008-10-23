/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\oi.zero.branch\\pocketplay\\src\\org\\openintents\\pocketplay\\playback\\IAudioPlayerCallback.aidl
 */
package org.openintents.pocketplay.playback;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
import android.util.Log;

public interface IAudioPlayerCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.openintents.pocketplay.playback.IAudioPlayerCallback
{
private static final java.lang.String DESCRIPTOR = "org.openintents.pocketplay.playback.IAudioPlayerCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IAudioPlayerCallback interface,
 * generating a proxy if needed.
 */
public static org.openintents.pocketplay.playback.IAudioPlayerCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
org.openintents.pocketplay.playback.IAudioPlayerCallback in = (org.openintents.pocketplay.playback.IAudioPlayerCallback)obj.queryLocalInterface(DESCRIPTOR);
if ((in!=null)) {
return in;
}
return new org.openintents.pocketplay.playback.IAudioPlayerCallback.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
	Log.d("IAUIDIOCALLBACK","onTRansact code>"+code);

switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onAudioPlay:
{
data.enforceInterface(DESCRIPTOR);
this.onAudioPlay();
reply.writeNoException();
return true;
}
case TRANSACTION_onAudioPause:
{
data.enforceInterface(DESCRIPTOR);
this.onAudioPause();
reply.writeNoException();
return true;
}
case TRANSACTION_onAudioStop:
{
data.enforceInterface(DESCRIPTOR);
this.onAudioStop();
reply.writeNoException();
return true;
}
case TRANSACTION_onTrackChange:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
int _arg4;
_arg4 = data.readInt();

this.onTrackChange(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();

return true;
}
case TRANSACTION_onPositionChange:
{
data.enforceInterface(DESCRIPTOR);
this.onPositionChange();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.openintents.pocketplay.playback.IAudioPlayerCallback
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
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void onAudioPlay() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onAudioPlay, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void onAudioPause() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onAudioPause, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void onAudioStop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onAudioStop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void onTrackChange(java.lang.String trackUri, java.lang.String artist, java.lang.String title, java.lang.String playlist, int playlistPosition) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(trackUri);
_data.writeString(artist);
_data.writeString(title);
_data.writeString(playlist);
_data.writeInt(playlistPosition);
mRemote.transact(Stub.TRANSACTION_onTrackChange, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void onPositionChange() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onPositionChange, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onAudioPlay = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onAudioPause = (IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onAudioStop = (IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onTrackChange = (IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_onPositionChange = (IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void onAudioPlay() throws android.os.RemoteException;
public void onAudioPause() throws android.os.RemoteException;
public void onAudioStop() throws android.os.RemoteException;
public void onTrackChange(java.lang.String trackUri, java.lang.String artist, java.lang.String title, java.lang.String playlist, int playlistPosition) throws android.os.RemoteException;
public void onPositionChange() throws android.os.RemoteException;
}
