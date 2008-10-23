/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\oi.zero.branch\\pocketplay\\src\\org\\openintents\\pocketplay\\playback\\IAudioPlayerService.aidl
 */
package org.openintents.pocketplay.playback;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
public interface IAudioPlayerService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.openintents.pocketplay.playback.IAudioPlayerService
{
private static final java.lang.String DESCRIPTOR = "org.openintents.pocketplay.playback.IAudioPlayerService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IAudioPlayerService interface,
 * generating a proxy if needed.
 */
public static org.openintents.pocketplay.playback.IAudioPlayerService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
org.openintents.pocketplay.playback.IAudioPlayerService in = (org.openintents.pocketplay.playback.IAudioPlayerService)obj.queryLocalInterface(DESCRIPTOR);
if ((in!=null)) {
return in;
}
return new org.openintents.pocketplay.playback.IAudioPlayerService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_play:
{
data.enforceInterface(DESCRIPTOR);
this.play();
reply.writeNoException();
return true;
}
case TRANSACTION_pause:
{
data.enforceInterface(DESCRIPTOR);
this.pause();
reply.writeNoException();
return true;
}
case TRANSACTION_stop:
{
data.enforceInterface(DESCRIPTOR);
this.stop();
reply.writeNoException();
return true;
}
case TRANSACTION_nextTrack:
{
data.enforceInterface(DESCRIPTOR);
this.nextTrack();
reply.writeNoException();
return true;
}
case TRANSACTION_previousTrack:
{
data.enforceInterface(DESCRIPTOR);
this.previousTrack();
reply.writeNoException();
return true;
}
case TRANSACTION_loadPlaylist:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.loadPlaylist(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_loadFile:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.loadFile(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
org.openintents.pocketplay.playback.IAudioPlayerCallback _arg0;
_arg0 = org.openintents.pocketplay.playback.IAudioPlayerCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
org.openintents.pocketplay.playback.IAudioPlayerCallback _arg0;
_arg0 = org.openintents.pocketplay.playback.IAudioPlayerCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_isPlaying:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPlaying();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_invalidatePlaylist:
{
data.enforceInterface(DESCRIPTOR);
this.invalidatePlaylist();
reply.writeNoException();
return true;
}
case TRANSACTION_getNext5:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String[] _result = this.getNext5();
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_getPrevious5:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String[] _result = this.getPrevious5();
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.openintents.pocketplay.playback.IAudioPlayerService
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
public void play() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_play, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void pause() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_pause, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void stop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void nextTrack() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_nextTrack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void previousTrack() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_previousTrack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void loadPlaylist(java.lang.String sPlaylistUri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sPlaylistUri);
mRemote.transact(Stub.TRANSACTION_loadPlaylist, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void loadFile(java.lang.String sFileUri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sFileUri);
mRemote.transact(Stub.TRANSACTION_loadFile, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void registerCallback(org.openintents.pocketplay.playback.IAudioPlayerCallback iapc) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((iapc!=null))?(iapc.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void unregisterCallback(org.openintents.pocketplay.playback.IAudioPlayerCallback iapc) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((iapc!=null))?(iapc.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public boolean isPlaying() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPlaying, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void invalidatePlaylist() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_invalidatePlaylist, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.lang.String[] getNext5() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getNext5, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String[] getPrevious5() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPrevious5, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_play = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_pause = (IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_stop = (IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_nextTrack = (IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_previousTrack = (IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_loadPlaylist = (IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_loadFile = (IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_registerCallback = (IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_unregisterCallback = (IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_isPlaying = (IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_invalidatePlaylist = (IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getNext5 = (IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_getPrevious5 = (IBinder.FIRST_CALL_TRANSACTION + 12);
}
public void play() throws android.os.RemoteException;
public void pause() throws android.os.RemoteException;
public void stop() throws android.os.RemoteException;
public void nextTrack() throws android.os.RemoteException;
public void previousTrack() throws android.os.RemoteException;
public void loadPlaylist(java.lang.String sPlaylistUri) throws android.os.RemoteException;
public void loadFile(java.lang.String sFileUri) throws android.os.RemoteException;
public void registerCallback(org.openintents.pocketplay.playback.IAudioPlayerCallback iapc) throws android.os.RemoteException;
public void unregisterCallback(org.openintents.pocketplay.playback.IAudioPlayerCallback iapc) throws android.os.RemoteException;
public boolean isPlaying() throws android.os.RemoteException;
public void invalidatePlaylist() throws android.os.RemoteException;
public java.lang.String[] getNext5() throws android.os.RemoteException;
public java.lang.String[] getPrevious5() throws android.os.RemoteException;
}
